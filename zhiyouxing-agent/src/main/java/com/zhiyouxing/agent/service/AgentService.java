package com.zhiyouxing.agent.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.zhiyouxing.agent.entity.TravelPlanRequest;
import com.zhiyouxing.agent.entity.TravelPlanResponse;
import com.zhiyouxing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AgentService {

    /** yml 中的占位 Key，与之相等说明未配置真实 Key */
    public static final String UNCONFIGURED_API_KEY = "sk-not-configured";

    /** 最多带入的上下文条数（一条 user + 一条 assistant 算两条），前端还会再限一次。 */
    private static final int MAX_HISTORY_SIZE = 8;

    /** 单条上下文的字符上限，防止客户端塞进超长文本把上下文撑爆；实测一份完整方案约 1200 字。 */
    private static final int MAX_HISTORY_CONTENT_LENGTH = 4000;

    private final ReactAgent travelAgent;
    private final String dashScopeApiKey;

    public AgentService(ReactAgent travelAgent,
                        @Value("${spring.ai.dashscope.api-key:}") String dashScopeApiKey) {
        this.travelAgent = travelAgent;
        this.dashScopeApiKey = dashScopeApiKey;
    }

    public TravelPlanResponse plan(TravelPlanRequest request) {
        if (!StringUtils.hasText(dashScopeApiKey) || UNCONFIGURED_API_KEY.equals(dashScopeApiKey)) {
            throw new BusinessException("未配置大模型 API Key，请设置环境变量 DASHSCOPE_API_KEY 后重试");
        }
        String userMessage = buildUserMessage(request);
        List<Message> messages = buildMessages(request, userMessage);
        log.info("收到旅行规划请求：{}（带入 {} 条上下文）", userMessage, messages.size() - 1);
        try {
            AssistantMessage message = travelAgent.call(messages);
            TravelPlanResponse response = new TravelPlanResponse();
            response.setContent(message.getText());
            return response;
        } catch (GraphRunnerException e) {
            log.error("Agent 执行失败", e);
            throw new BusinessException("生成旅行方案失败：" + e.getMessage());
        }
    }

    /**
     * 把前端带回的对话上下文拼成本次的完整消息序列，末条是本次需求。
     * 记忆由前端持有（服务端无状态），这里只做条数与长度上的收口，避免客户端传超长内容。
     */
    private List<Message> buildMessages(TravelPlanRequest request, String userMessage) {
        List<Message> messages = new ArrayList<>();
        List<TravelPlanRequest.Turn> history = request.getHistory();
        if (history != null && !history.isEmpty()) {
            int from = Math.max(0, history.size() - MAX_HISTORY_SIZE);
            for (TravelPlanRequest.Turn turn : history.subList(from, history.size())) {
                String content = clip(turn == null ? null : turn.getContent());
                if (!StringUtils.hasText(content)) {
                    continue;
                }
                messages.add(isAssistant(turn.getRole()) ? new AssistantMessage(content) : new UserMessage(content));
            }
        }
        messages.add(new UserMessage(userMessage));
        return messages;
    }

    private static boolean isAssistant(String role) {
        return "assistant".equalsIgnoreCase(role);
    }

    private static String clip(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > MAX_HISTORY_CONTENT_LENGTH
                ? content.substring(0, MAX_HISTORY_CONTENT_LENGTH)
                : content;
    }

    private String buildUserMessage(TravelPlanRequest request) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(request.getLongitude()) && StringUtils.hasText(request.getLatitude())) {
            builder.append("用户当前位置（经纬度，高德 GCJ-02 坐标系）：")
                    .append(request.getLongitude()).append(",").append(request.getLatitude())
                    .append("\n");
        }
        if (StringUtils.hasText(request.getOrigin())) {
            builder.append("出发地：").append(request.getOrigin()).append("\n");
        }
        builder.append("需求：").append(request.getRequirement());
        return builder.toString();
    }
}
