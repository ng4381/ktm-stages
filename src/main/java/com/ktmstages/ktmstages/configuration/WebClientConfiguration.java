package com.ktmstages.ktmstages.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@org.springframework.context.annotation.Configuration
public class WebClientConfiguration {

    @Value("${order-service.url}")
    private String orderServiceUrl;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(orderServiceUrl)
                .build();
    }

}
