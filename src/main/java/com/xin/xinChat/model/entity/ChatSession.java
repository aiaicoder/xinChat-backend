package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName ChatSession
 */
@TableName(value ="ChatSession")
@Data
public class ChatSession implements Serializable {
    /**
     * 会话id
     */
    @TableId
    private String sessionId;

    /**
     * 最后接受的消息
     */
    private String lastMessage;

    /**
     * 最后接受消息时间毫秒
     */
    private Long lastReceiveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}