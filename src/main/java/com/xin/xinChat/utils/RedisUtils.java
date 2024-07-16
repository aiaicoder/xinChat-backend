package com.xin.xinChat.utils;

import cn.hutool.core.collection.ListUtil;
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
     * 批量插入
     */
    public void addUserContactBatch(String userId, List<String> userContactList, Long expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForList().leftPushAll(REDIS_USER_CONTACT_KEY + userId, userContactList);
        stringRedisTemplate.expire(REDIS_USER_CONTACT_KEY + userId, expireTime, timeUnit);
    }



    public List<String> getContactList(String userId) {
        List<String> userContact = stringRedisTemplate.opsForList().range(REDIS_USER_CONTACT_KEY + userId, 0, -1);
        return userContact == null ? ListUtil.empty() : userContact;
    }

}
