package com.zhiyouxing.common.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.entity.view.StoreupView;
import com.zhiyouxing.common.entity.vo.StoreupVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 收藏表
 */
public interface StoreupDao extends BaseMapper<StoreupEntity> {

	List<StoreupVO> selectListVO(@Param("ew") Wrapper<StoreupEntity> wrapper);

	StoreupVO selectVO(@Param("ew") Wrapper<StoreupEntity> wrapper);

	List<StoreupView> selectListView(@Param("ew") Wrapper<StoreupEntity> wrapper);

	List<StoreupView> selectListView(Page<?> page, @Param("ew") Wrapper<StoreupEntity> wrapper);


	StoreupView selectView(@Param("ew") Wrapper<StoreupEntity> wrapper);


}
