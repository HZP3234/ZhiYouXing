package com.zhiyouxing.agent.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 12306 火车票查询：以 stdio 方式拉起 {@code npx -y 12306-mcp} 子进程作为 MCP server，
 * 把三档方案里的火车票价从模型估算变成真实查询值。
 *
 * <p>刻意不用 spring-ai 的 MCP 自动装配：自动装配在 MCP server 起不来时会影响应用启动。
 * 这里整段包 try/catch，连不上就退化为「不注册任何工具」，agent 照常运行、票价回退为模型估算。
 */
@Configuration
@ConditionalOnProperty(prefix = "ticket.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TicketMcpConfig {

    private static final Logger log = LoggerFactory.getLogger(TicketMcpConfig.class);

    private static final String NPM_PACKAGE = "12306-mcp";
    /** MCP 客户端标识；单客户端下工具名不加前缀，直接是 get-tickets 等原名（见启动日志） */
    private static final String CLIENT_NAME = "12306";

    private McpSyncClient client;

    @Bean
    public ToolCallbackProvider ticketToolCallbackProvider() {
        try {
            StdioClientTransport transport =
                    new StdioClientTransport(serverParameters(), McpJsonMapper.getDefault());
            McpSyncClient syncClient = McpClient.sync(transport)
                    .clientInfo(new McpSchema.Implementation(CLIENT_NAME, "1.0.0"))
                    .requestTimeout(Duration.ofSeconds(20))
                    .initializationTimeout(Duration.ofSeconds(60))
                    .build();
            syncClient.initialize();
            this.client = syncClient;

            ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(syncClient);
            ToolCallback[] callbacks = provider.getToolCallbacks();
            log.info("12306 票价 MCP 已注册 {} 个工具：{}", callbacks.length,
                    Arrays.toString(Arrays.stream(callbacks).map(c -> c.getToolDefinition().name()).toArray()));
            return provider;
        }
        catch (Exception e) {
            log.warn("12306 票价 MCP 不可用，票价回退为模型估算：{}", e.toString());
            return ToolCallbackProvider.from(List.of());
        }
    }

    @PreDestroy
    public void close() {
        if (client == null) {
            return;
        }
        try {
            client.close();
        }
        catch (Exception e) {
            log.debug("关闭 12306 MCP 客户端失败", e);
        }
    }

    private ServerParameters serverParameters() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        // Windows 下 npx 是 .cmd 脚本，ProcessBuilder 不能直接执行，必须经 cmd /c 转一层
        String command = windows ? "cmd" : "npx";
        List<String> args = windows
                ? List.of("/c", "npx", "-y", NPM_PACKAGE)
                : List.of("-y", NPM_PACKAGE);
        return ServerParameters.builder(command).args(args).build();
    }
}
