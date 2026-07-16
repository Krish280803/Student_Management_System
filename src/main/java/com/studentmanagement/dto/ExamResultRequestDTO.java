package com.studentmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultRequestDTO {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Marks obtained is required")
    @Min(value = 0, message = "Marks obtained must be at least 0")
    private Double marksObtained;

    @NotBlank(message = "Grade is required")
    @Size(max = 5, message = "Grade must not exceed 5 characters")
    private String grade;
}
