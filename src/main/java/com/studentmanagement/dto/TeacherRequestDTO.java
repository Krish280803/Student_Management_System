package com.studentmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRequestDTO {

    @NotBlank(message = "Teacher number is required")
    @Size(max = 30, message = "Teacher number must not exceed 30 characters")
    @Pattern(regexp = "^TCH-\\d{4}-\\d{4}$", message = "Teacher number must match format TCH-YYYY-NNNN (e.g., TCH-2026-0001)")
    private String teacherNumber;

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

    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
