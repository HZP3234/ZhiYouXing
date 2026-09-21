package com.zhiyouxing.agent.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TravelPlanRequest {

    @NotBlank(message = "旅行需求不能为空")
    private String requirement;

    /** 出发地，可选；未填写时由 Agent 从需求中推断 */
    private String origin;

    /** 用户当前经度（高德 GCJ-02 坐标系），可选；由前端定位得到，优先于出发地城市名 */
    private String longitude;

    /** 用户当前纬度（高德 GCJ-02 坐标系），可选；由前端定位得到，优先于出发地城市名 */
    private String latitude;

    /**
     * 此前几轮对话，用于多轮追问（如「那改成 7 天」）。按时间先后排列，可为空。
     * 记忆由前端随请求带回，服务端不保存会话状态。
     */
    private List<Turn> history;

    @Data
    public static class Turn {

        /** user 或 assistant，其余值按 user 处理 */
        private String role;

        private String content;
    }
}
