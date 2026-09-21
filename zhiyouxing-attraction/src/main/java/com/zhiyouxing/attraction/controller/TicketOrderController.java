package com.zhiyouxing.attraction.controller;

import cn.hutool.core.util.StrUtil;
import com.zhiyouxing.attraction.dao.RealNameDao;
import com.zhiyouxing.attraction.entity.TicketOrderEntity;
import com.zhiyouxing.attraction.entity.view.TicketOrderView;
import com.zhiyouxing.attraction.entity.vo.RealNameInfo;
import com.zhiyouxing.attraction.service.TicketOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 门票订单
 * 后端接口
 */
@RestController
@RequestMapping("/ticket_order")
public class TicketOrderController {
    @Autowired
    private TicketOrderService ticketOrderService;

    /** 只读 user_identity，用来给下单把住「已实名」这道门 */
    @Autowired
    private RealNameDao realNameDao;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TicketOrderEntity ticketOrder,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
			ticketOrder.setUserAccount((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();

		PageUtils page = ticketOrderService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ticketOrder), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 订单列表。
     *
     * 这里原先带 @IgnoreAuth 且不带任何账号条件 —— 等于匿名就能把全库订单连取票人姓名、
     * 手机号一起拉走，比 /detail/{id} 那个洞还大。现在与 /page 同一口径：要登录，
     * 普通用户只看得到自己的单，管理员（table_name 非 user）才拿全量。
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TicketOrderEntity ticketOrder,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
			ticketOrder.setUserAccount((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();

		PageUtils page = ticketOrderService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ticketOrder), params), params));
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TicketOrderEntity ticketOrder){
       	QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
      	ew.allEq(MPUtil.allEQMapPre( ticketOrder, "ticket_order")); 
        return R.ok().put("data", ticketOrderService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TicketOrderEntity ticketOrder){
        QueryWrapper< TicketOrderEntity> ew = new QueryWrapper< TicketOrderEntity>();
 		ew.allEq(MPUtil.allEQMapPre( ticketOrder, "ticket_order")); 
		TicketOrderView ticketOrderView =  ticketOrderService.selectView(ew);
		return R.ok("查询门票订单成功").put("data", ticketOrderView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TicketOrderEntity ticketOrder = ticketOrderService.getById(id);
        return R.ok().put("data", ticketOrder);
    }

    /**
     * 前端详情。
     *
     * 保持免登录 —— 电子票是可能以链接分享出去的 —— 但只回「这单买了什么」，
     * 不回「票是谁的」：userAccount / userName / contactPhone 一律摘掉（证件号另有
     * @JsonIgnore 兜着）。原先整个实体直接吐出去，猜个 id 就能拿到别人的姓名和手机号。
     * 管理端要看取票人信息走 /info/{id}，那个要登录。
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TicketOrderEntity ticketOrder = ticketOrderService.getById(id);
        if (ticketOrder == null) {
            return R.ok().put("data", null);
        }
        Map<String, Object> safe = new LinkedHashMap<>();
        safe.put("id", ticketOrder.getId());
        safe.put("orderNo", ticketOrder.getOrderNo());
        safe.put("attractionName", ticketOrder.getAttractionName());
        safe.put("image", ticketOrder.getImage());
        safe.put("attractionType", ticketOrder.getAttractionType());
        safe.put("ticketPrice", ticketOrder.getTicketPrice());
        safe.put("quantity", ticketOrder.getQuantity());
        safe.put("totalAmount", ticketOrder.getTotalAmount());
        safe.put("purchaseTime", fmtDate(ticketOrder.getPurchaseTime(), "yyyy-MM-dd"));
        safe.put("isPay", ticketOrder.getIsPay());
        safe.put("addTime", fmtDate(ticketOrder.getAddTime(), "yyyy-MM-dd HH:mm:ss"));
        return R.ok().put("data", safe);
    }

    /**
     * 按实体上 @JsonFormat 的口径格式化日期。
     * 一旦改成 Map 返回，实体上的注解就不生效了，日期会退化成 epoch 毫秒 ——
     * 这里手动补上，保证 /detail 的响应形状和以前一致。
     */
    private static String fmtDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sdf.format(date);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TicketOrderEntity ticketOrder, HttpServletRequest request){
        return createOrder(ticketOrder, request);
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TicketOrderEntity ticketOrder, HttpServletRequest request){
        return createOrder(ticketOrder, request);
    }

    /**
     * 下单。
     *
     * 实名认证是硬前置：景区门票按人出票，没有认证记录一律不建单；
     * 提交了但管理员还没审过（或审下来是驳回）同样不建单，
     * 因为票面上的姓名与证件号是从认证记录抄的，没审过就出票等于实名制形同虚设。
     * 前端购票页也会先弹认证表单，但那只是体验 —— 接口是公开的，
     * 校验必须落在这里，否则绕开页面直接 POST /ticket_order/save 就能买到票。
     *
     * 归属与实名信息全部以服务端为准，请求体里的 userAccount / userName / idCard
     * 一律被覆盖：那三个字段决定了「票是谁的」，让调用方说了算等于可以替别人下单、
     * 或把票挂在一个假的实名信息上。价格与库存不在这里复核（详情接口给的就是库里的值）。
     *
     * 返回值只带 id 与订单号：AUTO 主键由 MP 回填，前端拿到 id 才能接着调
     * /user/consumption/pay 完成支付。
     */
    private R createOrder(TicketOrderEntity ticketOrder, HttpServletRequest request) {
        String account = (String) request.getSession().getAttribute("username");
        if (StrUtil.isBlank(account)) {
            return R.error(401, "请先登录");
        }

        RealNameInfo identity = realNameDao.selectByAccount(account);
        String block = identity == null ? "购票前请先完成实名认证" : identity.auditBlockReason();
        if (block != null) {
            return R.error(block);
        }

        ticketOrder.setUserAccount(account);
        ticketOrder.setUserName(identity.getRealName());
        ticketOrder.setIdCard(identity.getIdCard());
        // 新单必然是未支付：付款走 /user/consumption/pay，这里收下「已支付」等于白送一张票
        ticketOrder.setIsPay("未支付");

        ticketOrderService.save(ticketOrder);

        /*
         * 只回 id 与订单号，不回整条实体：实体里有刚写进去的完整证件号，
         * 而实名接口对外给的从来是脱敏号（UserIdentityVO），这里整条吐出去就破了那个口径。
         * 前端要 id 是为了接着调 /user/consumption/pay 付款。
         */
        Map<String, Object> saved = new HashMap<>();
        saved.put("id", ticketOrder.getId());
        saved.put("orderNo", ticketOrder.getOrderNo());
        return R.ok().put("data", saved);
    }





    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TicketOrderEntity ticketOrder, HttpServletRequest request){
        //ValidatorUtils.validateEntity(ticketOrder);
        ticketOrderService.updateById(ticketOrder);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        ticketOrderService.removeByIds(Arrays.asList(ids));
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
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
		}
        List<Map<String, Object>> result = ticketOrderService.selectValue(params, ew);
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
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = ticketOrderService.selectValue(params, ew);
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
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = ticketOrderService.selectTimeStatValue(params, ew);
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
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = ticketOrderService.selectTimeStatValue(params, ew);
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
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ew.eq("user_account", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = ticketOrderService.selectGroup(params, ew);
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
    public R count(@RequestParam Map<String, Object> params,TicketOrderEntity ticketOrder, HttpServletRequest request){
        String tableName = request.getSession().getAttribute("table_name").toString();
        if(tableName.equals("user")) {
            ticketOrder.setUserAccount((String)request.getSession().getAttribute("username"));
        }
        QueryWrapper<TicketOrderEntity> ew = new QueryWrapper<TicketOrderEntity>();
        long count = ticketOrderService.count(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ticketOrder), params), params));
        return R.ok().put("data", count);
    }



}
