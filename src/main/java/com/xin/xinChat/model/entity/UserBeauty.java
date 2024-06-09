package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 靓号表
 * @TableName UserBeauty
 */
@TableName(value ="UserBeauty")
@Data
public class UserBeauty implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private String userid;

    /**
     * 0：未使用 1：已使用
     */
    private Integer status;

    /**
     * 用户邮箱
     */
    private String email;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}