package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xin.xinChat.model.enums.UserContactEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author 15712
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
    private String contactId;

    /**
     * 会话Id
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 最后收到消息内容
     */
    @TableField(exist = false)
    private String lastMessage;

    /**
     * 最后收到消息时间
     */
    @TableField(exist = false)
    private Long lastReceiveTime;

    /**
     * 如果是群组那么记录成员数量
     */
    @TableField(exist = false)
    private Integer memberCount;


    @TableField(exist = false)
    private String avatar;

    /**
     * 联系人类型，0：好友，1：群组
     */
    @TableField(exist = false)
    private Integer contactType;

    public Integer getContactType() {
        return Objects.requireNonNull(UserContactEnum.getEnumByPrefix(contactId)).getType();

    }


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}