package com.ktmstages.ktmstages;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class KtmStagesApplication {

	public static void main(String[] args) {
		SpringApplication.run(KtmStagesApplication.class, args);
	}
}
