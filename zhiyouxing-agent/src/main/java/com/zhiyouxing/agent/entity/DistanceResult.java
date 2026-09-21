package com.zhiyouxing.agent.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * 两地之间的实际里程与耗时，用于判断可选交通方式。
 */
@Data
public class DistanceResult {

    @JsonProperty("originName")
    @JsonPropertyDescription("起点标准名称，例如：上海市")
    private String originName;

    @JsonProperty("destinationName")
    @JsonPropertyDescription("终点标准名称，例如：杭州市")
    private String destinationName;

    @JsonProperty("distanceKm")
    @JsonPropertyDescription("实际驾车里程（公里）")
    private Double distanceKm;

    @JsonProperty("drivingDuration")
    @JsonPropertyDescription("预计驾车耗时，例如：2小时30分")
    private String drivingDuration;

    @JsonProperty("suggestedModes")
    @JsonPropertyDescription("按里程给出的交通方式建议")
    private String suggestedModes;

    @JsonProperty("message")
    @JsonPropertyDescription("测距不可用时的说明，可用时为空")
    private String message;
}
