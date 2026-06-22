package com.esports.msvc_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsvcAuthApplication.class, args);
    }
}
