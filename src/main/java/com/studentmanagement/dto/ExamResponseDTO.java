package com.studentmanagement.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResponseDTO {
    private Long id;
    private String examName;
    private String courseName;
    private LocalDate examDate;
    private String room;
    private Integer maxMarks;
    
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
}
