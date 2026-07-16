package com.studentmanagement.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class HealthController {

    private final DataSource dataSource;
    private final Instant startTime = Instant.now();

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthStatusResponse> checkHealth() {
        log.debug("Received health check request");
        
        boolean dbHealthy = false;
        String dbInfo = "Unknown";
        
        try (Connection conn = dataSource.getConnection()) {
            dbHealthy = true;
            dbInfo = conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion();
        } catch (SQLException e) {
            log.error("Health check failed for Database DataSource", e);
            dbInfo = "Error: " + e.getMessage();
        }

        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptime = rb.getUptime(); // in milliseconds

        HealthStatusResponse response = HealthStatusResponse.builder()
                .status(dbHealthy ? "UP" : "DEGRADED")
                .appName("Student Management System")
                .databaseStatus(dbHealthy ? "CONNECTED" : "DISCONNECTED")
                .databaseDetails(dbInfo)
                .jvmVersion(System.getProperty("java.version"))
                .uptimeMs(uptime)
                .timestamp(Instant.now().toString())
                .build();

        HttpStatus status = dbHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(response, status);
    }

    @Getter
    @Builder
    public static class HealthStatusResponse {
        private final String status;
        private final String appName;
        private final String databaseStatus;
        private final String databaseDetails;
        private final String jvmVersion;
        private final long uptimeMs;
        private final String timestamp;
    }
}
