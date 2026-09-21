package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.HelpRequestEntity;
import com.zhiyouxing.travel.entity.view.HelpRequestView;
import com.zhiyouxing.travel.service.HelpRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 求救信息
 */
@RestController
@RequestMapping("/help_request")
public class HelpRequestController {
    @Autowired
    private HelpRequestService helpRequestService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,HelpRequestEntity helpRequest,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
			helpRequest.setUserAccount((String)request.getSession().getAttribute("username"));
		}
		if(tableName.equals("tour_guide")) {
			helpRequest.setGuideNo((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<HelpRequestEntity> ew = new QueryWrapper<HelpRequestEntity>();

		PageUtils page = helpRequestService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, helpRequest), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,HelpRequestEntity helpRequest, 
		HttpServletRequest request){
        QueryWrapper<HelpRequestEntity> ew = new QueryWrapper<HelpRequestEntity>();

		PageUtils page = helpRequestService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, helpRequest), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( HelpRequestEntity helpRequest){
       	QueryWrapper<HelpRequestEntity> ew = new QueryWrapper<HelpRequestEntity>();
      	ew.allEq(MPUtil.allEQMapPre( helpRequest, "help_request")); 
        return R.ok().put("data", helpRequestService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(HelpRequestEntity helpRequest){
        QueryWrapper< HelpRequestEntity> ew = new QueryWrapper< HelpRequestEntity>();
 		ew.allEq(MPUtil.allEQMapPre( helpRequest, "help_request")); 
		HelpRequestView helpRequestView =  helpRequestService.selectView(ew);
		return R.ok("查询求救信息成功").put("data", helpRequestView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        HelpRequestEntity helpRequest = helpRequestService.getById(id);
        return R.ok().put("data", helpRequest);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        HelpRequestEntity helpRequest = helpRequestService.getById(id);
        return R.ok().put("data", helpRequest);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody HelpRequestEntity helpRequest, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(helpRequest);
        helpRequestService.save(helpRequest);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody HelpRequestEntity helpRequest, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(helpRequest);
        helpRequestService.save(helpRequest);
        return R.ok();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody HelpRequestEntity helpRequest, HttpServletRequest request){
        //ValidatorUtils.validateEntity(helpRequest);
        helpRequestService.updateById(helpRequest);//全部更新
        return R.ok();
    }

    /**
     * 响应（处理求救）
     */
    @RequestMapping("/shBatch")
    @Transactional
    public R shBatch(@RequestBody Long[] ids, @RequestParam String auditStatus, @RequestParam String replyContent){
        List<HelpRequestEntity> list = new ArrayList<HelpRequestEntity>();
        for(Long id : ids) {
            HelpRequestEntity helpRequest = helpRequestService.getById(id);
            helpRequest.setAuditStatus(auditStatus);
            helpRequest.setReplyContent(replyContent);
            list.add(helpRequest);
        }
        helpRequestService.updateBatchById(list);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        helpRequestService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,HelpRequestEntity helpRequest, HttpServletRequest request){
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            helpRequest.setUserAccount((String)request.getSession().getAttribute("username"));
        }
        if(tableName.equals("tour_guide")) {
            helpRequest.setGuideNo((String)request.getSession().getAttribute("username"));
        }
        QueryWrapper<HelpRequestEntity> ew = new QueryWrapper<HelpRequestEntity>();
        long count = helpRequestService.count(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, helpRequest), params), params));
        return R.ok().put("data", count);
    }

}
