package com.zhiyouxing.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.hotel.entity.HotelCommentEntity;
import com.zhiyouxing.hotel.entity.view.HotelCommentView;
import com.zhiyouxing.hotel.service.HotelCommentService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 酒店信息评论表
 */
@RestController
@RequestMapping("/hotel_comment")
public class HotelCommentController {
    @Autowired
    private HotelCommentService hotelCommentService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,HotelCommentEntity hotelComment,
		HttpServletRequest request){
        QueryWrapper<HotelCommentEntity> ew = new QueryWrapper<HotelCommentEntity>();

		PageUtils page = hotelCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelComment), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,HotelCommentEntity hotelComment, 
		HttpServletRequest request){
        QueryWrapper<HotelCommentEntity> ew = new QueryWrapper<HotelCommentEntity>();

		PageUtils page = hotelCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelComment), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( HotelCommentEntity hotelComment){
       	QueryWrapper<HotelCommentEntity> ew = new QueryWrapper<HotelCommentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( hotelComment, "hotel_comment")); 
        return R.ok().put("data", hotelCommentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(HotelCommentEntity hotelComment){
        QueryWrapper< HotelCommentEntity> ew = new QueryWrapper< HotelCommentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( hotelComment, "hotel_comment")); 
		HotelCommentView hotelCommentView =  hotelCommentService.selectView(ew);
		return R.ok("查询酒店信息评论表成功").put("data", hotelCommentView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        HotelCommentEntity hotelComment = hotelCommentService.getById(id);
        return R.ok().put("data", hotelComment);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        HotelCommentEntity hotelComment = hotelCommentService.getById(id);
        return R.ok().put("data", hotelComment);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody HotelCommentEntity hotelComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(hotelComment);
        hotelCommentService.save(hotelComment);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody HotelCommentEntity hotelComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(hotelComment);
        hotelCommentService.save(hotelComment);
        return R.ok();
    }

     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        HotelCommentEntity hotelComment = hotelCommentService.getOne(new QueryWrapper<HotelCommentEntity>().eq("nickname", username));
        return R.ok().put("data", hotelComment);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody HotelCommentEntity hotelComment, HttpServletRequest request){
        //ValidatorUtils.validateEntity(hotelComment);
        hotelCommentService.updateById(hotelComment);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        hotelCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,HotelCommentEntity hotelComment, HttpServletRequest request,String pre){
        QueryWrapper<HotelCommentEntity> ew = new QueryWrapper<HotelCommentEntity>();
        Map<String, Object> newMap = new HashMap<String, Object>();
        Map<String, Object> param = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			String newKey = entry.getKey();
			if (StrUtil.isEmpty(pre)) {
				newMap.put(newKey, entry.getValue());
			} else if (pre.endsWith(".")) {
				newMap.put(pre + newKey, entry.getValue());
			} else {
				newMap.put(pre + "." + newKey, entry.getValue());
			}
		}
		params.put("sort", "click_time");
        params.put("order", "desc");
		PageUtils page = hotelCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelComment), params), params));
        return R.ok().put("data", page);
    }
}