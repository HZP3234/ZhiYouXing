package com.zhiyouxing.agent.entity;

import lombok.Data;

@Data
public class TravelPlanResponse {

    /** 最终旅行方案（Markdown），包含行程概要、最优/次优/第三优三档预算方案与小结 */
    private String content;
}
