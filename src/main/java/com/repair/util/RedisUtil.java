package com.repair.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 存
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 存（带过期时间）
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 取
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // 判断是否存在
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}