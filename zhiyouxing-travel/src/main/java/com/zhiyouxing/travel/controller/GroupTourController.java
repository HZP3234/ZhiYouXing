package com.zhiyouxing.travel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.dao.GroupTourDao;
import com.zhiyouxing.travel.dao.RealNameDao;
import com.zhiyouxing.travel.entity.GroupTourEntity;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.model.GroupTourOrderForm;
import com.zhiyouxing.travel.entity.view.GroupTourView;
import com.zhiyouxing.travel.entity.vo.RealNameInfo;
import com.zhiyouxing.travel.service.GroupTourService;
import com.zhiyouxing.travel.service.TravelRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 报团信息
 */
@RestController
@RequestMapping("/group_tour")
public class GroupTourController {

    /**
     * 支付时限，分钟。与 zhiyouxing-user 的 ConsumptionController.PAY_TIMEOUT_MINUTES 同值。
     *
     * 复核名额时必须用同一把尺子：那边的超时取消是惰性的（要等用户打开个人中心才跑），
     * 这把尺子比它的值，才能既排除真正作废的单、又不放过还在等付款的单。
     * 两边都做成常量而不是配置项，改的时候记得一起改。
     */
    private static final int PAY_TIMEOUT_MINUTES = 10;

    /** 单次报名人数上限。不是业务规则，是防呆：免得一单把名额全占、还算出个天文数字 */
    private static final int MAX_SIGNUP = 20;

    @Autowired
    private GroupTourService groupTourService;

    @Autowired
    private GroupTourDao groupTourDao;

    @Autowired
    private TravelRouteService travelRouteService;

    @Autowired
    private RealNameDao realNameDao;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,GroupTourEntity groupTour,
                @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date departureDateStart,
                @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date departureDateEnd,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("tour_guide")) {
			groupTour.setGuideNo((String)request.getSession().getAttribute("username"));
		}
		if(tableName.equals("user")) {
			groupTour.setUserAccount((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
                if(departureDateStart!=null) ew.ge("departure_date", departureDateStart);
                if(departureDateEnd!=null) ew.le("departure_date", departureDateEnd);

		PageUtils page = groupTourService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, groupTour), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,GroupTourEntity groupTour, 
                @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date departureDateStart,
                @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date departureDateEnd,
		HttpServletRequest request){
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
                if(departureDateStart!=null) ew.ge("departure_date", departureDateStart);
                if(departureDateEnd!=null) ew.le("departure_date", departureDateEnd);

		PageUtils page = groupTourService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, groupTour), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( GroupTourEntity groupTour){
       	QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
      	ew.allEq(MPUtil.allEQMapPre( groupTour, "group_tour")); 
        return R.ok().put("data", groupTourService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(GroupTourEntity groupTour){
        QueryWrapper< GroupTourEntity> ew = new QueryWrapper< GroupTourEntity>();
 		ew.allEq(MPUtil.allEQMapPre( groupTour, "group_tour")); 
		GroupTourView groupTourView =  groupTourService.selectView(ew);
		return R.ok("查询报团信息成功").put("data", groupTourView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        GroupTourEntity groupTour = groupTourService.getById(id);
        return R.ok().put("data", groupTour);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        GroupTourEntity groupTour = groupTourService.getById(id);
        return R.ok().put("data", groupTour);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody GroupTourOrderForm form, HttpServletRequest request){
        return createOrder(form, request);
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody GroupTourOrderForm form, HttpServletRequest request){
        return createOrder(form, request);
    }

    /**
     * 报团下单。
     *
     * 实名认证是硬前置：报团单上要写游客姓名，没有认证记录一律不建单。
     * 前端报名弹窗也会先弹认证表单，但那只是体验 —— 接口是公开的，
     * 校验必须落在这里，否则绕开页面直接 POST /group_tour/save 就能报名。
     *
     * 线路名/图片/费用/出发日期/导游工号一律取自线路表，请求体里带什么都不认：
     * 金额让调用方说了算，等于可以一块钱报团。归属与姓名同样以服务端为准。
     *
     * 名额也在这步复核：线路的 group_quota 减掉已被占用的，不够就拒。
     * 两个人同时下单时这里不是原子的（都可能通过检查），和门票/酒店一样不做库存锁 ——
     * 演示项目不值得为它上 SELECT ... FOR UPDATE；真要做，锁的应该是线路那一行。
     *
     * 返回值只带 id 与报团金额：AUTO 主键由 MP 回填，前端拿到 id 才能接着调
     * /user/consumption/pay（source=group）完成支付。
     *
     * 注：管理端的「报团信息」走 TourGuideScopeController 的 /tour_guide/group_tour/save，
     * 不经过这里，所以这道闸门拦不到导游自己录单。
     */
    private R createOrder(GroupTourOrderForm form, HttpServletRequest request) {
        String account = (String) request.getSession().getAttribute("username");
        if (StrUtil.isBlank(account)) {
            return R.error(401, "请先登录");
        }
        if (form.getRouteId() == null) {
            return R.error("缺少线路id");
        }
        TravelRouteEntity route = travelRouteService.getById(form.getRouteId());
        if (route == null) {
            return R.error("线路不存在或已下架");
        }

        Integer seats = form.getSignupCount();
        if (seats == null || seats < 1) {
            return R.error("报名人数至少 1 人");
        }
        if (seats > MAX_SIGNUP) {
            return R.error("单次最多报名 " + MAX_SIGNUP + " 人");
        }

        // 没配名额（null）当作不限 —— 名额是后加的列，早先的线路数据可能没填
        Integer quota = route.getGroupQuota();
        if (quota != null) {
            Integer taken = groupTourDao.sumReservedSeats(route.getRouteName(), PAY_TIMEOUT_MINUTES);
            int left = quota - (taken == null ? 0 : taken);
            if (left <= 0) {
                return R.error("这条线路的名额已经满了");
            }
            if (seats > left) {
                return R.error("名额只剩 " + left + " 个，请减少报名人数");
            }
        }

        RealNameInfo identity = realNameDao.selectByAccount(account);
        // 提交过还不够：审核没过（待审核 / 已驳回）一样不能建单，理由见 RealNameInfo.auditBlockReason
        String block = identity == null ? "报名前请先完成实名认证" : identity.auditBlockReason();
        if (block != null) {
            return R.error(block);
        }

        Date now = new Date();
        GroupTourEntity order = new GroupTourEntity();
        order.setRouteName(route.getRouteName());
        order.setRouteImage(route.getRouteImage());
        order.setDepartureDate(route.getDepartureDate());
        order.setGuideNo(route.getGuideNo());
        order.setRouteFee(route.getRouteFee());
        order.setSignupCount(seats);
        order.setGroupTourAmount(amountOf(route.getRouteFee(), seats));
        order.setUserAccount(account);
        order.setUserName(identity.getRealName());
        // 没填电话就用登录账号 —— 游客账号本身就是手机号
        order.setContactPhone(StrUtil.isBlank(form.getContactPhone())
                ? account : StrUtil.trim(form.getContactPhone()));
        order.setGroupTourTime(now);
        order.setAddTime(now);
        // 新单必然是未支付：付款走 /user/consumption/pay，这里收下「已支付」等于白送一个名额
        order.setIsPay("未支付");

        groupTourService.save(order);

        Map<String, Object> saved = new HashMap<>();
        saved.put("id", order.getId());
        saved.put("amount", order.getGroupTourAmount());
        return R.ok().put("data", saved);
    }

    /**
     * 报团金额 = 单人费用 × 人数。
     *
     * 保留两位小数是因为浮点乘法会留下尾巴：99.9 × 3 = 299.70000000000005，
     * 直接落库的话个人中心和详情页都会原样显示出来。
     * 线路没配费用时按 0 算，避免 NPE —— 这是个数据问题，不该在这里报错挡住下单。
     */
    private static double amountOf(Double routeFee, int seats) {
        double unit = routeFee == null ? 0d : routeFee;
        return Math.round(unit * seats * 100) / 100.0;
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody GroupTourEntity groupTour, HttpServletRequest request){
        //ValidatorUtils.validateEntity(groupTour);
        groupTourService.updateById(groupTour);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        groupTourService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * （按值统计）
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("tour_guide")) {
            ew.eq("guide_no", (String)request.getSession().getAttribute("username"));
		}
		if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
		}
        List<Map<String, Object>> result = groupTourService.selectValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计(多)）
     */
    @RequestMapping("/valueMul/{xColumnName}")
    public R valueMul(@PathVariable("xColumnName") String xColumnName,@RequestParam String yColumnNameMul, HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String,Object>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("tour_guide")) {
            ew.eq("guide_no", (String)request.getSession().getAttribute("username"));
        }
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = groupTourService.selectValue(params, ew);
            for(Map<String, Object> m : result) {
                for(String k : m.keySet()) {
                    if(m.get(k) instanceof Date) {
                        m.put(k, sdf.format((Date)m.get(k)));
                    }
                }
            }
            result2.add(result);
        }
        return R.ok().put("data", result2);
    }

    /**
     * （按值统计）时间统计类型
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}/{timeStatType}")
    public R valueDay(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        params.put("timeStatType", timeStatType);
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("tour_guide")) {
            ew.eq("guide_no", (String)request.getSession().getAttribute("username"));
        }
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = groupTourService.selectTimeStatValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计）时间统计类型(多)
     */
    @RequestMapping("/valueMul/{xColumnName}/{timeStatType}")
    public R valueMulDay(@PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,@RequestParam String yColumnNameMul,HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("timeStatType", timeStatType);
        List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String,Object>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("tour_guide")) {
            ew.eq("guide_no", (String)request.getSession().getAttribute("username"));
        }
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = groupTourService.selectTimeStatValue(params, ew);
            for(Map<String, Object> m : result) {
                for(String k : m.keySet()) {
                    if(m.get(k) instanceof Date) {
                        m.put(k, sdf.format((Date)m.get(k)));
                    }
                }
            }
            result2.add(result);
        }
        return R.ok().put("data", result2);
    }

    /**
     * 分组统计
     */
    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("column", columnName);
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("tour_guide")) {
            ew.eq("guide_no", (String)request.getSession().getAttribute("username"));
        }
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = groupTourService.selectGroup(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,GroupTourEntity groupTour, HttpServletRequest request){
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("tour_guide")) {
            groupTour.setGuideNo((String)request.getSession().getAttribute("username"));
        }
        if(tableName.equals("user")) {
            groupTour.setUserAccount((String)request.getSession().getAttribute("username"));
        }
        QueryWrapper<GroupTourEntity> ew = new QueryWrapper<GroupTourEntity>();
        long count = groupTourService.count(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, groupTour), params), params));
        return R.ok().put("data", count);
    }
}
