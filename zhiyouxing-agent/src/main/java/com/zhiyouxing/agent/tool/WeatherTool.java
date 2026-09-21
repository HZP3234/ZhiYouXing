package com.zhiyouxing.agent.tool;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhiyouxing.agent.entity.WeatherResponse;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 基于和风天气（QWeather）实时天气接口，供 Agent 查询目的地天气。
 */
@Slf4j
@Component
public class WeatherTool {

    @Value("${hefeng.weather.host:}")
    private String apiHost;

    @Value("${hefeng.api.key:}")
    private String apiKey;

    @Tool(description = "查询指定城市的实时天气，返回温度、体感温度、天气状况、风向风力、湿度、能见度等信息")
    public WeatherResponse getWeather(@ToolParam(description = "目的地城市名称，例如：杭州") String city) {
        if (!StringUtils.hasText(city)) {
            return unavailable("未提供城市名称，无法查询天气");
        }
        if (!StringUtils.hasText(apiHost) || !StringUtils.hasText(apiKey)) {
            log.warn("未配置 hefeng.weather.host / hefeng.api.key，跳过实时天气查询");
            return unavailable("天气服务未配置（需设置 HEFENG_WEATHER_HOST 与 HEFENG_API_KEY），暂时无法获取实时天气");
        }

        try {
            String locationId = lookupLocationId(city);
            if (!StringUtils.hasText(locationId)) {
                return unavailable("未查询到城市「" + city + "」的天气信息");
            }
            return queryNow(locationId);
        } catch (Exception e) {
            log.error("查询天气失败，城市：{}", city, e);
            return unavailable("天气服务暂时不可用：" + e.getMessage());
        }
    }

    private String lookupLocationId(String city) {
        @Cleanup
        HttpResponse response = HttpUtil.createGet(apiHost + "/geo/v2/city/lookup")
                .header("Content-Type", "application/json")
                .header("X-QW-Api-Key", apiKey)
                .form("location", city)
                .execute();
        String body = response.body();
        log.info("城市搜索接口返回：{}", body);
        return JSONUtil.getByPath(JSONUtil.parseObj(body), "$.location[0].id", null);
    }

    private WeatherResponse queryNow(String locationId) {
        @Cleanup
        HttpResponse response = HttpUtil.createGet(apiHost + "/v7/weather/now")
                .header("Content-Type", "application/json")
                .header("X-QW-Api-Key", apiKey)
                .form("location", locationId)
                .form("lang", "zh")
                .execute();
        String body = response.body();
        log.info("实时天气接口返回：{}", body);
        JSONObject now = JSONUtil.parseObj(body).getJSONObject("now");
        if (now == null) {
            return unavailable("天气服务返回数据异常");
        }
        return now.toBean(WeatherResponse.class);
    }

    private WeatherResponse unavailable(String message) {
        WeatherResponse response = new WeatherResponse();
        response.setText(message);
        return response;
    }
}
