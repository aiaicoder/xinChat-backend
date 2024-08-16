package com.xin.xinChat.constant;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/9 12:40
 */
public interface RedisKeyConstant {

    /**
     *
     */
    String REDIS_KEY_CHECK_CODE = "xinChat:checkCode:";

    /**
     * 两分钟过期
     */
    Long CHECK_CODE_EXPIRE_TIME = 3L;

    /**
     * 系统设置
     */
    String REDIS_KEY_SYS_SETTING = "xinChat:sysSetting:";


    /**
     * 用户联系人
     */
    String REDIS_USER_CONTACT_KEY = "xinChat:userContact:";


    /**
     * 用户信息缓存
     */
    String REDIS_USER_INFO_KEY = "xinChat:user_info:";

    String REDIS_AI_KEY = "xinChat:ai:";


    /**
     * 心跳时间
     */
    Long REDIS_HEART_BEAT_TIME = 10L;

    /**
     * 用户心跳
     */
    String REDIS_USER_HEART_BEAT_KEY = "xinChat:user_heart_beat:";

    String LIMIT_KEY_PREFIX = "xinChat:checkCode:limit:";



}
