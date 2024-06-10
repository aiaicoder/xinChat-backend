package com.xin.xinChat.utils;

import cn.hutool.core.util.RandomUtil;
import com.xin.xinChat.constant.UserConstant;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/10 19:48
 */
public class StringUtil {

    public static String getUserId() {
        return "U" + RandomUtil.randomString(UserConstant.ID_LENGTH);
    }
}
