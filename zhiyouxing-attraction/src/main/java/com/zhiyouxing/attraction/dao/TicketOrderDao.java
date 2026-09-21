package com.zhiyouxing.attraction.dao;

import com.zhiyouxing.attraction.entity.TicketOrderEntity;
import com.zhiyouxing.attraction.entity.view.TicketOrderView;
import com.zhiyouxing.attraction.entity.vo.TicketOrderVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 门票订单
 */
public interface TicketOrderDao extends BaseMapper<TicketOrderEntity> {
	
	List<TicketOrderVO> selectListVO(@Param("ew") Wrapper<TicketOrderEntity> wrapper);
	
	TicketOrderVO selectVO(@Param("ew") Wrapper<TicketOrderEntity> wrapper);
	
	List<TicketOrderView> selectListView(@Param("ew") Wrapper<TicketOrderEntity> wrapper);

	List<TicketOrderView> selectListView(Page<?> page,@Param("ew") Wrapper<TicketOrderEntity> wrapper);

	
	TicketOrderView selectView(@Param("ew") Wrapper<TicketOrderEntity> wrapper);
	

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TicketOrderEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TicketOrderEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TicketOrderEntity> wrapper);



}
