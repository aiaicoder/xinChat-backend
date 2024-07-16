package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName ChatMessage
 */
@TableName(value ="ChatMessage")
@Data
public class ChatMessage implements Serializable {
    /**
     * 消息自增Id
     */
    @TableId(type = IdType.AUTO)
    private Long messageId;

    /**
     * 会话Id
     */
    private String sessionId;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 发送人Id
     */
    private String sendUserId;

    /**
     * 发送人名称
     */
    private String sendUserName;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 接收人Id
     */
    private String contactId;

    /**
     * 联系人类型：0：单聊，1：群聊
     */
    private Integer contactType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 状态 0：正在发送 1：已发送
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}