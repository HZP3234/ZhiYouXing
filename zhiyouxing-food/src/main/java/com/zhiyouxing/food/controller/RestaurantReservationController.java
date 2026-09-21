package com.zhiyouxing.food.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.food.entity.RestaurantReservationEntity;
import com.zhiyouxing.food.entity.view.RestaurantReservationView;
import com.zhiyouxing.food.service.RestaurantReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 餐厅预约
 * 后端接口
 */
@RestController
@RequestMapping("/restaurant_reservation")
public class RestaurantReservationController {
    @Autowired
    private RestaurantReservationService restaurantReservationService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,RestaurantReservationEntity restaurantReservation,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
			restaurantReservation.setUserAccount((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();

		PageUtils page = restaurantReservationService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurantReservation), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,RestaurantReservationEntity restaurantReservation,
		HttpServletRequest request){
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();

		PageUtils page = restaurantReservationService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurantReservation), params), params));
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( RestaurantReservationEntity restaurantReservation){
       	QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
      	ew.allEq(MPUtil.allEQMapPre( restaurantReservation, "restaurant_reservation"));
        return R.ok().put("data", restaurantReservationService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(RestaurantReservationEntity restaurantReservation){
        QueryWrapper< RestaurantReservationEntity> ew = new QueryWrapper< RestaurantReservationEntity>();
 		ew.allEq(MPUtil.allEQMapPre( restaurantReservation, "restaurant_reservation"));
		RestaurantReservationView restaurantReservationView =  restaurantReservationService.selectView(ew);
		return R.ok("查询餐厅预约成功").put("data", restaurantReservationView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        RestaurantReservationEntity restaurantReservation = restaurantReservationService.getById(id);
        return R.ok().put("data", restaurantReservation);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        RestaurantReservationEntity restaurantReservation = restaurantReservationService.getById(id);
        return R.ok().put("data", restaurantReservation);
    }




    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody RestaurantReservationEntity restaurantReservation, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(restaurantReservation);
        restaurantReservationService.save(restaurantReservation);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody RestaurantReservationEntity restaurantReservation, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(restaurantReservation);
        restaurantReservationService.save(restaurantReservation);
        return R.ok();
    }





    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody RestaurantReservationEntity restaurantReservation, HttpServletRequest request){
        //ValidatorUtils.validateEntity(restaurantReservation);
        restaurantReservationService.updateById(restaurantReservation);//全部更新
        return R.ok();
    }

    /**
     * 审核
     */
    @RequestMapping("/shBatch")
    @Transactional
    public R update(@RequestBody Long[] ids, @RequestParam String auditStatus, @RequestParam String auditReply){
        List<RestaurantReservationEntity> list = new ArrayList<RestaurantReservationEntity>();
        for(Long id : ids) {
            RestaurantReservationEntity restaurantReservation = restaurantReservationService.getById(id);
            restaurantReservation.setAuditStatus(auditStatus);
            restaurantReservation.setAuditReply(auditReply);
            list.add(restaurantReservation);
        }
        restaurantReservationService.updateBatchById(list);
        return R.ok();
    }




    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        restaurantReservationService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }







    /**
     * （按值统计）
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
		}
        List<Map<String, Object>> result = restaurantReservationService.selectValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计(多)）
     */
    @RequestMapping("/valueMul/{xColumnName}")
    public R valueMul(@PathVariable("xColumnName") String xColumnName,@RequestParam String yColumnNameMul, HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String,Object>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = restaurantReservationService.selectValue(params, ew);
            for(Map<String, Object> m : result) {
                for(String k : m.keySet()) {
                    if(m.get(k) instanceof Date) {
                        m.put(k, sdf.format((Date)m.get(k)));
                    }
                }
            }
            result2.add(result);
        }
        return R.ok().put("data", result2);
    }

    /**
     * （按值统计）时间统计类型
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}/{timeStatType}")
    public R valueDay(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        params.put("timeStatType", timeStatType);
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = restaurantReservationService.selectTimeStatValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计）时间统计类型(多)
     */
    @RequestMapping("/valueMul/{xColumnName}/{timeStatType}")
    public R valueMulDay(@PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,@RequestParam String yColumnNameMul,HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("timeStatType", timeStatType);
        List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String,Object>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = restaurantReservationService.selectTimeStatValue(params, ew);
            for(Map<String, Object> m : result) {
                for(String k : m.keySet()) {
                    if(m.get(k) instanceof Date) {
                        m.put(k, sdf.format((Date)m.get(k)));
                    }
                }
            }
            result2.add(result);
        }
        return R.ok().put("data", result2);
    }

    /**
     * 分组统计
     */
    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("column", columnName);
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = restaurantReservationService.selectGroup(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }




    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,RestaurantReservationEntity restaurantReservation, HttpServletRequest request){
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            restaurantReservation.setUserAccount((String)request.getSession().getAttribute("username"));
        }
        QueryWrapper<RestaurantReservationEntity> ew = new QueryWrapper<RestaurantReservationEntity>();
        long count = restaurantReservationService.count(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurantReservation), params), params));
        return R.ok().put("data", count);
    }



}
