package com.getmyuri.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Clear the Redis counter (base67:counter or shorturl:counter)
    @PostMapping("/reset-counter")
    public ResponseEntity<String> resetCounter(@RequestParam String secret) {
        logger.info("Received request to reset Redis counter");
        if (!"vetor".equals(secret)) {
            logger.warn("Unauthorized attempt to reset Redis counter");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Unauthorized");
        }
        redisTemplate.opsForValue().set("base67:counter", "0");
        logger.info("Redis counter reset to 0");
        return ResponseEntity.ok("Redis counter reset to 0");
    }

    // Optional: Clear all clicks
    @DeleteMapping("/clear-clicks")
    public ResponseEntity<String> clearClicks() {
        logger.info("Received request to clear Redis click data");
        Set<String> keys = redisTemplate.keys("clicks:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            logger.info("Deleted {} click keys from Redis", keys.size());
        } else {
            logger.info("No click keys found in Redis to delete");
        }

        return ResponseEntity.ok("🧹 Clicks cleared");
    }
}
