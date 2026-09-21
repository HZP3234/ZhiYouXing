package com.zhiyouxing.food.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.food.entity.RestaurantCommentEntity;
import com.zhiyouxing.food.entity.view.RestaurantCommentView;
import com.zhiyouxing.food.service.RestaurantCommentService;
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
 * 美食餐厅评论表
 * 后端接口
 */
@RestController
@RequestMapping("/restaurant_comment")
public class RestaurantCommentController {
    @Autowired
    private RestaurantCommentService restaurantCommentService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,RestaurantCommentEntity restaurantComment,
		HttpServletRequest request){
        QueryWrapper<RestaurantCommentEntity> ew = new QueryWrapper<RestaurantCommentEntity>();

		PageUtils page = restaurantCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurantComment), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,RestaurantCommentEntity restaurantComment,
		HttpServletRequest request){
        QueryWrapper<RestaurantCommentEntity> ew = new QueryWrapper<RestaurantCommentEntity>();

		PageUtils page = restaurantCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurantComment), params), params));
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( RestaurantCommentEntity restaurantComment){
       	QueryWrapper<RestaurantCommentEntity> ew = new QueryWrapper<RestaurantCommentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( restaurantComment, "restaurant_comment"));
        return R.ok().put("data", restaurantCommentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(RestaurantCommentEntity restaurantComment){
        QueryWrapper< RestaurantCommentEntity> ew = new QueryWrapper< RestaurantCommentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( restaurantComment, "restaurant_comment"));
		RestaurantCommentView restaurantCommentView =  restaurantCommentService.selectView(ew);
		return R.ok("查询美食餐厅评论表成功").put("data", restaurantCommentView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        RestaurantCommentEntity restaurantComment = restaurantCommentService.getById(id);
        return R.ok().put("data", restaurantComment);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        RestaurantCommentEntity restaurantComment = restaurantCommentService.getById(id);
        return R.ok().put("data", restaurantComment);
    }




    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody RestaurantCommentEntity restaurantComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(restaurantComment);
        restaurantCommentService.save(restaurantComment);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody RestaurantCommentEntity restaurantComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(restaurantComment);
        restaurantCommentService.save(restaurantComment);
        return R.ok();
    }



     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        RestaurantCommentEntity restaurantComment = restaurantCommentService.getOne(new QueryWrapper<RestaurantCommentEntity>().eq("nickname", username));
        return R.ok().put("data", restaurantComment);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody RestaurantCommentEntity restaurantComment, HttpServletRequest request){
        //ValidatorUtils.validateEntity(restaurantComment);
        restaurantCommentService.updateById(restaurantComment);//全部更新
        return R.ok();
    }





    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        restaurantCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }


	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,RestaurantCommentEntity restaurantComment, HttpServletRequest request,String pre){
        QueryWrapper<RestaurantCommentEntity> ew = new QueryWrapper<RestaurantCommentEntity>();
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
		params.put("sort", "add_time");
        params.put("order", "desc");
		PageUtils page = restaurantCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurantComment), params), params));
        return R.ok().put("data", page);
    }

















}
