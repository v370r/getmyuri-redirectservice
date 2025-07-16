package com.getmyuri.url_redirect_service.config;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexConfig implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexConfig.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void afterPropertiesSet() {
        Index index = new Index()
                .on("expiresAt", Sort.Direction.ASC)
                .expire(0, TimeUnit.SECONDS);
        mongoTemplate.indexOps("links").ensureIndex(index);
        logger.info("TTL index created on 'links' collection for field 'expiresAt' with expiration of 0 seconds");
    }
}
