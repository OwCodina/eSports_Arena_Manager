package com.esports.msvc_team;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcTeamApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcTeamApplication.class, args);
	}

}
