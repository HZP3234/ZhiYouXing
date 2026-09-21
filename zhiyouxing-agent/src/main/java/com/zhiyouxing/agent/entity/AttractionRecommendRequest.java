package com.zhiyouxing.agent.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点智能推荐请求：候选景点由 attraction 服务检索后传入，本服务只负责排序与生成理由。
 */
@Data
public class AttractionRecommendRequest {

    private String city;

    private List<String> preferences;

    /** 人均门票预算 */
    private BigDecimal budget;

    private Integer days;

    private Integer topN;

    private List<Candidate> candidates;

    /** 候选景点，字段与 attraction 服务的契约保持一致。 */
    @Data
    public static class Candidate {

        private Long id;

        private String name;

        private String tags;

        private String category;

        private String level;

        private BigDecimal score;

        private BigDecimal minPrice;

        private String description;
    }
}
