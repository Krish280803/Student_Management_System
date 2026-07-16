package com.studentmanagement.controller;

import com.studentmanagement.dto.ExamRequestDTO;
import com.studentmanagement.dto.ExamResponseDTO;
import com.studentmanagement.dto.ExamResultRequestDTO;
import com.studentmanagement.dto.ExamResultResponseDTO;
import com.studentmanagement.service.ExamService;
import com.studentmanagement.service.ExamResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exam Department Interface", description = "Endpoints for scheduling exams and recording student grades")
public class ExamController {

    private final ExamService examService;
    private final ExamResultService examResultService;

    @GetMapping
    @Operation(summary = "Fetch all active scheduled examinations")
    public ResponseEntity<List<ExamResponseDTO>> getAllExams() {
        log.info("REST request to fetch all active exams");
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed scheduled exam by ID")
    public ResponseEntity<ExamResponseDTO> getExamById(@PathVariable Long id) {
        log.info("REST request to fetch exam by ID: {}", id);
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @PostMapping
    @Operation(summary = "Schedule a new examination (Faculty/Admin only)")
    public ResponseEntity<ExamResponseDTO> createExam(@Valid @RequestBody ExamRequestDTO dto) {
        log.info("REST request to schedule new exam: {}", dto.getExamName());
        ExamResponseDTO created = examService.createExam(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update details of scheduled exam (Faculty/Admin only)")
    public ResponseEntity<ExamResponseDTO> updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequestDTO dto) {
        log.info("REST request to update exam ID: {}", id);
        return ResponseEntity.ok(examService.updateExam(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete scheduled exam from registry (Faculty/Admin only)")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        log.info("REST request to soft delete exam ID: {}", id);
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "Restore soft-deleted exam back to active schedules (Faculty/Admin only)")
    public ResponseEntity<Void> restoreExam(@PathVariable Long id) {
        log.info("REST request to restore exam ID: {}", id);
        examService.restoreExam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deleted")
    @Operation(summary = "Retrieve all soft-deleted exams (Faculty/Admin only)")
    public ResponseEntity<List<ExamResponseDTO>> getSoftDeletedExams() {
        log.info("REST request to fetch soft-deleted exams");
        return ResponseEntity.ok(examService.getSoftDeletedExams());
    }

    // =========================================================================
    // Exam Results & Grades endpoints
    // =========================================================================
    @GetMapping("/{id}/results")
    @Operation(summary = "Retrieve recorded grade results for a specific exam")
    public ResponseEntity<List<ExamResultResponseDTO>> getResultsByExamId(@PathVariable Long id) {
        log.info("REST request to fetch grades for exam ID: {}", id);
        return ResponseEntity.ok(examResultService.getResultsByExamId(id));
    }

    @PostMapping("/results")
    @Operation(summary = "Record or update student exam result grade (Faculty/Admin only)")
    public ResponseEntity<ExamResultResponseDTO> recordResult(@Valid @RequestBody ExamResultRequestDTO dto) {
        log.info("REST request to record exam result grade for student: {}", dto.getStudentId());
        ExamResultResponseDTO saved = examResultService.saveOrUpdateResult(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Retrieve all exam results for a specific student")
    public ResponseEntity<List<ExamResultResponseDTO>> getResultsByStudentId(@PathVariable Long studentId) {
        log.info("REST request to fetch exam results for student ID: {}", studentId);
        return ResponseEntity.ok(examResultService.getResultsByStudentId(studentId));
    }
}
