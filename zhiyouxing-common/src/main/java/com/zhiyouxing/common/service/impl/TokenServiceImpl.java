
package com.zhiyouxing.common.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.dao.TokenDao;
import com.zhiyouxing.common.entity.TokenEntity;
import com.zhiyouxing.common.service.TokenService;
import com.zhiyouxing.common.utils.CommonUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * token：登录态存 Redis，DB 里的 token 表只留给管理端列表方法。
 */
@Service("tokenService")
public class TokenServiceImpl extends ServiceImpl<TokenDao, TokenEntity> implements TokenService {

	/** token → 用户信息 JSON */
	private static final String TOKEN_KEY_PREFIX = "zhiyouxing:token:";

	/** 反向索引 token 串，用来在同账号+同角色重登时踢掉旧 token */
	private static final String TOKEN_INDEX_PREFIX = "zhiyouxing:token:index:";

	/** 与原 DB 版一致：1 小时过期，交给 Redis 的 TTL 兜底 */
	private static final long TOKEN_TTL_SECONDS = 3600L;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		Page<TokenEntity> page = this.page(
                new Query<TokenEntity>(params).getPage(),
                new QueryWrapper<TokenEntity>()
        );
        return new PageUtils(page);
	}

	@Override
	public List<TokenEntity> selectListView(Wrapper<TokenEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params,
			Wrapper<TokenEntity> wrapper) {
		 Page<TokenEntity> page =new Query<TokenEntity>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
	}

	@Override
	public String generateToken(Long userId,String username, String tableName, String role) {
		String indexKey = tokenIndexKey(tableName, userId, role);
		String oldToken = stringRedisTemplate.opsForValue().get(indexKey);
		if (StrUtil.isNotBlank(oldToken)) {
			stringRedisTemplate.delete(TOKEN_KEY_PREFIX + oldToken);
		}

		String token = CommonUtil.getRandomString(32);
		Map<String, Object> payload = new HashMap<>();
		payload.put("userId", userId);
		payload.put("username", username);
		payload.put("tableName", tableName);
		payload.put("role", role);

		stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, JSONUtil.toJsonStr(payload),
				TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
		stringRedisTemplate.opsForValue().set(indexKey, token, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
		return token;
	}

	@Override
	public TokenEntity getTokenEntity(String token) {
		if (StrUtil.isBlank(token)) {
			return null;
		}
		String json = stringRedisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
		if (StrUtil.isBlank(json)) {
			// 不存在或已到期被 Redis 自动删除
			return null;
		}
		return JSONUtil.toBean(json, TokenEntity.class);
	}

	@Override
	public void removeToken(String token) {
		if (StrUtil.isBlank(token)) {
			return;
		}
		String key = TOKEN_KEY_PREFIX + token;
		String json = stringRedisTemplate.opsForValue().get(key);
		if (StrUtil.isNotBlank(json)) {
			TokenEntity entity = JSONUtil.toBean(json, TokenEntity.class);
			stringRedisTemplate.delete(tokenIndexKey(entity.getTableName(), entity.getUserId(), entity.getRole()));
		}
		stringRedisTemplate.delete(key);
	}

	private String tokenIndexKey(String tableName, Long userId, String role) {
		return TOKEN_INDEX_PREFIX + tableName + ":" + userId + ":" + role;
	}
}
