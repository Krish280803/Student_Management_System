package com.studentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // In a production application, this would fetch details from SecurityContextHolder
        // (e.g. SecurityContextHolder.getContext().getAuthentication().getName()).
        // For Sprint 2 (pre-security), we return a fallback static user identifier.
        return () -> Optional.of("SYSTEM_USER");
    }
}
