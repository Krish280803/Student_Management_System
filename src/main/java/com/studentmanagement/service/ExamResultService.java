package com.studentmanagement.service;

import com.studentmanagement.dto.ExamResultRequestDTO;
import com.studentmanagement.dto.ExamResultResponseDTO;

import java.util.List;

public interface ExamResultService {
    ExamResultResponseDTO saveOrUpdateResult(ExamResultRequestDTO dto);
    ExamResultResponseDTO getResultById(Long id);
    List<ExamResultResponseDTO> getResultsByExamId(Long examId);
    List<ExamResultResponseDTO> getResultsByStudentId(Long studentId);
    void deleteResult(Long id);
    void restoreResult(Long id);
}
