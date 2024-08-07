package com.xin.xinChat.constant;

import com.xin.xinChat.model.enums.UserContactEnum;
import org.springframework.beans.factory.annotation.Value;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * id长度
     */
    int ID_LENGTH = 11;

    /**
     * 默认头像
     */
    String DEFAULT_AVATAR ="https://my-notes-li.oss-cn-beijing.aliyuncs.com/li/4d7582688ef167dd8c910c111f22dae.jpg";

    /**
     * 默认随机生成用户名
     */
    //取后5位
    String DEFAULT_USERNAME = "用户-"+String.valueOf(System.currentTimeMillis()).substring(10);


    /**
     * 默认机器人名称
     */
    String ROBOT_UID = UserContactEnum.USER.getPrefix() + "robot";

    /**
     * 默认申请信息
     */
    String DEFAULT_APPLY_INFO = "我是%s";



    /**
     * 盐值，混淆密码
     */
    String SALT = "xin";

    /**
     * 前三天时间毫秒
     */
    Long THREE_DAYS_MILLIS = 3 * 24 * 60 * 60 * 1000L;
}
