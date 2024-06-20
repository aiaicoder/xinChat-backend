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

    String REDIS_KEY_SYS_SETTING = "xinChat:syssetting:";



}
