package com.xin.xinChat.utils;

import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.model.enums.UserContactEnum;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/10 19:48
 */
public class StringUtil {

    public static String getUserId() {
        return UserContactEnum.USER.getPrefix() + RandomStringUtils.random(UserConstant.ID_LENGTH, false, true);
    }

    public static String getGroupId() {
        return UserContactEnum.GROUP.getPrefix() + RandomStringUtils.random(UserConstant.ID_LENGTH, false, true);
    }

}
