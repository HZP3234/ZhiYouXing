package com.zhiyouxing.travel.controller;

import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.service.TravelGuideTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 旅游攻略标签（字典）。
 *
 * <p>只有一个读接口 —— 筛选栏那排标签 chips 的字典（带每个标签下的篇数）。
 * 标签是作者在写作页里随手建的，没有增删改接口，管理端也不登记它，
 * 所以这里刻意不做成代码生成器那套 CRUD。
 *
 * <p>@IgnoreAuth：浏览攻略不需要登录（和 /travel_guide/list 一个口径）。
 */
@RestController
@RequestMapping("/travel_guide_tag")
public class TravelGuideTagController {

    @Autowired
    private TravelGuideTagService travelGuideTagService;

    @IgnoreAuth
    @RequestMapping("/list")
    public R list() {
        return R.ok().put("data", travelGuideTagService.listWithCount());
    }
}
