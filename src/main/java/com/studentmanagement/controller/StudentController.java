package com.studentmanagement.controller;

import com.studentmanagement.dto.StudentRequestDTO;
import com.studentmanagement.dto.StudentResponseDTO;
import com.studentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Management", description = "Endpoints for managing student profiles, handling soft-deletes and restorations")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Register a new student profile", description = "Creates a new student record. Validates unique student number and email.")
    @ApiResponse(responseCode = "201", description = "Student successfully created",
            content = @Content(schema = @Schema(implementation = StudentResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request validation errors")
    @ApiResponse(responseCode = "409", description = "Student number or email already exists")
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO request) {
        log.info("REST request to register new student: {}", request.getStudentNumber());
        StudentResponseDTO response = studentService.createStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Fetch all active students", description = "Retrieves all students who are not soft-deleted.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active list")
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        log.info("REST request to fetch all active students");
        List<StudentResponseDTO> response = studentService.getAllStudents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch an active student by primary database ID")
    @ApiResponse(responseCode = "200", description = "Student profile found",
            content = @Content(schema = @Schema(implementation = StudentResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Student not found or soft-deleted")
    public ResponseEntity<StudentResponseDTO> getStudentById(
            @Parameter(description = "Primary key ID of the student") @PathVariable Long id) {
        log.info("REST request to fetch student by ID: {}", id);
        StudentResponseDTO response = studentService.getStudentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{studentNumber}")
    @Operation(summary = "Fetch an active student by unique registration number")
    @ApiResponse(responseCode = "200", description = "Student profile found")
    @ApiResponse(responseCode = "404", description = "Student number not found or soft-deleted")
    public ResponseEntity<StudentResponseDTO> getStudentByNumber(
            @Parameter(description = "Registration student number (STD-YYYY-NNNN)") @PathVariable String studentNumber) {
        log.info("REST request to fetch student by number: {}", studentNumber);
        StudentResponseDTO response = studentService.getStudentByNumber(studentNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search active students by surname keyword (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully searched students")
    public ResponseEntity<List<StudentResponseDTO>> searchStudentsByLastName(
            @Parameter(description = "Surname/Last name query text") @RequestParam String lastName) {
        log.info("REST request to search students by last name containing: {}", lastName);
        List<StudentResponseDTO> response = studentService.getStudentsByLastName(lastName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "List all active students registered under a specific department")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @ApiResponse(responseCode = "404", description = "Department ID not found")
    public ResponseEntity<List<StudentResponseDTO>> getStudentsByDepartment(
            @Parameter(description = "Primary key ID of the department") @PathVariable Long departmentId) {
        log.info("REST request to fetch students for department ID: {}", departmentId);
        List<StudentResponseDTO> response = studentService.getStudentsByDepartment(departmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    @Operation(summary = "List all soft-deleted student profiles (Administrative view)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved deleted list")
    public ResponseEntity<List<StudentResponseDTO>> getSoftDeletedStudents() {
        log.info("REST request to fetch all soft-deleted students");
        List<StudentResponseDTO> response = studentService.getSoftDeletedStudents();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing student profile details", description = "Modifies student profile. Validates unique fields.")
    @ApiResponse(responseCode = "200", description = "Student updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request validation errors")
    @ApiResponse(responseCode = "404", description = "Student or department not found")
    @ApiResponse(responseCode = "409", description = "Updated email or student number already in use")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @Parameter(description = "Primary key ID of the student") @PathVariable Long id,
            @Valid @RequestBody StudentRequestDTO request) {
        log.info("REST request to update student ID: {}", id);
        StudentResponseDTO response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a student profile from the active logs")
    @ApiResponse(responseCode = "204", description = "Student soft-deleted successfully (No Content)")
    @ApiResponse(responseCode = "404", description = "Student not found")
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "Primary key ID of the student to soft delete") @PathVariable Long id) {
        log.info("REST request to delete student ID: {}", id);
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted student profile back to active status")
    @ApiResponse(responseCode = "200", description = "Student profile successfully restored")
    @ApiResponse(responseCode = "404", description = "Student ID not found in soft-deleted registers")
    public ResponseEntity<Void> restoreStudent(
            @Parameter(description = "Primary key ID of the student to restore") @PathVariable Long id) {
        log.info("REST request to restore student ID: {}", id);
        studentService.restoreStudent(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile picture for a student")
    @ApiResponse(responseCode = "200", description = "Photo uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Student profile ID not found")
    public ResponseEntity<StudentResponseDTO> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile file) throws IOException {
        log.info("REST request to upload profile photo for student ID: {}", id);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        // Validate image file content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image file uploads are supported");
        }

        // Determine filename
        String originalFilename = file.getOriginalFilename();
        String extension = "jpg"; // Default
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        String fileName = "student_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        // Path settings
        // Base workspace static directory
        Path workspaceDir = Paths.get("src/main/resources/static/uploads");
        if (!Files.exists(workspaceDir)) {
            Files.createDirectories(workspaceDir);
        }
        Path workspacePath = workspaceDir.resolve(fileName);
        Files.copy(file.getInputStream(), workspacePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved file to workspace resources: {}", workspacePath.toAbsolutePath());

        // Runtime build target directory (so Spring Boot serves it immediately without restarts)
        Path targetDir = Paths.get("target/classes/static/uploads");
        if (Files.exists(Paths.get("target/classes/static"))) {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(workspacePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved file to active target classpath directory: {}", targetPath.toAbsolutePath());
        }

        // Update student profile database mapping
        String relativePhotoUrl = "/uploads/" + fileName;
        StudentResponseDTO response = studentService.updatePhoto(id, relativePhotoUrl);

        return ResponseEntity.ok(response);
    }
}
