package com.jts.login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost:4200")
				.allowedMethods("*")  // Autorise toutes les méthodes
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600);  // Cache les options preflight pendant 1 heure
	}
}