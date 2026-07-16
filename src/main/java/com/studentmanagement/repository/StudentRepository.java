package com.studentmanagement.repository;

import com.studentmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Retrieve active student by student number.
     * Soft-deleted students are automatically excluded by Hibernate @SQLRestriction.
     */
    Optional<Student> findByStudentNumber(String studentNumber);

    /**
     * Retrieve active student by email.
     * Soft-deleted students are automatically excluded by Hibernate @SQLRestriction.
     */
    Optional<Student> findByEmail(String email);

    /**
     * Search active students whose last name contains the given string (case-insensitive).
     */
    List<Student> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Retrieve all active students registered under a specific department.
     */
    List<Student> findByDepartmentId(Long departmentId);

    /**
     * Check if an active student exists with the given student number.
     */
    boolean existsByStudentNumber(String studentNumber);

    /**
     * Check if an active student exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Bypass Hibernate default filters using a native query to list soft-deleted students.
     * Utilized for administration recovery tools.
     */
    @Query(value = "SELECT * FROM students WHERE is_deleted = 1", nativeQuery = true)
    List<Student> findSoftDeletedStudents();

    /**
     * Restore a soft-deleted student record by resetting the is_deleted flag and deleted_at timestamp.
     */
    @Modifying
    @Query(value = "UPDATE students SET is_deleted = 0, deleted_at = NULL WHERE id = ?", nativeQuery = true)
    void restoreStudentById(Long id);
}
