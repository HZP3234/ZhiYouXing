package com.zhiyouxing.user.entity.model;

import java.io.Serializable;

/**
 * 消费记录上两个动作（去支付 / 去评价）的请求体。
 *
 * 两个动作共用一份：都只需要「哪张表的哪一行」，评价再多两个字段。
 * 拆成两个类的话，支付那个会只剩 source + id 两个字段，不值当。
 *
 * 注意这里没有 userAccount：账号一律由 Controller 从登录态取，
 * 请求体里带账号等于把改别人订单的入口开出来。
 */
public class ConsumptionForm implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 记录来源：ticket / hotel / group */
	private String source;

	/** 订单主键 */
	private Long id;

	/** 评分 1~5（评价用）。线路评论表没有评分列，报团的评分会被忽略 */
	private Double score;

	/** 评价内容（评价用） */
	private String content;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
