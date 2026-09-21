package com.zhiyouxing.agent.service;

import com.zhiyouxing.agent.config.KnowledgeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 知识库：把 resources/knowledge 下的通用出行素材按二级标题切分后写入 Milvus，并对外提供相似度检索。
 * <p>
 * 语料刻意只覆盖「与目的地无关」的内容（行前准备、行李退改、带娃带老人、季节穿衣与避坑），
 * 城市相关的点位素材走高德 POI，两者互不重叠——这样检索不存在「语料没收录该城市就返回别的城市」的问题。
 * 未启用（未连接 Milvus）时所有检索返回空结果，由调用方降级提示，不影响其余工具。
 */
@Slf4j
@Service
public class KnowledgeBase {

    /** 二级标题行，一个章节切一个片段。 */
    private static final String SECTION_PREFIX = "## ";

    private static final String DEFAULT_SECTION = "概览";

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final KnowledgeProperties properties;
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public KnowledgeBase(ObjectProvider<VectorStore> vectorStoreProvider, KnowledgeProperties properties) {
        this.vectorStoreProvider = vectorStoreProvider;
        this.properties = properties;
    }

    public boolean available() {
        return vectorStoreProvider.getIfAvailable() != null;
    }

    /**
     * 按查询语句检索知识库片段。
     *
     * @param query 检索语句，带上场景与关注点，例如：带老人出游 注意事项 应急
     */
    public List<Document> search(String query) {
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store == null) {
            return List.of();
        }
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(properties.getTopK())
                .build();
        try {
            return store.similaritySearch(request);
        } catch (Exception e) {
            log.error("知识库检索失败，query={}", query, e);
            return List.of();
        }
    }

    /** 把检索结果整理成「【主题 · 章节】正文」形式的素材文本，空列表返回空串。 */
    public static String format(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Document document : documents) {
            builder.append("【").append(section(document)).append("】\n")
                    .append(text(document)).append('\n');
        }
        return builder.toString().trim();
    }

    /** 片段所属的「主题 · 章节」，用于素材标题与日志。 */
    public static String section(Document document) {
        Object section = document.getMetadata().get("section");
        Object topic = document.getMetadata().get("topic");
        return topic + " · " + (section == null ? "资料" : section);
    }

    /** 片段正文首行是「主题 · 章节」的检索用前缀，返回给模型时去掉，避免重复。 */
    private static String text(Document document) {
        String text = document.getText();
        if (!StringUtils.hasText(text)) {
            return "";
        }
        int newline = text.indexOf('\n');
        return newline >= 0 ? text.substring(newline + 1).trim() : text.trim();
    }

    /**
     * 集合为空时把知识库文档灌进去。放在启动完成后执行，避免向量化耗时拖慢启动，
     * 也避免文档已入库时每次重启都重复写一遍。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadIfEmpty() {
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store == null) {
            log.info("未启用 RAG 知识库，跳过灌库");
            return;
        }
        try {
            if (!probe(store).isEmpty()) {
                log.info("知识库已有数据，跳过灌库");
                return;
            }
            List<Document> chunks = loadChunks();
            if (chunks.isEmpty()) {
                log.warn("未在 {} 下找到任何知识库文档", String.join(",", properties.getLocations()));
                return;
            }
            store.add(chunks);
            log.info("知识库灌库完成，共写入 {} 个片段", chunks.size());
        } catch (Exception e) {
            log.error("知识库灌库失败", e);
        }
    }

    private List<Document> probe(VectorStore store) {
        return store.similaritySearch(SearchRequest.builder().query("出行准备").topK(1).build());
    }

    private List<Document> loadChunks() throws IOException {
        List<Document> chunks = new ArrayList<>();
        for (String location : properties.getLocations()) {
            for (Resource resource : resourceResolver.getResources(location)) {
                if (!resource.isReadable()) {
                    continue;
                }
                String topic = topicOf(resource);
                chunks.addAll(splitSections(read(resource), topic, resource.getFilename()));
            }
        }
        return chunks;
    }

    /** 以文件名作为主题名（如 带娃带老人.md -> 带娃带老人）。 */
    private static String topicOf(Resource resource) {
        String filename = resource.getFilename();
        if (!StringUtils.hasText(filename)) {
            return "通用出行";
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    /** 一级标题与二级标题之间的正文归入「概览」，其后每个二级标题各成一个片段。 */
    private static List<Document> splitSections(String markdown, String topic, String source) {
        Map<String, StringBuilder> sections = new LinkedHashMap<>();
        String current = DEFAULT_SECTION;
        sections.put(current, new StringBuilder());

        for (String line : markdown.split("\r?\n")) {
            if (line.startsWith(SECTION_PREFIX)) {
                current = line.substring(SECTION_PREFIX.length()).trim();
                sections.computeIfAbsent(current, key -> new StringBuilder());
                continue;
            }
            if (line.startsWith("# ")) {
                continue;
            }
            sections.get(current).append(line).append('\n');
        }

        List<Document> chunks = new ArrayList<>();
        sections.forEach((section, body) -> {
            String text = body.toString().trim();
            if (!StringUtils.hasText(text)) {
                return;
            }
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("topic", topic);
            metadata.put("section", section);
            metadata.put("source", source);
            // 把主题与章节名拼进正文，提升检索命中率
            chunks.add(new Document(topic + " · " + section + "\n" + text, metadata));
        });
        return chunks;
    }

    private static String read(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
