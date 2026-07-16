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
@Tag(name = "Teacher Management", description = "Endpoints for managing teacher profiles")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @Operation(summary = "Register a new teacher profile")
    @ApiResponse(responseCode = "201", description = "Teacher successfully created")
    public ResponseEntity<TeacherResponseDTO> createTeacher(@Valid @RequestBody TeacherRequestDTO request) {
        log.info("REST request to register new teacher: {}", request.getTeacherNumber());
        TeacherResponseDTO response = teacherService.createTeacher(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Fetch all active teachers")
    public ResponseEntity<List<TeacherResponseDTO>> getAllTeachers() {
        log.info("REST request to fetch all active teachers");
        List<TeacherResponseDTO> response = teacherService.getAllTeachers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch an active teacher by ID")
    public ResponseEntity<TeacherResponseDTO> getTeacherById(@PathVariable Long id) {
        log.info("REST request to fetch teacher by ID: {}", id);
        TeacherResponseDTO response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{teacherNumber}")
    @Operation(summary = "Fetch an active teacher by registration number")
    public ResponseEntity<TeacherResponseDTO> getTeacherByNumber(@PathVariable String teacherNumber) {
        log.info("REST request to fetch teacher by number: {}", teacherNumber);
        TeacherResponseDTO response = teacherService.getTeacherByNumber(teacherNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "List all active teachers by department")
    public ResponseEntity<List<TeacherResponseDTO>> getTeachersByDepartment(@PathVariable Long departmentId) {
        log.info("REST request to fetch teachers for department ID: {}", departmentId);
        List<TeacherResponseDTO> response = teacherService.getTeachersByDepartment(departmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    @Operation(summary = "List all soft-deleted teachers")
    public ResponseEntity<List<TeacherResponseDTO>> getSoftDeletedTeachers() {
        log.info("REST request to fetch all soft-deleted teachers");
        List<TeacherResponseDTO> response = teacherService.getSoftDeletedTeachers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update teacher profile details")
    public ResponseEntity<TeacherResponseDTO> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequestDTO request) {
        log.info("REST request to update teacher ID: {}", id);
        TeacherResponseDTO response = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a teacher")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        log.info("REST request to delete teacher ID: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted teacher")
    public ResponseEntity<Void> restoreTeacher(@PathVariable Long id) {
        log.info("REST request to restore teacher ID: {}", id);
        teacherService.restoreTeacher(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile picture for a teacher")
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
        log.info("Saved file to workspace resources: {}", workspacePath.toAbsolutePath());

        Path targetDir = Paths.get("target/classes/static/uploads");
        if (Files.exists(Paths.get("target/classes/static"))) {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(workspacePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved file to active target classpath directory: {}", targetPath.toAbsolutePath());
        }

        String relativePhotoUrl = "/uploads/" + fileName;
        TeacherResponseDTO response = teacherService.updatePhoto(id, relativePhotoUrl);

        return ResponseEntity.ok(response);
    }
}
