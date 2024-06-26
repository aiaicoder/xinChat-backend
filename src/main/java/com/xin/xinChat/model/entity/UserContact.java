package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 联系人表
 * @author 15712
 * @TableName userContact
 */
@TableName(value ="userContact")
@Data
public class UserContact implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 联系人类型 0:好友 1：群组
     */
    private Integer contactType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 状态 0:非好友 1：好友 2：已删除好友，3：被好友删除，4：已拉黑好友，5：被好友拉黑
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}