package com.getmyuri.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisClickService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisClickService.class);

    private static final String CLICK_PREFIX = "click:";

    public void incrementClick(String username, String aliasPath) {
        String key = CLICK_PREFIX + username + ":" + aliasPath;
        redisTemplate.opsForValue().increment(key);
        logger.debug("Incremented click for key: {}", key);
    }

    public Map<String, Long> getAndClearClicks() {
        Set<String> keys = redisTemplate.keys(CLICK_PREFIX + "*");
        Map<String, Long> result = new HashMap<>();

        if (keys != null && !keys.isEmpty()) {
            logger.info("Flushing {} click key(s) from Redis", keys.size());
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    String alias = key.replaceFirst(CLICK_PREFIX, "");
                    result.put(alias, Long.parseLong(value));
                    redisTemplate.delete(key); // remove after flush
                    logger.debug("Flushed and deleted key: {} (count={})", key, value);
                }
            }
        } else {
            logger.info("No click keys found to flush.");
        }

        return result;
    }

    public Map<String, Long> getAllClicks() {
        Set<String> keys = redisTemplate.keys(CLICK_PREFIX + "*");
        Map<String, Long> result = new HashMap<>();

        if (keys != null && !keys.isEmpty()) {
            logger.info("Retrieving {} click key(s) from Redis", keys.size());
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    String alias = key.replaceFirst(CLICK_PREFIX, "");
                    result.put(alias, Long.parseLong(value));
                    logger.debug("Retrieved key: {} (count={})", key, value);
                }
            }
        }else {
            logger.info("No click keys found in Redis.");
        }

        return result;
    }
}
