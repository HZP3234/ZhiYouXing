package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.TravelRouteCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteCommentView;
import com.zhiyouxing.travel.service.TravelRouteCommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 旅游线路评论表
 */
@RestController
@RequestMapping("/travel_route_comment")
public class TravelRouteCommentController {
    @Autowired
    private TravelRouteCommentService travelRouteCommentService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TravelRouteCommentEntity travelRouteComment,
		HttpServletRequest request){
        QueryWrapper<TravelRouteCommentEntity> ew = new QueryWrapper<TravelRouteCommentEntity>();

		PageUtils page = travelRouteCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelRouteComment), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TravelRouteCommentEntity travelRouteComment, 
		HttpServletRequest request){
        QueryWrapper<TravelRouteCommentEntity> ew = new QueryWrapper<TravelRouteCommentEntity>();

		PageUtils page = travelRouteCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelRouteComment), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TravelRouteCommentEntity travelRouteComment){
       	QueryWrapper<TravelRouteCommentEntity> ew = new QueryWrapper<TravelRouteCommentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( travelRouteComment, "travel_route_comment")); 
        return R.ok().put("data", travelRouteCommentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TravelRouteCommentEntity travelRouteComment){
        QueryWrapper< TravelRouteCommentEntity> ew = new QueryWrapper< TravelRouteCommentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( travelRouteComment, "travel_route_comment")); 
		TravelRouteCommentView travelRouteCommentView =  travelRouteCommentService.selectView(ew);
		return R.ok("查询旅游线路评论表成功").put("data", travelRouteCommentView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TravelRouteCommentEntity travelRouteComment = travelRouteCommentService.getById(id);
        return R.ok().put("data", travelRouteComment);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TravelRouteCommentEntity travelRouteComment = travelRouteCommentService.getById(id);
        return R.ok().put("data", travelRouteComment);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TravelRouteCommentEntity travelRouteComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(travelRouteComment);
        travelRouteCommentService.save(travelRouteComment);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TravelRouteCommentEntity travelRouteComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(travelRouteComment);
        travelRouteCommentService.save(travelRouteComment);
        return R.ok();
    }

     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        TravelRouteCommentEntity travelRouteComment = travelRouteCommentService.getOne(new QueryWrapper<TravelRouteCommentEntity>().eq("", username));
        return R.ok().put("data", travelRouteComment);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TravelRouteCommentEntity travelRouteComment, HttpServletRequest request){
        //ValidatorUtils.validateEntity(travelRouteComment);
        travelRouteCommentService.updateById(travelRouteComment);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        travelRouteCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,TravelRouteCommentEntity travelRouteComment, HttpServletRequest request,String pre){
        QueryWrapper<TravelRouteCommentEntity> ew = new QueryWrapper<TravelRouteCommentEntity>();
        Map<String, Object> newMap = new HashMap<String, Object>();
        Map<String, Object> param = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			String newKey = entry.getKey();
			if (pre.endsWith(".")) {
				newMap.put(pre + newKey, entry.getValue());
			} else if (StringUtils.isEmpty(pre)) {
				newMap.put(newKey, entry.getValue());
			} else {
				newMap.put(pre + "." + newKey, entry.getValue());
			}
		}
		params.put("sort", "click_time");
        params.put("order", "desc");
		PageUtils page = travelRouteCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelRouteComment), params), params));
        return R.ok().put("data", page);
    }

}
