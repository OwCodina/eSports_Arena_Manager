package com.esports.msvc_match;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcMatchApplication.class, args);
	}

}
