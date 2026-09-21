package com.zhiyouxing.food.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.exception.BusinessException;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.food.entity.RestaurantCommentEntity;
import com.zhiyouxing.food.entity.RestaurantEntity;
import com.zhiyouxing.food.entity.RestaurantReservationEntity;
import com.zhiyouxing.food.entity.RestaurantStaffEntity;
import com.zhiyouxing.food.service.RestaurantCommentService;
import com.zhiyouxing.food.service.RestaurantReservationService;
import com.zhiyouxing.food.service.RestaurantService;
import com.zhiyouxing.food.service.RestaurantStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 餐厅前台管理端（数据限定在「自己那家餐厅」）
 *
 * <p>前端管理页按 /&lt;表名&gt;/&lt;资源&gt;/&lt;动作&gt; 拼地址（见 frontend 的
 * pages/admin/AdminCrud.vue 与 common/admin.js），所以 /restaurant_staff 这一层要提供
 * 登录态（RestaurantStaffController）与按资源分发的业务接口。
 *
 * <p>作用域取登录员工的 restaurant_staff.restaurant_name：
 * <ul>
 *   <li>restaurant —— 只看、只改自己那家；改完盖成「待审核」，
 *       等管理员在 /users/restaurant_audit 放行（游客端才看得到）</li>
 *   <li>restaurant_reservation —— 只操作自己那家的订座（按 restaurant_name 归属）</li>
 *   <li>restaurant_comment —— 只操作自己那家餐厅下的评价（ref_id 指向 restaurant.id）</li>
 * </ul>
 *
 * <p>餐厅没有「类型字典」这种全局资源，所以这里只有三个资源。
 * 与 AttractionStaffScopeController 同一套写法，保持一致好对照。
 */
@RestController
@RequestMapping("/restaurant_staff")
public class RestaurantStaffScopeController {

    /** 员工没绑餐厅时的提示：不能退化成「看全表」 */
    private static final String UNBOUND = "当前账号未绑定餐厅，请联系管理员";

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantReservationService restaurantReservationService;

    @Autowired
    private RestaurantCommentService restaurantCommentService;

    @Autowired
    private RestaurantStaffService restaurantStaffService;

    // ==================== 餐厅 ====================

    @RequestMapping("/restaurant/page")
    public R restaurantPage(@RequestParam Map<String, Object> params, RestaurantEntity restaurant,
                            HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<RestaurantEntity> ew = new QueryWrapper<RestaurantEntity>();
        MPUtil.likeOrEq(ew, restaurant);
        ew.eq("restaurant_name", scope);
        PageUtils page = restaurantService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/restaurant/info/{id}")
    public R restaurantInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RestaurantEntity row = restaurantService.getById(id);
        if (row == null) {
            return R.error("餐厅不存在");
        }
        if (!scope.equals(row.getRestaurantName())) {
            return R.error("无权查看其它餐厅的数据");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/restaurant/save")
    public R restaurantSave(@RequestBody RestaurantEntity restaurant, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 归属列不接受前端传值：新建的必然是自己那家，否则这条记录一存下就管不到了
        restaurant.setRestaurantName(scope);
        stampPending(restaurant);
        restaurantService.save(restaurant);
        return R.ok();
    }

    @RequestMapping("/restaurant/update")
    public R restaurantUpdate(@RequestBody RestaurantEntity restaurant, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RestaurantEntity exist = restaurantService.getById(restaurant.getId());
        if (exist == null) {
            return R.error("餐厅不存在");
        }
        if (!scope.equals(exist.getRestaurantName())) {
            return R.error("无权修改其它餐厅的数据");
        }
        // 归属列不可改：把库里的值盖回请求体（改名等于把记录送走）
        restaurant.setRestaurantName(exist.getRestaurantName());
        stampPending(restaurant);
        restaurantService.updateById(restaurant);//全部更新
        /*
         * updateById 跳过 null 字段，上面把 audit_reply/audit_time 置空清不掉库里的旧值
         * （会出现「状态回到待审核、回复栏里还挂着上一轮的意见」）。要显式 set null。
         * 同 HotelStaffScopeController.update 的写法。
         */
        restaurantService.update(new UpdateWrapper<RestaurantEntity>()
                .eq("id", restaurant.getId())
                .set("audit_reply", null)
                .set("audit_time", null));
        return R.ok();
    }

    /**
     * 盖「刚提交」的章：状态钉成待审核、清掉回复与时间。
     *
     * <p>这一层必须自己盖（不能只靠 RestaurantController.stampPending）：权威拦截器
     * AuthorizationInterceptor 只对 /users/** 做角色判断，/restaurant_staff/** 任何登录角色都能打。
     * 否则拿一个餐厅前台 token 直接 POST /restaurant_staff/restaurant/update 带上
     * auditStatus=「已通过」，餐厅就**绕过审核**直接出现在游客端了。
     *
     * <p>这只管餐厅<b>主表</b>；下面 restaurant_reservation 那套审核（餐厅前台审订座）
     * 是另一套字段、另一套语义，不走这里。
     */
    private void stampPending(RestaurantEntity restaurant) {
        restaurant.setAuditStatus("待审核");
        restaurant.setAuditReply(null);
        restaurant.setAuditTime(null);
    }

    @RequestMapping("/restaurant/delete")
    public R restaurantDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            RestaurantEntity row = restaurantService.getById(id);
            if (row == null) {
                return R.error("餐厅不存在");
            }
            if (!scope.equals(row.getRestaurantName())) {
                return R.error("无权删除其它餐厅的数据");
            }
        }
        restaurantService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 餐厅订座 ====================

    @RequestMapping("/restaurant_reservation/page")
    public R restaurantReservationPage(@RequestParam Map<String, Object> params,
                                       RestaurantReservationEntity restaurantReservation,
                                       HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
        MPUtil.likeOrEq(ew, restaurantReservation);
        ew.eq("restaurant_name", scope);
        PageUtils page = restaurantReservationService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/restaurant_reservation/info/{id}")
    public R restaurantReservationInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RestaurantReservationEntity row = restaurantReservationService.getById(id);
        if (row == null) {
            return R.error("订座不存在");
        }
        if (!scope.equals(row.getRestaurantName())) {
            return R.error("无权查看其它餐厅的订座");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/restaurant_reservation/save")
    public R restaurantReservationSave(@RequestBody RestaurantReservationEntity restaurantReservation,
                                       HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        restaurantReservation.setRestaurantName(scope);
        restaurantReservationService.save(restaurantReservation);
        return R.ok();
    }

    @RequestMapping("/restaurant_reservation/update")
    public R restaurantReservationUpdate(@RequestBody RestaurantReservationEntity restaurantReservation,
                                         HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 管理端的「审核通过/驳回」是只带 id + 一两个字段的局部更新，
        // 所以要拿库里的行判归属，而不是请求体（它没有归属列）。
        RestaurantReservationEntity exist = restaurantReservationService.getById(restaurantReservation.getId());
        if (exist == null) {
            return R.error("订座不存在");
        }
        if (!scope.equals(exist.getRestaurantName())) {
            return R.error("无权修改其它餐厅的订座");
        }
        restaurantReservation.setRestaurantName(exist.getRestaurantName());
        restaurantReservationService.updateById(restaurantReservation);
        return R.ok();
    }

    @RequestMapping("/restaurant_reservation/delete")
    public R restaurantReservationDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            RestaurantReservationEntity row = restaurantReservationService.getById(id);
            if (row == null) {
                return R.error("订座不存在");
            }
            if (!scope.equals(row.getRestaurantName())) {
                return R.error("无权删除其它餐厅的订座");
            }
        }
        restaurantReservationService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 餐厅评价 ====================

    @RequestMapping("/restaurant_comment/page")
    public R restaurantCommentPage(@RequestParam Map<String, Object> params, RestaurantCommentEntity restaurantComment,
                                   HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<RestaurantCommentEntity> ew = new QueryWrapper<RestaurantCommentEntity>();
        MPUtil.likeOrEq(ew, restaurantComment);
        List<Long> owned = ownedRestaurantIds(scope);
        if (owned.isEmpty()) {
            // 名下一家餐厅都没有时给空列表，别把全表评价漏出去
            ew.eq("id", -1L);
        } else {
            ew.in("ref_id", owned);
        }
        PageUtils page = restaurantCommentService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/restaurant_comment/info/{id}")
    public R restaurantCommentInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RestaurantCommentEntity row = restaurantCommentService.getById(id);
        if (row == null) {
            return R.error("评价不存在");
        }
        if (!ownedRestaurantIds(scope).contains(row.getRefId())) {
            return R.error("无权查看其它餐厅的评价");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/restaurant_comment/save")
    public R restaurantCommentSave(@RequestBody RestaurantCommentEntity restaurantComment, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        if (restaurantComment.getRefId() == null || !ownedRestaurantIds(scope).contains(restaurantComment.getRefId())) {
            return R.error("只能给自己餐厅下的评价留言");
        }
        restaurantCommentService.save(restaurantComment);
        return R.ok();
    }

    @RequestMapping("/restaurant_comment/update")
    public R restaurantCommentUpdate(@RequestBody RestaurantCommentEntity restaurantComment, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RestaurantCommentEntity exist = restaurantCommentService.getById(restaurantComment.getId());
        if (exist == null) {
            return R.error("评价不存在");
        }
        List<Long> owned = ownedRestaurantIds(scope);
        if (!owned.contains(exist.getRefId())) {
            return R.error("无权修改其它餐厅的评价");
        }
        // ref_id 不可改（改了就跑到别人家去了），只允许改内容/评分/回复
        restaurantComment.setRefId(exist.getRefId());
        restaurantCommentService.updateById(restaurantComment);
        return R.ok();
    }

    @RequestMapping("/restaurant_comment/delete")
    public R restaurantCommentDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        List<Long> owned = ownedRestaurantIds(scope);
        for (Long id : ids) {
            RestaurantCommentEntity row = restaurantCommentService.getById(id);
            if (row == null) {
                return R.error("评价不存在");
            }
            if (!owned.contains(row.getRefId())) {
                return R.error("无权删除其它餐厅的评价");
            }
        }
        restaurantCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 作用域 ====================

    /**
     * 当前登录员工绑定的餐厅名；未绑定返回 null（由调用方给出提示），
     * 连登录态都没有则抛异常（正常已被 AuthorizationInterceptor 拦下）。
     */
    private String scopeName(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute("user_id");
        if (!(userId instanceof Long id)) {
            throw new BusinessException("请先登录");
        }
        RestaurantStaffEntity staff = restaurantStaffService.getById(id);
        if (staff == null) {
            throw new BusinessException("登录员工不存在，请重新登录");
        }
        return StrUtil.isBlank(staff.getRestaurantName()) ? null : staff.getRestaurantName();
    }

    /** 自己那家餐厅的 id 列表，评价表靠 ref_id 反查归属 */
    private List<Long> ownedRestaurantIds(String scope) {
        return restaurantService.list(new QueryWrapper<RestaurantEntity>().eq("restaurant_name", scope))
                .stream().map(RestaurantEntity::getId).toList();
    }
}
