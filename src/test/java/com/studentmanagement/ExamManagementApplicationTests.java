package com.studentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.ExamRequestDTO;
import com.studentmanagement.dto.ExamResponseDTO;
import com.studentmanagement.dto.ExamResultRequestDTO;
import com.studentmanagement.dto.ExamResultResponseDTO;
import com.studentmanagement.exception.DuplicateStudentException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.service.ExamService;
import com.studentmanagement.service.ExamResultService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
class ExamManagementApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamResultService examResultService;

    @Autowired
    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    @BeforeEach
    void setupDatabase() throws Exception {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) cache.clear();
            });
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/schema.sql"));
        populator.addScript(new ClassPathResource("db/seed.sql"));
        try (Connection conn = dataSource.getConnection()) {
            populator.populate(conn);
        }
    }

    @Test
    void contextLoads() {
        assertNotNull(examService, "ExamService must be injected");
        assertNotNull(examResultService, "ExamResultService must be injected");
        assertNotNull(mockMvc, "MockMvc must be injected");
    }

    @Test
    void testExamRequestDTOValidation() {
        ExamRequestDTO validDto = ExamRequestDTO.builder()
                .examName("Midterm Exam")
                .courseName("Introduction to Java Programming")
                .examDate(LocalDate.of(2026, 4, 15))
                .room("Lab 3")
                .maxMarks(100)
                .build();

        Set<ConstraintViolation<ExamRequestDTO>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "Valid DTO should trigger zero validation violations");

        ExamRequestDTO invalidDto = ExamRequestDTO.builder()
                .examName("")
                .courseName("   ")
                .examDate(null)
                .room("Room 101")
                .maxMarks(0)
                .build();

        Set<ConstraintViolation<ExamRequestDTO>> invalidViolations = validator.validate(invalidDto);
        assertFalse(invalidViolations.isEmpty(), "Invalid DTO parameters must trigger validation errors");
    }

    @Test
    void testExamServiceCRUD() {
        ExamRequestDTO request = ExamRequestDTO.builder()
                .examName("Sprint 10 Quiz")
                .courseName("Principles of Financial Accounting")
                .examDate(LocalDate.of(2026, 5, 2))
                .room("Room 102")
                .maxMarks(50)
                .build();

        ExamResponseDTO created = examService.createExam(request);
        assertNotNull(created.getId());
        assertEquals("Sprint 10 Quiz", created.getExamName());

        assertThrows(DuplicateStudentException.class, () -> examService.createExam(request));

        ExamResponseDTO fetched = examService.getExamById(created.getId());
        assertEquals("Room 102", fetched.getRoom());

        List<ExamResponseDTO> list = examService.getAllExams();
        assertFalse(list.isEmpty());

        request.setRoom("Lab 5");
        ExamResponseDTO updated = examService.updateExam(created.getId(), request);
        assertEquals("Lab 5", updated.getRoom());

        examService.deleteExam(created.getId());
        assertThrows(ResourceNotFoundException.class, () -> examService.getExamById(created.getId()));

        List<ExamResponseDTO> softDeleted = examService.getSoftDeletedExams();
        assertTrue(softDeleted.stream().anyMatch(e -> e.getId().equals(created.getId())));

        examService.restoreExam(created.getId());
        assertNotNull(examService.getExamById(created.getId()));
    }

    @Test
    void testExamResultServiceCRUD() {
        ExamResultRequestDTO request = ExamResultRequestDTO.builder()
                .examId(1L)
                .studentId(3L) // Bob Johnson
                .marksObtained(88.0)
                .grade("A")
                .build();

        ExamResultResponseDTO saved = examResultService.saveOrUpdateResult(request);
        assertNotNull(saved.getId());
        assertEquals(88.0, saved.getMarksObtained());
        assertEquals("Bob Johnson", saved.getStudentName());

        List<ExamResultResponseDTO> results = examResultService.getResultsByExamId(1L);
        assertEquals(3, results.size()); // 2 seeded + 1 added
    }

    @Test
    void testExamControllerEndpoints() throws Exception {
        ExamRequestDTO request = ExamRequestDTO.builder()
                .examName("Lab Quiz 2")
                .courseName("Database Management Systems")
                .examDate(LocalDate.of(2026, 6, 12))
                .room("Room 304")
                .maxMarks(20)
                .build();

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.examName", is("Lab Quiz 2")));

        mockMvc.perform(get("/api/exams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)); // 2 seeded + 1 added

        // Test grade ledger endpoints
        mockMvc.perform(get("/api/exams/1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // John & Jane
    }

    @Test
    void testStudentMarksheetExportPDF() throws Exception {
        mockMvc.perform(get("/api/students/1/marksheet/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PDF_VALUE)));
    }
}
