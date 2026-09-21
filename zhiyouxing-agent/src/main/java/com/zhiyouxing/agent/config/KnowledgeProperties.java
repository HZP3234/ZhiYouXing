package com.zhiyouxing.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG 知识库配置，对应 application.yml 中的 agent.knowledge。
 */
@Data
@ConfigurationProperties(prefix = "agent.knowledge")
public class KnowledgeProperties {

    /** 关闭后不连接 Milvus，检索工具降级为提示信息。 */
    private boolean enabled;

    private String collectionName = "zhiyouxing_general_knowledge";

    /** 每次检索取回的片段数。 */
    private int topK = 4;

    /** 向量维度，必须与 embedding 模型的输出维度一致。 */
    private int embeddingDimension = 1024;

    /** 知识库文档位置，支持 Spring 资源通配符，可写多个。 */
    private String[] locations = {"classpath:knowledge/*.md"};

    private Milvus milvus = new Milvus();

    @Data
    public static class Milvus {
        private String host = "127.0.0.1";
        private int port = 19530;
        private String database = "default";
    }
}
