package com.zhiyouxing.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 收藏表
 */
@Data
@TableName("store_up")
public class StoreupEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 关联条目id
     */
    private Long refId;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 名称
     */
    private String name;

    /**
     * 图片
     */
    private String picture;

    /**
     * 类型
     */
    private String type;

    /**
     * 推荐类型
     */
    private String intelType;

    /**
     * 备注
     */
    private String remark;
}
