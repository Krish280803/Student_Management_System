package com.studentmanagement.service;

import com.studentmanagement.dto.StudentRequestDTO;
import com.studentmanagement.dto.StudentResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.DuplicateStudentException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.utils.StudentMapper;
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
@Transactional(readOnly = true) // Optimises read operations by default
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"students", "studentsList"}, allEntries = true)
    public StudentResponseDTO createStudent(StudentRequestDTO request) {
        log.info("Attempting to register a new student with registration number: {}", request.getStudentNumber());

        // 1. Business Validation: Check for duplicates
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            log.warn("Student registration number already exists: {}", request.getStudentNumber());
            throw new DuplicateStudentException("Student number " + request.getStudentNumber() + " is already registered.");
        }
        if (studentRepository.existsByEmail(request.getEmail())) {
            log.warn("Student email address already exists: {}", request.getEmail());
            throw new DuplicateStudentException("Email address " + request.getEmail() + " is already registered.");
        }

        // 2. Fetch parent Department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> {
                    log.warn("Department ID not found: {}", request.getDepartmentId());
                    return new ResourceNotFoundException("Department with ID " + request.getDepartmentId() + " not found.");
                });

        // 3. Map & Save
        Student student = StudentMapper.toEntity(request, department);
        Student savedStudent = studentRepository.save(student);

        log.info("Successfully registered student with ID: {} and registration number: {}", savedStudent.getId(), savedStudent.getStudentNumber());
        return StudentMapper.toResponseDTO(savedStudent);
    }

    @Override
    @Cacheable(value = "students", key = "#id")
    public StudentResponseDTO getStudentById(Long id) {
        log.debug("Fetching student with database ID: {}", id);
        return studentRepository.findById(id)
                .map(StudentMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Student with ID {} not found", id);
                    return new ResourceNotFoundException("Student with ID " + id + " not found.");
                });
    }

    @Override
    @Cacheable(value = "students", key = "#studentNumber")
    public StudentResponseDTO getStudentByNumber(String studentNumber) {
        log.debug("Fetching student with registration number: {}", studentNumber);
        return studentRepository.findByStudentNumber(studentNumber)
                .map(StudentMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Student with number {} not found", studentNumber);
                    return new ResourceNotFoundException("Student with registration number " + studentNumber + " not found.");
                });
    }

    @Override
    @Cacheable(value = "students", key = "#email")
    public StudentResponseDTO getStudentByEmail(String email) {
        log.debug("Fetching student with email address: {}", email);
        return studentRepository.findByEmail(email)
                .map(StudentMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Student with email {} not found", email);
                    return new ResourceNotFoundException("Student with email address " + email + " not found.");
                });
    }

    @Override
    @Cacheable(value = "studentsList")
    public List<StudentResponseDTO> getAllStudents() {
        log.debug("Fetching all active students (Cache Miss - Querying DB)");
        return studentRepository.findAll().stream()
                .map(StudentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponseDTO> getStudentsByLastName(String lastName) {
        log.debug("Fetching students matching last name keyword: {}", lastName);
        return studentRepository.findByLastNameContainingIgnoreCase(lastName).stream()
                .map(StudentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponseDTO> getStudentsByDepartment(Long departmentId) {
        log.debug("Fetching students registered under department ID: {}", departmentId);
        
        // Check department exists
        if (!departmentRepository.existsById(departmentId)) {
            log.warn("Department ID not found: {}", departmentId);
            throw new ResourceNotFoundException("Department with ID " + departmentId + " not found.");
        }

        return studentRepository.findByDepartmentId(departmentId).stream()
                .map(StudentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"students", "studentsList"}, allEntries = true)
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
        log.info("Attempting to update student profile with database ID: {}", id);

        // 1. Fetch target student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Student to update with ID {} not found", id);
                    return new ResourceNotFoundException("Student with ID " + id + " not found.");
                });

        // 2. Validate email changes for duplicates
        if (!student.getEmail().equalsIgnoreCase(request.getEmail()) && 
                studentRepository.existsByEmail(request.getEmail())) {
            log.warn("Cannot update email. New email address already exists: {}", request.getEmail());
            throw new DuplicateStudentException("Email address " + request.getEmail() + " is already registered.");
        }

        // 3. Validate student number changes for duplicates
        if (!student.getStudentNumber().equalsIgnoreCase(request.getStudentNumber()) && 
                studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            log.warn("Cannot update registration number. New number already exists: {}", request.getStudentNumber());
            throw new DuplicateStudentException("Student number " + request.getStudentNumber() + " is already registered.");
        }

        // 4. Fetch department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> {
                    log.warn("Department ID not found: {}", request.getDepartmentId());
                    return new ResourceNotFoundException("Department with ID " + request.getDepartmentId() + " not found.");
                });

        // 5. Update Entity & Flush changes
        StudentMapper.updateEntity(student, request, department);
        Student updatedStudent = studentRepository.save(student);

        log.info("Successfully updated student profile with ID: {}", id);
        return StudentMapper.toResponseDTO(updatedStudent);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"students", "studentsList"}, allEntries = true)
    public void deleteStudent(Long id) {
        log.info("Attempting to delete student with ID: {}", id);
        
        // Verify student exists before deletion
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete aborted. Student with ID {} not found", id);
                    return new ResourceNotFoundException("Student with ID " + id + " not found.");
                });

        studentRepository.delete(student);
        log.info("Successfully soft-deleted student with ID: {}", id);
    }

    @Override
    public List<StudentResponseDTO> getSoftDeletedStudents() {
        log.debug("Fetching list of all soft-deleted students");
        return studentRepository.findSoftDeletedStudents().stream()
                .map(StudentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"students", "studentsList"}, allEntries = true)
    public void restoreStudent(Long id) {
        log.info("Attempting to restore soft-deleted student with ID: {}", id);

        // Verify if the student exists in the soft-deleted list
        List<Student> softDeleted = studentRepository.findSoftDeletedStudents();
        boolean exists = softDeleted.stream().anyMatch(s -> s.getId().equals(id));
        
        if (!exists) {
            log.warn("Restore aborted. Soft-deleted student with ID {} not found", id);
            throw new ResourceNotFoundException("Soft-deleted student with ID " + id + " not found.");
        }

        studentRepository.restoreStudentById(id);
        log.info("Successfully restored student profile with ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"students", "studentsList"}, allEntries = true)
    public StudentResponseDTO updatePhoto(Long id, String photoPath) {
        log.info("Updating photo path for student ID: {} to: {}", id, photoPath);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + id + " not found."));

        student.setPhotoPath(photoPath);
        Student updatedStudent = studentRepository.save(student);
        return StudentMapper.toResponseDTO(updatedStudent);
    }
}
