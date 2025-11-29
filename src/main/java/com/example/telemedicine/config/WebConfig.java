package com.example.telemedicine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC, Model View Controller, configuration class for handling CORS
 * (Cross-Origin Resource Sharing) in the telemedicine application.
 * Configures which origins, HTTP methods and headers are allowed to access the server
 * from a web frontend running on a different domain or port.
 */
@Configuration
public class WebConfig {

    /**
     * Configures CORS mappings for the application.
     * Allows multiple localhost and HTTPS origins and permits credentials.
     * Supports CRUD methods such as GET, POST, PUT, DELETE and OPTIONS.
     * @return WebMvcConfigurer that applies the CORS configuration.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://127.0.0.1:5500",
                                "http://localhost:5500",
                                "http://127.0.0.1:5501",
                                "http://localhost:5501",
                                "https://localhost:3000",
                                "https://127.0.0.1:3000",
                                "https://127.0.0.1:5500",
                                "https://localhost:5500",
                                "https://127.0.0.1:5501",
                                "https://localhost:5501",
                                "https://127.0.0.1:8443",
                                "https://localhost:8443",
                                "https://127.0.0.1:5501",
                                "https://localhost:63342"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}