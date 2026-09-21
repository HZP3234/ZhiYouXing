package com.zhiyouxing.agent.tool;

import com.zhiyouxing.agent.service.KnowledgeBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * RAG 检索工具：从本地知识库取回通用出行知识（行前准备、行李退改、带娃带老人、季节穿衣与避坑），
 * 供 Agent 在用户偏好涉及具体场景时引用，避免凭记忆编造。
 * <p>
 * 只覆盖与目的地无关的内容；城市点位素材走 {@link PoiTool} 的高德实时接口，两者不重叠。
 */
@Slf4j
@Component
public class KnowledgeTool {

    private final KnowledgeBase knowledgeBase;

    public KnowledgeTool(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Tool(description = "从智游星知识库检索通用出行知识，返回行前准备、行李与退改规则、带娃或带老人的注意事项、季节穿衣与通用避坑提醒；与具体城市无关，需要时（如亲子游、带老人、冬季出行）调用")
    public String searchTravelKnowledge(
            @ToolParam(description = "检索语句，带上场景与关注点，例如：带老人出游 注意事项 应急") String query) {
        if (!StringUtils.hasText(query)) {
            return "未提供检索语句，无法检索知识库。";
        }
        if (!knowledgeBase.available()) {
            return "知识库未启用（未连接 Milvus），本次无法检索通用出行知识，请基于通用经验作答并说明这一限制。";
        }
        List<Document> documents = knowledgeBase.search(query);
        if (documents.isEmpty()) {
            return "知识库中未检索到与「" + query + "」相关的素材，请基于通用经验作答并说明这一限制。";
        }
        log.info("知识库检索命中 {} 个片段，query={}，片段={}", documents.size(), query,
                documents.stream().map(KnowledgeBase::section).toList());
        return "以下为知识库检索到的通用出行素材：\n" + KnowledgeBase.format(documents);
    }
}
