package com.example.telemedicine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for operator credentials in the telemedicine application.
 * Loads the username and password for the system operator from the application
 * configuration properties (e.g., application.yml or application.properties) using
 * the prefix "operator".
 **/
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "operator")
public class OperatorConfig {
    private String username;
    private String password;
}
