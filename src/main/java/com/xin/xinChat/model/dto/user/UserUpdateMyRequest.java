package com.xin.xinChat.model.dto.user;

import java.io.Serializable;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 用户地区编号
     */
    private Integer areaCode;

    /**
     * 用户地区
     */
    private String areaName;

    /**
     * 好友申请条件
     */
    private Integer joinType;

    private static final long serialVersionUID = 1L;
}