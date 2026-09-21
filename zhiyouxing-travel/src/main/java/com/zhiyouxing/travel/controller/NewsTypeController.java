package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.NewsTypeEntity;
import com.zhiyouxing.travel.entity.view.NewsTypeView;
import com.zhiyouxing.travel.service.NewsTypeService;
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
 * 旅游资讯分类
 */
@RestController
@RequestMapping("/news_type")
public class NewsTypeController {
    @Autowired
    private NewsTypeService newsTypeService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,NewsTypeEntity newsType,
		HttpServletRequest request){
        QueryWrapper<NewsTypeEntity> ew = new QueryWrapper<NewsTypeEntity>();

		PageUtils page = newsTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, newsType), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,NewsTypeEntity newsType, 
		HttpServletRequest request){
        QueryWrapper<NewsTypeEntity> ew = new QueryWrapper<NewsTypeEntity>();

		PageUtils page = newsTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, newsType), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( NewsTypeEntity newsType){
       	QueryWrapper<NewsTypeEntity> ew = new QueryWrapper<NewsTypeEntity>();
      	ew.allEq(MPUtil.allEQMapPre( newsType, "news_type")); 
        return R.ok().put("data", newsTypeService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(NewsTypeEntity newsType){
        QueryWrapper< NewsTypeEntity> ew = new QueryWrapper< NewsTypeEntity>();
 		ew.allEq(MPUtil.allEQMapPre( newsType, "news_type")); 
		NewsTypeView newsTypeView =  newsTypeService.selectView(ew);
		return R.ok("查询旅游资讯分类成功").put("data", newsTypeView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        NewsTypeEntity newsType = newsTypeService.getById(id);
        return R.ok().put("data", newsType);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        NewsTypeEntity newsType = newsTypeService.getById(id);
        return R.ok().put("data", newsType);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody NewsTypeEntity newsType, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(newsType);
        newsTypeService.save(newsType);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody NewsTypeEntity newsType, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(newsType);
        newsTypeService.save(newsType);
        return R.ok();
    }

     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        NewsTypeEntity newsType = newsTypeService.getOne(new QueryWrapper<NewsTypeEntity>().eq("", username));
        return R.ok().put("data", newsType);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody NewsTypeEntity newsType, HttpServletRequest request){
        //ValidatorUtils.validateEntity(newsType);
        newsTypeService.updateById(newsType);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        newsTypeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,NewsTypeEntity newsType, HttpServletRequest request,String pre){
        QueryWrapper<NewsTypeEntity> ew = new QueryWrapper<NewsTypeEntity>();
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
		PageUtils page = newsTypeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, newsType), params), params));
        return R.ok().put("data", page);
    }
}
