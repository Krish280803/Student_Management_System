package com.studentmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRequestDTO {

    @NotBlank(message = "Exam name is required")
    @Size(max = 100, message = "Exam name must not exceed 100 characters")
    private String examName;

    @NotBlank(message = "Course name is required")
    @Size(max = 100, message = "Course name must not exceed 100 characters")
    private String courseName;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotBlank(message = "Room is required")
    @Size(max = 50, message = "Room must not exceed 50 characters")
    private String room;

    @NotNull(message = "Max marks is required")
    @Min(value = 1, message = "Max marks must be at least 1")
    private Integer maxMarks;
}
