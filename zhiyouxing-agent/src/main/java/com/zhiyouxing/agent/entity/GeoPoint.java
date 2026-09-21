package com.zhiyouxing.agent.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * 地理定位结果，经纬度采用高德坐标系（GCJ-02）。
 */
@Data
public class GeoPoint {

    @JsonProperty("province")
    @JsonPropertyDescription("省 / 直辖市，例如：浙江省")
    private String province;

    @JsonProperty("city")
    @JsonPropertyDescription("城市，例如：杭州市；直辖市与省级地址回退为省名")
    private String city;

    @JsonProperty("district")
    @JsonPropertyDescription("区县，例如：西湖区")
    private String district;

    @JsonProperty("adcode")
    @JsonPropertyDescription("行政区划编码，例如：330100")
    private String adcode;

    @JsonProperty("longitude")
    @JsonPropertyDescription("经度（高德 GCJ-02 坐标系）")
    private String longitude;

    @JsonProperty("latitude")
    @JsonPropertyDescription("纬度（高德 GCJ-02 坐标系）")
    private String latitude;

    @JsonProperty("formattedAddress")
    @JsonPropertyDescription("完整地址描述")
    private String formattedAddress;

    @JsonProperty("message")
    @JsonPropertyDescription("定位不可用时的说明，可用时为空")
    private String message;
}
