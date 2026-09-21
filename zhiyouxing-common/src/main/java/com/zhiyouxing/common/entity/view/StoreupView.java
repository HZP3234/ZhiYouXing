package com.zhiyouxing.common.entity.view;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.common.entity.StoreupEntity;

import java.io.Serializable;


/**
 * 收藏表
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date 2024-02-25 14:46:56
 */
@TableName("store_up")
public class StoreupView  extends StoreupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public StoreupView(){
	}
 
 	public StoreupView(StoreupEntity storeUpEntity){
		BeanUtil.copyProperties(storeUpEntity, this);
	}


}
