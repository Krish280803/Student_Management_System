package com.studentmanagement.repository;

import com.studentmanagement.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    /**
     * Retrieve active teacher by teacher number.
     */
    Optional<Teacher> findByTeacherNumber(String teacherNumber);

    /**
     * Retrieve active teacher by email.
     */
    Optional<Teacher> findByEmail(String email);

    /**
     * Retrieve all active teachers registered under a specific department.
     */
    List<Teacher> findByDepartmentId(Long departmentId);

    /**
     * Check if an active teacher exists with the given teacher number.
     */
    boolean existsByTeacherNumber(String teacherNumber);

    /**
     * Check if an active teacher exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Bypass Hibernate default filters using a native query to list soft-deleted teachers.
     */
    @Query(value = "SELECT * FROM teachers WHERE is_deleted = 1", nativeQuery = true)
    List<Teacher> findSoftDeletedTeachers();

    /**
     * Restore a soft-deleted teacher record.
     */
    @Modifying
    @Query(value = "UPDATE teachers SET is_deleted = 0, deleted_at = NULL WHERE id = ?", nativeQuery = true)
    void restoreTeacherById(Long id);
}
