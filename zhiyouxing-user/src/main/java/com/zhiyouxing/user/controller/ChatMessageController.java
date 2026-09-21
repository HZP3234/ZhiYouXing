package com.zhiyouxing.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.entity.ChatMessageEntity;
import com.zhiyouxing.user.entity.view.ChatMessageView;
import com.zhiyouxing.user.service.ChatMessageService;
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
 * 消息表
 */
@RestController
@RequestMapping("/chat_message")
public class ChatMessageController {
    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,ChatMessageEntity chatMessage,
		HttpServletRequest request){
        QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();

		PageUtils page = chatMessageService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chatMessage), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,ChatMessageEntity chatMessage, 
		HttpServletRequest request){
        QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();

		PageUtils page = chatMessageService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chatMessage), params), params));
        return R.ok().put("data", page);
    }


    /**
     * 消息列表
     */
    @RequestMapping("/mlist")
    public R mlist(@RequestParam Map<String, Object> params,ChatMessageEntity chatMessage, HttpServletRequest request){
        QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();
        ew.eq("uid", chatMessage.getUid()).eq("fid", chatMessage.getFid()).or().eq("fid", chatMessage.getUid()).eq("uid", chatMessage.getFid());
        PageUtils page = chatMessageService.queryPage(params, ew);
        chatMessageService.update(new UpdateWrapper<ChatMessageEntity>().set("is_read", 1).eq("is_read", 0).eq("fid", chatMessage.getUid()));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( ChatMessageEntity chatMessage){
       	QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();
      	ew.allEq(MPUtil.allEQMapPre( chatMessage, "chat_message")); 
        return R.ok().put("data", chatMessageService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(ChatMessageEntity chatMessage){
        QueryWrapper< ChatMessageEntity> ew = new QueryWrapper< ChatMessageEntity>();
 		ew.allEq(MPUtil.allEQMapPre( chatMessage, "chat_message")); 
		ChatMessageView chatMessageView =  chatMessageService.selectView(ew);
		return R.ok("查询消息表成功").put("data", chatMessageView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        ChatMessageEntity chatMessage = chatMessageService.getById(id);
        return R.ok().put("data", chatMessage);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        ChatMessageEntity chatMessage = chatMessageService.getById(id);
        return R.ok().put("data", chatMessage);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ChatMessageEntity chatMessage, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(chatMessage);
        chatMessageService.save(chatMessage);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody ChatMessageEntity chatMessage, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(chatMessage);
        chatMessageService.save(chatMessage);
        return R.ok();
    }



     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        ChatMessageEntity chatMessage = chatMessageService.getOne(new QueryWrapper<ChatMessageEntity>().eq("", username));
        return R.ok().put("data", chatMessage);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody ChatMessageEntity chatMessage, HttpServletRequest request){
        //ValidatorUtils.validateEntity(chatMessage);
        chatMessageService.updateById(chatMessage);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        chatMessageService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,ChatMessageEntity chatMessage, HttpServletRequest request,String pre){
        QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();
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
		PageUtils page = chatMessageService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chatMessage), params), params));
        return R.ok().put("data", page);
    }










}
