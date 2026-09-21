package com.zhiyouxing.agent.tool;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.zhiyouxing.agent.entity.DistanceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 生成出发地到目的地的交通出行方案，供 Agent 调用。
 * 距离与驾车耗时来自高德实测，避免大模型凭空编造里程。
 */
@Slf4j
@Component
public class TrafficTool {

    private static final String PROMPT = """
            请生成从「%s」到「%s」的交通出行方案。%s%s
            要求使用 Markdown 输出，内容精炼，总长度控制在 400 字以内，包含以下部分：
            ## 出行方式对比
            以表格列出 3 种最可行方式（列：方式、大致耗时、参考票价、适合档位、优点、缺点），
            其中「适合档位」填经济型/舒适型/品质型之一，便于按预算分档选用；
            耗时须与上述里程相称，不确定的票价可标注为大致区间，不要编造具体车次。
            ## 三档推荐
            按经济型、舒适型、品质型各给一行，形如「- **经济型**：高铁二等座，约 ¥73，45 分钟」，
            每行写清具体方式 + 参考票价 + 大致耗时，供分档方案直接引用。
            三档的交通档次必须依次递增：经济型用普速列车硬座或长途大巴，
            舒适型用高铁二等座，品质型用高铁一等座/商务座、飞机或含接送的专车，
            不得出现高档位反而用低档交通的情况。
            ## 目的地市内交通
            用 3 条以内要点说明地铁/公交/打车及交通卡建议。
            ## 购票与出行提示
            用 3 条以内要点说明购票渠道、提前预订时间、换乘注意事项。
            若出发地或目的地不明确，请先说明假设再给出通用建议。
            """;

    /** 限制单次生成的输出长度，避免长文生成拖慢整体响应。 */
    private static final DashScopeChatOptions LIMITED_OPTIONS = DashScopeChatOptions.builder()
            .maxToken(800)
            .build();

    private final ChatClient chatClient;
    private final LocationTool locationTool;

    public TrafficTool(ChatModel chatModel, LocationTool locationTool) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.locationTool = locationTool;
    }

    @Tool(description = "生成出发地到目的地的交通出行方案，包含出行方式对比、推荐方案、目的地市内交通和购票提示")
    public String planTraffic(
            @ToolParam(description = "出发地城市名称，未知时传「未知」", required = false) String origin,
            @ToolParam(description = "目的地城市名称，例如：杭州") String destination,
            @ToolParam(description = "出行偏好，例如：时间优先、性价比优先、自驾", required = false) String preference) {
        if (!StringUtils.hasText(destination)) {
            return "未提供目的地，无法生成交通方案。";
        }
        String from = StringUtils.hasText(origin) ? origin : "出发地（用户未明确）";
        String distancePart = StringUtils.hasText(origin) ? describeDistance(locationTool.measureDistance(origin, destination)) : "";
        String preferencePart = StringUtils.hasText(preference) ? "出行偏好：" + preference + "。" : "";
        try {
            return chatClient.prompt()
                    .user(String.format(PROMPT, from, destination, distancePart, preferencePart))
                    .options(LIMITED_OPTIONS)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("生成交通方案失败，{} -> {}", from, destination, e);
            return "交通方案生成失败：" + e.getMessage();
        }
    }

    /** 把实测距离转成提示词中的一段事实描述，测距不可用时返回空串。 */
    private static String describeDistance(DistanceResult distance) {
        if (distance == null || StringUtils.hasText(distance.getMessage()) || distance.getDistanceKm() == null) {
            return "";
        }
        return String.format("两地实测里程约 %.1f 公里，驾车约 %s；建议交通方式：%s。",
                distance.getDistanceKm(), distance.getDrivingDuration(), distance.getSuggestedModes());
    }
}
