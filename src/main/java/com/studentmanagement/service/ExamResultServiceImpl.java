package com.studentmanagement.service;

import com.studentmanagement.dto.ExamResultRequestDTO;
import com.studentmanagement.dto.ExamResultResponseDTO;
import com.studentmanagement.entity.Exam;
import com.studentmanagement.entity.ExamResult;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.ExamRepository;
import com.studentmanagement.repository.ExamResultRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.utils.ExamResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamResultServiceImpl implements ExamResultService {

    private final ExamResultRepository examResultRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    @CacheEvict(value = "exam_results", allEntries = true)
    public ExamResultResponseDTO saveOrUpdateResult(ExamResultRequestDTO dto) {
        log.info("Recording marks: {} for exam ID: {} and student ID: {}", dto.getMarksObtained(), dto.getExamId(), dto.getStudentId());

        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam with ID " + dto.getExamId() + " not found"));

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + dto.getStudentId() + " not found"));

        Optional<ExamResult> existingResult = examResultRepository.findByExamIdAndStudentId(dto.getExamId(), dto.getStudentId());
        ExamResult result;

        if (existingResult.isPresent()) {
            result = existingResult.get();
            result.setMarksObtained(dto.getMarksObtained());
            result.setGrade(dto.getGrade());
            log.info("Updating existing marks entry ID: {}", result.getId());
        } else {
            result = ExamResult.builder()
                    .exam(exam)
                    .student(student)
                    .marksObtained(dto.getMarksObtained())
                    .grade(dto.getGrade())
                    .build();
            log.info("Creating new marks entry");
        }

        ExamResult saved = examResultRepository.save(result);
        return ExamResultMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "exam_results", key = "#id")
    public ExamResultResponseDTO getResultById(Long id) {
        ExamResult result = examResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam result with ID " + id + " not found"));
        return ExamResultMapper.toResponseDTO(result);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "exam_results", key = "'exam-' + #examId")
    public List<ExamResultResponseDTO> getResultsByExamId(Long examId) {
        log.info("Fetching exam results for exam ID: {}", examId);
        return examResultRepository.findByExamId(examId).stream()
                .map(ExamResultMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "exam_results", key = "'student-' + #studentId")
    public List<ExamResultResponseDTO> getResultsByStudentId(Long studentId) {
        log.info("Fetching exam results for student ID: {}", studentId);
        return examResultRepository.findByStudentId(studentId).stream()
                .map(ExamResultMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "exam_results", allEntries = true)
    public void deleteResult(Long id) {
        log.info("Soft deleting exam result ID: {}", id);
        ExamResult result = examResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam result with ID " + id + " not found"));
        examResultRepository.delete(result);
    }

    @Override
    @Transactional
    @CacheEvict(value = "exam_results", allEntries = true)
    public void restoreResult(Long id) {
        log.info("Restoring soft-deleted exam result ID: {}", id);
        examResultRepository.restoreExamResultById(id);
    }
}
