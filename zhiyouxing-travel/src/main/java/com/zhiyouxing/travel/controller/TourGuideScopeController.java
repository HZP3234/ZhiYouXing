package com.zhiyouxing.travel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.exception.BusinessException;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.GroupTourEntity;
import com.zhiyouxing.travel.entity.TourGuideEntity;
import com.zhiyouxing.travel.entity.TravelGuideEntity;
import com.zhiyouxing.travel.entity.TravelRouteDayEntity;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;
import com.zhiyouxing.travel.service.GroupTourService;
import com.zhiyouxing.travel.service.TourGuideService;
import com.zhiyouxing.travel.service.TravelGuideService;
import com.zhiyouxing.travel.service.TravelRouteDayService;
import com.zhiyouxing.travel.service.TravelRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 导游管理端（数据限定在「自己带的团」）
 *
 * <p>前端管理页按 /&lt;表名&gt;/&lt;资源&gt;/&lt;动作&gt; 拼地址（见 frontend 的
 * pages/admin/AdminCrud.vue 与 common/admin.js），导游这个角色的表名是 tour_guide，
 * 所以这一层挂在 /tour_guide 上；登录态由 TourGuideController 提供。
 *
 * <p>作用域取登录导游的 tour_guide.guide_no：
 * <ul>
 *   <li>travel_route —— 只看、只改自己的线路</li>
 *   <li>group_tour —— 只操作自己线路上的跟团报名</li>
 *   <li>travel_guide —— <b>不限</b>：这张表是用户发的游记攻略（user_account 存的是游客手机号），
 *       没有能指向导游的归属列，前端把它当「攻略内容维护」入口，所以这里保持原有的全量 CRUD</li>
 * </ul>
 *
 * <p>与 AttractionStaffScopeController 同一套写法，保持一致好对照。
 */
@RestController
@RequestMapping("/tour_guide")
public class TourGuideScopeController {

    /** 导游没绑工号时的提示：不能退化成「看全表」 */
    private static final String UNBOUND = "当前账号缺少工号，请联系管理员";

    /**
     * 线路一经导游手（新建或改动）就回到这个状态，只有管理员能改出去。
     * 前台 /travel_route/search 只放行「已通过」，所以导游自建自改的线路不会漏到游客页。
     */
    private static final String AUDIT_PENDING = "待审核";

    @Autowired
    private TravelRouteService travelRouteService;

    @Autowired
    private GroupTourService groupTourService;

    @Autowired
    private TravelGuideService travelGuideService;

    @Autowired
    private TourGuideService tourGuideService;

    @Autowired
    private TravelRouteDayService travelRouteDayService;

    // ==================== 旅游线路 ====================

    @RequestMapping("/travel_route/page")
    public R travelRoutePage(@RequestParam Map<String, Object> params, TravelRouteEntity travelRoute,
                             HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<TravelRouteEntity> ew = new QueryWrapper<TravelRouteEntity>();
        MPUtil.likeOrEq(ew, travelRoute);
        ew.eq("guide_no", scope);
        PageUtils page = travelRouteService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/travel_route/info/{id}")
    public R travelRouteInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        TravelRouteEntity row = travelRouteService.getById(id);
        if (row == null) {
            return R.error("线路不存在");
        }
        if (!scope.equals(row.getGuideNo())) {
            return R.error("无权查看其它导游的线路");
        }
        /*
         * 编辑弹窗要预填「每日行程」，所以这里与游客端 /travel_route/detail 一样
         * 把 daily 一起带上。不带的话前端 openEdit 拿到的是 undefined，
         * 逐日编辑器显示成空的，导游一保存就把已有行程整条清掉。
         */
        TravelRouteView view = new TravelRouteView(row);
        travelRouteDayService.attachDaily(Collections.singletonList(view));
        return R.ok().put("data", view);
    }

    @RequestMapping("/travel_route/save")
    public R travelRouteSave(@RequestBody TravelRouteView form, HttpServletRequest request) {
        TourGuideEntity guide = currentGuide(request);
        String scope = StrUtil.isBlank(guide.getGuideNo()) ? null : guide.getGuideNo();
        if (scope == null) {
            return R.error(UNBOUND);
        }
        /*
         * 请求体是 View（比实体多一个 daily 数组），入库前必须先拷回裸实体：
         * daily 不是 travel_route 的列，拿 View 直接调 save 会把 `daily` 拼进 INSERT，
         * MySQL 报 Unknown column。构造器里的 copyProperties 只copy两边都有的属性，
         * daily 自然被跳过。
         */
        TravelRouteEntity travelRoute = new TravelRouteEntity<>(form);
        // 归属列不接受前端传值：新建的必然是自己带的线路，否则这条记录一存下就管不到了
        travelRoute.setGuideNo(scope);
        stampGuide(travelRoute, guide);
        // 新建的一律进审核队列；审核结论只能由管理员写，前端传什么都不认
        travelRoute.setAuditStatus(AUDIT_PENDING);
        travelRoute.setAuditReply(null);
        travelRoute.setAuditTime(null);
        travelRoute.setDays(dayCount(form.getDaily()));
        travelRouteService.save(travelRoute);
        // 主表拿到自增 id 之后才能给行程行挂 ref_id
        travelRouteDayService.replaceDaily(travelRoute.getId(), form.getDaily());
        return R.ok();
    }

    @RequestMapping("/travel_route/update")
    public R travelRouteUpdate(@RequestBody TravelRouteView form, HttpServletRequest request) {
        TourGuideEntity guide = currentGuide(request);
        String scope = StrUtil.isBlank(guide.getGuideNo()) ? null : guide.getGuideNo();
        if (scope == null) {
            return R.error(UNBOUND);
        }
        if (form.getId() == null) {
            return R.error("缺少线路 id");
        }
        TravelRouteEntity exist = travelRouteService.getById(form.getId());
        if (exist == null) {
            return R.error("线路不存在");
        }
        if (!scope.equals(exist.getGuideNo())) {
            return R.error("无权修改其它导游的线路");
        }
        TravelRouteEntity travelRoute = new TravelRouteEntity<>(form);
        // 归属列不可改：把库里的值盖回请求体（改工号等于把线路送走）
        travelRoute.setGuideNo(exist.getGuideNo());
        stampGuide(travelRoute, guide);
        // 改过内容就退回待审核重新审；已通过的线路因此不会「改完还挂着已通过的牌子」
        travelRoute.setAuditStatus(AUDIT_PENDING);
        // 天数是非 null 的整数（没有行程就是 0），所以 updateById 一定会写进去，
        // 不会像 days=null 那样被跳过、留下改之前的天数
        travelRoute.setDays(dayCount(form.getDaily()));
        travelRouteService.updateById(travelRoute);
        /*
         * updateById 跳过 null 字段，清不掉上一轮的审核结论 —— 会出现「改了线路、
         * 状态回到待审核，回复栏里还挂着上次的驳回原因」。要显式 set null。
         * 同 QualificationController.save 重提交的写法。
         */
        travelRouteService.update(new UpdateWrapper<TravelRouteEntity>()
                .eq("id", travelRoute.getId())
                .set("audit_reply", null)
                .set("audit_time", null));
        // 行程整体替换：前端编辑器就是一个数组，改完整个数组发过来
        travelRouteDayService.replaceDaily(travelRoute.getId(), form.getDaily());
        return R.ok();
    }

    @RequestMapping("/travel_route/delete")
    public R travelRouteDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            TravelRouteEntity row = travelRouteService.getById(id);
            if (row == null) {
                return R.error("线路不存在");
            }
            if (!scope.equals(row.getGuideNo())) {
                return R.error("无权删除其它导游的线路");
            }
        }
        travelRouteService.removeByIds(Arrays.asList(ids));
        /*
         * 顺手清掉这些线路的行程行。不清也能跑（没有外键、自增 id 也不会复用），
         * 但会在 travel_route_day 里留下永远查不到的垃圾行。
         */
        for (Long id : ids) {
            travelRouteDayService.replaceDaily(id, null);
        }
        return R.ok();
    }

    // ==================== 跟团报名 ====================

    @RequestMapping("/group_tour/page")
    public R groupTourPage(@RequestParam Map<String, Object> params, GroupTourEntity groupTour,
                           HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
        MPUtil.likeOrEq(ew, groupTour);
        ew.eq("guide_no", scope);
        PageUtils page = groupTourService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/group_tour/info/{id}")
    public R groupTourInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        GroupTourEntity row = groupTourService.getById(id);
        if (row == null) {
            return R.error("报名不存在");
        }
        if (!scope.equals(row.getGuideNo())) {
            return R.error("无权查看其它导游的报名");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/group_tour/save")
    public R groupTourSave(@RequestBody GroupTourEntity groupTour, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        groupTour.setGuideNo(scope);
        groupTourService.save(groupTour);
        return R.ok();
    }

    @RequestMapping("/group_tour/update")
    public R groupTourUpdate(@RequestBody GroupTourEntity groupTour, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 管理端的「标记已支付」是只带 id + 一两个字段的局部更新，
        // 所以要拿库里的行判归属，而不是请求体（它没有归属列）。
        GroupTourEntity exist = groupTourService.getById(groupTour.getId());
        if (exist == null) {
            return R.error("报名不存在");
        }
        if (!scope.equals(exist.getGuideNo())) {
            return R.error("无权修改其它导游的报名");
        }
        groupTour.setGuideNo(exist.getGuideNo());
        groupTourService.updateById(groupTour);
        return R.ok();
    }

    @RequestMapping("/group_tour/delete")
    public R groupTourDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            GroupTourEntity row = groupTourService.getById(id);
            if (row == null) {
                return R.error("报名不存在");
            }
            if (!scope.equals(row.getGuideNo())) {
                return R.error("无权删除其它导游的报名");
            }
        }
        groupTourService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 旅游攻略（全量，无归属列） ====================

    @RequestMapping("/travel_guide/page")
    public R travelGuidePage(@RequestParam Map<String, Object> params, TravelGuideEntity travelGuide) {
        QueryWrapper<TravelGuideEntity> ew = new QueryWrapper<TravelGuideEntity>();
        PageUtils page = travelGuideService.queryPage(params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelGuide), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/travel_guide/info/{id}")
    public R travelGuideInfo(@PathVariable("id") Long id) {
        return R.ok().put("data", travelGuideService.getById(id));
    }

    @RequestMapping("/travel_guide/save")
    public R travelGuideSave(@RequestBody TravelGuideEntity travelGuide) {
        travelGuideService.save(travelGuide);
        return R.ok();
    }

    @RequestMapping("/travel_guide/update")
    public R travelGuideUpdate(@RequestBody TravelGuideEntity travelGuide) {
        travelGuideService.updateById(travelGuide);
        return R.ok();
    }

    @RequestMapping("/travel_guide/delete")
    public R travelGuideDelete(@RequestBody Long[] ids) {
        travelGuideService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 作用域 ====================

    /**
     * 当前登录导游的工号；未绑定返回 null（由调用方给出提示），
     * 连登录态都没有则抛异常（正常已被 AuthorizationInterceptor 拦下）。
     */
    private String scopeName(HttpServletRequest request) {
        TourGuideEntity guide = currentGuide(request);
        return StrUtil.isBlank(guide.getGuideNo()) ? null : guide.getGuideNo();
    }

    /**
     * 当前登录导游。未登录抛异常（正常已被 AuthorizationInterceptor 拦下）。
     */
    private TourGuideEntity currentGuide(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute("user_id");
        if (!(userId instanceof Long id)) {
            throw new BusinessException("请先登录");
        }
        TourGuideEntity guide = tourGuideService.getById(id);
        if (guide == null) {
            throw new BusinessException("登录导游不存在，请重新登录");
        }
        return guide;
    }

    /**
     * 线路上的导游信息由登录态带出，不接受前端传值：表单里已经没有这三项，
     * 真让前端传就等于谁都能把联系方式改成别人的。
     */
    private void stampGuide(TravelRouteEntity route, TourGuideEntity guide) {
        route.setGuideName(guide.getGuideName());
        route.setContactPhone(guide.getContactPhone());
        route.setGuideResume(guide.getGuideResume());
    }

    /**
     * 行程天数 = 逐日行程的行数。表单里已经不收 days 了。
     *
     * <p>不推导就会有两个真值来源（手填的天数 vs 实际行程行数），迟早对不上，
     * 而且错得一眼可见：卡片角标写着「5 天」，点开抽屉只有 3 天的安排。
     * 行程为空时给 0 —— 游客页 `r.days ? ... : '天数待定'` 对 0 和 null 一样处理。
     */
    private int dayCount(List<TravelRouteDayEntity> daily) {
        return daily == null ? 0 : daily.size();
    }
}
