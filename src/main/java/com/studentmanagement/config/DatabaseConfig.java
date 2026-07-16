package com.studentmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@Configuration
@EnableTransactionManagement
@Slf4j
public class DatabaseConfig {

    @Bean
    public CommandLineRunner databaseConnectionLogger(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                log.info("Database Connection Established Successfully!");
                log.info("Database Product Name: {}", metaData.getDatabaseProductName());
                log.info("Database Product Version: {}", metaData.getDatabaseProductVersion());
                log.info("JDBC Driver Name: {}", metaData.getDriverName());
                log.info("JDBC Driver Version: {}", metaData.getDriverVersion());
            } catch (SQLException e) {
                log.error("CRITICAL ERROR: Failed to connect to the database! Please check your MySQL server status and configuration.", e);
            }
        };
    }
}
