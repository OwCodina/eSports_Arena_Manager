package com.esports.msvc_result;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcResultApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcResultApplication.class, args);
	}

}
