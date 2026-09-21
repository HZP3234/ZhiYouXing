package com.zhiyouxing.attraction.entity.view;

import com.zhiyouxing.attraction.entity.TicketOrderEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 门票订单
 * 后端返回视图实体辅助类
 */
@TableName("ticket_order")
public class TicketOrderView  extends TicketOrderEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public TicketOrderView(){
	}
 
 	public TicketOrderView(TicketOrderEntity ticketOrderEntity){
		BeanUtils.copyProperties(ticketOrderEntity, this);
 		
	}


}
