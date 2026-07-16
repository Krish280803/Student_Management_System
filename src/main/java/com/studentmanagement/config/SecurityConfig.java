package com.studentmanagement.config;

import com.studentmanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST endpoints
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                // Return 401 Unauthorized for secure endpoints instead of redirecting to login page
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authorizeHttpRequests(authorize -> authorize
                // Allow static resources
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico", "/uploads/**", "/SMS_Architecture_Project_Report.pdf").permitAll()
                // Allow auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Allow system health endpoint
                .requestMatchers("/api/health").permitAll()
                // Allow swagger documentation paths
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Read-only access for students & admins
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/students/**").hasAnyRole("STUDENT", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teachers/**").hasAnyRole("STUDENT", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/exams/**").hasAnyRole("STUDENT", "ADMIN")
                
                // Mutations require admin rights
                .requestMatchers("/api/students/**").hasRole("ADMIN")
                .requestMatchers("/api/teachers/**").hasRole("ADMIN")
                .requestMatchers("/api/exams/**").hasRole("ADMIN")
                
                // Secure all other APIs
                .anyRequest().authenticated()
            );

        // Add our JWT auth interceptor before the standard username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
