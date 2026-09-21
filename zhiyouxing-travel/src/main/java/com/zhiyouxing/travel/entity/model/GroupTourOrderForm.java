package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;

/**
 * 报团下单的入参（/group_tour/save 与 /group_tour/add 的请求体）。
 *
 * <p>为什么不直接收 GroupTourEntity：订单上除「报几个人、留哪个电话」之外的所有字段
 * 都该由服务端从线路推出来（线路名/图片/费用/出发日期/导游工号）或是从会话推出来
 * （用户账号、用户姓名）。让实体当入参，等于把金额、导游工号、是否已支付这些
 * 决定「这单算谁的、值多少钱」的字段都交给调用方填 —— 这里只放三个字段，
 * 其余的 Jackson 直接丢弃，比事后逐个覆盖更省心（controller 里仍显式写一遍，双保险）。
 *
 * <p>没有 routeId 列在 group_tour 上（订单表按 route_name 关联线路，见
 * ConsumptionDao.selectGroupRefId），所以 routeId 只能作为入参存在，不落库。
 */
public class GroupTourOrderForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 线路id。用来查线路、取费用与出发日期，并校验名额
     */
    private Long routeId;

    /**
     * 报名人数。至少 1 人；上限在 controller 里另有一道（防止一次把名额全占）
     */
    private Integer signupCount;

    /**
     * 联系电话。留空则回落到登录账号（游客账号就是手机号）
     */
    private String contactPhone;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Integer getSignupCount() {
        return signupCount;
    }

    public void setSignupCount(Integer signupCount) {
        this.signupCount = signupCount;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
