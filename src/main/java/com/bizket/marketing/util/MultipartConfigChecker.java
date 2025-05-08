package com.bizket.marketing.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MultipartConfigChecker implements ApplicationRunner {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;

    private final Logger logger = LoggerFactory.getLogger(MultipartConfigChecker.class);

    @Override
    public void run(ApplicationArguments args) {
        logger.info("★★★★★★★★★★★★★★★★★★★★★ Current max-file-size: {}", maxFileSize);
        logger.info("★★★★★★★★★★★★★★★★★★★★★ Current max-request-size: {}", maxRequestSize);
    }
}