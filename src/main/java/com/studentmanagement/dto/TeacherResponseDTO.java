package com.studentmanagement.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponseDTO {

    private Long id;
    private String teacherNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    
    // Relation attributes
    private Long departmentId;
    private String departmentCode;
    private String departmentName;

    private String photoPath;

    // Audit metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
