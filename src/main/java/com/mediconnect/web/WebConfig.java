package com.mediconnect.web;

import com.mediconnect.service.MediConnectService;
import com.mediconnect.service.PlatformService;
import com.mediconnect.service.JdbcPlatformService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Selects one explicit persistence mode when the application starts. */
@Configuration
public class WebConfig {
    @Bean MediConnectService platformService() {
        String mode = System.getenv().getOrDefault("MEDICONNECT_MODE", "demo").trim().toLowerCase();
        return switch (mode) {
            case "demo" -> new PlatformService();
            case "mysql" -> new JdbcPlatformService();
            default -> throw new IllegalStateException("MEDICONNECT_MODE must be either 'demo' or 'mysql'.");
        };
    }
}
