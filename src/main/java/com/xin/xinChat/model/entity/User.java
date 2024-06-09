package com.xin.xinChat.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息表
 * @author 15712
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId
    private String id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 0:直接加入，1：同意后添加好友
     */
    private Integer joinType;

    /**
     * 1：男,0:女
     */
    private Integer sex;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 地区
     */
    private String areaName;

    /**
     * 地区编码
     */
    private String areaCode;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 最后下线时间
     */
    private Long lastOffTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)//只为了前端和后端的验证，该字段不加在数据库表里面
    private String token;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}