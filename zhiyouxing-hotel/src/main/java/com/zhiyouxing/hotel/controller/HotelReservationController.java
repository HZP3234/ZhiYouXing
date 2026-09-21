package com.zhiyouxing.hotel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.hotel.dao.RealNameDao;
import com.zhiyouxing.hotel.entity.HotelReservationEntity;
import com.zhiyouxing.hotel.entity.view.HotelReservationView;
import com.zhiyouxing.hotel.entity.vo.RealNameInfo;
import com.zhiyouxing.hotel.service.HotelReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 酒店预订
 */
@RestController
@RequestMapping("/hotel_reservation")
public class HotelReservationController {
    @Autowired
    private HotelReservationService hotelReservationService;

    /** 只读 user_identity，用来给下单把住「已实名」这道门 */
    @Autowired
    private RealNameDao realNameDao;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,HotelReservationEntity hotelReservation,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
			hotelReservation.setUserAccount((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();

		PageUtils page = hotelReservationService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelReservation), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,HotelReservationEntity hotelReservation, 
		HttpServletRequest request){
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();

		PageUtils page = hotelReservationService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelReservation), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( HotelReservationEntity hotelReservation){
       	QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
      	ew.allEq(MPUtil.allEQMapPre( hotelReservation, "hotel_reservation")); 
        return R.ok().put("data", hotelReservationService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(HotelReservationEntity hotelReservation){
        QueryWrapper< HotelReservationEntity> ew = new QueryWrapper< HotelReservationEntity>();
 		ew.allEq(MPUtil.allEQMapPre( hotelReservation, "hotel_reservation")); 
		HotelReservationView hotelReservationView =  hotelReservationService.selectView(ew);
		return R.ok("查询酒店预订成功").put("data", hotelReservationView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        HotelReservationEntity hotelReservation = hotelReservationService.getById(id);
        return R.ok().put("data", hotelReservation);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        HotelReservationEntity hotelReservation = hotelReservationService.getById(id);
        return R.ok().put("data", hotelReservation);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody HotelReservationEntity hotelReservation, HttpServletRequest request){
        return createOrder(hotelReservation, request);
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody HotelReservationEntity hotelReservation, HttpServletRequest request){
        return createOrder(hotelReservation, request);
    }

    /**
     * 预订下单。
     *
     * 实名认证是硬前置：酒店订单上要写住宿人，没有认证记录一律不建单。
     * 前端下单页也会先弹认证表单，但那只是体验 —— 接口是公开的，
     * 校验必须落在这里，否则绕开页面直接 POST /hotel_reservation/save 就能订到房。
     *
     * 归属与实名信息全部以服务端为准，请求体里的 userAccount / userName / idCard
     * 一律被覆盖：那三个字段决定了「房是谁订的、谁入住」，让调用方说了算等于可以替别人下单、
     * 或把订单挂在一个假的实名信息上（idCard 上还挂着 @JsonIgnore，连传都传不进来）。
     * 价格与库存不在这里复核（详情接口给的就是库里的值）。
     *
     * 返回值只带 id 与单号：AUTO 主键由 MP 回填，前端拿到 id 才能接着调
     * /user/consumption/pay 在当前页面完成支付（ConsumptionController 的 "hotel" 分支
     * 就是按这个 id 去核 user_account 再改 is_pay 的）。
     *
     * 注：管理端的酒店订单走的是 HotelStaffScopeController 自带的 /hotel_reservation/save，
     * 不经过这里，所以这道闸门拦不到前台员工建单。
     */
    private R createOrder(HotelReservationEntity hotelReservation, HttpServletRequest request) {
        String account = (String) request.getSession().getAttribute("username");
        if (StrUtil.isBlank(account)) {
            return R.error(401, "请先登录");
        }

        RealNameInfo identity = realNameDao.selectByAccount(account);
        // 提交过还不够：审核没过（待审核 / 已驳回）一样不能建单，理由见 RealNameInfo.auditBlockReason
        String block = identity == null ? "预订酒店前请先完成实名认证" : identity.auditBlockReason();
        if (block != null) {
            return R.error(block);
        }

        hotelReservation.setUserAccount(account);
        hotelReservation.setUserName(identity.getRealName());
        hotelReservation.setIdCard(identity.getIdCard());
        // 新单必然是未支付：付款走 /user/consumption/pay，这里收下「已支付」等于白送一间房
        hotelReservation.setIsPay("未支付");

        hotelReservationService.save(hotelReservation);

        /*
         * 只回 id 与单号，不回整条实体：实体里有刚写进去的完整证件号，
         * 而实名接口对外给的从来是脱敏号（UserIdentityVO），这里整条吐出去就破了那个口径。
         */
        Map<String, Object> saved = new HashMap<>();
        saved.put("id", hotelReservation.getId());
        saved.put("reservationNo", hotelReservation.getReservationNo());
        return R.ok().put("data", saved);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody HotelReservationEntity hotelReservation, HttpServletRequest request){
        //ValidatorUtils.validateEntity(hotelReservation);
        hotelReservationService.updateById(hotelReservation);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        hotelReservationService.removeByIds(Arrays.asList(ids));
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
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
		}
        List<Map<String, Object>> result = hotelReservationService.selectValue(params, ew);
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
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = hotelReservationService.selectValue(params, ew);
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
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = hotelReservationService.selectTimeStatValue(params, ew);
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
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = hotelReservationService.selectTimeStatValue(params, ew);
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
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = hotelReservationService.selectGroup(params, ew);
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
    public R count(@RequestParam Map<String, Object> params,HotelReservationEntity hotelReservation, HttpServletRequest request){
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            hotelReservation.setUserAccount((String)request.getSession().getAttribute("username"));
        }
        QueryWrapper<HotelReservationEntity> ew = new QueryWrapper<HotelReservationEntity>();
        long count = hotelReservationService.count(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, hotelReservation), params), params));
        return R.ok().put("data", count);
    }
}