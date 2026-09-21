package com.zhiyouxing.agent.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于高德地图 POI 检索，为 Agent 提供目的地的真实点位素材。
 * 城市点位一律走这里，不用向量检索：本地语料覆盖不了任意目的地，检索未收录的城市会返回无关片段，
 * 反而把三档方案带偏；高德对任意城市都能取到带评分与人均消费的真实点位。
 * Milvus 只保留「与目的地无关」的通用知识（见 {@link KnowledgeTool}），两者不重叠。
 */
@Slf4j
@Component
public class PoiTool {

    /** 默认检索的三组素材，顺序即输出顺序。 */
    private static final List<Category> DEFAULT_CATEGORIES = List.of(
            new Category("必游景点", "景点"),
            new Category("特色小吃", "特色小吃"),
            new Category("住宿与交通枢纽", "地铁站"));

    /** 每组取回的点位数。 */
    private static final int CATEGORY_LIMIT = 6;

    /** 高德 QPS 超限的错误码。 */
    private static final String QPS_LIMIT_INFO_CODE = "10021";

    /** 命中 QPS 限制后的等待时长，稍等重试一次基本都能过。 */
    private static final long QPS_RETRY_DELAY_MILLIS = 800;

    @Value("${amap.host:https://restapi.amap.com}")
    private String apiHost;

    @Value("${amap.key:}")
    private String apiKey;

    /**
     * 「目的地|关键词」-> 素材文本。高德 Web 服务 QPS 限制很严（实测 1 秒内第 4 次调用就会被拒），
     * 而一次问答里 Agent 与本项目的 generateGuide 会各取一遍，故缓存；只缓存取到内容的结果，失败不缓存。
     */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Tool(description = "用高德地图检索目的地的真实点位素材：默认返回必游景点、特色小吃、住宿与交通枢纽三组，附高德评分与人均消费，撰写行程前调用；传 keyword 时改为按该关键词检索指定类型的点位")
    public String searchDestination(
            @ToolParam(description = "目的地城市名称，例如：北京") String destination,
            @ToolParam(description = "可选，聚焦关键词，例如：博物馆、温泉、亲子乐园；省略则返回三组默认素材", required = false) String keyword) {
        if (!StringUtils.hasText(destination)) {
            return "未提供目的地，无法检索点位素材。";
        }
        String city = destination.trim();
        String material = material(city, StringUtils.hasText(keyword) ? keyword.trim() : null);
        if (!StringUtils.hasText(material)) {
            return "未检索到「" + city + "」的点位素材（可能未配置 AMAP_KEY，或高德接口暂时不可用）。"
                    + "请基于通用经验作答，不要编造具体店名与实际票价。";
        }
        return "高德检索到的「" + city + "」点位素材：\n" + material;
    }

    /**
     * 取目的地的默认点位素材，供本工具与 TravelGuideTool 内部调用。
     * 取不到时返回空串，由调用方决定降级措辞。
     */
    public String materialFor(String destination) {
        return StringUtils.hasText(destination) ? material(destination.trim(), null) : "";
    }

    /** 带缓存的取素材：keyword 为空时取默认三组。 */
    private String material(String city, String keyword) {
        String cacheKey = keyword == null ? city : city + "|" + keyword;
        String cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<Category> categories = keyword == null
                ? DEFAULT_CATEGORIES
                : List.of(new Category(keyword, keyword));
        String material = collect(city, categories);
        if (StringUtils.hasText(material)) {
            cache.put(cacheKey, material);
        }
        return material;
    }

    private String collect(String city, List<Category> categories) {
        if (!configured()) {
            log.warn("未配置 amap.key，跳过 POI 检索");
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Category category : categories) {
            String block = search(city, category);
            if (StringUtils.hasText(block)) {
                builder.append("【").append(city).append(" · ").append(category.title()).append("】\n").append(block);
            }
        }
        return builder.toString().trim();
    }

    /** 单组检索：接口异常或无结果都返回空串，由调用方跳过该组，不影响其余组。 */
    private String search(String city, Category category) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("city", city);
        params.put("keywords", category.keyword());
        // 限定本市，否则会串到周边城市的同名点位
        params.put("citylimit", "true");
        params.put("offset", String.valueOf(CATEGORY_LIMIT));
        params.put("page", "1");
        // extensions=all 才会带出 biz_ext（评分、人均消费）
        params.put("extensions", "all");
        try {
            JSONObject json = get("/v3/place/text", params);
            if (qpsLimited(json)) {
                log.info("高德 POI 检索触发 QPS 限制，{}ms 后重试一次，city={}，keyword={}",
                        QPS_RETRY_DELAY_MILLIS, city, category.keyword());
                sleepQuietly();
                json = get("/v3/place/text", params);
            }
            if (!ok(json)) {
                log.warn("高德 POI 检索返回异常，city={}，keyword={}，info={}", city, category.keyword(), json.getStr("info"));
                return "";
            }
            JSONArray pois = json.getJSONArray("pois");
            if (pois == null || pois.isEmpty()) {
                log.info("高德 POI 检索无结果，city={}，keyword={}", city, category.keyword());
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (Object item : pois) {
                String line = describe((JSONObject) item);
                if (StringUtils.hasText(line)) {
                    builder.append("- ").append(line).append('\n');
                }
            }
            log.info("高德 POI 检索命中 {} 个点位，city={}，keyword={}", pois.size(), city, category.keyword());
            return builder.toString();
        } catch (Exception e) {
            log.error("高德 POI 检索失败，city={}，keyword={}", city, category.keyword(), e);
            return "";
        }
    }

    /** 单个点位整理成「名称（区域，人均 ¥X，评分 Y）」，只写高德确实返回了的字段。 */
    private static String describe(JSONObject poi) {
        String name = poi.getStr("name");
        if (!StringUtils.hasText(name)) {
            return "";
        }
        List<String> extras = new ArrayList<>();
        addIfPresent(extras, text(poi, "adname"));
        JSONObject bizExt = poi.getJSONObject("biz_ext");
        if (bizExt != null) {
            String cost = text(bizExt, "cost");
            if (StringUtils.hasText(cost)) {
                extras.add("人均 ¥" + cost);
            }
            String rating = text(bizExt, "rating");
            if (StringUtils.hasText(rating)) {
                extras.add("评分 " + rating);
            }
        }
        return extras.isEmpty() ? name : name + "（" + String.join("，", extras) + "）";
    }

    private JSONObject get(String path, Map<String, String> params) {
        HttpRequest request = HttpUtil.createGet(apiHost + path).header("Content-Type", "application/json");
        params.forEach(request::form);
        request.form("key", apiKey);
        @Cleanup
        HttpResponse response = request.execute();
        return JSONUtil.parseObj(response.body());
    }

    private static boolean ok(JSONObject json) {
        return json != null && "1".equals(json.getStr("status"));
    }

    private static boolean qpsLimited(JSONObject json) {
        return json != null && QPS_LIMIT_INFO_CODE.equals(json.getStr("infocode"));
    }

    private static void sleepQuietly() {
        try {
            Thread.sleep(QPS_RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean configured() {
        return StringUtils.hasText(apiHost) && StringUtils.hasText(apiKey);
    }

    /** 高德对无值字段会返回空数组而不是空串，这里统一转成空串。 */
    private static String text(JSONObject json, String key) {
        Object value = json.get(key);
        if (value == null || value instanceof JSONArray) {
            return "";
        }
        String text = value.toString();
        return "[]".equals(text) ? "" : text;
    }

    private static void addIfPresent(List<String> target, String value) {
        if (StringUtils.hasText(value)) {
            target.add(value);
        }
    }

    /** 一组素材：展示用的标题 + 实际检索关键词。 */
    private record Category(String title, String keyword) {
    }
}
