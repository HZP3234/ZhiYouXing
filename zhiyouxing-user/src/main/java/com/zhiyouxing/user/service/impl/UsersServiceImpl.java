
package com.zhiyouxing.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.user.dao.UsersDao;
import com.zhiyouxing.user.entity.UsersEntity;
import com.zhiyouxing.user.service.UsersService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * 系统用户
 */
@Service("usersService")
public class UsersServiceImpl extends ServiceImpl<UsersDao, UsersEntity> implements UsersService {

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		Page<UsersEntity> page = this.page(
                new Query<UsersEntity>(params).getPage(),
                new QueryWrapper<UsersEntity>()
        );
        return new PageUtils(page);
	}

	@Override
	public List<UsersEntity> selectListView(Wrapper<UsersEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params,
			Wrapper<UsersEntity> wrapper) {
		 Page<UsersEntity> page =new Query<UsersEntity>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
	}
}
