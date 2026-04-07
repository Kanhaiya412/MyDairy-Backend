package com.MyFarmerApp.MyFarmer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotationConfiguration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GroqConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
