package com.zhiyouxing.common.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.common.entity.TokenEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * token
 */
public interface TokenDao extends BaseMapper<TokenEntity> {

	List<TokenEntity> selectListView(@Param("ew") Wrapper<TokenEntity> wrapper);

	List<TokenEntity> selectListView(Page<?> page, @Param("ew") Wrapper<TokenEntity> wrapper);

}
