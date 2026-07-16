package com.studentmanagement.service;

import com.studentmanagement.dto.TeacherRequestDTO;
import com.studentmanagement.dto.TeacherResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.exception.DuplicateStudentException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.utils.TeacherMapper;
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
@Transactional(readOnly = true)
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"teachers", "teachersList"}, allEntries = true)
    public TeacherResponseDTO createTeacher(TeacherRequestDTO request) {
        log.info("Attempting to register a new teacher with registration number: {}", request.getTeacherNumber());

        if (teacherRepository.existsByTeacherNumber(request.getTeacherNumber())) {
            log.warn("Teacher registration number already exists: {}", request.getTeacherNumber());
            throw new DuplicateStudentException("Teacher number " + request.getTeacherNumber() + " is already registered.");
        }
        if (teacherRepository.existsByEmail(request.getEmail())) {
            log.warn("Teacher email address already exists: {}", request.getEmail());
            throw new DuplicateStudentException("Email address " + request.getEmail() + " is already registered.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> {
                    log.warn("Department ID not found: {}", request.getDepartmentId());
                    return new ResourceNotFoundException("Department with ID " + request.getDepartmentId() + " not found.");
                });

        Teacher teacher = TeacherMapper.toEntity(request, department);
        Teacher savedTeacher = teacherRepository.save(teacher);

        log.info("Successfully registered teacher with ID: {} and registration number: {}", savedTeacher.getId(), savedTeacher.getTeacherNumber());
        return TeacherMapper.toResponseDTO(savedTeacher);
    }

    @Override
    @Cacheable(value = "teachers", key = "#id")
    public TeacherResponseDTO getTeacherById(Long id) {
        log.debug("Fetching teacher with database ID: {}", id);
        return teacherRepository.findById(id)
                .map(TeacherMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Teacher with ID {} not found", id);
                    return new ResourceNotFoundException("Teacher with ID " + id + " not found.");
                });
    }

    @Override
    @Cacheable(value = "teachers", key = "#teacherNumber")
    public TeacherResponseDTO getTeacherByNumber(String teacherNumber) {
        log.debug("Fetching teacher with registration number: {}", teacherNumber);
        return teacherRepository.findByTeacherNumber(teacherNumber)
                .map(TeacherMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Teacher with number {} not found", teacherNumber);
                    return new ResourceNotFoundException("Teacher with registration number " + teacherNumber + " not found.");
                });
    }

    @Override
    @Cacheable(value = "teachers", key = "#email")
    public TeacherResponseDTO getTeacherByEmail(String email) {
        log.debug("Fetching teacher with email address: {}", email);
        return teacherRepository.findByEmail(email)
                .map(TeacherMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Teacher with email {} not found", email);
                    return new ResourceNotFoundException("Teacher with email address " + email + " not found.");
                });
    }

    @Override
    @Cacheable(value = "teachersList")
    public List<TeacherResponseDTO> getAllTeachers() {
        log.debug("Fetching all active teachers (Cache Miss - Querying DB)");
        return teacherRepository.findAll().stream()
                .map(TeacherMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponseDTO> getTeachersByLastName(String lastName) {
        log.debug("Fetching teachers matching last name keyword: {}", lastName);
        return teacherRepository.findByLastNameContainingIgnoreCase(lastName).stream()
                .map(TeacherMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponseDTO> getTeachersByDepartment(Long departmentId) {
        log.debug("Fetching teachers registered under department ID: {}", departmentId);
        
        if (!departmentRepository.existsById(departmentId)) {
            log.warn("Department ID not found: {}", departmentId);
            throw new ResourceNotFoundException("Department with ID " + departmentId + " not found.");
        }

        return teacherRepository.findByDepartmentId(departmentId).stream()
                .map(TeacherMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachers", "teachersList"}, allEntries = true)
    public TeacherResponseDTO updateTeacher(Long id, TeacherRequestDTO request) {
        log.info("Attempting to update teacher profile with database ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Teacher to update with ID {} not found", id);
                    return new ResourceNotFoundException("Teacher with ID " + id + " not found.");
                });

        if (!teacher.getEmail().equalsIgnoreCase(request.getEmail()) && 
                teacherRepository.existsByEmail(request.getEmail())) {
            log.warn("Cannot update email. New email address already exists: {}", request.getEmail());
            throw new DuplicateStudentException("Email address " + request.getEmail() + " is already registered.");
        }

        if (!teacher.getTeacherNumber().equalsIgnoreCase(request.getTeacherNumber()) && 
                teacherRepository.existsByTeacherNumber(request.getTeacherNumber())) {
            log.warn("Cannot update registration number. New number already exists: {}", request.getTeacherNumber());
            throw new DuplicateStudentException("Teacher number " + request.getTeacherNumber() + " is already registered.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> {
                    log.warn("Department ID not found: {}", request.getDepartmentId());
                    return new ResourceNotFoundException("Department with ID " + request.getDepartmentId() + " not found.");
                });

        TeacherMapper.updateEntity(teacher, request, department);
        Teacher updatedTeacher = teacherRepository.save(teacher);

        log.info("Successfully updated teacher profile with ID: {}", id);
        return TeacherMapper.toResponseDTO(updatedTeacher);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachers", "teachersList"}, allEntries = true)
    public void deleteTeacher(Long id) {
        log.info("Attempting to delete teacher with ID: {}", id);
        
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete aborted. Teacher with ID {} not found", id);
                    return new ResourceNotFoundException("Teacher with ID " + id + " not found.");
                });

        teacherRepository.delete(teacher);
        log.info("Successfully soft-deleted teacher with ID: {}", id);
    }

    @Override
    public List<TeacherResponseDTO> getSoftDeletedTeachers() {
        log.debug("Fetching list of all soft-deleted teachers");
        return teacherRepository.findSoftDeletedTeachers().stream()
                .map(TeacherMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachers", "teachersList"}, allEntries = true)
    public void restoreTeacher(Long id) {
        log.info("Attempting to restore soft-deleted teacher with ID: {}", id);

        List<Teacher> softDeleted = teacherRepository.findSoftDeletedTeachers();
        boolean exists = softDeleted.stream().anyMatch(t -> t.getId().equals(id));
        
        if (!exists) {
            log.warn("Restore aborted. Soft-deleted teacher with ID {} not found", id);
            throw new ResourceNotFoundException("Soft-deleted teacher with ID " + id + " not found.");
        }

        teacherRepository.restoreTeacherById(id);
        log.info("Successfully restored teacher profile with ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachers", "teachersList"}, allEntries = true)
    public TeacherResponseDTO updatePhoto(Long id, String photoPath) {
        log.info("Updating photo path for teacher ID: {} to: {}", id, photoPath);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher with ID " + id + " not found."));

        teacher.setPhotoPath(photoPath);
        Teacher updatedTeacher = teacherRepository.save(teacher);
        return TeacherMapper.toResponseDTO(updatedTeacher);
    }
}
