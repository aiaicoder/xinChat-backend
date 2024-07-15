package com.xin.xinChat.constant;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/9 12:40
 */
public interface RedisKeyConstant {
    String REDIS_KEY_CHECK_CODE = "xinChat:checkCode:";

    /**
     * 两分钟过期
     */
    Long CHECK_CODE_EXPIRE_TIME = 3L;

    /**
     * 系统设置
     */
    String REDIS_KEY_SYS_SETTING = "xinChat:syssetting:";


    /**
     * 用户联系人
     */
    String REDIS_USER_CONTACT_KEY = "user_contact:";


    /**
     * 心跳时间
     */
    Long REDIS_HEART_BEAT_TIME = 60L;

    /**
     * 用户心跳
     */
    String REDIS_USER_HEART_BEAT_KEY = "user_heart_beat:";



}
