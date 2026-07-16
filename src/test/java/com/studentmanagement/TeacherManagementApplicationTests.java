package com.studentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.TeacherRequestDTO;
import com.studentmanagement.dto.TeacherResponseDTO;
import com.studentmanagement.exception.DuplicateStudentException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.service.TeacherService;
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
class TeacherManagementApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupDatabase() throws Exception {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/schema.sql"));
        populator.addScript(new ClassPathResource("db/seed.sql"));
        try (Connection conn = dataSource.getConnection()) {
            populator.populate(conn);
        }
    }

    @Test
    void contextLoads() {
        assertNotNull(teacherService, "TeacherService must be injected");
        assertNotNull(validator, "Validator must be injected");
        assertNotNull(mockMvc, "MockMvc must be injected");
        assertNotNull(objectMapper, "ObjectMapper must be injected");
    }

    @Test
    void testTeacherRequestDTOValidation() {
        TeacherRequestDTO validDto = TeacherRequestDTO.builder()
                .teacherNumber("TCH-2026-9999")
                .firstName("Robert")
                .lastName("Martin")
                .email("uncle.bob@cleanarchitecture.com")
                .phone("+1-555-9876")
                .hireDate(LocalDate.of(2015, 12, 5))
                .departmentId(1L)
                .build();

        Set<ConstraintViolation<TeacherRequestDTO>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "Valid DTO should trigger zero validation violations");

        TeacherRequestDTO invalidDto = TeacherRequestDTO.builder()
                .teacherNumber("INVALID-REG-ID")
                .firstName("  ")
                .lastName("Uncle Bob")
                .email("invalidemail.com")
                .phone("123")
                .hireDate(null)
                .departmentId(null)
                .build();

        Set<ConstraintViolation<TeacherRequestDTO>> invalidViolations = validator.validate(invalidDto);
        assertFalse(invalidViolations.isEmpty(), "Invalid DTO parameters must trigger validation errors");
    }

    @Test
    void testTeacherServiceCRUD() {
        TeacherRequestDTO request = TeacherRequestDTO.builder()
                .teacherNumber("TCH-2026-0005")
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace.hopper@cobol.org")
                .phone("+1-555-0909")
                .hireDate(LocalDate.of(1950, 1, 1))
                .departmentId(1L)
                .build();

        TeacherResponseDTO created = teacherService.createTeacher(request);
        assertNotNull(created.getId());
        assertEquals("TCH-2026-0005", created.getTeacherNumber());

        assertThrows(DuplicateStudentException.class, () -> teacherService.createTeacher(request));

        TeacherResponseDTO fetched = teacherService.getTeacherById(created.getId());
        assertEquals("Grace", fetched.getFirstName());

        List<TeacherResponseDTO> list = teacherService.getAllTeachers();
        assertFalse(list.isEmpty());

        request.setFirstName("Grace M.");
        TeacherResponseDTO updated = teacherService.updateTeacher(created.getId(), request);
        assertEquals("Grace M.", updated.getFirstName());

        teacherService.deleteTeacher(created.getId());
        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherById(created.getId()));

        List<TeacherResponseDTO> softDeleted = teacherService.getSoftDeletedTeachers();
        assertTrue(softDeleted.stream().anyMatch(t -> t.getId().equals(created.getId())));

        teacherService.restoreTeacher(created.getId());
        assertNotNull(teacherService.getTeacherById(created.getId()));
    }

    @Test
    void testTeacherControllerEndpoints() throws Exception {
        TeacherRequestDTO request = TeacherRequestDTO.builder()
                .teacherNumber("TCH-2026-1111")
                .firstName("Margaret")
                .lastName("Hamilton")
                .email("margaret.hamilton@nasa.gov")
                .phone("+1-555-1969")
                .hireDate(LocalDate.of(1965, 8, 1))
                .departmentId(1L)
                .build();

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teacherNumber", is("TCH-2026-1111")));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(get("/api/teachers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Alan")));

        mockMvc.perform(delete("/api/teachers/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/teachers/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/api/teachers/1/restore"))
                .andExpect(status().isOk());
    }

    @Test
    void testTeacherExportEndpoints() throws Exception {
        mockMvc.perform(get("/api/teachers/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));

        mockMvc.perform(get("/api/teachers/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));

        mockMvc.perform(get("/api/teachers/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }
}
