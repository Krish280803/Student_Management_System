package com.studentmanagement.service;

import com.studentmanagement.dto.StudentRequestDTO;
import com.studentmanagement.dto.StudentResponseDTO;

import java.util.List;

public interface StudentService {

    /**
     * Create a new student after validating that student number and email are not duplicate.
     * @param request the student details
     * @return the created student response details
     */
    StudentResponseDTO createStudent(StudentRequestDTO request);

    /**
     * Retrieve active student details by database primary key.
     * @param id the database ID
     * @return the student response details
     */
    StudentResponseDTO getStudentById(Long id);

    /**
     * Retrieve active student details by registration student number.
     * @param studentNumber the student number
     * @return the student response details
     */
    StudentResponseDTO getStudentByNumber(String studentNumber);

    /**
     * Retrieve active student details by email.
     * @param email the email address
     * @return the student response details
     */
    StudentResponseDTO getStudentByEmail(String email);

    /**
     * Retrieve list of all active students.
     * @return list of active students
     */
    List<StudentResponseDTO> getAllStudents();

    /**
     * Search active students whose last name matches a string wildcard (case-insensitive).
     * @param lastName the search keyword
     * @return list of matching active students
     */
    List<StudentResponseDTO> getStudentsByLastName(String lastName);

    /**
     * Search active students enrolled under a specific department.
     * @param departmentId the department ID
     * @return list of active students belonging to the department
     */
    List<StudentResponseDTO> getStudentsByDepartment(Long departmentId);

    /**
     * Update an existing active student record after ensuring unique criteria holds.
     * @param id the database ID of the target student
     * @param request updated student details
     * @return the updated student response details
     */
    StudentResponseDTO updateStudent(Long id, StudentRequestDTO request);

    /**
     * Soft delete a student from standard system view.
     * @param id the database ID of the student to delete
     */
    void deleteStudent(Long id);

    /**
     * List all soft-deleted student records.
     * @return list of soft-deleted students
     */
    List<StudentResponseDTO> getSoftDeletedStudents();

    /**
     * Restore a soft-deleted student profile.
     * @param id the database ID of the student to restore
     */
    void restoreStudent(Long id);

    /**
     * Update photo path for a student.
     * @param id the student database ID
     * @param photoPath the file system relative path to the image
     * @return the updated student response details
     */
    StudentResponseDTO updatePhoto(Long id, String photoPath);
}
