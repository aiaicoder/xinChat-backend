package com.xin.xinChat.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.digest.MD5;
import com.qcloud.cos.utils.Md5Utils;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.model.enums.UserContactEnum;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Arrays;

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

    /**
     * 获取用户之间的会话id
     * @param userIds
     * @return
     */
    public static String getSessionId(String[] userIds) {
        //先对userIds进行排序
        Arrays.sort(userIds);
        //进行md5加密,保证了唯一姓，即使删除了好友重新添加也不会改变
        return DigestUtils.md5DigestAsHex(StringUtils.join(userIds,"").getBytes());
    }

    /**
     * 获取群组的会话id
     * @param userId
     * @return
     */
    public static String getSessionIdGroup(String userId){
        return DigestUtils.md5DigestAsHex(userId.getBytes());
    }
}
