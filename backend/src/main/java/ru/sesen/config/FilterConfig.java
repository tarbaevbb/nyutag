package ru.sesen.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sesen.telegram.TelegramAuthFilter;

import jakarta.servlet.Filter;

@Configuration
public class FilterConfig {
    private final TelegramAuthFilter telegramAuthFilter;

    public FilterConfig(TelegramAuthFilter telegramAuthFilter) {
        this.telegramAuthFilter = telegramAuthFilter;
    }

    @Bean
    public FilterRegistrationBean<Filter> telegramAuthFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(telegramAuthFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}