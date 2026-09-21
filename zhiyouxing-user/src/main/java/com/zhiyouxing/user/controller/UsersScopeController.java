package com.zhiyouxing.user.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.dao.AttractionAuditDao;
import com.zhiyouxing.user.dao.HotelInfoAuditDao;
import com.zhiyouxing.user.dao.QualificationDao;
import com.zhiyouxing.user.dao.RestaurantAuditDao;
import com.zhiyouxing.user.dao.StaffBindingDao;
import com.zhiyouxing.user.dao.TravelRouteAuditDao;
import com.zhiyouxing.user.dao.UserIdentityDao;
import com.zhiyouxing.user.entity.AttractionAuditEntity;
import com.zhiyouxing.user.entity.HotelInfoAuditEntity;
import com.zhiyouxing.user.entity.QualificationEntity;
import com.zhiyouxing.user.entity.RestaurantAuditEntity;
import com.zhiyouxing.user.entity.TravelRouteAuditEntity;
import com.zhiyouxing.user.entity.UserIdentityEntity;
import com.zhiyouxing.user.entity.vo.IdentityAuditVO;
import com.zhiyouxing.user.entity.vo.UserIdentityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员的审核台（实名认证 + 管理端资质）。
 *
 * <p>前端管理页按 /&lt;表名&gt;/&lt;资源&gt;/&lt;动作&gt; 拼地址（见 frontend 的
 * pages/admin/AdminCrud.vue 与 common/admin.js）。管理员这一角色的「表名」是 users，
 * 所以这里的端点全部挂在 /users 下，与其它四个角色的 *_staff ScopeController 同构。
 *
 * <p>两个资源都只有「查」和「改审核结论」，没有真正意义上的新增：
 * 实名认证由游客自己提交（IdentityController），资质由四个管理端角色自己提交
 * （QualificationController）。管理端只改 audit_status / audit_reply / audit_time 三列 ——
 * 请求体里其它字段一律忽略，管理员改不动提交人的姓名与证件号。
 * 唯一的例外是资质「已通过」时会把 org_name 回写到该角色的归属列（见 bindStaff）。
 *
 * <p>权限：AuthorizationInterceptor 把 /users/** 除登录注册那几条外全部限定为
 * 「管理员」角色，所以这里不用再自己判一次角色。
 *
 * <p>另外三套审核（travel_route_audit / hotel_info_audit / attraction_audit /
 * restaurant_audit）审的是别的服务写的表：travel_route 归 travel 服务，
 * hotel_info 归 hotel，attraction 归 attraction，restaurant 归 food。
 * 靠的是五个服务连同一个 zhiyouxing 库，各读一份裁剪过的实体（见对应 Entity 的类注释）。
 */
@RestController
@RequestMapping("/users")
public class UsersScopeController {

    /** 合法的审核结论。只有三个值，写别的直接拒掉，免得库里出现第四种状态 */
    private static final Set<String> AUDIT_STATES =
            Set.of(UserIdentityVO.PENDING, UserIdentityVO.APPROVED, UserIdentityVO.REJECTED);

    @Autowired
    private UserIdentityDao userIdentityDao;

    @Autowired
    private QualificationDao qualificationDao;

    @Autowired
    private StaffBindingDao staffBindingDao;

    @Autowired
    private TravelRouteAuditDao travelRouteAuditDao;

    @Autowired
    private HotelInfoAuditDao hotelInfoAuditDao;

    @Autowired
    private AttractionAuditDao attractionAuditDao;

    @Autowired
    private RestaurantAuditDao restaurantAuditDao;

    // ==================== 实名认证 ====================

    @RequestMapping("/user_identity/page")
    public R identityPage(@RequestParam Map<String, Object> params, UserIdentityEntity query) {
        QueryWrapper<UserIdentityEntity> ew = new QueryWrapper<UserIdentityEntity>();
        Page<UserIdentityEntity> page = new Query<UserIdentityEntity>(params).getPage();
        userIdentityDao.selectPage(page,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, query), params), params));
        /*
         * 逐行换成脱敏 VO 再出去。整条实体吐出去等于把完整证件号发到浏览器，
         * 与 /user/identity/detail 的口径不一致（见 IdentityAuditVO 的类注释）。
         */
        List<IdentityAuditVO> rows = page.getRecords().stream().map(IdentityAuditVO::of).toList();
        return R.ok().put("data", new PageUtils(rows, (int) page.getTotal(),
                (int) page.getSize(), (int) page.getCurrent()));
    }

    @RequestMapping("/user_identity/info/{id}")
    public R identityInfo(@PathVariable("id") Long id) {
        UserIdentityEntity row = userIdentityDao.selectById(id);
        if (row == null) {
            return R.error("认证记录不存在");
        }
        return R.ok().put("data", IdentityAuditVO.of(row));
    }

    /**
     * 改审核结论。「通过」与「驳回」两个按钮都打这里，前端只发 id + 结论 + 回复。
     *
     * verify_time 跟着结论走：通过时补上（已经有就保留，别把首次通过时间刷成现在），
     * 驳回时清掉 —— 一条被驳回的记录不该带着「认证通过时间」。
     */
    @RequestMapping("/user_identity/update")
    public R identityUpdate(@RequestBody UserIdentityEntity form) {
        if (form.getId() == null) {
            return R.error("缺少记录 id");
        }
        String status = form.getAuditStatus();
        if (!AUDIT_STATES.contains(status)) {
            return R.error("审核结论只能是 待审核 / 已通过 / 已驳回");
        }
        UserIdentityEntity exist = userIdentityDao.selectById(form.getId());
        if (exist == null) {
            return R.error("认证记录不存在");
        }

        UpdateWrapper<UserIdentityEntity> uw = new UpdateWrapper<UserIdentityEntity>()
                .eq("id", exist.getId())
                .set("audit_status", status)
                .set("audit_reply", form.getAuditReply())
                .set("audit_time", new Date());
        if (UserIdentityVO.APPROVED.equals(status)) {
            if (exist.getVerifyTime() == null) {
                uw.set("verify_time", new Date());
            }
        } else {
            uw.set("verify_time", null);
        }
        userIdentityDao.update(null, uw);
        return R.ok();
    }

    @RequestMapping("/user_identity/delete")
    public R identityDelete(@RequestBody Long[] ids) {
        userIdentityDao.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 管理端资质 ====================

    /**
     * 资质列表。四类角色共用这一张表，靠 table_name 分流 ——
     * 管理端菜单里四类资质是四个页面，前端各自固定传 tableName
     * （见 frontend/src/config/adminResources.js 的 fixedQuery）。
     */
    @RequestMapping("/qualification/page")
    public R qualificationPage(@RequestParam Map<String, Object> params, QualificationEntity query) {
        QueryWrapper<QualificationEntity> ew = new QueryWrapper<QualificationEntity>();
        Page<QualificationEntity> page = new Query<QualificationEntity>(params).getPage();
        qualificationDao.selectPage(page,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, query), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    @RequestMapping("/qualification/info/{id}")
    public R qualificationInfo(@PathVariable("id") Long id) {
        QualificationEntity row = qualificationDao.selectById(id);
        if (row == null) {
            return R.error("资质记录不存在");
        }
        return R.ok().put("data", row);
    }

    /** 改审核结论。与实名认证同一套规则，只是没有 verify_time 要跟着动 */
    @RequestMapping("/qualification/update")
    public R qualificationUpdate(@RequestBody QualificationEntity form) {
        if (form.getId() == null) {
            return R.error("缺少记录 id");
        }
        String status = form.getAuditStatus();
        if (!AUDIT_STATES.contains(status)) {
            return R.error("审核结论只能是 待审核 / 已通过 / 已驳回");
        }
        QualificationEntity exist = qualificationDao.selectById(form.getId());
        if (exist == null) {
            return R.error("资质记录不存在");
        }
        if (UserIdentityVO.REJECTED.equals(status) && StrUtil.isBlank(form.getAuditReply())) {
            return R.error("驳回要写清原因，提交人会看到这条回复");
        }

        qualificationDao.update(null, new UpdateWrapper<QualificationEntity>()
                .eq("id", form.getId())
                .set("audit_status", status)
                .set("audit_reply", form.getAuditReply())
                .set("audit_time", new Date()));

        // 通过才回写归属列；驳回 / 退回待审核都不动（前台尚未「属于」那家店）
        if (UserIdentityVO.APPROVED.equals(status)) {
            bindStaff(exist);
        }
        return R.ok();
    }

    /**
     * 把资质里的「所属单位」回写成该角色在自己表里的归属列（见 {@link StaffBindingDao}）。
     *
     * <p>没有这一步，新注册的景点 / 酒店 / 餐厅前台即使资质审核通过，归属列仍是 NULL，
     * 一创建内容就被 /&lt;表名&gt;/&lt;资源&gt;/save 的 scopeName() 判成「未绑定」而拒绝。
     * 种子数据里那几个演示账号能用，只是因为它们的归属列在种子里就预填了，
     * 所以这个坑只在「自己注册 + 提交资质」的路径上暴露。
     *
     * <p>导游不在此列：线路的作用域取 token 里的 guide_no，没有归属列要写。
     */
    private void bindStaff(QualificationEntity qual) {
        String org = StrUtil.trim(qual.getOrgName());
        if (StrUtil.isBlank(org) || qual.getTableName() == null) {
            return;
        }
        String account = qual.getUserAccount();
        switch (qual.getTableName()) {
            case "attraction_staff" -> staffBindingDao.bindAttraction(account, org);
            case "hotel_staff" -> staffBindingDao.bindHotel(account, org);
            case "restaurant_staff" -> staffBindingDao.bindRestaurant(account, org);
            default -> { }
        }
    }

    @RequestMapping("/qualification/delete")
    public R qualificationDelete(@RequestBody Long[] ids) {
        qualificationDao.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 线路审核 ====================

    /**
     * 线路审核列表。线路本身由导游在自己的管理端建（/tour_guide/travel_route/save），
     * 提交时服务端已经把 audit_status 盖成「待审核」，所以这里跟资质审核台一样只有查和改。
     *
     * <p>不过滤状态：管理员要能翻到已通过 / 已驳回的历史线路，前端列表上有状态列和搜索框。
     */
    @RequestMapping("/travel_route_audit/page")
    public R travelRouteAuditPage(@RequestParam Map<String, Object> params,
                                  TravelRouteAuditEntity query) {
        QueryWrapper<TravelRouteAuditEntity> ew = new QueryWrapper<TravelRouteAuditEntity>();
        Page<TravelRouteAuditEntity> page = new Query<TravelRouteAuditEntity>(params).getPage();
        travelRouteAuditDao.selectPage(page,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, query), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    @RequestMapping("/travel_route_audit/info/{id}")
    public R travelRouteAuditInfo(@PathVariable("id") Long id) {
        TravelRouteAuditEntity row = travelRouteAuditDao.selectById(id);
        if (row == null) {
            return R.error("线路不存在");
        }
        return R.ok().put("data", row);
    }

    /**
     * 改审核结论。通过后线路才会出现在游客「旅游线路」页（/travel_route/search 只放行已通过），
     * 驳回则只是回到导游的管理端、附上原因。
     *
     * <p>只写三列，请求体里的线路字段一律不落地 —— 管理员审的是导游填的内容，
     * 不该顺手把线路文案也改了（真的要改，让导游改，改完会重新进审核）。
     */
    @RequestMapping("/travel_route_audit/update")
    public R travelRouteAuditUpdate(@RequestBody TravelRouteAuditEntity form) {
        if (form.getId() == null) {
            return R.error("缺少记录 id");
        }
        String status = form.getAuditStatus();
        if (!AUDIT_STATES.contains(status)) {
            return R.error("审核结论只能是 待审核 / 已通过 / 已驳回");
        }
        if (travelRouteAuditDao.selectById(form.getId()) == null) {
            return R.error("线路不存在");
        }
        if (UserIdentityVO.REJECTED.equals(status) && StrUtil.isBlank(form.getAuditReply())) {
            return R.error("驳回要写清原因，导游会在自己的线路列表里看到这条回复");
        }

        travelRouteAuditDao.update(null, new UpdateWrapper<TravelRouteAuditEntity>()
                .eq("id", form.getId())
                .set("audit_status", status)
                .set("audit_reply", form.getAuditReply())
                .set("audit_time", new Date()));
        return R.ok();
    }

    @RequestMapping("/travel_route_audit/delete")
    public R travelRouteAuditDelete(@RequestBody Long[] ids) {
        travelRouteAuditDao.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 酒店信息审核 ====================

    /**
     * 酒店审核列表。酒店由酒店前台在自己的管理端建（/hotel_staff/hotel_info/save），
     * 提交时服务端已经把 audit_status 盖成「待审核」，所以这里跟线路审核台一样只有查和改。
     *
     * <p>不过滤状态：管理员要能翻到已通过 / 已驳回的历史酒店，前端列表上有状态列和搜索框。
     */
    @RequestMapping("/hotel_info_audit/page")
    public R hotelInfoAuditPage(@RequestParam Map<String, Object> params,
                                HotelInfoAuditEntity query) {
        QueryWrapper<HotelInfoAuditEntity> ew = new QueryWrapper<HotelInfoAuditEntity>();
        Page<HotelInfoAuditEntity> page = new Query<HotelInfoAuditEntity>(params).getPage();
        hotelInfoAuditDao.selectPage(page,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, query), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    @RequestMapping("/hotel_info_audit/info/{id}")
    public R hotelInfoAuditInfo(@PathVariable("id") Long id) {
        HotelInfoAuditEntity row = hotelInfoAuditDao.selectById(id);
        if (row == null) {
            return R.error("酒店不存在");
        }
        return R.ok().put("data", row);
    }

    /**
     * 改审核结论。通过后酒店才算营业：游客端 /hotel_info/list、/hotel_info/detail/{id}
     * 以及 /room_type/list、/room_type/detail/{id} 都只放行已通过的酒店。驳回则回到
     * 酒店前台的酒店信息页，附上原因。
     *
     * <p>只写三列，请求体里的酒店字段一律不落地 —— 管理员审的是酒店前台填的内容。
     */
    @RequestMapping("/hotel_info_audit/update")
    public R hotelInfoAuditUpdate(@RequestBody HotelInfoAuditEntity form) {
        if (form.getId() == null) {
            return R.error("缺少记录 id");
        }
        String status = form.getAuditStatus();
        if (!AUDIT_STATES.contains(status)) {
            return R.error("审核结论只能是 待审核 / 已通过 / 已驳回");
        }
        if (hotelInfoAuditDao.selectById(form.getId()) == null) {
            return R.error("酒店不存在");
        }
        if (UserIdentityVO.REJECTED.equals(status) && StrUtil.isBlank(form.getAuditReply())) {
            return R.error("驳回要写清原因，酒店前台会在自己的酒店信息页看到这条回复");
        }

        hotelInfoAuditDao.update(null, new UpdateWrapper<HotelInfoAuditEntity>()
                .eq("id", form.getId())
                .set("audit_status", status)
                .set("audit_reply", form.getAuditReply())
                .set("audit_time", new Date()));
        return R.ok();
    }

    @RequestMapping("/hotel_info_audit/delete")
    public R hotelInfoAuditDelete(@RequestBody Long[] ids) {
        hotelInfoAuditDao.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 景点信息审核 ====================

    /**
     * 景点审核列表。景点由景点前台在自己的管理端建（/attraction_staff/attraction/save），
     * 提交时服务端已经把 audit_status 盖成「待审核」，所以这里跟酒店审核台一样只有查和改。
     *
     * <p>不过滤状态：管理员要能翻到已通过 / 已驳回的历史景点，前端列表上有状态列和搜索框。
     */
    @RequestMapping("/attraction_audit/page")
    public R attractionAuditPage(@RequestParam Map<String, Object> params,
                                 AttractionAuditEntity query) {
        QueryWrapper<AttractionAuditEntity> ew = new QueryWrapper<AttractionAuditEntity>();
        Page<AttractionAuditEntity> page = new Query<AttractionAuditEntity>(params).getPage();
        attractionAuditDao.selectPage(page,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, query), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    @RequestMapping("/attraction_audit/info/{id}")
    public R attractionAuditInfo(@PathVariable("id") Long id) {
        AttractionAuditEntity row = attractionAuditDao.selectById(id);
        if (row == null) {
            return R.error("景点不存在");
        }
        return R.ok().put("data", row);
    }

    /**
     * 改审核结论。通过后游客端 /attraction/list 与 /attraction/detail/{id} 才放行这个景点，
     * 驳回则回到景点前台的景点信息页，附上原因。
     *
     * <p>只写三列，请求体里的景点字段一律不落地 —— 管理员审的是景点前台填的内容。
     */
    @RequestMapping("/attraction_audit/update")
    public R attractionAuditUpdate(@RequestBody AttractionAuditEntity form) {
        if (form.getId() == null) {
            return R.error("缺少记录 id");
        }
        String status = form.getAuditStatus();
        if (!AUDIT_STATES.contains(status)) {
            return R.error("审核结论只能是 待审核 / 已通过 / 已驳回");
        }
        if (attractionAuditDao.selectById(form.getId()) == null) {
            return R.error("景点不存在");
        }
        if (UserIdentityVO.REJECTED.equals(status) && StrUtil.isBlank(form.getAuditReply())) {
            return R.error("驳回要写清原因，景点前台会在自己的景点信息页看到这条回复");
        }

        attractionAuditDao.update(null, new UpdateWrapper<AttractionAuditEntity>()
                .eq("id", form.getId())
                .set("audit_status", status)
                .set("audit_reply", form.getAuditReply())
                .set("audit_time", new Date()));
        return R.ok();
    }

    @RequestMapping("/attraction_audit/delete")
    public R attractionAuditDelete(@RequestBody Long[] ids) {
        attractionAuditDao.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 餐厅信息审核 ====================

    /**
     * 餐厅审核列表。餐厅由餐厅前台在自己的管理端建（/restaurant_staff/restaurant/save），
     * 提交时服务端已经把 audit_status 盖成「待审核」。
     *
     * <p><b>别与 restaurant_reservation 搞混</b>：那套 audit_status / audit_reply 是
     * 餐厅前台审「订座」用的（前端资源 restaurant_reservation，也挂在 /restaurant_staff 下）。
     * 这里审的是餐厅<b>主表的内容</b>，审核人是管理员，通过后游客端 /restaurant/list 与
     * /restaurant/detail/{id} 才放行这家店。
     */
    @RequestMapping("/restaurant_audit/page")
    public R restaurantAuditPage(@RequestParam Map<String, Object> params,
                                 RestaurantAuditEntity query) {
        QueryWrapper<RestaurantAuditEntity> ew = new QueryWrapper<RestaurantAuditEntity>();
        Page<RestaurantAuditEntity> page = new Query<RestaurantAuditEntity>(params).getPage();
        restaurantAuditDao.selectPage(page,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, query), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    @RequestMapping("/restaurant_audit/info/{id}")
    public R restaurantAuditInfo(@PathVariable("id") Long id) {
        RestaurantAuditEntity row = restaurantAuditDao.selectById(id);
        if (row == null) {
            return R.error("餐厅不存在");
        }
        return R.ok().put("data", row);
    }

    /** 改审核结论。只写三列，请求体里的餐厅字段一律不落地 */
    @RequestMapping("/restaurant_audit/update")
    public R restaurantAuditUpdate(@RequestBody RestaurantAuditEntity form) {
        if (form.getId() == null) {
            return R.error("缺少记录 id");
        }
        String status = form.getAuditStatus();
        if (!AUDIT_STATES.contains(status)) {
            return R.error("审核结论只能是 待审核 / 已通过 / 已驳回");
        }
        if (restaurantAuditDao.selectById(form.getId()) == null) {
            return R.error("餐厅不存在");
        }
        if (UserIdentityVO.REJECTED.equals(status) && StrUtil.isBlank(form.getAuditReply())) {
            return R.error("驳回要写清原因，餐厅前台会在自己的餐厅信息页看到这条回复");
        }

        restaurantAuditDao.update(null, new UpdateWrapper<RestaurantAuditEntity>()
                .eq("id", form.getId())
                .set("audit_status", status)
                .set("audit_reply", form.getAuditReply())
                .set("audit_time", new Date()));
        return R.ok();
    }

    @RequestMapping("/restaurant_audit/delete")
    public R restaurantAuditDelete(@RequestBody Long[] ids) {
        restaurantAuditDao.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }
}
