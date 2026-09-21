package com.zhiyouxing.attraction.controller;

import com.zhiyouxing.attraction.entity.AttractionTypeEntity;
import com.zhiyouxing.attraction.entity.view.AttractionTypeView;
import com.zhiyouxing.attraction.service.AttractionTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * 景点类型
 * 后端接口
 */
@RestController
@RequestMapping("/attraction_type")
public class AttractionTypeController {
    @Autowired
    private AttractionTypeService attractionTypeService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,AttractionTypeEntity attractionType,
		HttpServletRequest request){
        QueryWrapper<AttractionTypeEntity> ew = new QueryWrapper<AttractionTypeEntity>();

		PageUtils page = attractionTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, attractionType), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,AttractionTypeEntity attractionType, 
		HttpServletRequest request){
        QueryWrapper<AttractionTypeEntity> ew = new QueryWrapper<AttractionTypeEntity>();

		PageUtils page = attractionTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, attractionType), params), params));
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( AttractionTypeEntity attractionType){
       	QueryWrapper<AttractionTypeEntity> ew = new QueryWrapper<AttractionTypeEntity>();
      	ew.allEq(MPUtil.allEQMapPre( attractionType, "attraction_type")); 
        return R.ok().put("data", attractionTypeService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(AttractionTypeEntity attractionType){
        QueryWrapper< AttractionTypeEntity> ew = new QueryWrapper< AttractionTypeEntity>();
 		ew.allEq(MPUtil.allEQMapPre( attractionType, "attraction_type")); 
		AttractionTypeView attractionTypeView =  attractionTypeService.selectView(ew);
		return R.ok("查询景点类型成功").put("data", attractionTypeView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        AttractionTypeEntity attractionType = attractionTypeService.getById(id);
        return R.ok().put("data", attractionType);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        AttractionTypeEntity attractionType = attractionTypeService.getById(id);
        return R.ok().put("data", attractionType);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttractionTypeEntity attractionType, HttpServletRequest request){
        if(attractionTypeService.count(new QueryWrapper<AttractionTypeEntity>().eq("attraction_type", attractionType.getAttractionType()))>0) {
            return R.error("景点类型已存在");
        }
    	//ValidatorUtils.validateEntity(attractionType);
        attractionTypeService.save(attractionType);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody AttractionTypeEntity attractionType, HttpServletRequest request){
        if(attractionTypeService.count(new QueryWrapper<AttractionTypeEntity>().eq("attraction_type", attractionType.getAttractionType()))>0) {
            return R.error("景点类型已存在");
        }
    	//ValidatorUtils.validateEntity(attractionType);
        attractionTypeService.save(attractionType);
        return R.ok();
    }





    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody AttractionTypeEntity attractionType, HttpServletRequest request){
        //ValidatorUtils.validateEntity(attractionType);
        if(attractionTypeService.count(new QueryWrapper<AttractionTypeEntity>().ne("id", attractionType.getId()).eq("attraction_type", attractionType.getAttractionType()))>0) {
            return R.error("景点类型已存在");
        }
        attractionTypeService.updateById(attractionType);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        attractionTypeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	










}
