package com.studentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.StudentRequestDTO;
import com.studentmanagement.dto.StudentResponseDTO;
import com.studentmanagement.exception.DuplicateStudentException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.service.StudentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
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
@AutoConfigureMockMvc // Configures MockMvc automatically for MVC controller tests
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
class StudentManagementApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StudentService studentService;

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
        assertNotNull(studentService, "StudentService must be injected");
        assertNotNull(validator, "Validator must be injected");
        assertNotNull(mockMvc, "MockMvc must be injected");
        assertNotNull(objectMapper, "ObjectMapper must be injected");
    }

    @Test
    void testStudentRequestDTOValidation() {
        // Create a valid DTO
        StudentRequestDTO validDto = StudentRequestDTO.builder()
                .studentNumber("STD-2026-9999")
                .firstName("Robert")
                .lastName("Martin")
                .email("uncle.bob@cleanarchitecture.com")
                .phone("+1-555-9876")
                .dateOfBirth(LocalDate.of(1952, 12, 5))
                .departmentId(1L)
                .build();

        Set<ConstraintViolation<StudentRequestDTO>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "Valid DTO should trigger zero validation violations");

        // Test Invalid DTO
        StudentRequestDTO invalidDto = StudentRequestDTO.builder()
                .studentNumber("INVALID-REG-ID")
                .firstName("  ")
                .lastName("Uncle Bob")
                .email("invalidemail.com")
                .phone("123")
                .dateOfBirth(LocalDate.now().plusDays(2))
                .departmentId(null)
                .build();

        Set<ConstraintViolation<StudentRequestDTO>> badViolations = validator.validate(invalidDto);
        assertFalse(badViolations.isEmpty(), "Invalid DTO must trigger violations");
    }

    @Test
    void testCreateStudentSuccess() {
        StudentRequestDTO request = StudentRequestDTO.builder()
                .studentNumber("STD-2026-0099")
                .firstName("Grady")
                .lastName("Booch")
                .email("grady.booch@uml.org")
                .phone("+1-555-8888")
                .dateOfBirth(LocalDate.of(1955, 2, 27))
                .departmentId(1L)
                .build();

        StudentResponseDTO response = studentService.createStudent(request);

        assertNotNull(response.getId());
        assertEquals("STD-2026-0099", response.getStudentNumber());
        assertEquals("Grady", response.getFirstName());
        assertEquals("CS", response.getDepartmentCode());
        assertEquals("Computer Science & Engineering", response.getDepartmentName());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void testCreateStudentDuplicateRegistrationFails() {
        StudentRequestDTO request = StudentRequestDTO.builder()
                .studentNumber("STD-2026-0001")
                .firstName("Grady")
                .lastName("Booch")
                .email("grady.booch@uml.org")
                .phone("+1-555-8888")
                .dateOfBirth(LocalDate.of(1955, 2, 27))
                .departmentId(1L)
                .build();

        assertThrows(DuplicateStudentException.class, () -> {
            studentService.createStudent(request);
        }, "Should throw DuplicateStudentException for duplicate registration code");
    }

    @Test
    void testCreateStudentDuplicateEmailFails() {
        StudentRequestDTO request = StudentRequestDTO.builder()
                .studentNumber("STD-2026-0099")
                .firstName("Grady")
                .lastName("Booch")
                .email("john.doe@university.edu")
                .phone("+1-555-8888")
                .dateOfBirth(LocalDate.of(1955, 2, 27))
                .departmentId(1L)
                .build();

        assertThrows(DuplicateStudentException.class, () -> {
            studentService.createStudent(request);
        }, "Should throw DuplicateStudentException for duplicate email address");
    }

    @Test
    void testGetStudentByIdSuccessAndNotFound() {
        StudentResponseDTO response = studentService.getStudentById(1L);
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());

        assertThrows(ResourceNotFoundException.class, () -> {
            studentService.getStudentById(99L);
        }, "Should throw ResourceNotFoundException for invalid primary key search");
    }

    @Test
    void testUpdateStudentSuccess() {
        StudentRequestDTO updateRequest = StudentRequestDTO.builder()
                .studentNumber("STD-2026-0001")
                .firstName("Jonathan")
                .lastName("Doe")
                .email("john.newemail@university.edu")
                .phone("+1-555-7777")
                .dateOfBirth(LocalDate.of(2004, 5, 15))
                .departmentId(2L)
                .build();

        StudentResponseDTO response = studentService.updateStudent(1L, updateRequest);

        assertEquals("Jonathan", response.getFirstName());
        assertEquals("john.newemail@university.edu", response.getEmail());
        assertEquals("+1-555-7777", response.getPhone());
        assertEquals("ME", response.getDepartmentCode());
        assertEquals("Mechanical Engineering", response.getDepartmentName());
    }

    @Test
    void testDeleteAndRestoreFlow() {
        List<StudentResponseDTO> activeStudentsBefore = studentService.getAllStudents();
        assertTrue(activeStudentsBefore.stream().anyMatch(s -> s.getId().equals(1L)));

        List<StudentResponseDTO> deletedBefore = studentService.getSoftDeletedStudents();
        assertFalse(deletedBefore.stream().anyMatch(s -> s.getId().equals(1L)));

        studentService.deleteStudent(1L);

        List<StudentResponseDTO> activeStudentsAfter = studentService.getAllStudents();
        assertFalse(activeStudentsAfter.stream().anyMatch(s -> s.getId().equals(1L)));

        List<StudentResponseDTO> deletedAfter = studentService.getSoftDeletedStudents();
        assertTrue(deletedAfter.stream().anyMatch(s -> s.getId().equals(1L)));

        studentService.restoreStudent(1L);

        List<StudentResponseDTO> activeStudentsFinal = studentService.getAllStudents();
        assertTrue(activeStudentsFinal.stream().anyMatch(s -> s.getId().equals(1L)));
    }

    // =========================================================================
    // REST API CONTROLLER INTEGRATION TESTS (MockMvc)
    // =========================================================================

    @Test
    void testGetAllStudentsAPI() throws Exception {
        mockMvc.perform(get("/api/students")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].studentNumber", is("STD-2026-0001")));
    }

    @Test
    void testGetStudentByIdAPI_Success() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@university.edu")));
    }

    @Test
    void testGetStudentByIdAPI_NotFound() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 99L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Student with ID 99 not found.")));
    }

    @Test
    void testCreateStudentAPI_Success() throws Exception {
        StudentRequestDTO request = StudentRequestDTO.builder()
                .studentNumber("STD-2026-8888")
                .firstName("James")
                .lastName("Gosling")
                .email("james.gosling@java.net")
                .phone("+1-555-0987")
                .dateOfBirth(LocalDate.of(1955, 5, 19))
                .departmentId(1L)
                .build();

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.studentNumber", is("STD-2026-8888")))
                .andExpect(jsonPath("$.firstName", is("James")))
                .andExpect(jsonPath("$.departmentCode", is("CS")));
    }

    @Test
    void testCreateStudentAPI_ValidationErrors() throws Exception {
        // Send a request failing multiple parameters (blank first name, bad email syntax, future DOB)
        StudentRequestDTO badRequest = StudentRequestDTO.builder()
                .studentNumber("STD-BAD-NUMBER") // violates regex STD-YYYY-NNNN
                .firstName(" ") // blank
                .lastName("Gosling")
                .email("bad-email") // invalid email structure
                .phone("+1-5") // invalid phone format (too short)
                .dateOfBirth(LocalDate.now().plusDays(10)) // future date
                .departmentId(null)
                .build();

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed. Please verify submitted parameters.")))
                .andExpect(jsonPath("$.details", hasKey("studentNumber")))
                .andExpect(jsonPath("$.details", hasKey("firstName")))
                .andExpect(jsonPath("$.details", hasKey("email")))
                .andExpect(jsonPath("$.details", hasKey("phone")))
                .andExpect(jsonPath("$.details", hasKey("dateOfBirth")))
                .andExpect(jsonPath("$.details", hasKey("departmentId")));
    }

    @Test
    void testSoftDeleteAndRestoreAPI_Flow() throws Exception {
        // 1. Delete student ID 1 (John Doe)
        mockMvc.perform(delete("/api/students/{id}", 1L))
                .andExpect(status().isNoContent());

        // 2. Try fetching John Doe - should be 404
        mockMvc.perform(get("/api/students/{id}", 1L))
                .andExpect(status().isNotFound());

        // 3. Fetch deleted student list - should show John Doe
        mockMvc.perform(get("/api/students/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));

        // 4. Restore John Doe
        mockMvc.perform(put("/api/students/{id}/restore", 1L))
                .andExpect(status().isOk());

        // 5. Fetch John Doe again - should be 200 OK
        mockMvc.perform(get("/api/students/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAuthLoginAPI_Success() throws Exception {
        com.studentmanagement.dto.LoginRequestDTO loginRequest = com.studentmanagement.dto.LoginRequestDTO.builder()
                .username("admin")
                .password("password")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")));
    }

    @Test
    void testAuthLoginAPI_Failure() throws Exception {
        com.studentmanagement.dto.LoginRequestDTO badRequest = com.studentmanagement.dto.LoginRequestDTO.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testExportExcelAPI_Success() throws Exception {
        mockMvc.perform(get("/api/students/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=students_")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testExportPdfAPI_Success() throws Exception {
        mockMvc.perform(get("/api/students/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=students_")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testExportCsvAPI_Success() throws Exception {
        mockMvc.perform(get("/api/students/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv"))
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=students_")));
    }
}
