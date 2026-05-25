package com.esports.msvc_sanction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcSanctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcSanctionApplication.class, args);
	}

}
