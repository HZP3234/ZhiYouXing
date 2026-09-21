package com.zhiyouxing.attraction.service;

import com.zhiyouxing.attraction.entity.TicketOrderEntity;
import com.zhiyouxing.attraction.entity.view.TicketOrderView;
import com.zhiyouxing.attraction.entity.vo.TicketOrderVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 门票订单
 */
public interface TicketOrderService extends IService<TicketOrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TicketOrderVO> selectListVO(Wrapper<TicketOrderEntity> wrapper);
   	
   	TicketOrderVO selectVO(@Param("ew") Wrapper<TicketOrderEntity> wrapper);
   	
   	List<TicketOrderView> selectListView(Wrapper<TicketOrderEntity> wrapper);
   	
   	TicketOrderView selectView(@Param("ew") Wrapper<TicketOrderEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TicketOrderEntity> wrapper);

   	

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<TicketOrderEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<TicketOrderEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<TicketOrderEntity> wrapper);



}

