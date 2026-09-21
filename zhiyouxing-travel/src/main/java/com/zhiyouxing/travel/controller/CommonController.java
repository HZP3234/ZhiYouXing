package com.zhiyouxing.travel.controller;

import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.service.CommonService;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.common.utils.SqlIdentifierValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 通用接口
 */
@RestController
public class CommonController{
	@Autowired
	private CommonService commonService;

	/**
	 * 获取table表中的column列表(联动接口)
	 */
	@IgnoreAuth
	@RequestMapping("/option/{table_name}/{columnName}")
	public R getOption(@PathVariable("table_name") String tableName, @PathVariable("columnName") String columnName,@RequestParam(required = false) String conditionColumn,@RequestParam(required = false) String conditionValue,String level,String parent) {
		SqlIdentifierValidator.validateOptionColumn(tableName, columnName);
		if(StringUtils.isNotBlank(conditionColumn)) {
			SqlIdentifierValidator.validateColumn(tableName, conditionColumn);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		params.put("column", columnName);
		if(StringUtils.isNotBlank(level)) {
			params.put("level", level);
		}
		if(StringUtils.isNotBlank(parent)) {
			params.put("parent", parent);
		}
        if(StringUtils.isNotBlank(conditionColumn)) {
            params.put("conditionColumn", conditionColumn);
        }
        if(StringUtils.isNotBlank(conditionValue)) {
            params.put("conditionValue", conditionValue);
        }
		List<String> data = commonService.getOption(params);
		return R.ok().put("data", data);
	}
	
	/**
	 * 根据table中的column获取单条记录
	 */
	@IgnoreAuth
	@RequestMapping("/follow/{table_name}/{columnName}")
	public R getFollowByOption(@PathVariable("table_name") String tableName, @PathVariable("columnName") String columnName, @RequestParam String columnValue) {
		SqlIdentifierValidator.validateTable(tableName);
		SqlIdentifierValidator.validateColumn(tableName, columnName);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		params.put("column", columnName);
		params.put("columnValue", columnValue);
		Map<String, Object> result = commonService.getFollowByOption(params);
		return R.ok().put("data", result);
	}
	
	/**
	 * 修改table表的auditStatus状态
	 */
	@RequestMapping("/sh/{table_name}")
	public R sh(@PathVariable("table_name") String tableName, @RequestBody Map<String, Object> map) {
		SqlIdentifierValidator.validateTable(tableName);
		map.put("table", tableName);
		commonService.sh(map);
		return R.ok();
	}
	
	/**
	 * 获取需要提醒的记录数
	 */
	@IgnoreAuth
	@RequestMapping("/remind/{table_name}/{columnName}/{type}")
	public R remindCount(@PathVariable("table_name") String tableName, @PathVariable("columnName") String columnName,
						 @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		SqlIdentifierValidator.validateTable(tableName);
		SqlIdentifierValidator.validateColumn(tableName, columnName);
		map.put("table", tableName);
		map.put("column", columnName);
		map.put("type", type);
		
		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}
		
		int count = commonService.remindCount(map);
		return R.ok().put("count", count);
	}
	
	/**
	 * 单列求和
	 */
	@IgnoreAuth
	@RequestMapping("/cal/{table_name}/{columnName}")
	public R cal(@PathVariable("table_name") String tableName, @PathVariable("columnName") String columnName) {
		SqlIdentifierValidator.validateTable(tableName);
		SqlIdentifierValidator.validateColumn(tableName, columnName);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		params.put("column", columnName);
		Map<String, Object> result = commonService.selectCal(params);
		return R.ok().put("data", result);
	}
	
	/**
	 * 分组统计
	 */
	@IgnoreAuth
	@RequestMapping("/group/{table_name}/{columnName}")
	public R group(@PathVariable("table_name") String tableName, @PathVariable("columnName") String columnName) {
		SqlIdentifierValidator.validateTable(tableName);
		SqlIdentifierValidator.validateColumn(tableName, columnName);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		params.put("column", columnName);
		List<Map<String, Object>> result = commonService.selectGroup(params);
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
	 * （按值统计）
	 */
	@IgnoreAuth
	@RequestMapping("/value/{table_name}/{xColumnName}/{yColumnName}")
	public R value(@PathVariable("table_name") String tableName, @PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName) {
		SqlIdentifierValidator.validateTable(tableName);
		SqlIdentifierValidator.validateColumn(tableName, xColumnName);
		SqlIdentifierValidator.validateColumn(tableName, yColumnName);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		params.put("xColumn", xColumnName);
		params.put("yColumn", yColumnName);
		List<Map<String, Object>> result = commonService.selectValue(params);
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
 	 * （按值统计）时间统计类型
	 */
	@IgnoreAuth
	@RequestMapping("/value/{table_name}/{xColumnName}/{yColumnName}/{timeStatType}")
	public R valueDay(@PathVariable("table_name") String tableName, @PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType) {
		SqlIdentifierValidator.validateTable(tableName);
		SqlIdentifierValidator.validateColumn(tableName, xColumnName);
		SqlIdentifierValidator.validateColumn(tableName, yColumnName);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		params.put("xColumn", xColumnName);
		params.put("yColumn", yColumnName);
		params.put("timeStatType", timeStatType);
		List<Map<String, Object>> result = commonService.selectTimeStatValue(params);
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
	



}
