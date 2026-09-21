package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.TravelRouteDayEntity;
import com.zhiyouxing.travel.entity.TravelRouteEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;


/**
 * 旅游线路
 */
@TableName("travel_route")
public class TravelRouteView  extends TravelRouteEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 这条线路的每日行程（travel_route_day 里 ref_id 指向本级 id 的那些行，按 day 升序）。
	 * **不是 travel_route 的列**，口径与 TravelGuideView.tagNames 一致：
	 *
	 * 为什么不放在实体上：MPUtil.likeOrEq 是裸反射、不认 @TableField(exist = false)，
	 * 而实体会被 /list /lists /query 当成请求参数的绑定目标 —— 实体上多一个非 String
	 * 字段，匿名请求 ?daily=x 就会拼出 `daily = ?` 直接 500（/list 是 @IgnoreAuth 的）。
	 * View 只出现在 selectListView / selectView 的返回值位置，永远不是绑定目标。
	 *
	 * 只有 /travel_route/search 与 /travel_route/detail/{id} 会把它填上，
	 * 别的返回路径上是 null（前端按空数组处理）。
	 */
	private List<TravelRouteDayEntity> daily;

	public TravelRouteView(){
	}

 	public TravelRouteView(TravelRouteEntity travelRouteEntity){
		BeanUtils.copyProperties(travelRouteEntity, this);

	}

	public List<TravelRouteDayEntity> getDaily() {
		return daily;
	}

	public void setDaily(List<TravelRouteDayEntity> daily) {
		this.daily = daily;
	}
}
