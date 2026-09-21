package com.zhiyouxing.agent.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhiyouxing.agent.entity.DistanceResult;
import com.zhiyouxing.agent.entity.GeoPoint;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于高德地图 Web 服务 API，为 Agent 提供用户定位与两地实际距离/耗时测算。
 * 所有经纬度均为高德坐标系（GCJ-02）。
 */
@Slf4j
@Component
public class LocationTool {

    /** 匹配「经度,纬度」形式的坐标串。 */
    private static final Pattern COORD_PATTERN =
            Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    @Value("${amap.host:https://restapi.amap.com}")
    private String apiHost;

    @Value("${amap.key:}")
    private String apiKey;

    @Tool(description = "定位用户当前位置。优先用经纬度（高德 GCJ-02 坐标系）做逆地理编码，否则用城市名做地理编码，返回省、市、区、经纬度")
    public GeoPoint locate(
            @ToolParam(description = "城市名称，例如：杭州；仅在未提供经纬度时使用", required = false) String city,
            @ToolParam(description = "经度（高德 GCJ-02 坐标系），例如：120.15", required = false) String longitude,
            @ToolParam(description = "纬度（高德 GCJ-02 坐标系），例如：30.28", required = false) String latitude) {
        if (!configured()) {
            log.warn("未配置 amap.key，跳过定位");
            return unavailablePoint("定位服务未配置（需设置 AMAP_KEY），暂时无法获取用户位置");
        }
        try {
            if (StringUtils.hasText(longitude) && StringUtils.hasText(latitude)) {
                return regeo(longitude.trim(), latitude.trim());
            }
            if (StringUtils.hasText(city)) {
                return geocode(city);
            }
            return unavailablePoint("未提供经纬度或城市名，无法定位");
        } catch (Exception e) {
            log.error("定位失败，city={}, lng={}, lat={}", city, longitude, latitude, e);
            return unavailablePoint("定位服务暂时不可用：" + e.getMessage());
        }
    }

    @Tool(description = "测量出发地到目的地的实际驾车里程与预计耗时，并按里程给出可选交通方式建议。出发地与目的地均可传城市名（如 上海）或经纬度（lng,lat）")
    public DistanceResult measureDistance(
            @ToolParam(description = "出发地，城市名（如 上海）或经纬度（lng,lat）") String origin,
            @ToolParam(description = "目的地，城市名（如 杭州）或经纬度（lng,lat）") String destination) {
        if (!StringUtils.hasText(origin) || !StringUtils.hasText(destination)) {
            return unavailableDistance("出发地或目的地缺失，无法测算距离");
        }
        if (!configured()) {
            log.warn("未配置 amap.key，跳过距离测算");
            return unavailableDistance("测距服务未配置（需设置 AMAP_KEY），暂时无法获取距离");
        }
        try {
            GeoPoint from = resolve(origin);
            GeoPoint to = resolve(destination);
            if (unavailable(from) || unavailable(to)) {
                return unavailableDistance("无法解析出发地或目的地：" + origin + " -> " + destination);
            }
            return drivingDistance(from, to);
        } catch (Exception e) {
            log.error("测量距离失败，{} -> {}", origin, destination, e);
            return unavailableDistance("测距服务暂时不可用：" + e.getMessage());
        }
    }

    private DistanceResult drivingDistance(GeoPoint from, GeoPoint to) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("origins", from.getLongitude() + "," + from.getLatitude());
        params.put("destination", to.getLongitude() + "," + to.getLatitude());
        params.put("type", "1");
        JSONObject json = get("/v3/distance", params);
        if (!ok(json)) {
            return unavailableDistance("高德测距接口返回异常：" + json.getStr("info"));
        }
        JSONArray results = json.getJSONArray("results");
        if (results == null || results.isEmpty()) {
            return unavailableDistance("未查询到 " + from.getCity() + " 到 " + to.getCity() + " 的路线");
        }
        JSONObject first = results.getJSONObject(0);
        double meters = number(first.getStr("distance"));
        double seconds = number(first.getStr("duration"));

        DistanceResult result = new DistanceResult();
        result.setOriginName(from.getCity());
        result.setDestinationName(to.getCity());
        result.setDistanceKm(Math.round(meters / 100.0) / 10.0);
        result.setDrivingDuration(formatDuration((long) seconds));
        result.setSuggestedModes(suggestModes(result.getDistanceKm()));
        return result;
    }

    /** 把城市名或「经度,纬度」统一解析为标准地理点。 */
    private GeoPoint resolve(String location) {
        Matcher matcher = COORD_PATTERN.matcher(location);
        if (matcher.matches()) {
            return regeo(matcher.group(1), matcher.group(2));
        }
        return geocode(location);
    }

    private GeoPoint regeo(String longitude, String latitude) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("location", longitude + "," + latitude);
        JSONObject json = get("/v3/geocode/regeo", params);
        if (!ok(json)) {
            return unavailablePoint("逆地理编码失败：" + json.getStr("info"));
        }
        JSONObject regeocode = json.getJSONObject("regeocode");
        if (regeocode == null) {
            return unavailablePoint("坐标 " + longitude + "," + latitude + " 未匹配到地址");
        }
        JSONObject component = regeocode.getJSONObject("addressComponent");
        GeoPoint point = new GeoPoint();
        point.setLongitude(longitude);
        point.setLatitude(latitude);
        point.setFormattedAddress(regeocode.getStr("formatted_address"));
        if (component != null) {
            point.setProvince(text(component, "province"));
            // 直辖市在逆地理编码中 city 为空数组，回退为省名
            String city = text(component, "city");
            point.setCity(StringUtils.hasText(city) ? city : point.getProvince());
            point.setDistrict(text(component, "district"));
            point.setAdcode(component.getStr("adcode"));
        }
        return point;
    }

    private GeoPoint geocode(String name) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("address", name);
        JSONObject json = get("/v3/geocode/geo", params);
        if (!ok(json)) {
            return unavailablePoint("地理编码失败：" + json.getStr("info"));
        }
        JSONArray geocodes = json.getJSONArray("geocodes");
        if (geocodes == null || geocodes.isEmpty()) {
            return unavailablePoint("未查询到「" + name + "」对应的位置");
        }
        JSONObject first = geocodes.getJSONObject(0);
        String[] coord = splitLocation(first.getStr("location"));
        if (coord == null) {
            return unavailablePoint("「" + name + "」返回的坐标格式异常");
        }
        GeoPoint point = new GeoPoint();
        point.setLongitude(coord[0]);
        point.setLatitude(coord[1]);
        point.setProvince(text(first, "province"));
        String city = text(first, "city");
        point.setCity(StringUtils.hasText(city) ? city : point.getProvince());
        point.setDistrict(text(first, "district"));
        point.setAdcode(first.getStr("adcode"));
        point.setFormattedAddress(first.getStr("formatted_address"));
        return point;
    }

    private JSONObject get(String path, Map<String, String> params) {
        HttpRequest request = HttpUtil.createGet(apiHost + path)
                .header("Content-Type", "application/json");
        params.forEach(request::form);
        request.form("key", apiKey);
        @Cleanup
        HttpResponse response = request.execute();
        String body = response.body();
        log.info("高德接口 {} 返回：{}", path, body);
        return JSONUtil.parseObj(body);
    }

    private static boolean ok(JSONObject json) {
        return json != null && "1".equals(json.getStr("status"));
    }

    private static boolean unavailable(GeoPoint point) {
        return point == null || StringUtils.hasText(point.getMessage());
    }

    private boolean configured() {
        return StringUtils.hasText(apiHost) && StringUtils.hasText(apiKey);
    }

    /** 高德在无下级行政区时会返回空数组，这里统一转成空串。 */
    private static String text(JSONObject json, String key) {
        Object value = json.get(key);
        if (value == null || value instanceof JSONArray) {
            return "";
        }
        String text = value.toString();
        return "[]".equals(text) ? "" : text;
    }

    private static String[] splitLocation(String location) {
        return StringUtils.hasText(location) && location.contains(",") ? location.split(",", 2) : null;
    }

    private static double number(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "未知";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return minutes > 0 ? hours + "小时" + minutes + "分" : hours + "小时";
        }
        return Math.max(minutes, 1) + "分钟";
    }

    private static String suggestModes(double km) {
        if (km < 50) {
            return "同城或近郊，自驾、打车或地铁公交即可，无需长途交通";
        }
        if (km < 150) {
            return "短途，自驾或长途大巴为主，高铁/动车为辅";
        }
        if (km < 600) {
            return "中途，高铁/动车为首选，自驾次之，飞机性价比低";
        }
        if (km < 1200) {
            return "中长途，高铁（约 4-6 小时）与飞机竞争，高铁门到门更省心";
        }
        return "长途，飞机为首选，高铁耗时偏长，不建议自驾";
    }

    private static GeoPoint unavailablePoint(String message) {
        GeoPoint point = new GeoPoint();
        point.setMessage(message);
        return point;
    }

    private static DistanceResult unavailableDistance(String message) {
        DistanceResult result = new DistanceResult();
        result.setMessage(message);
        return result;
    }
}
