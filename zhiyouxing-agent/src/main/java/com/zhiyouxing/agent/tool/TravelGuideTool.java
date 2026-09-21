package com.zhiyouxing.agent.tool;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 生成目的地旅行攻略，供 Agent 调用。
 */
@Slf4j
@Component
public class TravelGuideTool {

    private static final String PROMPT = """
            请为「%s」生成经济型、舒适型、品质型三档预算的旅行方案%s%s。
            要求使用 Markdown 输出，三档合计不超过 750 字，只给要点，不要复述背景或概况。

            每档统一使用如下结构，标题形如「### 经济型（人均约 ¥1200）」：
            - **行程**：每天一行，形如「D1 上午 西湖骑行；下午 灵隐寺；晚上 河坊街」，
              只写景点或活动名称，禁止括号补充说明，禁止「含…」「如…」这类展开。
            - **住宿**：推荐区域 + 档次 + 参考价位，一行。
            - **预算**：写成一行等式，四项必须齐全，形如
              「交通 ¥60 + 住宿 ¥1700 + 门票 ¥125 + 餐饮 ¥115 = ¥2000」。
              餐饮优先引用参考素材里给出的人均消费，门票、住宿等素材未覆盖的按区间估算（按人均口径）；
              住宿按「天数 - 1」晚计算。
              等式左边四项相加必须等于右边合计，不得漏写其中一项，也不得虚增总额凑数。
              若按素材价位的住宿档次会撑破该档预算，就下调住宿档次。
            - **取舍**：一句话说明这一档适合谁、短板是什么。

            依次给出经济型、舒适型、品质型三档，结构完全相同，每档控制在 250 字以内。
            标题括号里的金额必须与档内「合计」金额完全一致。
            三档的差异必须体现在行程密度、住宿档次与预算上，不得三档内容雷同。
            金额一律按「人均」口径并标明为估算。

            三档之后另起一节，标题为「## 必吃与贴士」，从参考素材中提取：
            - **必吃**：2-3 道代表菜或小吃，各带参考素材里的店名或觅食区域。
            - **贴士**：2-3 条避坑提醒（预约、购票渠道、安全等）。
            本节合计不超过 120 字；素材未覆盖的不要编造，若无素材则写「暂无本地数据」。
            「贴士」只能复述素材里已有的提醒，不要添加素材未提及的渠道名（具体 App、公众号）或时间要求。
            %s""";

    /** 有检索素材时追加的约束，素材中没有的价格与时间不要臆造。 */
    private static final String REFERENCE_RULE = """
            下方参考素材为撰写依据，三档方案均须以素材为准组织内容，不得与素材冲突；
            素材未覆盖的票价、开放时间等细节不要编造，可省略或注明需以官方为准。""";

    /**
     * 无素材时的硬约束。实测缺素材时模型会自行补出看似具体的店名与门店名（如「王记」「湖墅店」），
     * 这类细节比留空更有害，因此明确禁止。
     */
    private static final String NO_REFERENCE_RULE = """
            本次没有取到点位素材（高德接口不可用或未收录），只能写通用行程：
            不得编造具体店名、门店或分店名、票价、开放时间、预约渠道名称，这些位置一律留空或写「以官方为准」。""";

    /** reference 短于此长度视为模型没把素材抄全，改由本工具自行检索。 */
    private static final int MIN_REFERENCE_LENGTH = 100;

    /** 限制单次生成的输出长度，避免长文生成拖慢整体响应。 */
    private static final DashScopeChatOptions LIMITED_OPTIONS = DashScopeChatOptions.builder()
            .maxToken(2000)
            .build();

    private final ChatClient chatClient;
    private final PoiTool poiTool;

    public TravelGuideTool(ChatModel chatModel, PoiTool poiTool) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.poiTool = poiTool;
    }

    @Tool(description = "生成目的地的经济型/舒适型/品质型三档预算旅行方案，每档含逐日行程、住宿区域、人均预算与取舍说明")
    public String generateGuide(
            @ToolParam(description = "目的地城市名称，例如：杭州") String destination,
            @ToolParam(description = "出行天数，例如：3", required = false) String days,
            @ToolParam(description = "用户偏好，例如：自然风光、亲子游、预算2000元", required = false) String preferences,
            @ToolParam(description = "点位素材原文；不传或过短时本工具会自行检索高德点位，可省略", required = false) String reference) {
        if (!StringUtils.hasText(destination)) {
            return "未提供目的地，无法生成旅行攻略。";
        }
        String dayPart = StringUtils.hasText(days) ? "，行程共 " + days + " 天" : "";
        String preferencePart = StringUtils.hasText(preferences) ? "，用户偏好：" + preferences : "";
        String material = effectiveReference(destination, reference);
        String referencePart = StringUtils.hasText(material) ? "\n参考资料：\n" + material : "";
        String rulePart = StringUtils.hasText(material) ? REFERENCE_RULE : NO_REFERENCE_RULE;
        log.info("生成三档方案，destination={}，素材={} 字（reference 入参 {} 字）", destination,
                material.length(), StringUtils.hasText(reference) ? reference.length() : 0);
        try {
            return chatClient.prompt()
                    .user(String.format(PROMPT, destination, dayPart, preferencePart, referencePart + rulePart))
                    .options(LIMITED_OPTIONS)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("生成旅行攻略失败，目的地：{}", destination, e);
            return "旅行攻略生成失败：" + e.getMessage();
        }
    }

    /**
     * 取用于生成攻略的素材：模型传入的 reference 够完整就用它，否则改由本工具按目的地直接检索高德点位。
     * 实测模型转抄并不可靠（同一请求出现过 715 字与 27 字两次），素材不足时它会用记忆补出假店名，
     * 因此把素材来源收敛到工具自身，reference 退化为可选补充。
     */
    private String effectiveReference(String destination, String reference) {
        if (StringUtils.hasText(reference) && reference.length() >= MIN_REFERENCE_LENGTH) {
            return reference;
        }
        String retrieved = poiTool.materialFor(destination);
        if (StringUtils.hasText(retrieved)) {
            log.info("reference 入参不足，已自行检索高德点位，destination={}，取到 {} 字", destination, retrieved.length());
            return retrieved;
        }
        return StringUtils.hasText(reference) ? reference : "";
    }
}
