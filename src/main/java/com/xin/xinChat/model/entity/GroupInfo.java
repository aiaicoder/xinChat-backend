package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 
 * @author 15712
 * @TableName groupInfo
 */
@TableName(value ="groupInfo")
@Data
public class GroupInfo implements Serializable {
    /**
     * 群聊id
     */
    @TableId
    private String groupId;

    /**
     * 群组名
     */
    private String groupName;

    /**
     * 群主id
     */
    private String groupOwnerId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 0:直接加入，1：管理员同意后加入

     */
    private Integer joinType;

    /**
     * 1:正常，0：解散

     */
    private Integer status;

    /**
     * 用户头像
     */
    private String groupAvatar;

    /**
     * 群员数量
     */
    @TableField(exist = false)
    private long memberCount;

    /**
     * 逻辑删除
     */
    @TableLogic
    @JsonIgnore
    private Integer isDelete;

    /**
     * 群组创建者名字
     */
    @TableField(exist = false)
    private String ownerName;

    /**
     * 群聊状态
     */
    @TableField(exist = false)
    private Integer contactStatus;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}