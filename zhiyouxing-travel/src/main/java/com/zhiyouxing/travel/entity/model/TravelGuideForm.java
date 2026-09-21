package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;
import java.util.List;

/**
 * 写攻略的入参（/travel_guide/save 与 /travel_guide/update 的请求体）。
 *
 * <p>为什么不让正文接口直接收 TravelGuideEntity：
 * <ol>
 *   <li><b>结构上堵死伪造作者。</b> 这里**故意只放作者可填的字段** ——
 *       请求体里塞 userAccount / userName / addTime / 四个计数器是塞不进来的，
 *       Jackson 直接丢弃，比事后 setUserAccount 覆盖更省心（controller 里仍然显式写一次，双保险）。</li>
 *   <li><b>tagNames 不能挂在实体上。</b> MPUtil.likeOrEq 是裸反射、不认 @TableField：
 *       实体会被 /list /lists /query 当请求参数绑定目标，一旦实体上有个非 String 的
 *       tagNames，匿名请求 ?tagNames=x 就会拼出 `tag_names = ?` 直接 500。
 *       所以标签只在写入侧走这个 DTO，读取侧挂在 TravelGuideView 上。</li>
 * </ol>
 *
 * <p>tagNames 的三种语义：null = 不动标签（局部更新时别误清）、
 * 空数组 = 清空标签、非空 = 整体替换。名字里的新标签会被自动创建。
 */
public class TravelGuideForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id。仅 /update 用得上，/save 会强制置空（id 一律由库生成）
     */
    private Long id;

    /**
     * 攻略标题
     */
    private String guideTitle;

    /**
     * 攻略详情（正文，段落式纯文本，空行分段）
     */
    private String guideDetail;

    /**
     * 标签名列表。作者点选的已有标签与随手新写的标签混在一起，服务端按名字去重、缺的自动建
     */
    private List<String> tagNames;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuideTitle() {
        return guideTitle;
    }

    public void setGuideTitle(String guideTitle) {
        this.guideTitle = guideTitle;
    }

    public String getGuideDetail() {
        return guideDetail;
    }

    public void setGuideDetail(String guideDetail) {
        this.guideDetail = guideDetail;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}
