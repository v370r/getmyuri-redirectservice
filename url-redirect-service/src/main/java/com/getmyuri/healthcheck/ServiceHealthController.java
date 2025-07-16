package com.getmyuri.healthcheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.getmyuri.constants.ServiceConstants;

@RestController
@RequestMapping
public class ServiceHealthController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceHealthController.class);

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public Map<String, String> checkServices(@RequestParam String service) {
        logger.info("Health check initiated for service(s): {}", service);

        Map<String, String> statusMap = new HashMap<>();
        List<String> servicesToCheck;

        if (ServiceConstants.ALL.equalsIgnoreCase(service.trim())) {
            servicesToCheck = new ArrayList<>(ServiceConstants.KNOWN_SERVICES);
        } else {
            servicesToCheck = Arrays.asList(service.toLowerCase().split(","));
        }

        for (String s : servicesToCheck) {
            switch (s.trim()) {
                case ServiceConstants.REDIS_CLICK:
                    try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                        String ping = connection.ping();
                        String status = "PONG".equalsIgnoreCase(ping) ? "UP" : "DOWN";
                        statusMap.put(ServiceConstants.REDIS_CLICK, status);
                        logger.info("Redis health check: {}", status);
                    } catch (Exception e) {
                        logger.error("Redis health check failed: {}", e.getMessage());
                        statusMap.put(ServiceConstants.REDIS_CLICK, "DOWN");
                    }
                    break;

                case ServiceConstants.MONGO:
                    try {
                        mongoTemplate.executeCommand("{ ping: 1 }");
                        statusMap.put(ServiceConstants.MONGO, "UP");
                        logger.info("MongoDB health check: UP");
                    } catch (Exception e) {
                        logger.error("MongoDB health check failed: {}", e.getMessage());
                        statusMap.put(ServiceConstants.MONGO, "DOWN");
                    }
                    break;
                case ServiceConstants.POSTGRES:
                    try {
                        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                        String status = (result != null && result == 1) ? "UP" : "DOWN";
                        statusMap.put(ServiceConstants.POSTGRES, status);
                        logger.info("PostgreSQL health check: {}", status);
                    } catch (Exception e) {
                        logger.error("PostgreSQL health check failed: {}", e.getMessage());
                        statusMap.put(ServiceConstants.POSTGRES, "DOWN");
                    }
                    break;
                default:
                    logger.warn("Unknown service requested: {}", s.trim());
                    statusMap.put(s.trim(), "UNKNOWN SERVICE");
            }
        }
        logger.info("Health check completed: {}", statusMap);
        return statusMap;
    }

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("getmyuri root is live!");
    }

}
