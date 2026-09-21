package com.zhiyouxing.attraction.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.attraction.entity.AttractionCommentEntity;
import com.zhiyouxing.attraction.entity.AttractionEntity;
import com.zhiyouxing.attraction.entity.AttractionStaffEntity;
import com.zhiyouxing.attraction.entity.AttractionTypeEntity;
import com.zhiyouxing.attraction.entity.TicketOrderEntity;
import com.zhiyouxing.attraction.service.AttractionCommentService;
import com.zhiyouxing.attraction.service.AttractionService;
import com.zhiyouxing.attraction.service.AttractionStaffService;
import com.zhiyouxing.attraction.service.AttractionTypeService;
import com.zhiyouxing.attraction.service.TicketOrderService;
import com.zhiyouxing.common.exception.BusinessException;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
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
 * 景点前台管理端（数据限定在「自己那家景点」）
 *
 * <p>前端管理页按 /&lt;表名&gt;/&lt;资源&gt;/&lt;动作&gt; 拼地址（见 frontend 的
 * pages/admin/AdminCrud.vue 与 common/admin.js），所以同名的 *_staff 控制器要提供两件事：
 * 登录态（AttractionStaffController）与这一层按资源分发的业务接口。
 *
 * <p>作用域取登录员工的 attraction_staff.attraction_name：
 * <ul>
 *   <li>attraction —— 只看、只改自己那家；改完盖成「待审核」，
 *       等管理员在 /users/attraction_audit 放行（游客端才看得到）</li>
 *   <li>ticket_order —— 只操作自己那家的门票单（按 attraction_name 归属）</li>
 *   <li>attraction_comment —— 只操作自己那家景点下的评价（ref_id 指向 attraction.id）</li>
 *   <li>attraction_type —— 全局字典，各景点共用一份，不限定</li>
 * </ul>
 *
 * <p>越权一律返回 R.error（走 Result 的 {code,message} 前端读不到 msg，文案会丢），
 * 只有「连登录态都没有」才抛 BusinessException。
 */
@RestController
@RequestMapping("/attraction_staff")
public class AttractionStaffScopeController {

    /** 员工没绑景点时的提示：不能退化成「看全表」 */
    private static final String UNBOUND = "当前账号未绑定景点，请联系管理员";

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private AttractionTypeService attractionTypeService;

    @Autowired
    private TicketOrderService ticketOrderService;

    @Autowired
    private AttractionCommentService attractionCommentService;

    @Autowired
    private AttractionStaffService attractionStaffService;

    // ==================== 景点 ====================

    @RequestMapping("/attraction/page")
    public R attractionPage(@RequestParam Map<String, Object> params, AttractionEntity attraction,
                            HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<AttractionEntity> ew = new QueryWrapper<AttractionEntity>();
        MPUtil.likeOrEq(ew, attraction);
        ew.eq("attraction_name", scope);
        PageUtils page = attractionService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/attraction/info/{id}")
    public R attractionInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        AttractionEntity row = attractionService.getById(id);
        if (row == null) {
            return R.error("景点不存在");
        }
        if (!scope.equals(row.getAttractionName())) {
            return R.error("无权查看其它景点的数据");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/attraction/save")
    public R attractionSave(@RequestBody AttractionEntity attraction, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 归属列不接受前端传值：新建的必然是自己那家，否则这条记录一存下就管不到了
        attraction.setAttractionName(scope);
        stampPending(attraction);
        attractionService.save(attraction);
        return R.ok();
    }

    @RequestMapping("/attraction/update")
    public R attractionUpdate(@RequestBody AttractionEntity attraction, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        AttractionEntity exist = attractionService.getById(attraction.getId());
        if (exist == null) {
            return R.error("景点不存在");
        }
        if (!scope.equals(exist.getAttractionName())) {
            return R.error("无权修改其它景点的数据");
        }
        // 归属列不可改：把库里的值盖回请求体（改名等于把记录送走）
        attraction.setAttractionName(exist.getAttractionName());
        stampPending(attraction);
        attractionService.updateById(attraction);//全部更新
        /*
         * updateById 跳过 null 字段，上面把 audit_reply/audit_time 置空清不掉库里的旧值
         * （会出现「状态回到待审核、回复栏里还挂着上一轮的意见」）。要显式 set null。
         * 同 HotelStaffScopeController.update 的写法。
         */
        attractionService.update(new UpdateWrapper<AttractionEntity>()
                .eq("id", attraction.getId())
                .set("audit_reply", null)
                .set("audit_time", null));
        return R.ok();
    }

    /**
     * 盖「刚提交」的章：状态钉成待审核、清掉回复与时间。
     *
     * <p>这一层必须自己盖（不能只靠 AttractionController.stampPending）：权威拦截器
     * AuthorizationInterceptor 只对 /users/** 做角色判断，/attraction_staff/** 任何登录角色都能打。
     * 否则拿一个景点前台 token 直接 POST /attraction_staff/attraction/update 带上
     * auditStatus=「已通过」，景点就**绕过审核**直接出现在游客端了。
     */
    private void stampPending(AttractionEntity attraction) {
        attraction.setAuditStatus("待审核");
        attraction.setAuditReply(null);
        attraction.setAuditTime(null);
    }

    @RequestMapping("/attraction/delete")
    public R attractionDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            AttractionEntity row = attractionService.getById(id);
            if (row == null) {
                return R.error("景点不存在");
            }
            if (!scope.equals(row.getAttractionName())) {
                return R.error("无权删除其它景点的数据");
            }
        }
        attractionService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 景点类型（全局字典，不限作用域） ====================

    @RequestMapping("/attraction_type/page")
    public R attractionTypePage(@RequestParam Map<String, Object> params, AttractionTypeEntity attractionType) {
        QueryWrapper<AttractionTypeEntity> ew = new QueryWrapper<AttractionTypeEntity>();
        PageUtils page = attractionTypeService.queryPage(params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, attractionType), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/attraction_type/info/{id}")
    public R attractionTypeInfo(@PathVariable("id") Long id) {
        return R.ok().put("data", attractionTypeService.getById(id));
    }

    @RequestMapping("/attraction_type/save")
    public R attractionTypeSave(@RequestBody AttractionTypeEntity attractionType) {
        attractionTypeService.save(attractionType);
        return R.ok();
    }

    @RequestMapping("/attraction_type/update")
    public R attractionTypeUpdate(@RequestBody AttractionTypeEntity attractionType) {
        attractionTypeService.updateById(attractionType);
        return R.ok();
    }

    @RequestMapping("/attraction_type/delete")
    public R attractionTypeDelete(@RequestBody Long[] ids) {
        attractionTypeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 门票订单 ====================

    @RequestMapping("/ticket_order/page")
    public R ticketOrderPage(@RequestParam Map<String, Object> params, TicketOrderEntity ticketOrder,
                             HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
        MPUtil.likeOrEq(ew, ticketOrder);
        ew.eq("attraction_name", scope);
        PageUtils page = ticketOrderService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/ticket_order/info/{id}")
    public R ticketOrderInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        TicketOrderEntity row = ticketOrderService.getById(id);
        if (row == null) {
            return R.error("订单不存在");
        }
        if (!scope.equals(row.getAttractionName())) {
            return R.error("无权查看其它景点的订单");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/ticket_order/save")
    public R ticketOrderSave(@RequestBody TicketOrderEntity ticketOrder, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        ticketOrder.setAttractionName(scope);
        ticketOrderService.save(ticketOrder);
        return R.ok();
    }

    @RequestMapping("/ticket_order/update")
    public R ticketOrderUpdate(@RequestBody TicketOrderEntity ticketOrder, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 管理端的「标记已支付」是只带 id + 一两个字段的局部更新，
        // 所以要拿库里的行判归属，而不是请求体（它没有归属列）。
        TicketOrderEntity exist = ticketOrderService.getById(ticketOrder.getId());
        if (exist == null) {
            return R.error("订单不存在");
        }
        if (!scope.equals(exist.getAttractionName())) {
            return R.error("无权修改其它景点的订单");
        }
        ticketOrder.setAttractionName(exist.getAttractionName());
        ticketOrderService.updateById(ticketOrder);
        return R.ok();
    }

    @RequestMapping("/ticket_order/delete")
    public R ticketOrderDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            TicketOrderEntity row = ticketOrderService.getById(id);
            if (row == null) {
                return R.error("订单不存在");
            }
            if (!scope.equals(row.getAttractionName())) {
                return R.error("无权删除其它景点的订单");
            }
        }
        ticketOrderService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 景点评价 ====================

    @RequestMapping("/attraction_comment/page")
    public R attractionCommentPage(@RequestParam Map<String, Object> params, AttractionCommentEntity attractionComment,
                                   HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<AttractionCommentEntity> ew = new QueryWrapper<AttractionCommentEntity>();
        MPUtil.likeOrEq(ew, attractionComment);
        List<Long> owned = ownedAttractionIds(scope);
        if (owned.isEmpty()) {
            // 名下一家景点都没有时给空列表，别把全表评价漏出去
            ew.eq("id", -1L);
        } else {
            ew.in("ref_id", owned);
        }
        PageUtils page = attractionCommentService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/attraction_comment/info/{id}")
    public R attractionCommentInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        AttractionCommentEntity row = attractionCommentService.getById(id);
        if (row == null) {
            return R.error("评价不存在");
        }
        if (!ownedAttractionIds(scope).contains(row.getRefId())) {
            return R.error("无权查看其它景点的评价");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/attraction_comment/save")
    public R attractionCommentSave(@RequestBody AttractionCommentEntity attractionComment, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        if (attractionComment.getRefId() == null || !ownedAttractionIds(scope).contains(attractionComment.getRefId())) {
            return R.error("只能给自己景点下的评价留言");
        }
        attractionCommentService.save(attractionComment);
        return R.ok();
    }

    @RequestMapping("/attraction_comment/update")
    public R attractionCommentUpdate(@RequestBody AttractionCommentEntity attractionComment, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        AttractionCommentEntity exist = attractionCommentService.getById(attractionComment.getId());
        if (exist == null) {
            return R.error("评价不存在");
        }
        List<Long> owned = ownedAttractionIds(scope);
        if (!owned.contains(exist.getRefId())) {
            return R.error("无权修改其它景点的评价");
        }
        // ref_id 不可改（改了就跑到别人的景点去了），只允许改内容/评分/回复
        attractionComment.setRefId(exist.getRefId());
        attractionCommentService.updateById(attractionComment);
        return R.ok();
    }

    @RequestMapping("/attraction_comment/delete")
    public R attractionCommentDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        List<Long> owned = ownedAttractionIds(scope);
        for (Long id : ids) {
            AttractionCommentEntity row = attractionCommentService.getById(id);
            if (row == null) {
                return R.error("评价不存在");
            }
            if (!owned.contains(row.getRefId())) {
                return R.error("无权删除其它景点的评价");
            }
        }
        attractionCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 作用域 ====================

    /**
     * 当前登录员工绑定的景点名；未绑定返回 null（由调用方给出提示），
     * 连登录态都没有则抛异常（正常已被 AuthorizationInterceptor 拦下）。
     */
    private String scopeName(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute("user_id");
        if (!(userId instanceof Long id)) {
            throw new BusinessException("请先登录");
        }
        AttractionStaffEntity staff = attractionStaffService.getById(id);
        if (staff == null) {
            throw new BusinessException("登录员工不存在，请重新登录");
        }
        return StrUtil.isBlank(staff.getAttractionName()) ? null : staff.getAttractionName();
    }

    /** 自己那家景点的 id 列表，评价表靠 ref_id 反查归属 */
    private List<Long> ownedAttractionIds(String scope) {
        return attractionService.list(new QueryWrapper<AttractionEntity>().eq("attraction_name", scope))
                .stream().map(AttractionEntity::getId).toList();
    }
}
