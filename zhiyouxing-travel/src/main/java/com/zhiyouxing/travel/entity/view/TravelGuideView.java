package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.TravelGuideEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;


/**
 * 旅游攻略
 */
@TableName("travel_guide")
public class TravelGuideView  extends TravelGuideEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 这篇攻略的标签名列表（不是表里的列）。
	 *
	 * 为什么不放在实体上：MPUtil.likeOrEq 是裸反射、不认 @TableField(exist = false)，
	 * 而实体会被 /list /lists /query 当成请求参数的绑定目标 —— 实体上只要有一个
	 * 非 String 的字段，匿名请求 ?tagNames=x 就会拼出 `tag_names = ?` 直接 500
	 * （而 /list 是 @IgnoreAuth 的）。View 只出现在 selectListView / selectView
	 * 的返回值位置，永远不是绑定目标，所以放这里安全。
	 *
	 * 只有 /travel_guide/search 与 /travel_guide/detail/{id} 会把它填上，
	 * 别的返回路径上是 null（前端按空数组处理）。
	 */
	private List<String> tagNames;

	public TravelGuideView(){
	}

 	public TravelGuideView(TravelGuideEntity travelGuideEntity){
		BeanUtils.copyProperties(travelGuideEntity, this);

	}

	public List<String> getTagNames() {
		return tagNames;
	}

	public void setTagNames(List<String> tagNames) {
		this.tagNames = tagNames;
	}

}
