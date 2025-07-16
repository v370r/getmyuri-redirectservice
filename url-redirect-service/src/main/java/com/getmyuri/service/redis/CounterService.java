package com.getmyuri.service.redis;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private static final Logger logger = LoggerFactory.getLogger(CounterService.class);
    private static final int BLOCK_SIZE = 1000;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Queue<Long> localCounterQueue = new ConcurrentLinkedQueue<>();

    public synchronized long getNextId() {
        if (localCounterQueue.isEmpty()) {
            long start = redisTemplate.opsForValue().increment("base67:counter", BLOCK_SIZE);
            long end = start - BLOCK_SIZE + 1;

            logger.info(" Redis block fetched: {} to {}", end, start);

            for (long i = end; i <= start; i++) {
                logger.debug("Adding {} to local queue", i);
                localCounterQueue.add(i);
            }
        }

        long nextId = localCounterQueue.poll();
        logger.info(" Serving counter: {}", nextId);
        return nextId;
    }
}
