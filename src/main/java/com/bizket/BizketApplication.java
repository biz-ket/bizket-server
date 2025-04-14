package com.bizket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BizketApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizketApplication.class, args);
    }

}
