package com.zhiyouxing.attraction.controller;

import com.zhiyouxing.attraction.entity.AttractionCommentEntity;
import com.zhiyouxing.attraction.entity.view.AttractionCommentView;
import com.zhiyouxing.attraction.service.AttractionCommentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
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
 * 热门景点评论表
 * 后端接口
 */
@RestController
@RequestMapping("/attraction_comment")
public class AttractionCommentController {
    @Autowired
    private AttractionCommentService attractionCommentService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,AttractionCommentEntity attractionComment,
		HttpServletRequest request){
        QueryWrapper<AttractionCommentEntity> ew = new QueryWrapper<AttractionCommentEntity>();

		PageUtils page = attractionCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, attractionComment), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,AttractionCommentEntity attractionComment, 
		HttpServletRequest request){
        QueryWrapper<AttractionCommentEntity> ew = new QueryWrapper<AttractionCommentEntity>();

		PageUtils page = attractionCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, attractionComment), params), params));
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( AttractionCommentEntity attractionComment){
       	QueryWrapper<AttractionCommentEntity> ew = new QueryWrapper<AttractionCommentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( attractionComment, "attraction_comment")); 
        return R.ok().put("data", attractionCommentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(AttractionCommentEntity attractionComment){
        QueryWrapper< AttractionCommentEntity> ew = new QueryWrapper< AttractionCommentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( attractionComment, "attraction_comment")); 
		AttractionCommentView attractionCommentView =  attractionCommentService.selectView(ew);
		return R.ok("查询热门景点评论表成功").put("data", attractionCommentView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        AttractionCommentEntity attractionComment = attractionCommentService.getById(id);
        return R.ok().put("data", attractionComment);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        AttractionCommentEntity attractionComment = attractionCommentService.getById(id);
        return R.ok().put("data", attractionComment);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttractionCommentEntity attractionComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(attractionComment);
        attractionCommentService.save(attractionComment);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody AttractionCommentEntity attractionComment, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(attractionComment);
        attractionCommentService.save(attractionComment);
        return R.ok();
    }



     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        AttractionCommentEntity attractionComment = attractionCommentService.getOne(new QueryWrapper<AttractionCommentEntity>().eq("nickname", username));
        return R.ok().put("data", attractionComment);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody AttractionCommentEntity attractionComment, HttpServletRequest request){
        //ValidatorUtils.validateEntity(attractionComment);
        attractionCommentService.updateById(attractionComment);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        attractionCommentService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,AttractionCommentEntity attractionComment, HttpServletRequest request,String pre){
        QueryWrapper<AttractionCommentEntity> ew = new QueryWrapper<AttractionCommentEntity>();
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
		PageUtils page = attractionCommentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, attractionComment), params), params));
        return R.ok().put("data", page);
    }










}
