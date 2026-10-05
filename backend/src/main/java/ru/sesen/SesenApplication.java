package ru.sesen.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SesenApplication {
    public static void main(String[] args) {
        SpringApplication.run(SesenApplication.class, args);
    }
}