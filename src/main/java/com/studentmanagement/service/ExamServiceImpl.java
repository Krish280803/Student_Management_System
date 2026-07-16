package com.studentmanagement.service;

import com.studentmanagement.dto.ExamRequestDTO;
import com.studentmanagement.dto.ExamResponseDTO;
import com.studentmanagement.entity.Exam;
import com.studentmanagement.exception.DuplicateStudentException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.ExamRepository;
import com.studentmanagement.utils.ExamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;

    @Override
    @Transactional
    @CacheEvict(value = "exams", allEntries = true)
    public ExamResponseDTO createExam(ExamRequestDTO dto) {
        log.info("Attempting to schedule exam: {} for course: {}", dto.getExamName(), dto.getCourseName());
        
        if (examRepository.findByExamNameAndCourseName(dto.getExamName(), dto.getCourseName()).isPresent()) {
            throw new DuplicateStudentException("Exam '" + dto.getExamName() + "' for course '" + dto.getCourseName() + "' is already scheduled.");
        }

        Exam exam = ExamMapper.toEntity(dto);
        Exam saved = examRepository.save(exam);
        log.info("Exam successfully scheduled with ID: {}", saved.getId());
        return ExamMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "exams", allEntries = true)
    public ExamResponseDTO updateExam(Long id, ExamRequestDTO dto) {
        log.info("Updating scheduled exam details for ID: {}", id);
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam with ID " + id + " not found"));

        exam.setExamName(dto.getExamName());
        exam.setCourseName(dto.getCourseName());
        exam.setExamDate(dto.getExamDate());
        exam.setRoom(dto.getRoom());
        exam.setMaxMarks(dto.getMaxMarks());

        Exam updated = examRepository.save(exam);
        return ExamMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "exams", key = "#id")
    public ExamResponseDTO getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam with ID " + id + " not found"));
        return ExamMapper.toResponseDTO(exam);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "exams")
    public List<ExamResponseDTO> getAllExams() {
        log.info("Fetching all active scheduled exams");
        return examRepository.findAll().stream()
                .map(ExamMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponseDTO> getSoftDeletedExams() {
        log.info("Fetching soft deleted exams");
        return examRepository.findSoftDeletedExams().stream()
                .map(ExamMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "exams", allEntries = true)
    public void deleteExam(Long id) {
        log.info("Soft deleting exam with ID: {}", id);
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam with ID " + id + " not found"));
        examRepository.delete(exam);
    }

    @Override
    @Transactional
    @CacheEvict(value = "exams", allEntries = true)
    public void restoreExam(Long id) {
        log.info("Restoring soft-deleted exam with ID: {}", id);
        examRepository.restoreExamById(id);
    }
}
