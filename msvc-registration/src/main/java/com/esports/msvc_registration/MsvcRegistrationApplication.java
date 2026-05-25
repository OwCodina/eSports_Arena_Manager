package com.esports.msvc_registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcRegistrationApplication.class, args);
	}

}
