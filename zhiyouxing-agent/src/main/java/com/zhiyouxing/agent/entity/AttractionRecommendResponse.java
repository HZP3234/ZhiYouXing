package com.zhiyouxing.agent.entity;

import lombok.Data;

import java.util.List;

/**
 * 景点智能推荐响应：按推荐优先级排列的景点及理由，外加一句话综述。
 */
@Data
public class AttractionRecommendResponse {

    private String summary;

    private List<Item> recommendations;

    @Data
    public static class Item {

        private Long attractionId;

        private String reason;
    }
}
