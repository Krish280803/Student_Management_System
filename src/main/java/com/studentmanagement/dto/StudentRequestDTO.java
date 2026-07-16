package com.studentmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequestDTO {

    @NotBlank(message = "Student number is required")
    @Size(max = 30, message = "Student number must not exceed 30 characters")
    @Pattern(regexp = "^STD-\\d{4}-\\d{4}$", message = "Student number must match format STD-YYYY-NNNN (e.g., STD-2026-0001)")
    private String studentNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email address must be syntactically valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^$|^\\+?[0-9\\-\\s]{7,20}$", message = "Phone must be a valid number (7-20 digits, optional leading +)")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
