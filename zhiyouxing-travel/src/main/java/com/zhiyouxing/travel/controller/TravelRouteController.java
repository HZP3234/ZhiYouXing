package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;
import com.zhiyouxing.travel.service.TravelRouteDayService;
import com.zhiyouxing.travel.service.TravelRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 旅游线路
 */
@RestController
@RequestMapping("/travel_route")
public class TravelRouteController {

    /**
     * 对外可见的线路状态。导游新建/改动后是「待审核」，要管理员在
     * /users/travel_route_audit 里点通过才变成这个值 —— /list、/search、/detail
     * 三个 @IgnoreAuth 接口都按它过滤，未过审的线路游客看不到。
     *
     * /page 不过滤：管理端要看到全部状态，导游那一侧另有 /tour_guide/travel_route/page。
     */
    private static final String AUDIT_APPROVED = "已通过";

    @Autowired
    private TravelRouteService travelRouteService;

    @Autowired
    private StoreupService storeUpService;

    @Autowired
    private TravelRouteDayService travelRouteDayService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TravelRouteEntity travelRoute,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("tour_guide")) {
			travelRoute.setGuideNo((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<TravelRouteEntity> ew = new QueryWrapper<TravelRouteEntity>();

		PageUtils page = travelRouteService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelRoute), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TravelRouteEntity travelRoute,
		HttpServletRequest request){
        QueryWrapper<TravelRouteEntity> ew = new QueryWrapper<TravelRouteEntity>();
        ew.eq("audit_status", AUDIT_APPROVED);

		PageUtils page = travelRouteService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelRoute), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 线路页的入口：按关键词搜线路，并把每条线路的每日行程一起带回去。
     *
     * <p>为什么不复用 /list：keyword 要同时命中线路名称、景点名称、起点、终点、途经路段，
     * 而 /list 的参数只按实体字段裸反射（MPUtil.likeOrEq），传 keyword 会被静默忽略 ——
     * 页面看着一切正常，实际筛选根本没生效。同一个坑在 /travel_guide/search 上已经踩过一次。
     *
     * <p>天数与预算走的是 MPUtil.between 那套约定（days_start / days_end、
     * routeFee_start / routeFee_end），那两个键不是实体字段，所以只在 /list 已有逻辑里生效，
     * 这里不需要额外代码；排序同理（sort=routeFee&order=asc，后端自己转下划线）。
     *
     * <p>返回信封与 /list 逐字一致（PageUtils：list / totalCount / pageSize / totalPage / currPage），
     * 前端两条路径的解析代码不用分叉。daily 是 TravelRouteView 上的派生字段（不是表里的列），
     * 由 TravelRouteDayService 批量回填，空线路是空数组。
     *
     * <p>@IgnoreAuth：没登录也能看线路。
     */
    @IgnoreAuth
    @RequestMapping("/search")
    public R search(@RequestParam Map<String, Object> params, TravelRouteEntity travelRoute) {
        QueryWrapper<TravelRouteEntity> ew = new QueryWrapper<TravelRouteEntity>();
        // 只放行审核通过的线路：导游新建/改动的一律是「待审核」，不在这里露头
        ew.eq("audit_status", AUDIT_APPROVED);

        Object keywordRaw = params.get("keyword");
        if (keywordRaw != null && !keywordRaw.toString().trim().isEmpty()) {
            String keyword = keywordRaw.toString().trim();
            // and(...) 会给整组套一层括号，五个字段的 OR 才不会被别的条件切开
            ew.and(w -> w.like("route_name", keyword)
                    .or().like("attraction_name", keyword)
                    .or().like("start_point", keyword)
                    .or().like("end_point", keyword)
                    .or().like("via_road", keyword));
        }

        PageUtils page = travelRouteService.queryPage(params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelRoute), params), params));
        if (page.getList() != null) {
            @SuppressWarnings("unchecked")
            List<TravelRouteView> routes = (List<TravelRouteView>) page.getList();
            travelRouteDayService.attachDaily(routes);
        }
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TravelRouteEntity travelRoute){
       	QueryWrapper<TravelRouteEntity> ew = new QueryWrapper<TravelRouteEntity>();
      	ew.allEq(MPUtil.allEQMapPre( travelRoute, "travel_route")); 
        return R.ok().put("data", travelRouteService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TravelRouteEntity travelRoute){
        QueryWrapper< TravelRouteEntity> ew = new QueryWrapper< TravelRouteEntity>();
 		ew.allEq(MPUtil.allEQMapPre( travelRoute, "travel_route")); 
		TravelRouteView travelRouteView =  travelRouteService.selectView(ew);
		return R.ok("查询旅游线路成功").put("data", travelRouteView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TravelRouteEntity travelRoute = travelRouteService.getById(id);
        return R.ok().put("data", travelRoute);
    }

    /**
     * 前端详情
     *
     * <p>返回 View 而不是实体，只为了多带一个 daily（详情抽屉里那串 Day 1 / Day 2）。
     * 别的字段与实体逐一对应，前端拿到的形状跟以前一样。
     *
     * <p>@IgnoreAuth：线路是公开内容，没登录也能读。
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TravelRouteEntity travelRoute = travelRouteService.getById(id);
        if (travelRoute == null || !AUDIT_APPROVED.equals(travelRoute.getAuditStatus())) {
            return R.error("线路不存在或尚未通过审核");
        }
        TravelRouteView view = new TravelRouteView(travelRoute);
        travelRouteDayService.attachDaily(Collections.singletonList(view));
        return R.ok().put("data", view);
    }

    /**
     * 赞或踩
     */
    @RequestMapping("/thumbsup/{id}")
    public R vote(@PathVariable("id") String id,String type){
        TravelRouteEntity travelRoute = travelRouteService.getById(id);
        if(type.equals("1")) {
        	travelRoute.setThumbsUpNum(travelRoute.getThumbsUpNum()+1);
        } else {
        	travelRoute.setCrazilyNum(travelRoute.getCrazilyNum()+1);
        }
        travelRouteService.updateById(travelRoute);
        return R.ok("投票成功");
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TravelRouteEntity travelRoute, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(travelRoute);
        stampPending(travelRoute);
        travelRouteService.save(travelRoute);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TravelRouteEntity travelRoute, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(travelRoute);
        stampPending(travelRoute);
        travelRouteService.save(travelRoute);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TravelRouteEntity travelRoute, HttpServletRequest request){
        //ValidatorUtils.validateEntity(travelRoute);
        stampPending(travelRoute);
        travelRouteService.updateById(travelRoute);//全部更新
        /*
         * updateById 跳过 null 字段，上面把 audit_reply/audit_time 置空清不掉库里的旧值
         * （会出现「状态回到待审核、回复栏里还挂着上一轮的意见」）。要显式 set null。
         * 同 TourGuideScopeController.travelRouteUpdate 的写法。
         */
        if (travelRoute.getId() != null) {
            travelRouteService.update(new UpdateWrapper<TravelRouteEntity>()
                    .eq("id", travelRoute.getId())
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
        travelRouteService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 把审核三列按「刚提交」处理：状态钉死成「待审核」，回复与时间清空。
     *
     * <p>为什么这个通用控制器也要盖一遍（导游自己的入口在 TourGuideScopeController，
     * 已经盖过一次了）：AuthorizationInterceptor 只对 /users/** 做角色判断，
     * 其余端点任何登录角色都能打。于是一个导游 token 直接 POST /travel_route/save
     * 带上 auditStatus=「已通过」，线路就**绕过审核**直接出现在游客的旅游线路页上 ——
     * 实测过，/search 里当场就能看到。
     *
     * <p>所以「audit_status 只能由管理员写」这条不变式得在两扇门上都守住：
     * 这一层不认请求体里的 audit 字段，也不接受前端传的审核结论。
     */
    private void stampPending(TravelRouteEntity travelRoute) {
        travelRoute.setAuditStatus("待审核");
        travelRoute.setAuditReply(null);
        travelRoute.setAuditTime(null);
    }
}
