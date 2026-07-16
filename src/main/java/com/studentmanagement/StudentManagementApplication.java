package com.studentmanagement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@Slf4j
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(StudentManagementApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        if (contextPath.isBlank()) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using localhost as default");
        }

        log.info("""
            
            ----------------------------------------------------------
            \tApplication '{}' is running! Access URLs:
            \tLocal:      {}://localhost:{}{}
            \tExternal:   {}://{}:{}{}
            \tJava:       {} (Vendor: {})
            \tOS:         {} (Arch: {})
            ----------------------------------------------------------
            """,
            env.getProperty("spring.application.name", "Student Management"),
            protocol, serverPort, contextPath,
            protocol, hostAddress, serverPort, contextPath,
            System.getProperty("java.version"), System.getProperty("java.vendor"),
            System.getProperty("os.name"), System.getProperty("os.arch")
        );
    }

    @Bean
    public CommandLineRunner startupVerification() {
        return args -> log.info("Student Management System initialization completed successfully. Ready for operations.");
    }
}
