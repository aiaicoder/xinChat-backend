package com.xin.xinChat.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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

}
