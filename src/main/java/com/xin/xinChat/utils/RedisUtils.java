package com.xin.xinChat.utils;

import cn.hutool.core.collection.ListUtil;
import com.xin.xinChat.model.vo.UserVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.xin.xinChat.constant.RedisKeyConstant.*;


/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/25 19:54
 */
@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;



    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 有过期时间
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit
     */
    public void set(String key, String value, Long expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    /**
     * 不设置过期时间
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }


    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    //下面是自定义操作
    /**
     * 获取心跳时间
     *
     * @param key
     * @return
     */
    public Long getHeartBeatTime(String key) {
        String time = stringRedisTemplate.opsForValue().get(REDIS_USER_HEART_BEAT_KEY + key);
        return Objects.isNull(time) ? null : Long.parseLong(time);
    }

    /**
     * 设置心跳时间
     *
     * @param key
     */
    public void setHeartBeatTime(String key) {
        stringRedisTemplate.opsForValue().set(REDIS_USER_HEART_BEAT_KEY + key, Long.toString(System.currentTimeMillis()), REDIS_HEART_BEAT_TIME, TimeUnit.SECONDS);
    }

    public void removeHeartBeatTime(String userId) {
        //移除心跳
        stringRedisTemplate.delete(REDIS_USER_HEART_BEAT_KEY + userId);
    }


    /**
     * 删除联系人列表
     *
     * @param userId
     */
    public void delUserContact(String userId) {
        stringRedisTemplate.delete(REDIS_USER_CONTACT_KEY + userId);
    }

    /**
     * 删除联系人
     */
    public void delUserContactInfo(String userId, String contactId) {
        stringRedisTemplate.opsForList().remove(REDIS_USER_CONTACT_KEY + userId, 1, contactId);
    }


    /**
     * 批量插入联系人信息
     */
    public void addUserContactBatch(String userId, List<String> userContactList, Long expireTime, TimeUnit timeUnit) {
        Long aLong = stringRedisTemplate.opsForList().leftPushAll(REDIS_USER_CONTACT_KEY + userId, userContactList);
        stringRedisTemplate.expire(REDIS_USER_CONTACT_KEY + userId, expireTime, timeUnit);
    }

    /**
     * 加入联系人信息
     * @param userId
     */
    public void addUserContact(String userId, String contactId,Long expireTime, TimeUnit timeUnit) {
        List<String> contactList = getContactList(userId);
        if (contactList.contains(contactId)) {
            return;
        }
        stringRedisTemplate.opsForList().leftPush(REDIS_USER_CONTACT_KEY + userId, contactId);
        stringRedisTemplate.expire(REDIS_USER_CONTACT_KEY + userId, expireTime, timeUnit);
    }



    public List<String> getContactList(String userId) {
        List<String> userContact = stringRedisTemplate.opsForList().range(REDIS_USER_CONTACT_KEY + userId, 0, -1);
        return userContact == null ? ListUtil.empty() : userContact;
    }

    /**
     * 设置用户信息缓存
     */
    public void setUserInfo(String userId, String userInfo, Long expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(REDIS_USER_INFO_KEY + userId, userInfo, expireTime, timeUnit);
    }


    /**
     * 移除用户信息
     */
    public void removeUserInfo(String userId) {
        stringRedisTemplate.delete(REDIS_USER_INFO_KEY + userId);
    }

    /**
     * 获取用户信息
     */
    public String getUserInfo(String userId) {
        return stringRedisTemplate.opsForValue().get(REDIS_USER_INFO_KEY + userId);
    }

}
