package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.entity.view.StoreupView;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
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
 * 收藏表
 */
@RestController
@RequestMapping("/store_up")
public class StoreupController {
    @Autowired
    private StoreupService storeUpService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,StoreupEntity storeUp,
		HttpServletRequest request){
        if(!request.getSession().getAttribute("role").toString().equals("管理员")) {
            storeUp.setUserId((Long)request.getSession().getAttribute("user_id"));
        }
        QueryWrapper<StoreupEntity> ew = new QueryWrapper<StoreupEntity>();

		PageUtils page = storeUpService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, storeUp), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,StoreupEntity storeUp, 
		HttpServletRequest request){
        QueryWrapper<StoreupEntity> ew = new QueryWrapper<StoreupEntity>();

		PageUtils page = storeUpService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, storeUp), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( StoreupEntity storeUp){
       	QueryWrapper<StoreupEntity> ew = new QueryWrapper<StoreupEntity>();
      	ew.allEq(MPUtil.allEQMapPre( storeUp, "store_up")); 
        return R.ok().put("data", storeUpService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(StoreupEntity storeUp){
        QueryWrapper< StoreupEntity> ew = new QueryWrapper< StoreupEntity>();
 		ew.allEq(MPUtil.allEQMapPre( storeUp, "store_up")); 
		StoreupView storeUpView =  storeUpService.selectView(ew);
		return R.ok("查询收藏表成功").put("data", storeUpView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        StoreupEntity storeUp = storeUpService.getById(id);
        return R.ok().put("data", storeUp);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        StoreupEntity storeUp = storeUpService.getById(id);
        return R.ok().put("data", storeUp);
    }


    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody StoreupEntity storeUp, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(storeUp);
    	storeUp.setUserId((Long)request.getSession().getAttribute("user_id"));
        storeUpService.save(storeUp);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody StoreupEntity storeUp, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(storeUp);
        storeUpService.save(storeUp);
        return R.ok();
    }

     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        StoreupEntity storeUp = storeUpService.getOne(new QueryWrapper<StoreupEntity>().eq("", username));
        return R.ok().put("data", storeUp);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody StoreupEntity storeUp, HttpServletRequest request){
        //ValidatorUtils.validateEntity(storeUp);
        storeUpService.updateById(storeUp);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        storeUpService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,StoreupEntity storeUp, HttpServletRequest request,String pre){
        QueryWrapper<StoreupEntity> ew = new QueryWrapper<StoreupEntity>();
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
		PageUtils page = storeUpService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, storeUp), params), params));
        return R.ok().put("data", page);
    }

}
