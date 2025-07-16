package com.getmyuri.url_redirect_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.getmyuri")
@EnableJdbcRepositories("com.getmyuri.repository")
@EnableJpaRepositories("com.getmyuri.repository")
@EnableMongoRepositories("com.getmyuri.repository")
@EntityScan("com.getmyuri.model")
@EnableScheduling
public class UrlRedirectServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlRedirectServiceApplication.class, args);
	}

}
