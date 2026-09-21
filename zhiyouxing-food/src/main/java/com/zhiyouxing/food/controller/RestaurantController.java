package com.zhiyouxing.food.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.common.utils.UserBasedCollaborativeFiltering;
import com.zhiyouxing.food.entity.RestaurantEntity;
import com.zhiyouxing.food.entity.view.RestaurantView;
import com.zhiyouxing.food.service.RestaurantService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 美食餐厅
 * 后端接口
 *
 * <p>餐厅要「审核通过方可展示」：餐厅前台建/改后服务端把 audit_status 盖成「待审核」，
 * 管理员在 /users/restaurant_audit 通过后，/list 与 /detail/{id} 这两个 @IgnoreAuth
 * 的游客接口才会放行。注意这跟 restaurant_reservation 上那套「餐厅前台审订座」是两回事。
 */
@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    /** 对外可见的餐厅状态。管理员审核台写这个值，游客端两个读接口按它过滤 */
    private static final String AUDIT_APPROVED = "已通过";

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private StoreupService storeUpService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,RestaurantEntity restaurant,
		HttpServletRequest request){
        QueryWrapper<RestaurantEntity> ew = new QueryWrapper<RestaurantEntity>();

		PageUtils page = restaurantService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurant), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表（游客端：只放行已通过审核的餐厅）
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,RestaurantEntity restaurant,
		HttpServletRequest request){
        QueryWrapper<RestaurantEntity> ew = new QueryWrapper<RestaurantEntity>();
        ew.eq("audit_status", AUDIT_APPROVED);

		PageUtils page = restaurantService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurant), params), params));
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( RestaurantEntity restaurant){
       	QueryWrapper<RestaurantEntity> ew = new QueryWrapper<RestaurantEntity>();
      	ew.allEq(MPUtil.allEQMapPre( restaurant, "restaurant"));
        return R.ok().put("data", restaurantService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(RestaurantEntity restaurant){
        QueryWrapper< RestaurantEntity> ew = new QueryWrapper< RestaurantEntity>();
 		ew.allEq(MPUtil.allEQMapPre( restaurant, "restaurant"));
		RestaurantView restaurantView =  restaurantService.selectView(ew);
		return R.ok("查询美食餐厅成功").put("data", restaurantView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        RestaurantEntity restaurant = restaurantService.getById(id);
		restaurant.setClickNum(restaurant.getClickNum()+1);
		restaurantService.updateById(restaurant);
        restaurant = restaurantService.selectView(new QueryWrapper<RestaurantEntity>().eq("id", id));
        return R.ok().put("data", restaurant);
    }

    /**
     * 前端详情（游客端：未过审的餐厅当作不存在）
     *
     * <p>审核判断要放在点击计数之前 —— 未过审的店不该被计一次点击；
     * 顺带把原来对不存在的 id 会 NPE 的问题一起修掉。
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        RestaurantEntity restaurant = restaurantService.getById(id);
        if (restaurant == null || !AUDIT_APPROVED.equals(restaurant.getAuditStatus())) {
            return R.error("餐厅不存在或尚未通过审核");
        }
		restaurant.setClickNum(restaurant.getClickNum()+1);
		restaurantService.updateById(restaurant);
        restaurant = restaurantService.selectView(new QueryWrapper<RestaurantEntity>().eq("id", id));
        return R.ok().put("data", restaurant);
    }



    /**
     * 赞或踩
     */
    @RequestMapping("/thumbsup/{id}")
    public R vote(@PathVariable("id") String id,String type){
        RestaurantEntity restaurant = restaurantService.getById(id);
        if(type.equals("1")) {
        	restaurant.setThumbsUpNum(restaurant.getThumbsUpNum()+1);
        } else {
        	restaurant.setCrazilyNum(restaurant.getCrazilyNum()+1);
        }
        restaurantService.updateById(restaurant);
        return R.ok("投票成功");
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody RestaurantEntity restaurant, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(restaurant);
        stampPending(restaurant);
        restaurantService.save(restaurant);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody RestaurantEntity restaurant, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(restaurant);
        stampPending(restaurant);
        restaurantService.save(restaurant);
        return R.ok();
    }

    /**
     * 把审核三列按「刚提交」处理：状态钉死成「待审核」，回复与时间清空。
     *
     * <p>这一层也要盖一遍（餐厅前台自己的入口在 RestaurantStaffScopeController，已经盖过一次）：
     * AuthorizationInterceptor 只对 /users/** 做角色判断，其余端点任何登录角色都能打。
     * 于是拿一个餐厅前台 token 直接 POST /restaurant/save 带上 auditStatus=「已通过」，
     * 餐厅就**绕过审核**直接出现在游客端了。所以「audit_status 只能由管理员写」这条
     * 不变式得在两扇门上都守住。
     */
    private void stampPending(RestaurantEntity restaurant) {
        restaurant.setAuditStatus("待审核");
        restaurant.setAuditReply(null);
        restaurant.setAuditTime(null);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody RestaurantEntity restaurant, HttpServletRequest request){
        //ValidatorUtils.validateEntity(restaurant);
        stampPending(restaurant);
        restaurantService.updateById(restaurant);//全部更新
        /*
         * updateById 跳过 null 字段，上面把 audit_reply/audit_time 置空清不掉库里的旧值
         * （会出现「状态回到待审核、回复栏里还挂着上一轮的意见」）。要显式 set null。
         * 同 HotelInfoController.update 的写法。
         */
        if (restaurant.getId() != null) {
            restaurantService.update(new UpdateWrapper<RestaurantEntity>()
                    .eq("id", restaurant.getId())
                    .set("audit_reply", null)
                    .set("audit_time", null));
        }
        return R.ok();
    }





    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        restaurantService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }


	/**
     * 前端智能排序（游客面：同样只放行已通过审核的餐厅）
     *
     * <p>酒店的 /autoSort 没加这道闸门，但前端全库没有任何地方调 autoSort
     * （grep 过 frontend/src），所以这里顺手补上 —— 零风险，且堵掉一个
     * 「未过审的餐厅也能被这个公开接口列出来」的口子。
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,RestaurantEntity restaurant, HttpServletRequest request,String pre){
        QueryWrapper<RestaurantEntity> ew = new QueryWrapper<RestaurantEntity>();
        ew.eq("audit_status", AUDIT_APPROVED);
        Map<String, Object> newMap = new HashMap<String, Object>();
        Map<String, Object> param = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			String newKey = entry.getKey();
			if (StrUtil.isEmpty(pre)) {
				newMap.put(newKey, entry.getValue());
			} else if (pre.endsWith(".")) {
				newMap.put(pre + newKey, entry.getValue());
			} else {
				newMap.put(pre + "." + newKey, entry.getValue());
			}
		}
		params.put("sort", "click_num");
        params.put("order", "desc");
		PageUtils page = restaurantService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, restaurant), params), params));
        return R.ok().put("data", page);
    }


    /**
     * 协同算法（基于用户收藏的协同算法）
     */
    @RequestMapping("/autoSort2")
    public R autoSort2(@RequestParam Map<String, Object> params,RestaurantEntity restaurant, HttpServletRequest request){
        String userId = request.getSession().getAttribute("user_id").toString();
        Integer limit = params.get("limit")==null?10:Integer.parseInt(params.get("limit").toString());
        List<StoreupEntity> storeUps = storeUpService.list(new QueryWrapper<StoreupEntity>().eq("type", 1).eq("table_name", "restaurant"));
        Map<String, Map<String, Double>> ratings = new HashMap<>();
        if(storeUps!=null && storeUps.size()>0) {
            for(StoreupEntity storeUp : storeUps) {
                Map<String, Double> userRatings = null;
                if(ratings.containsKey(storeUp.getUserId().toString())) {
                    userRatings = ratings.get(storeUp.getUserId().toString());
                } else {
                    userRatings = new HashMap<>();
                    ratings.put(storeUp.getUserId().toString(), userRatings);
                }

                if(userRatings.containsKey(storeUp.getRefId().toString())) {
                    userRatings.put(storeUp.getRefId().toString(), userRatings.get(storeUp.getRefId().toString())+1.0);
                } else {
                    userRatings.put(storeUp.getRefId().toString(), 1.0);
                }
            }
        }
        // 创建协同过滤对象
        UserBasedCollaborativeFiltering filter = new UserBasedCollaborativeFiltering(ratings);

        // 为指定用户推荐物品
        String targetUser = userId;
        int numRecommendations = limit;
        List<String> recommendations = filter.recommendItems(targetUser, numRecommendations);

        QueryWrapper<RestaurantEntity> ew = new QueryWrapper<RestaurantEntity>();
        if(!recommendations.isEmpty()) {
            ew.in("id", recommendations);
            ew.last("order by FIELD(id, "+"'"+String.join("','", recommendations)+"'"+")");
        }

        PageUtils page = restaurantService.queryPage(params, ew);
        List<RestaurantEntity> pageList = (List<RestaurantEntity>)page.getList();
        if(pageList.size()<limit) {
            int toAddNum = limit-pageList.size();
            ew = new QueryWrapper<RestaurantEntity>();
            if(!recommendations.isEmpty()) {
                ew.notIn("id", recommendations);
            }
            ew.orderBy(true, false, "id");
            ew.last("limit "+toAddNum);
            pageList.addAll(restaurantService.list(ew));
        } else if(pageList.size()>limit) {
            pageList = pageList.subList(0, limit);
        }
        page.setList(pageList);

        return R.ok().put("data", page);
    }

















}
