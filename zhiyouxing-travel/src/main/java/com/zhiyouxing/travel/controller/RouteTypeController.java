package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.RouteTypeEntity;
import com.zhiyouxing.travel.entity.view.RouteTypeView;
import com.zhiyouxing.travel.service.RouteTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * 线路类型
 */
@RestController
@RequestMapping("/route_type")
public class RouteTypeController {
    @Autowired
    private RouteTypeService routeTypeService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,RouteTypeEntity routeType,
		HttpServletRequest request){
        QueryWrapper<RouteTypeEntity> ew = new QueryWrapper<RouteTypeEntity>();

		PageUtils page = routeTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, routeType), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,RouteTypeEntity routeType, 
		HttpServletRequest request){
        QueryWrapper<RouteTypeEntity> ew = new QueryWrapper<RouteTypeEntity>();

		PageUtils page = routeTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, routeType), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( RouteTypeEntity routeType){
       	QueryWrapper<RouteTypeEntity> ew = new QueryWrapper<RouteTypeEntity>();
      	ew.allEq(MPUtil.allEQMapPre( routeType, "route_type")); 
        return R.ok().put("data", routeTypeService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(RouteTypeEntity routeType){
        QueryWrapper< RouteTypeEntity> ew = new QueryWrapper< RouteTypeEntity>();
 		ew.allEq(MPUtil.allEQMapPre( routeType, "route_type")); 
		RouteTypeView routeTypeView =  routeTypeService.selectView(ew);
		return R.ok("查询线路类型成功").put("data", routeTypeView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        RouteTypeEntity routeType = routeTypeService.getById(id);
        return R.ok().put("data", routeType);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        RouteTypeEntity routeType = routeTypeService.getById(id);
        return R.ok().put("data", routeType);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody RouteTypeEntity routeType, HttpServletRequest request){
        if(routeTypeService.count(new QueryWrapper<RouteTypeEntity>().eq("route_type", routeType.getRouteType()))>0) {
            return R.error("线路类型已存在");
        }
    	//ValidatorUtils.validateEntity(routeType);
        routeTypeService.save(routeType);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody RouteTypeEntity routeType, HttpServletRequest request){
        if(routeTypeService.count(new QueryWrapper<RouteTypeEntity>().eq("route_type", routeType.getRouteType()))>0) {
            return R.error("线路类型已存在");
        }
    	//ValidatorUtils.validateEntity(routeType);
        routeTypeService.save(routeType);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody RouteTypeEntity routeType, HttpServletRequest request){
        //ValidatorUtils.validateEntity(routeType);
        if(routeTypeService.count(new QueryWrapper<RouteTypeEntity>().ne("id", routeType.getId()).eq("route_type", routeType.getRouteType()))>0) {
            return R.error("线路类型已存在");
        }
        routeTypeService.updateById(routeType);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        routeTypeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
}
