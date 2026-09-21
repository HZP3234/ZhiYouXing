package com.zhiyouxing.common.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.entity.view.StoreupView;
import com.zhiyouxing.common.entity.vo.StoreupVO;
import com.zhiyouxing.common.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 收藏表
 *
 * @author
 * @email
 * @date 2024-02-25 14:46:56
 */
public interface StoreupService extends IService<StoreupEntity> {

	PageUtils queryPage(Map<String, Object> params);

	List<StoreupVO> selectListVO(Wrapper<StoreupEntity> wrapper);

	StoreupVO selectVO(@Param("ew") Wrapper<StoreupEntity> wrapper);

	List<StoreupView> selectListView(Wrapper<StoreupEntity> wrapper);

	StoreupView selectView(@Param("ew") Wrapper<StoreupEntity> wrapper);

	PageUtils queryPage(Map<String, Object> params, Wrapper<StoreupEntity> wrapper);

}
