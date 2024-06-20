package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName groupInfo
 */
@TableName(value ="groupInfo")
@Data
public class Groupinfo implements Serializable {
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
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}