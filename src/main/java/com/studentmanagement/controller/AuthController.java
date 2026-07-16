package com.studentmanagement.controller;

import com.studentmanagement.dto.AuthResponseDTO;
import com.studentmanagement.dto.LoginRequestDTO;
import com.studentmanagement.dto.RegisterRequestDTO;
import com.studentmanagement.entity.User;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Interface", description = "Endpoints for login, register and role verification exchanges")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and return JWT Bearer Token")
    @ApiResponse(responseCode = "200", description = "Successfully logged in, JWT token returned")
    @ApiResponse(responseCode = "401", description = "Invalid credentials credentials error")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Authentication request received for username: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Fetch User role
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in repository"));

        log.info("User {} successfully authenticated with role: {}", loginRequest.getUsername(), user.getRole());

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user profile")
    @ApiResponse(responseCode = "201", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Username already exists or validation errors")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO signUpRequest) {
        log.info("User registration request received for username: {}", signUpRequest.getUsername());

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            log.warn("Username is already taken: {}", signUpRequest.getUsername());
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // Standardize Roles
        String normalizedRole = signUpRequest.getRole().toUpperCase();
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(normalizedRole)
                .build();

        userRepository.save(user);

        log.info("User {} successfully registered with role: {}", signUpRequest.getUsername(), normalizedRole);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
}
