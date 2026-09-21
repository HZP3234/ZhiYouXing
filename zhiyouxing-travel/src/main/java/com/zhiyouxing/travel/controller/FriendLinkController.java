package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.FriendLinkEntity;
import com.zhiyouxing.travel.entity.view.FriendLinkView;
import com.zhiyouxing.travel.service.FriendLinkService;
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
 * 友情链接
 */
@RestController
@RequestMapping("/friend_link")
public class FriendLinkController {
    @Autowired
    private FriendLinkService friendLinkService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,FriendLinkEntity friendLink,
		HttpServletRequest request){
        QueryWrapper<FriendLinkEntity> ew = new QueryWrapper<FriendLinkEntity>();

		PageUtils page = friendLinkService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, friendLink), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,FriendLinkEntity friendLink, 
		HttpServletRequest request){
        QueryWrapper<FriendLinkEntity> ew = new QueryWrapper<FriendLinkEntity>();

		PageUtils page = friendLinkService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, friendLink), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( FriendLinkEntity friendLink){
       	QueryWrapper<FriendLinkEntity> ew = new QueryWrapper<FriendLinkEntity>();
      	ew.allEq(MPUtil.allEQMapPre( friendLink, "friend_link")); 
        return R.ok().put("data", friendLinkService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(FriendLinkEntity friendLink){
        QueryWrapper< FriendLinkEntity> ew = new QueryWrapper< FriendLinkEntity>();
 		ew.allEq(MPUtil.allEQMapPre( friendLink, "friend_link")); 
		FriendLinkView friendLinkView =  friendLinkService.selectView(ew);
		return R.ok("查询友情链接成功").put("data", friendLinkView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        FriendLinkEntity friendLink = friendLinkService.getById(id);
        return R.ok().put("data", friendLink);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        FriendLinkEntity friendLink = friendLinkService.getById(id);
        return R.ok().put("data", friendLink);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody FriendLinkEntity friendLink, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(friendLink);
        friendLinkService.save(friendLink);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody FriendLinkEntity friendLink, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(friendLink);
        friendLinkService.save(friendLink);
        return R.ok();
    }

     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        FriendLinkEntity friendLink = friendLinkService.getOne(new QueryWrapper<FriendLinkEntity>().eq("", username));
        return R.ok().put("data", friendLink);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody FriendLinkEntity friendLink, HttpServletRequest request){
        //ValidatorUtils.validateEntity(friendLink);
        friendLinkService.updateById(friendLink);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        friendLinkService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,FriendLinkEntity friendLink, HttpServletRequest request,String pre){
        QueryWrapper<FriendLinkEntity> ew = new QueryWrapper<FriendLinkEntity>();
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
		PageUtils page = friendLinkService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, friendLink), params), params));
        return R.ok().put("data", page);
    }

}
