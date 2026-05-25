package com.esports.msvc_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcNotificationApplication.class, args);
	}

}
