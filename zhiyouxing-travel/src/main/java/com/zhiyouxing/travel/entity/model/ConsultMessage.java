package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;

/**
 * 咨询会话里的一条消息。
 *
 * <p>**不是数据库实体**，没有 @TableName，也没有对应的表 —— 咨询内容按需求不落库，
 * 只活在 ConsultServiceImpl 的内存会话里，服务重启即清空。前端另存了一份
 * localStorage 历史（见 components/ConsultChat.vue），那是「留存」的实际载体。
 *
 * <p>id 用 UUID 而不是自增序号：服务重启后内存清空、序号会从头开始，
 * 而前端 localStorage 里还留着上一轮的消息 —— 用序号做主键两边会撞车，
 * 合并去重时旧消息会把新消息顶掉。UUID 让两轮消息天然不冲突。
 */
public class ConsultMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 消息id（UUID）。前端按它去重合并内存与 localStorage 两份历史 */
    private String id;

    /** 发送方："user"（游客）或 "guide"（导游） */
    private String from;

    /** 消息正文 */
    private String content;

    /** 发送时间，epoch 毫秒。前端按它排序与算未读 */
    private long time;

    public ConsultMessage() {
    }

    public ConsultMessage(String id, String from, String content, long time) {
        this.id = id;
        this.from = from;
        this.content = content;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
