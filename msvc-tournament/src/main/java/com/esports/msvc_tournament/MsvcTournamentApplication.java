package com.esports.msvc_tournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcTournamentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcTournamentApplication.class, args);
	}

}
