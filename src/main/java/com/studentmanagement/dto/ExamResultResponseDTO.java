package com.studentmanagement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultResponseDTO {
    private Long id;
    private Long examId;
    private String examName;
    private String courseName;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Double marksObtained;
    private Integer maxMarks;
    private String grade;
    
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
}
