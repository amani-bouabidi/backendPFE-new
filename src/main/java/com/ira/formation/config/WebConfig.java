package com.ira.formation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/documents/**")
                .addResourceLocations("file:uploads/documents/");

        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:uploads/videos/");
    }
}