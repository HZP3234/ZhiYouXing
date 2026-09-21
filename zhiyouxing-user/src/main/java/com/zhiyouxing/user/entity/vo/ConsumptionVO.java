package com.zhiyouxing.user.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 消费记录（个人中心）。
 *
 * 门票订单 / 酒店预订 / 报团记录 / 餐厅预约散在四张表里，列名各不相同，
 * 但个人中心要展示的是同一种卡片：来源、标题、图、金额、数量、时间、状态。
 * 所以由 ConsumptionDao 的四条 SQL 各自把列名和口径对齐到本类 ——
 * 数量文案（几张 / 几间几晚 / 几人 / 几位）在 SQL 里就拼好，前端不必按来源分支。
 *
 * source 是这张卡片的「出身」，回传时必须原样带回来：去支付和去评价
 * 都要靠它决定改哪张表、写哪张评论表。
 *
 * 餐厅是四类里唯一的例外：它是一张「订座审核」表，没有 is_pay / is_comment，
 * 状态走 audit_status（待审核 / 已通过 / 已驳回），到店结算，不参与在线支付。
 * 所以它的 isPay / isComment 恒为 null，状态由下面两个 audit 字段承载。
 *
 * 也只有餐厅有 reservationTime / remark 两个字段：另外三类订单的「时间」
 * 就是下单那一刻（addTime），而预约真正要看的是「约的哪个时段、有什么要求」。
 * 其余来源这两项为 null，前端按来源分支显示。
 */
public class ConsumptionVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 记录来源：ticket（门票）/ hotel（酒店）/ group（报团）/ restaurant（餐厅预约） */
	private String source;

	/** 来源中文名，直接展示 */
	private String sourceLabel;

	/** 订单表主键。只在「订单表 + 来源」这个组合下唯一，所以前端 key 要带上 source */
	private Long id;

	/** 订单号 / 预订单号。报团表没有这个列，会是 null */
	private String orderNo;

	/** 主标题：景点名 /「酒店 · 房型」/ 线路名 */
	private String title;

	private String image;

	private Double amount;

	/** 已经拼好的数量文案，例如「×2 张」「2 间 · 3 晚」「2 人」 */
	private String quantityDesc;

	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date addTime;

	/** 是否支付：'未支付' / '已支付' / '已取消'（超时或用户主动取消） */
	private String isPay;

	/** 是否评价：'待评价' / '已评价' */
	private String isComment;

	/** 餐厅专用：到店就餐时间。其余来源为 null */
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date reservationTime;

	/** 餐厅专用：用餐备注（忌口、靠窗等）。其余来源为 null */
	private String remark;

	/** 餐厅专用：审核状态 '待审核' / '已通过' / '已驳回'。其余来源为 null */
	private String auditStatus;

	/** 餐厅专用：商家审核回复。其余来源为 null */
	private String auditReply;

	public Date getReservationTime() {
		return reservationTime;
	}

	public void setReservationTime(Date reservationTime) {
		this.reservationTime = reservationTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * 待支付的截止时刻（epoch 毫秒）。= add_time + 超时时长，由 Controller 算好下发。
	 *
	 * 下发绝对时刻而不是「还剩多少秒」：前端每秒要重算一次剩余量，
	 * 给秒数的话得先自己减一遍再倒计时，页面开着不动时钟就走偏了；
	 * 给定点则每次重算都拿当前时间减，刷新不刷新都一样准。
	 *
	 * 只有参与支付的三个来源（门票 / 酒店 / 报团）有值；餐厅是到店结算的
	 * 订座审核，永远不会超时取消，这里给 null，前端据此不显示倒计时。
	 */
	private Long expireAt;

	/*
	 * 五个分区（待支付 / 已支付 / 待评价 / 已评价 / 已取消）全部由这三个布尔分出来。
	 * 不把中文字符串直接扔给前端比对：库里这两列是 varchar、没有约束，
	 * 一旦哪天写成「已付款」，前端就会静默地把它归到「待支付」里去。
	 *
	 * cancelled 必须单独有一个布尔，不能让前端拿 !paid 去推：已取消的单
	 * 也是「没付过款」，只看 paid 的话它会混进「待支付」里 —— 点开一看
	 * 是一张早就作废的单子，还挂着「去支付」。
	 */
	public boolean isPaid() {
		return "已支付".equals(isPay);
	}

	public boolean isCommented() {
		return "已评价".equals(isComment);
	}

	public boolean isCancelled() {
		return "已取消".equals(isPay);
	}

	public Long getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(Long expireAt) {
		this.expireAt = expireAt;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceLabel() {
		return sourceLabel;
	}

	public void setSourceLabel(String sourceLabel) {
		this.sourceLabel = sourceLabel;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getQuantityDesc() {
		return quantityDesc;
	}

	public void setQuantityDesc(String quantityDesc) {
		this.quantityDesc = quantityDesc;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getIsPay() {
		return isPay;
	}

	public void setIsPay(String isPay) {
		this.isPay = isPay;
	}

	public String getIsComment() {
		return isComment;
	}

	public void setIsComment(String isComment) {
		this.isComment = isComment;
	}

	public String getAuditStatus() {
		return auditStatus;
	}

	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}

	public String getAuditReply() {
		return auditReply;
	}

	public void setAuditReply(String auditReply) {
		this.auditReply = auditReply;
	}
}
