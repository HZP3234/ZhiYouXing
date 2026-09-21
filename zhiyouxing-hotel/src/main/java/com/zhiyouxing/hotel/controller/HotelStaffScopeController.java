package com.zhiyouxing.hotel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.exception.BusinessException;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.hotel.entity.HotelCommentEntity;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import com.zhiyouxing.hotel.entity.HotelReservationEntity;
import com.zhiyouxing.hotel.entity.HotelStaffEntity;
import com.zhiyouxing.hotel.entity.RoomTypeEntity;
import com.zhiyouxing.hotel.service.HotelCommentService;
import com.zhiyouxing.hotel.service.HotelInfoService;
import com.zhiyouxing.hotel.service.HotelReservationService;
import com.zhiyouxing.hotel.service.HotelStaffService;
import com.zhiyouxing.hotel.service.RoomTypeService;
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
 * 酒店前台管理端（数据限定在「自己那家酒店」）
 *
 * <p>前端管理页按 /&lt;表名&gt;/&lt;资源&gt;/&lt;动作&gt; 拼地址（见 frontend 的
 * pages/admin/AdminCrud.vue 与 common/admin.js），所以 /hotel_staff 这一层要提供
 * 登录态（HotelStaffController）与按资源分发的业务接口。
 *
 * <p>作用域取登录员工的 hotel_staff.hotel_name：
 * <ul>
 *   <li>hotel_info —— 只看、只改自己那家；改完盖成「待审核」，等管理员在
 *       /users/hotel_info_audit 放行（游客端才看得到）</li>
 *   <li>room_type —— 只看、只改自己那家的客房（客房本身不审核）</li>
 *   <li>hotel_reservation —— 只操作自己那家的预订；只能由用户发起，这里没有 save</li>
 *   <li>hotel_comment —— 只操作自己那家客房下的评价（ref_id 指向 room_type.id）</li>
 * </ul>
 *
 * <p>与 AttractionStaffScopeController 同一套写法，保持一致好对照。
 */
@RestController
@RequestMapping("/hotel_staff")
public class HotelStaffScopeController {

    /** 员工没绑酒店时的提示：不能退化成「看全表」 */
    private static final String UNBOUND = "当前账号未绑定酒店，请联系管理员";

    @Autowired
    private HotelInfoService hotelInfoService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HotelReservationService hotelReservationService;

    @Autowired
    private HotelCommentService hotelCommentService;

    @Autowired
    private HotelStaffService hotelStaffService;

    // ==================== 酒店信息（自己那家） ====================

    @RequestMapping("/hotel_info/page")
    public R hotelInfoPage(@RequestParam Map<String, Object> params, HotelInfoEntity hotelInfo,
                           HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<HotelInfoEntity> ew = new QueryWrapper<HotelInfoEntity>();
        MPUtil.likeOrEq(ew, hotelInfo);
        ew.eq("hotel_name", scope);
        PageUtils page = hotelInfoService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/hotel_info/info/{id}")
    public R hotelInfoInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        HotelInfoEntity row = hotelInfoService.getById(id);
        if (row == null) {
            return R.error("酒店不存在");
        }
        if (!scope.equals(row.getHotelName())) {
            return R.error("无权查看其它酒店的数据");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/hotel_info/save")
    public R hotelInfoSave(@RequestBody HotelInfoEntity hotelInfo, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 归属列不接受前端传值：新建的必然是自己那家，否则这条记录一存下就管不到了
        hotelInfo.setHotelName(scope);
        stampPending(hotelInfo);
        hotelInfoService.save(hotelInfo);
        return R.ok();
    }

    @RequestMapping("/hotel_info/update")
    public R hotelInfoUpdate(@RequestBody HotelInfoEntity hotelInfo, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        HotelInfoEntity exist = hotelInfoService.getById(hotelInfo.getId());
        if (exist == null) {
            return R.error("酒店不存在");
        }
        if (!scope.equals(exist.getHotelName())) {
            return R.error("无权修改其它酒店的数据");
        }
        // 归属列不可改：把库里的值盖回请求体（改名等于把记录送走）
        hotelInfo.setHotelName(exist.getHotelName());
        stampPending(hotelInfo);
        hotelInfoService.updateById(hotelInfo);//全部更新
        /*
         * updateById 跳过 null 字段，上面把 audit_reply/audit_time 置空清不掉库里的旧值
         * （会出现「状态回到待审核、回复栏里还挂着上一轮的意见」）。要显式 set null。
         * 同 HotelInfoController.update 的写法。
         */
        hotelInfoService.update(new UpdateWrapper<HotelInfoEntity>()
                .eq("id", hotelInfo.getId())
                .set("audit_reply", null)
                .set("audit_time", null));
        return R.ok();
    }

    @RequestMapping("/hotel_info/delete")
    public R hotelInfoDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            HotelInfoEntity row = hotelInfoService.getById(id);
            if (row == null) {
                return R.error("酒店不存在");
            }
            if (!scope.equals(row.getHotelName())) {
                return R.error("无权删除其它酒店的数据");
            }
        }
        hotelInfoService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 盖「刚提交」的章：状态钉成待审核、清掉回复与时间。
     *
     * <p>这一层必须自己盖（不能只靠 HotelInfoController.stampPending）：权威拦截器
     * AuthorizationInterceptor 只对 /users/** 做角色判断，/hotel_staff/** 任何登录角色都能打。
     * 否则拿一个酒店前台 token 直接 POST /hotel_staff/hotel_info/update 带上
     * auditStatus=「已通过」，酒店就**绕过审核**直接出现在游客端了。
     */
    private void stampPending(HotelInfoEntity hotelInfo) {
        hotelInfo.setAuditStatus("待审核");
        hotelInfo.setAuditReply(null);
        hotelInfo.setAuditTime(null);
    }

    // ==================== 客房（限定在自己那家酒店） ====================

    @RequestMapping("/room_type/page")
    public R roomTypePage(@RequestParam Map<String, Object> params, RoomTypeEntity roomType,
                          HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<RoomTypeEntity> ew = new QueryWrapper<RoomTypeEntity>();
        ew.eq("hotel_name", scope);
        PageUtils page = roomTypeService.queryPage(params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, roomType), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/room_type/info/{id}")
    public R roomTypeInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RoomTypeEntity row = roomTypeService.getById(id);
        if (row == null) {
            return R.error("客房不存在");
        }
        if (!scope.equals(row.getHotelName())) {
            return R.error("无权查看其它酒店的客房");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/room_type/save")
    public R roomTypeSave(@RequestBody RoomTypeEntity roomType, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 同 hotel_info/save：归属列由服务端写死成自己那家
        roomType.setHotelName(scope);
        roomTypeService.save(roomType);
        return R.ok();
    }

    @RequestMapping("/room_type/update")
    public R roomTypeUpdate(@RequestBody RoomTypeEntity roomType, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        RoomTypeEntity exist = roomTypeService.getById(roomType.getId());
        if (exist == null) {
            return R.error("客房不存在");
        }
        if (!scope.equals(exist.getHotelName())) {
            return R.error("无权修改其它酒店的客房");
        }
        roomType.setHotelName(exist.getHotelName());
        roomTypeService.updateById(roomType);
        return R.ok();
    }

    @RequestMapping("/room_type/delete")
    public R roomTypeDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            RoomTypeEntity row = roomTypeService.getById(id);
            if (row == null) {
                return R.error("客房不存在");
            }
            if (!scope.equals(row.getHotelName())) {
                return R.error("无权删除其它酒店的客房");
            }
        }
        roomTypeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 客房预订 ====================

    @RequestMapping("/hotel_reservation/page")
    public R hotelReservationPage(@RequestParam Map<String, Object> params, HotelReservationEntity hotelReservation,
                                  HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
        MPUtil.likeOrEq(ew, hotelReservation);
        ew.eq("hotel_name", scope);
        PageUtils page = hotelReservationService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/hotel_reservation/info/{id}")
    public R hotelReservationInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        HotelReservationEntity row = hotelReservationService.getById(id);
        if (row == null) {
            return R.error("预订不存在");
        }
        if (!scope.equals(row.getHotelName())) {
            return R.error("无权查看其它酒店的预订");
        }
        return R.ok().put("data", row);
    }

    /*
     * 没有 /hotel_reservation/save：预订只能由用户发起（酒店预订页的「新增」已去掉）。
     * 前台仍要能 page/info/update/delete —— 主要用来标记已支付、清掉废单。
     */

    @RequestMapping("/hotel_reservation/update")
    public R hotelReservationUpdate(@RequestBody HotelReservationEntity hotelReservation, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        // 管理端的「标记已支付」是只带 id + 一两个字段的局部更新，
        // 所以要拿库里的行判归属，而不是请求体（它没有归属列）。
        HotelReservationEntity exist = hotelReservationService.getById(hotelReservation.getId());
        if (exist == null) {
            return R.error("预订不存在");
        }
        if (!scope.equals(exist.getHotelName())) {
            return R.error("无权修改其它酒店的预订");
        }
        hotelReservation.setHotelName(exist.getHotelName());
        hotelReservationService.updateById(hotelReservation);
        return R.ok();
    }

    @RequestMapping("/hotel_reservation/delete")
    public R hotelReservationDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        for (Long id : ids) {
            HotelReservationEntity row = hotelReservationService.getById(id);
            if (row == null) {
                return R.error("预订不存在");
            }
            if (!scope.equals(row.getHotelName())) {
                return R.error("无权删除其它酒店的预订");
            }
        }
        hotelReservationService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 酒店评价 ====================

    @RequestMapping("/hotel_comment/page")
    public R hotelCommentPage(@RequestParam Map<String, Object> params, HotelCommentEntity hotelComment,
                              HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        QueryWrapper<HotelCommentEntity> ew = new QueryWrapper<HotelCommentEntity>();
        MPUtil.likeOrEq(ew, hotelComment);
        List<Long> owned = ownedRoomIds(scope);
        if (owned.isEmpty()) {
            // 名下一条房型都没有时给空列表，别把全表评价漏出去
            ew.eq("id", -1L);
        } else {
            ew.in("ref_id", owned);
        }
        PageUtils page = hotelCommentService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/hotel_comment/info/{id}")
    public R hotelCommentInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        HotelCommentEntity row = hotelCommentService.getById(id);
        if (row == null) {
            return R.error("评价不存在");
        }
        if (!ownedRoomIds(scope).contains(row.getRefId())) {
            return R.error("无权查看其它酒店的评价");
        }
        return R.ok().put("data", row);
    }

    @RequestMapping("/hotel_comment/save")
    public R hotelCommentSave(@RequestBody HotelCommentEntity hotelComment, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        if (hotelComment.getRefId() == null || !ownedRoomIds(scope).contains(hotelComment.getRefId())) {
            return R.error("只能给自己酒店的房型留言");
        }
        hotelCommentService.save(hotelComment);
        return R.ok();
    }

    @RequestMapping("/hotel_comment/update")
    public R hotelCommentUpdate(@RequestBody HotelCommentEntity hotelComment, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        HotelCommentEntity exist = hotelCommentService.getById(hotelComment.getId());
        if (exist == null) {
            return R.error("评价不存在");
        }
        List<Long> owned = ownedRoomIds(scope);
        if (!owned.contains(exist.getRefId())) {
            return R.error("无权修改其它酒店的评价");
        }
        // ref_id 不可改（改了就跑到别人家去了），只允许改内容/评分/回复
        hotelComment.setRefId(exist.getRefId());
        hotelCommentService.updateById(hotelComment);
        return R.ok();
    }

    @RequestMapping("/hotel_comment/delete")
    public R hotelCommentDelete(@RequestBody Long[] ids, HttpServletRequest request) {
        String scope = scopeName(request);
        if (scope == null) {
            return R.error(UNBOUND);
        }
        List<Long> owned = ownedRoomIds(scope);
        for (Long id : ids) {
            HotelCommentEntity row = hotelCommentService.getById(id);
            if (row == null) {
                return R.error("评价不存在");
            }
            if (!owned.contains(row.getRefId())) {
                return R.error("无权删除其它酒店的评价");
            }
        }
        hotelCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    // ==================== 作用域 ====================

    /**
     * 当前登录员工绑定的酒店名；未绑定返回 null（由调用方给出提示），
     * 连登录态都没有则抛异常（正常已被 AuthorizationInterceptor 拦下）。
     */
    private String scopeName(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute("user_id");
        if (!(userId instanceof Long id)) {
            throw new BusinessException("请先登录");
        }
        HotelStaffEntity staff = hotelStaffService.getById(id);
        if (staff == null) {
            throw new BusinessException("登录员工不存在，请重新登录");
        }
        return StrUtil.isBlank(staff.getHotelName()) ? null : staff.getHotelName();
    }

    /** 自己那家酒店的客房 id 列表，评价表靠 ref_id 反查归属（ref_id 指向 room_type.id） */
    private List<Long> ownedRoomIds(String scope) {
        return roomTypeService.list(new QueryWrapper<RoomTypeEntity>().eq("hotel_name", scope))
                .stream().map(RoomTypeEntity::getId).toList();
    }
}
