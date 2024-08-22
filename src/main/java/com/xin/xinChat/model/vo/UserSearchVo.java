package com.xin.xinChat.model.vo;

import com.xin.xinChat.model.enums.UserContactStatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/2 20:04
 */
@Data
public class UserSearchVo implements Serializable {


    private static final long serialVersionUID = 3026125900919499144L;
    // 联系人id
    private String contactId;
    // 联系人或者群组名称
    private String nickName;
    // 联系人类型
    private String contactType;
    // 状态
    private Integer status;
    // 状态名称
    private String statusName;
    // 性别
    private String sex;
    // 地区
    private String areaName;
    //头像
    private String avatar;

    public String getStatusName(){
        UserContactStatusEnum userContactStatusEnum = UserContactStatusEnum.getEnumByStatus(status);
        return userContactStatusEnum == null ? null : userContactStatusEnum.getDesc();
    }



}
