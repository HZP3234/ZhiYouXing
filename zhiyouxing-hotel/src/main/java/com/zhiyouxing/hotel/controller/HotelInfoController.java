package com.zhiyouxing.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.common.utils.UserBasedCollaborativeFiltering;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import com.zhiyouxing.hotel.entity.view.HotelInfoView;
import com.zhiyouxing.hotel.service.HotelInfoService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 酒店信息（酒店级，客房在 /room_type）
 *
 * <p>酒店要「审核通过方可营业」：酒店前台建/改后服务端把 audit_status 盖成「待审核」，
 * 管理员在 /users/hotel_info_audit 通过后，/list 与 /detail/{id} 这两个 @IgnoreAuth
 * 的游客接口才会放行。未过审的酒店游客看不到、也订不到（客房接口另有一道同样的闸门）。
 */
@RestController
@RequestMapping("/hotel_info")
public class HotelInfoController {

    /** 对外可见的酒店状态。管理员审核台写这个值，游客端三个读接口按它过滤 */
    private static final String AUDIT_APPROVED = "已通过";

    @Autowired
    private HotelInfoService hotelInfoService;

    @Autowired
    private StoreupService storeUpService;

    /**
     * 后端列表（管理端：不过滤状态，酒店前台要能看到自己那条的审核进度）
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,HotelInfoEntity hotelInfo,
		HttpServletRequest request){
        QueryWrapper<HotelInfoEntity> ew = new QueryWrapper<HotelInfoEntity>();

		PageUtils page = hotelInfoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelInfo), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表（游客端：只放行已通过审核的酒店）
     *
     * <p>价格区间不能再在这里过滤：价格是客房（room_type）的列，酒店表没有。
     * 游客端按价格筛选时先筛客房、再由客房反推哪些酒店该出现（见 hotelInfoList.vue）。
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,HotelInfoEntity hotelInfo,
		HttpServletRequest request){
        QueryWrapper<HotelInfoEntity> ew = new QueryWrapper<HotelInfoEntity>();
        ew.eq("audit_status", AUDIT_APPROVED);

		PageUtils page = hotelInfoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelInfo), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( HotelInfoEntity hotelInfo){
       	QueryWrapper<HotelInfoEntity> ew = new QueryWrapper<HotelInfoEntity>();
      	ew.allEq(MPUtil.allEQMapPre( hotelInfo, "hotel_info")); 
        return R.ok().put("data", hotelInfoService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(HotelInfoEntity hotelInfo){
        QueryWrapper< HotelInfoEntity> ew = new QueryWrapper< HotelInfoEntity>();
 		ew.allEq(MPUtil.allEQMapPre( hotelInfo, "hotel_info")); 
		HotelInfoView hotelInfoView =  hotelInfoService.selectView(ew);
		return R.ok("查询酒店信息成功").put("data", hotelInfoView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        HotelInfoEntity hotelInfo = hotelInfoService.getById(id);
		hotelInfo.setClickNum(hotelInfo.getClickNum()+1);
		hotelInfoService.updateById(hotelInfo);
        hotelInfo = hotelInfoService.selectView(new QueryWrapper<HotelInfoEntity>().eq("id", id));
        return R.ok().put("data", hotelInfo);
    }

    /**
     * 前端详情（游客端：未过审的酒店当作不存在）
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        HotelInfoEntity hotelInfo = hotelInfoService.getById(id);
        if (hotelInfo == null || !AUDIT_APPROVED.equals(hotelInfo.getAuditStatus())) {
            return R.error("酒店不存在或尚未通过审核");
        }
		hotelInfo.setClickNum(hotelInfo.getClickNum()+1);
		hotelInfoService.updateById(hotelInfo);
        hotelInfo = hotelInfoService.selectView(new QueryWrapper<HotelInfoEntity>().eq("id", id));
        return R.ok().put("data", hotelInfo);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody HotelInfoEntity hotelInfo, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(hotelInfo);
        stampPending(hotelInfo);
        hotelInfoService.save(hotelInfo);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody HotelInfoEntity hotelInfo, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(hotelInfo);
        stampPending(hotelInfo);
        hotelInfoService.save(hotelInfo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody HotelInfoEntity hotelInfo, HttpServletRequest request){
        //ValidatorUtils.validateEntity(hotelInfo);
        stampPending(hotelInfo);
        hotelInfoService.updateById(hotelInfo);//全部更新
        /*
         * updateById 跳过 null 字段，上面把 audit_reply/audit_time 置空清不掉库里的旧值
         * （会出现「状态回到待审核、回复栏里还挂着上一轮的意见」）。要显式 set null。
         * 同 TravelRouteController.update 的写法。
         */
        if (hotelInfo.getId() != null) {
            hotelInfoService.update(new UpdateWrapper<HotelInfoEntity>()
                    .eq("id", hotelInfo.getId())
                    .set("audit_reply", null)
                    .set("audit_time", null));
        }
        return R.ok();
    }

    /**
     * 把审核三列按「刚提交」处理：状态钉死成「待审核」，回复与时间清空。
     *
     * <p>这一层也要盖一遍（酒店前台自己的入口在 HotelStaffScopeController，已经盖过一次）：
     * AuthorizationInterceptor 只对 /users/** 做角色判断，其余端点任何登录角色都能打。
     * 于是拿一个酒店前台 token 直接 POST /hotel_info/save 带上 auditStatus=「已通过」，
     * 酒店就**绕过审核**直接出现在游客端了。所以「audit_status 只能由管理员写」这条
     * 不变式得在两扇门上都守住。
     */
    private void stampPending(HotelInfoEntity hotelInfo) {
        hotelInfo.setAuditStatus("待审核");
        hotelInfo.setAuditReply(null);
        hotelInfo.setAuditTime(null);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        hotelInfoService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,HotelInfoEntity hotelInfo, HttpServletRequest request,String pre){
        QueryWrapper<HotelInfoEntity> ew = new QueryWrapper<HotelInfoEntity>();
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
		PageUtils page = hotelInfoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelInfo), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 协同算法（基于用户收藏的协同算法）
     */
    @RequestMapping("/autoSort2")
    public R autoSort2(@RequestParam Map<String, Object> params,HotelInfoEntity hotelInfo, HttpServletRequest request){
        String userId = request.getSession().getAttribute("user_id").toString();
        Integer limit = params.get("limit")==null?10:Integer.parseInt(params.get("limit").toString());
        List<StoreupEntity> storeUps = storeUpService.list(new QueryWrapper<StoreupEntity>().eq("type", 1).eq("table_name", "hotel_info"));
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

        // 输出推荐结果
        System.out.println("Recommendations for " + targetUser + ":");
        for (String item : recommendations) {
            System.out.println(item);
        }

        QueryWrapper<HotelInfoEntity> ew = new QueryWrapper<HotelInfoEntity>();
        if(!recommendations.isEmpty()) {
            ew.in("id", recommendations);
            ew.last("order by FIELD(id, "+"'"+String.join("','", recommendations)+"'"+")");
        }

        PageUtils page = hotelInfoService.queryPage(params, ew);
        List<HotelInfoEntity> pageList = (List<HotelInfoEntity>)page.getList();
        if(pageList.size()<limit) {
            int toAddNum = limit-pageList.size();
            ew = new QueryWrapper<HotelInfoEntity>();
            if(!recommendations.isEmpty()) {
                ew.notIn("id", recommendations);
            }
            ew.orderBy(true, false, "id");
            ew.last("limit "+toAddNum);
            pageList.addAll(hotelInfoService.list(ew));
        } else if(pageList.size()>limit) {
            pageList = pageList.subList(0, limit);
        }
        page.setList(pageList);

        return R.ok().put("data", page);
    }
}