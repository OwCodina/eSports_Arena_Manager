package com.esports.msvc_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcUserApplication.class, args);
	}

}
