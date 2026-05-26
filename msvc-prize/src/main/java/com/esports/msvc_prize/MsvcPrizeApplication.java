package com.esports.msvc_prize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcPrizeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcPrizeApplication.class, args);
	}

}
