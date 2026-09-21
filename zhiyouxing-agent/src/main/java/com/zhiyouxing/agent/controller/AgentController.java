package com.zhiyouxing.agent.controller;

import com.zhiyouxing.agent.entity.AttractionRecommendRequest;
import com.zhiyouxing.agent.entity.AttractionRecommendResponse;
import com.zhiyouxing.agent.entity.TravelPlanRequest;
import com.zhiyouxing.agent.entity.TravelPlanResponse;
import com.zhiyouxing.agent.service.AgentService;
import com.zhiyouxing.agent.service.AttractionRecommendService;
import com.zhiyouxing.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;
    private final AttractionRecommendService attractionRecommendService;

    public AgentController(AgentService agentService, AttractionRecommendService attractionRecommendService) {
        this.agentService = agentService;
        this.attractionRecommendService = attractionRecommendService;
    }

    @PostMapping("/travel/plan")
    public Result<TravelPlanResponse> plan(@Valid @RequestBody TravelPlanRequest request) {
        return Result.success(agentService.plan(request));
    }

    /** 景点智能推荐，由 attraction 服务调用，候选景点随请求传入。 */
    @PostMapping("/attraction/recommend")
    public Result<AttractionRecommendResponse> recommendAttractions(@RequestBody AttractionRecommendRequest request) {
        return Result.success(attractionRecommendService.recommend(request));
    }
}
