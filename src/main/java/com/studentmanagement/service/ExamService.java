package com.studentmanagement.service;

import com.studentmanagement.dto.ExamRequestDTO;
import com.studentmanagement.dto.ExamResponseDTO;

import java.util.List;

public interface ExamService {
    ExamResponseDTO createExam(ExamRequestDTO dto);
    ExamResponseDTO updateExam(Long id, ExamRequestDTO dto);
    ExamResponseDTO getExamById(Long id);
    List<ExamResponseDTO> getAllExams();
    List<ExamResponseDTO> getSoftDeletedExams();
    void deleteExam(Long id);
    void restoreExam(Long id);
}
