package com.studentmanagement.controller;

import com.studentmanagement.dto.TeacherRequestDTO;
import com.studentmanagement.dto.TeacherResponseDTO;
import com.studentmanagement.service.TeacherService;
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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Management", description = "Endpoints for managing teacher profiles, handling soft-deletes and restorations")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @Operation(summary = "Register a new teacher profile", description = "Creates a new teacher record. Validates unique teacher number and email.")
    @ApiResponse(responseCode = "201", description = "Teacher successfully created",
            content = @Content(schema = @Schema(implementation = TeacherResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request validation errors")
    @ApiResponse(responseCode = "409", description = "Teacher number or email already exists")
    public ResponseEntity<TeacherResponseDTO> createTeacher(@Valid @RequestBody TeacherRequestDTO request) {
        log.info("REST request to register new teacher: {}", request.getTeacherNumber());
        TeacherResponseDTO response = teacherService.createTeacher(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Fetch all active teachers", description = "Retrieves all teachers who are not soft-deleted.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active list")
    public ResponseEntity<List<TeacherResponseDTO>> getAllTeachers() {
        log.info("REST request to fetch all active teachers");
        List<TeacherResponseDTO> response = teacherService.getAllTeachers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch an active teacher by primary database ID")
    @ApiResponse(responseCode = "200", description = "Teacher profile found",
            content = @Content(schema = @Schema(implementation = TeacherResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Teacher not found or soft-deleted")
    public ResponseEntity<TeacherResponseDTO> getTeacherById(
            @Parameter(description = "Primary key ID of the teacher") @PathVariable Long id) {
        log.info("REST request to fetch teacher by ID: {}", id);
        TeacherResponseDTO response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{teacherNumber}")
    @Operation(summary = "Fetch an active teacher by unique registration number")
    @ApiResponse(responseCode = "200", description = "Teacher profile found")
    @ApiResponse(responseCode = "404", description = "Teacher number not found or soft-deleted")
    public ResponseEntity<TeacherResponseDTO> getTeacherByNumber(
            @Parameter(description = "Registration teacher number (TCH-YYYY-NNNN)") @PathVariable String teacherNumber) {
        log.info("REST request to fetch teacher by number: {}", teacherNumber);
        TeacherResponseDTO response = teacherService.getTeacherByNumber(teacherNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search active teachers by surname keyword (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully searched teachers")
    public ResponseEntity<List<TeacherResponseDTO>> searchTeachersByLastName(
            @Parameter(description = "Surname/Last name query text") @RequestParam String lastName) {
        log.info("REST request to search teachers by last name containing: {}", lastName);
        List<TeacherResponseDTO> response = teacherService.getTeachersByLastName(lastName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "List all active teachers registered under a specific department")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @ApiResponse(responseCode = "404", description = "Department ID not found")
    public ResponseEntity<List<TeacherResponseDTO>> getTeachersByDepartment(
            @Parameter(description = "Primary key ID of the department") @PathVariable Long departmentId) {
        log.info("REST request to fetch teachers for department ID: {}", departmentId);
        List<TeacherResponseDTO> response = teacherService.getTeachersByDepartment(departmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    @Operation(summary = "List all soft-deleted teacher profiles (Administrative view)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved deleted list")
    public ResponseEntity<List<TeacherResponseDTO>> getSoftDeletedTeachers() {
        log.info("REST request to fetch all soft-deleted teachers");
        List<TeacherResponseDTO> response = teacherService.getSoftDeletedTeachers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing teacher profile details", description = "Modifies teacher profile. Validates unique fields.")
    @ApiResponse(responseCode = "200", description = "Teacher updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request validation errors")
    @ApiResponse(responseCode = "404", description = "Teacher or department not found")
    @ApiResponse(responseCode = "409", description = "Updated email or teacher number already in use")
    public ResponseEntity<TeacherResponseDTO> updateTeacher(
            @Parameter(description = "Primary key ID of the teacher") @PathVariable Long id,
            @Valid @RequestBody TeacherRequestDTO request) {
        log.info("REST request to update teacher ID: {}", id);
        TeacherResponseDTO response = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a teacher profile from the active logs")
    @ApiResponse(responseCode = "204", description = "Teacher soft-deleted successfully (No Content)")
    @ApiResponse(responseCode = "404", description = "Teacher not found")
    public ResponseEntity<Void> deleteTeacher(
            @Parameter(description = "Primary key ID of the teacher to soft delete") @PathVariable Long id) {
        log.info("REST request to delete teacher ID: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted teacher profile back to active status")
    @ApiResponse(responseCode = "200", description = "Teacher profile successfully restored")
    @ApiResponse(responseCode = "404", description = "Teacher ID not found in soft-deleted registers")
    public ResponseEntity<Void> restoreTeacher(
            @Parameter(description = "Primary key ID of the teacher to restore") @PathVariable Long id) {
        log.info("REST request to restore teacher ID: {}", id);
        teacherService.restoreTeacher(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile picture for a teacher")
    @ApiResponse(responseCode = "200", description = "Photo uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Teacher profile ID not found")
    public ResponseEntity<TeacherResponseDTO> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile file) throws IOException {
        log.info("REST request to upload profile photo for teacher ID: {}", id);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image file uploads are supported");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        String fileName = "teacher_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        Path workspaceDir = Paths.get("src/main/resources/static/uploads");
        if (!Files.exists(workspaceDir)) {
            Files.createDirectories(workspaceDir);
        }
        Path workspacePath = workspaceDir.resolve(fileName);
        Files.copy(file.getInputStream(), workspacePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        Path targetDir = Paths.get("target/classes/static/uploads");
        if (Files.exists(Paths.get("target/classes/static"))) {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(workspacePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePhotoUrl = "/uploads/" + fileName;
        TeacherResponseDTO response = teacherService.updatePhoto(id, relativePhotoUrl);

        return ResponseEntity.ok(response);
    }
}
