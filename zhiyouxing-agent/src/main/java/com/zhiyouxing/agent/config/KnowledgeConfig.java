package com.zhiyouxing.agent.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 装配 Milvus 向量库。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(KnowledgeProperties.class)
public class KnowledgeConfig {

    private static final long CONNECT_TIMEOUT_SECONDS = 5;

    /** DashScope 文本向量接口单次最多接受 10 条文本。 */
    private static final int MAX_EMBEDDING_BATCH_SIZE = 10;

    /**
     * 按条数分批。默认的 TokenCountBatchingStrategy 按 token 数分批，
     * 会把整个知识库一起发出去，超过 DashScope 的 10 条上限直接返回 400。
     */
    private static final BatchingStrategy COUNT_BATCHING = documents -> {
        List<List<Document>> batches = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += MAX_EMBEDDING_BATCH_SIZE) {
            int end = Math.min(i + MAX_EMBEDDING_BATCH_SIZE, documents.size());
            batches.add(new ArrayList<>(documents.subList(i, end)));
        }
        return batches;
    };

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "agent.knowledge", name = "enabled", havingValue = "true")
    public MilvusServiceClient milvusServiceClient(KnowledgeProperties properties) {
        KnowledgeProperties.Milvus milvus = properties.getMilvus();
        String address = milvus.getHost() + ":" + milvus.getPort();
        log.info("连接 Milvus：{}，database={}", address, milvus.getDatabase());
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvus.getHost())
                .withPort(milvus.getPort())
                .withDatabaseName(milvus.getDatabase())
                .withConnectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        try {
            return new MilvusServiceClient(connectParam);
        } catch (Exception e) {
            throw new IllegalStateException("无法连接", e);
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "agent.knowledge", name = "enabled", havingValue = "true")
    public VectorStore knowledgeVectorStore(MilvusServiceClient milvusClient,
                                           EmbeddingModel embeddingModel,
                                           KnowledgeProperties properties) {
        MilvusVectorStore store = MilvusVectorStore.builder(milvusClient, embeddingModel)
                .databaseName(properties.getMilvus().getDatabase())
                .collectionName(properties.getCollectionName())
                .embeddingDimension(properties.getEmbeddingDimension())
                .batchingStrategy(COUNT_BATCHING)
                // 集合不存在时自动创建，省去手工建表
                .initializeSchema(true)
                .build();
        log.info("知识库集合已就绪：{}", properties.getCollectionName());
        return store;
    }
}
