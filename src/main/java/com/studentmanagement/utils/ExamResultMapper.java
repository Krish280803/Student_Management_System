package com.studentmanagement.utils;

import com.studentmanagement.dto.ExamResultRequestDTO;
import com.studentmanagement.dto.ExamResultResponseDTO;
import com.studentmanagement.entity.ExamResult;

public class ExamResultMapper {

    public static ExamResultResponseDTO toResponseDTO(ExamResult entity) {
        if (entity == null) return null;
        String studentName = (entity.getStudent() != null) ? 
                (entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName()) : "N/A";
        String studentNumber = (entity.getStudent() != null) ? entity.getStudent().getStudentNumber() : "N/A";
        String examName = (entity.getExam() != null) ? entity.getExam().getExamName() : "N/A";
        String courseName = (entity.getExam() != null) ? entity.getExam().getCourseName() : "N/A";
        Integer maxMarks = (entity.getExam() != null) ? entity.getExam().getMaxMarks() : 100;

        return ExamResultResponseDTO.builder()
                .id(entity.getId())
                .examId(entity.getExam() != null ? entity.getExam().getId() : null)
                .examName(examName)
                .courseName(courseName)
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .studentName(studentName)
                .studentNumber(studentNumber)
                .marksObtained(entity.getMarksObtained())
                .maxMarks(maxMarks)
                .grade(entity.getGrade())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
