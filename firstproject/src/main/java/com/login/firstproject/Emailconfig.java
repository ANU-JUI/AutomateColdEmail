package com.login.firstproject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Emailconfig {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    /**
     * Provides a SendGrid client usable by the rest of the application.
     * The API key should be supplied via configuration or environment variable.
     */
    @Bean
    public com.sendgrid.SendGrid sendGrid() {
        return new com.sendgrid.SendGrid(apiKey);
    }
}
