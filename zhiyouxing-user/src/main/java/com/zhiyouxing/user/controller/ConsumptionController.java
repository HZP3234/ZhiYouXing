package com.zhiyouxing.user.controller;

import cn.hutool.core.util.StrUtil;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.dao.ConsumptionDao;
import com.zhiyouxing.user.entity.UserEntity;
import com.zhiyouxing.user.entity.model.ConsumptionForm;
import com.zhiyouxing.user.entity.vo.ConsumptionVO;
import com.zhiyouxing.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 个人中心：最近消费记录。
 *
 * 门票订单 / 酒店预订 / 报团记录 / 餐厅预约分别在 attraction / hotel / travel / food
 * 四个服务名下，但个人中心归 user 服务管（网关把 /api/user/** 全给它）。四个服务连的是
 * 同一个库，所以这里直接跨表读这四张订单表 —— 代价是表结构耦合，换来的是个人中心
 * 一次请求拿全四类记录，不必让浏览器去扇出四个服务再自己合并。
 *
 * 餐厅预约（restaurant_reservation）是四类里唯一没有支付语义的：那张表既没有
 * is_pay 也没有金额，是一张「订座审核」表，状态走 audit_status，到店结算。
 * 它照常出现在列表里，只是 isPay / isComment 回 null —— 前端据此不显示
 * 「去支付 / 去评价」，改显示审核状态。
 */
@RestController
@RequestMapping("/user/consumption")
public class ConsumptionController {

    /**
     * 未支付订单的时限，分钟。超过它的单子在列表接口里就地翻成「已取消」。
     *
     * 做成常量而不是配置项：演示项目里改超时时间就是改这一行，
     * 多一个配置文件反而要多找一处。
     */
    private static final int PAY_TIMEOUT_MINUTES = 10;

    @Autowired
    private ConsumptionDao consumptionDao;

    @Autowired
    private UserService userService;

    /**
     * 消费记录列表。
     *
     * 四个来源合并成一条按时间倒序的列表，不在服务端分四份 ——
     * 分区的定义（已支付同时是待评价与已评价的并集）放在前端是为了让
     * 标签上的数字、以及用户点开某一个分区时看到的东西，用的是同一份数据。
     *
     * 进来先把超时未付的单子取消掉，再查列表。
     *
     * 为什么用这种「读的时候顺手清一遍」而不是定时任务：超时取消的结果只有
     * 用户自己看得到 —— 他不进个人中心，这单是「未支付」还是「已取消」对谁都
     * 没有区别。为一件只在读取时才有意义的事常驻一个调度线程，还得处理
     * 多实例重复执行，不划算。代价是库里的状态要等下次访问才更新，
     * 这个延迟在个人中心这个场景里没有观察者。
     */
    @RequestMapping("/list")
    public R list(HttpServletRequest request) {
        String account = currentAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }

        expireOverdueOrders(account);

        List<ConsumptionVO> rows = new ArrayList<>();
        rows.addAll(consumptionDao.selectTicketOrders(account));
        rows.addAll(consumptionDao.selectHotelReservations(account));
        rows.addAll(consumptionDao.selectGroupTours(account));
        rows.addAll(consumptionDao.selectRestaurantReservations(account));
        rows.sort(ConsumptionController::newestFirst);
        rows.forEach(this::fillExpireAt);

        return R.ok().put("data", rows);
    }

    /**
     * 去支付。
     *
     * 没有接支付渠道，这一步只把 is_pay 翻成「已支付」——
     * 订单表里本来就只有这一个状态位，没有流水号、没有支付时间。
     *
     * 请求体只说明「付哪一单」：这单属不属于当前账号，由 WHERE 里的 user_account
     * 判（影响 0 行就报错）。原先还要求提交一个与账号绑定的手机号来核对，
     * 那是把支付前的一道自证变成了必填表单 —— 演示里没人愿意为了付一次款
     * 先去个人中心补号码，已去掉。
     *
     * 付之前先清一遍超时的：付款那句 UPDATE 只认「未支付」，不先取消的话，
     * 一张其实已经超时的单子会以「订单不存在 / 已支付 / 已取消」这种含糊理由被拒 ——
     * 用户看不懂自己明明刚下的单为什么付不了。先取消再拦，至少库里是对的。
     */
    @RequestMapping("/pay")
    public R pay(@RequestBody ConsumptionForm form, HttpServletRequest request) {
        String account = currentAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }
        if (form.getId() == null) {
            return R.error("参数不完整");
        }

        expireOverdueOrders(account);

        int updated;
        if ("ticket".equals(form.getSource())) {
            updated = consumptionDao.payTicketOrder(form.getId(), account);
        } else if ("hotel".equals(form.getSource())) {
            updated = consumptionDao.payHotelReservation(form.getId(), account);
        } else if ("group".equals(form.getSource())) {
            updated = consumptionDao.payGroupTour(form.getId(), account);
        } else {
            return R.error("未知的记录类型");
        }

        /*
         * 0 行的三种可能（不存在 / 不是本人的 / 已经付过或取消了）合并成一句，
         * 不区分：前两种区分开等于给出「这个单号存在」的信息，而第三种
         * 用户自己心里有数（超时了，或者刚点过取消）。
         */
        return updated > 0 ? R.ok() : R.error("订单不存在、已支付或已取消，无法支付");
    }

    /**
     * 取消订单（用户主动）。
     *
     * 和超时取消落到同一个状态位、同一句 SQL 判据，区别只是「谁触发的」：
     * 这里点的是具体一行，不是按时间扫。
     */
    @RequestMapping("/cancel")
    public R cancel(@RequestBody ConsumptionForm form, HttpServletRequest request) {
        String account = currentAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }
        if (form.getId() == null) {
            return R.error("参数不完整");
        }

        int updated;
        if ("ticket".equals(form.getSource())) {
            updated = consumptionDao.cancelTicketOrder(form.getId(), account);
        } else if ("hotel".equals(form.getSource())) {
            updated = consumptionDao.cancelHotelReservation(form.getId(), account);
        } else if ("group".equals(form.getSource())) {
            updated = consumptionDao.cancelGroupTour(form.getId(), account);
        } else {
            return R.error("未知的记录类型");
        }

        // 付过款的单不在此列：取消那句 UPDATE 只认「未支付」，已支付的会是 0 行
        return updated > 0 ? R.ok() : R.error("订单不存在、已支付或已取消，无法取消");
    }

    /**
     * 去评价。
     *
     * 评价写进详情页在用的那几张评论表（attraction_comment / hotel_comment /
     * travel_route_comment），所以提交完在景点、酒店、线路的评论区能看到同一条 ——
     * 而不是只把订单标成「已评价」了事。
     */
    @RequestMapping("/comment")
    @Transactional
    public R comment(@RequestBody ConsumptionForm form, HttpServletRequest request) {
        String account = currentAccount(request);
        Long userId = currentUserId(request);
        if (account == null || userId == null) {
            return R.error(401, "请先登录");
        }
        if (form.getId() == null) {
            return R.error("参数不完整");
        }
        if (StrUtil.isBlank(form.getContent())) {
            return R.error("评价内容不能为空");
        }

        UserEntity user = userService.getById(userId);
        if (user == null) {
            return R.error("账号不存在");
        }
        String nickname = StrUtil.isBlank(user.getUserName()) ? account : user.getUserName();
        String content = form.getContent().trim();

        Long refId;
        int marked;
        if ("ticket".equals(form.getSource())) {
            refId = consumptionDao.selectTicketRefId(form.getId(), account);
            if (refId == null) {
                return R.error("这张门票订单不在你名下，或它对应的景点已经下架");
            }
            marked = consumptionDao.markTicketOrderCommented(form.getId(), account);
            if (marked == 0) {
                return R.error("这张订单已经评价过了");
            }
            consumptionDao.insertAttractionComment(refId, userId, user.getAvatar(), nickname, content, form.getScore());
        } else if ("hotel".equals(form.getSource())) {
            refId = consumptionDao.selectHotelRefId(form.getId(), account);
            if (refId == null) {
                return R.error("这笔预订不在你名下，或它对应的房型已经下架");
            }
            marked = consumptionDao.markHotelReservationCommented(form.getId(), account);
            if (marked == 0) {
                return R.error("这笔预订已经评价过了");
            }
            consumptionDao.insertHotelComment(refId, userId, user.getAvatar(), nickname, content, form.getScore());
        } else if ("group".equals(form.getSource())) {
            refId = consumptionDao.selectGroupRefId(form.getId(), account);
            if (refId == null) {
                return R.error("这条报团记录不在你名下，或它对应的线路已经下架");
            }
            marked = consumptionDao.markGroupTourCommented(form.getId(), account);
            if (marked == 0) {
                return R.error("这条报团记录已经评价过了");
            }
            // travel_route_comment 没有 score 列，报团的评价只有文字
            consumptionDao.insertRouteComment(refId, userId, user.getAvatar(), nickname, content);
        } else {
            return R.error("未知的记录类型");
        }

        return R.ok();
    }

    /*
     * 登录态取账号。
     * 前端拿的是「手机号即账号」，AuthorizationInterceptor 把 token 里的 username
     * 放进了 session，和订单表的 user_account 是同一个值。
     */
    private String currentAccount(HttpServletRequest request) {
        String account = (String) request.getSession().getAttribute("username");
        return StrUtil.isBlank(account) ? null : account;
    }

    /** 把当前账号下所有超时未付的单子翻成「已取消」。没有超时的就是三条空 UPDATE */
    private void expireOverdueOrders(String account) {
        consumptionDao.expireTicketOrders(account, PAY_TIMEOUT_MINUTES);
        consumptionDao.expireHotelReservations(account, PAY_TIMEOUT_MINUTES);
        consumptionDao.expireGroupTours(account, PAY_TIMEOUT_MINUTES);
    }

    /*
     * 给参与支付的三种记录算出截止时刻，下发绝对毫秒数给前端倒计时用。
     * 餐厅是到店结算，不参与支付，也就没有「过期」这回事，留给它 null。
     */
    private void fillExpireAt(ConsumptionVO row) {
        if ("restaurant".equals(row.getSource()) || row.getAddTime() == null) {
            return;
        }
        row.setExpireAt(row.getAddTime().getTime() + PAY_TIMEOUT_MINUTES * 60_000L);
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute("user_id");
    }

    /** 按 add_time 倒序，没有时间的沉到最后（不返回 null 在前，免得空值顶掉真实数据） */
    private static int newestFirst(ConsumptionVO a, ConsumptionVO b) {
        Date x = a.getAddTime();
        Date y = b.getAddTime();
        if (x == null) {
            return y == null ? 0 : 1;
        }
        if (y == null) {
            return -1;
        }
        return y.compareTo(x);
    }
}
