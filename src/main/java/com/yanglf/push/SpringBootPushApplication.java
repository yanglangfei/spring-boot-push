package com.yanglf.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringBootPushApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootPushApplication.class, args);
    }

}
