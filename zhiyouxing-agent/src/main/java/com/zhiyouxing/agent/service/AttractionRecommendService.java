package com.zhiyouxing.agent.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyouxing.agent.entity.AttractionRecommendRequest;
import com.zhiyouxing.agent.entity.AttractionRecommendResponse;
import com.zhiyouxing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 景点智能推荐：候选景点由 attraction 服务检索后传入，本服务只做排序与理由生成。
 *
 * <p>这里不挂 ReactAgent 也不调用工具——候选数据已经备齐，直接走一次 ChatClient 即可，
 * 既省一次工具编排的耗时，也避免模型把候选景点之外的景点编进来。
 */
@Slf4j
@Service
public class AttractionRecommendService {

    private static final int DEFAULT_TOP_N = 3;

    /** 限制输出长度：结果是一段 JSON，900 token 足够覆盖 10 个候选的理由。 */
    private static final DashScopeChatOptions LIMITED_OPTIONS = DashScopeChatOptions.builder()
            .maxToken(900)
            .build();

    private static final String PROMPT = """
            你是「智游星」的景点推荐助手。请从下面的候选景点中，为用户挑选最合适的 %d 个并排序。

            用户需求：
            - 城市：%s
            - 偏好：%s
            - 人均门票预算：%s
            - 游玩天数：%s

            候选景点：
            %s

            只输出一个 JSON 对象，不要输出 Markdown 代码块标记、前后缀说明或任何其他文字，格式为：
            {"summary":"一句话推荐综述，60 字以内","recommendations":[{"attractionId":候选景点中的 id,"reason":"推荐理由，60 字以内"}]}

            要求：
            1. recommendations 按推荐优先级从高到低排列，最多 %d 条，attractionId 必须来自候选景点；
            2. 推荐理由要具体，结合该景点的标签、类型、等级、评分与门票起价，说明为什么适合该用户；
            3. 只使用候选景点中给出的信息，不要编造开放时间、票价或候选之外的内容。
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String dashScopeApiKey;

    public AttractionRecommendService(ChatModel chatModel,
                                      ObjectMapper objectMapper,
                                      @Value("${spring.ai.dashscope.api-key:}") String dashScopeApiKey) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.objectMapper = objectMapper;
        this.dashScopeApiKey = dashScopeApiKey;
    }

    public AttractionRecommendResponse recommend(AttractionRecommendRequest request) {
        if (!StringUtils.hasText(dashScopeApiKey) || AgentService.UNCONFIGURED_API_KEY.equals(dashScopeApiKey)) {
            throw new BusinessException("未配置大模型 API Key，请设置环境变量 DASHSCOPE_API_KEY 后重试");
        }
        if (CollectionUtils.isEmpty(request.getCandidates())) {
            throw new BusinessException("候选景点为空，无法推荐");
        }

        int topN = request.getTopN() == null || request.getTopN() < 1 ? DEFAULT_TOP_N : request.getTopN();
        String prompt = String.format(PROMPT,
                topN,
                safe(request.getCity()),
                CollectionUtils.isEmpty(request.getPreferences()) ? "无特别偏好" : String.join("、", request.getPreferences()),
                request.getBudget() == null ? "不限" : "¥" + request.getBudget(),
                request.getDays() == null ? "未说明" : request.getDays() + " 天",
                writeCandidates(request),
                topN);

        String content;
        try {
            content = chatClient.prompt().user(prompt).options(LIMITED_OPTIONS).call().content();
        } catch (Exception e) {
            log.error("生成景点推荐失败，城市 {}", request.getCity(), e);
            throw new BusinessException("生成景点推荐失败：" + e.getMessage());
        }
        return parse(content);
    }

    /** 模型被要求只输出 JSON，但仍可能包上代码块或前后缀，这里做一次宽松提取。 */
    private AttractionRecommendResponse parse(String content) {
        String json = extractJson(content);
        if (json == null) {
            log.warn("景点推荐返回内容无法解析为 JSON：{}", abbreviate(content));
            return emptyResponse();
        }
        try {
            AttractionRecommendResponse response = objectMapper.readValue(json, AttractionRecommendResponse.class);
            return response == null ? emptyResponse() : response;
        } catch (Exception e) {
            log.warn("景点推荐返回内容解析失败：{}", abbreviate(content), e);
            return emptyResponse();
        }
    }

    private AttractionRecommendResponse emptyResponse() {
        AttractionRecommendResponse response = new AttractionRecommendResponse();
        response.setRecommendations(List.of());
        return response;
    }

    private static String extractJson(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            text = firstLineEnd < 0 ? text : text.substring(firstLineEnd + 1);
            int closingFence = text.lastIndexOf("```");
            if (closingFence >= 0) {
                text = text.substring(0, closingFence);
            }
            text = text.trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

    private String writeCandidates(AttractionRecommendRequest request) {
        try {
            return objectMapper.writeValueAsString(request.getCandidates());
        } catch (Exception e) {
            throw new BusinessException("候选景点序列化失败：" + e.getMessage());
        }
    }

    private static String safe(String value) {
        return StringUtils.hasText(value) ? value : "未说明";
    }

    private static String abbreviate(String content) {
        if (content == null) {
            return "<null>";
        }
        return content.length() <= 200 ? content : content.substring(0, 200) + "...";
    }
}
