package com.studentmanagement.utils;

import com.studentmanagement.dto.ExamRequestDTO;
import com.studentmanagement.dto.ExamResponseDTO;
import com.studentmanagement.entity.Exam;

public class ExamMapper {

    public static Exam toEntity(ExamRequestDTO dto) {
        if (dto == null) return null;
        return Exam.builder()
                .examName(dto.getExamName())
                .courseName(dto.getCourseName())
                .examDate(dto.getExamDate())
                .room(dto.getRoom())
                .maxMarks(dto.getMaxMarks())
                .build();
    }

    public static ExamResponseDTO toResponseDTO(Exam entity) {
        if (entity == null) return null;
        return ExamResponseDTO.builder()
                .id(entity.getId())
                .examName(entity.getExamName())
                .courseName(entity.getCourseName())
                .examDate(entity.getExamDate())
                .room(entity.getRoom())
                .maxMarks(entity.getMaxMarks())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
