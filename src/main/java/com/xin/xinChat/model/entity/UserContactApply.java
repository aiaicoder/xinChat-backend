package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xin.xinChat.model.enums.UserContactApplyStatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 联系人申请
 * @author 15712
 * @TableName userContactApply
 */
@TableName(value ="userContactApply")
@Data
public class UserContactApply implements Serializable {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer applyId;

    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 接收人ID
     */
    private String receiveUserId;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private Integer contactType;

    /**
     * 联系人群组ID
     */
    private String contactId;

    /**
     * 最后申请时间
     */
    private Long lastApplyTime;

    /**
     * 状态: 0:待处理 1:已同意 2:已拒绝 3:已拉黑
     */
    private Integer status;

    /**
     * 申请信息
     */
    private String applyInfo;

    /**
     * 申请人姓名
     */
    @TableField(exist = false)
    private String contactName;

    /**
     * 展示对应的状态
     */
    @TableField(exist = false)
    private String statusName;


    @TableField(exist = false)
    private String avatar;

    public String getStatusName() {
        UserContactApplyStatusEnum enumByStatus = UserContactApplyStatusEnum.getEnumByStatus(status);
        return enumByStatus != null ? enumByStatus.getDesc() : null;
    }


    /**
     * 逻辑删除
     */
    @JsonIgnore
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}