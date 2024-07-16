package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName ChatSessionUser
 */
@TableName(value ="ChatSessionUser")
@Data
public class ChatSessionUser implements Serializable {
    /**
     * 用户Id
     */
    @TableId
    private String userId;

    /**
     * 联系人Id
     */
    @TableId
    private String contactId;

    /**
     * 会话Id
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}