package com.zhiyouxing.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import com.zhiyouxing.hotel.entity.RoomTypeEntity;
import com.zhiyouxing.hotel.entity.view.RoomTypeView;
import com.zhiyouxing.hotel.service.HotelInfoService;
import com.zhiyouxing.hotel.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * 客房（酒店前台添加，无需审核）
 *
 * <p>「无需审核」说的是客房本身，不是它所属的酒店：酒店信息要管理员过审才能营业
 * （见 HotelInfoController），所以游客端的 /list 与 /detail/{id} 还要再按
 * 「所在酒店已通过」过滤一道 —— 否则一家没过审的酒店，房型照样能被搜到、被下单。
 */
@RestController
@RequestMapping("/room_type")
public class RoomTypeController {

    /** 与 HotelInfoController 同一个口径：只有已通过的酒店才算「营业中」 */
    private static final String AUDIT_APPROVED = "已通过";

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HotelInfoService hotelInfoService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,RoomTypeEntity roomType,
		HttpServletRequest request){
        QueryWrapper<RoomTypeEntity> ew = new QueryWrapper<RoomTypeEntity>();

		PageUtils page = roomTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, roomType), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表（游客端：只放行已营业酒店的客房）
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,RoomTypeEntity roomType,
		HttpServletRequest request){
        QueryWrapper<RoomTypeEntity> ew = new QueryWrapper<RoomTypeEntity>();
        // 子查询而不是「先查酒店名再 in(...)」：省一次往返，也免得酒店名很多时把 SQL 撑爆
        ew.inSql("hotel_name", "select hotel_name from hotel_info where audit_status = '" + AUDIT_APPROVED + "'");

		PageUtils page = roomTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, roomType), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( RoomTypeEntity roomType){
       	QueryWrapper<RoomTypeEntity> ew = new QueryWrapper<RoomTypeEntity>();
      	ew.allEq(MPUtil.allEQMapPre( roomType, "room_type")); 
        return R.ok().put("data", roomTypeService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(RoomTypeEntity roomType){
        QueryWrapper< RoomTypeEntity> ew = new QueryWrapper< RoomTypeEntity>();
 		ew.allEq(MPUtil.allEQMapPre( roomType, "room_type")); 
		RoomTypeView roomTypeView =  roomTypeService.selectView(ew);
		return R.ok("查询客房类型成功").put("data", roomTypeView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        RoomTypeEntity roomType = roomTypeService.getById(id);
        return R.ok().put("data", roomType);
    }

    /**
     * 前端详情（游客端：未过审酒店的客房当作不存在）
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        RoomTypeEntity roomType = roomTypeService.getById(id);
        if (roomType == null || !hotelApproved(roomType.getHotelName())) {
            return R.error("客房不存在或所属酒店尚未通过审核");
        }
        return R.ok().put("data", roomType);
    }

    /** 与 /list 同一道闸门：客房本身不审核，但它所属的酒店要过审才能被游客摸到 */
    private boolean hotelApproved(String hotelName) {
        if (hotelName == null) {
            return false;
        }
        return hotelInfoService.count(new QueryWrapper<HotelInfoEntity>()
                .eq("hotel_name", hotelName)
                .eq("audit_status", AUDIT_APPROVED)) > 0;
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody RoomTypeEntity roomType, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(roomType);
        roomTypeService.save(roomType);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody RoomTypeEntity roomType, HttpServletRequest request){
        roomTypeService.save(roomType);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody RoomTypeEntity roomType, HttpServletRequest request){
        //ValidatorUtils.validateEntity(roomType);
        roomTypeService.updateById(roomType);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        roomTypeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
}
