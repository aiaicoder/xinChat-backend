package com.xin.xinChat.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户视图（脱敏）
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 1：男,0:女
     */
    private Integer sex;

    /**
     * 0:直接加入，1：同意后添加好友
     */
    private Integer joinType;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 地区
     */
    private String areaName;

    /**
     * 地区编码
     */
    private String areaCode;


    /**
     * 用户返回的token
     */
    private String token;

    /**
     * 联系人类型
     */
    private Integer contactStatus;


    private static final long serialVersionUID = 1L;
}