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

    Optional<Teacher> findByTeacherNumber(String teacherNumber);

    Optional<Teacher> findByEmail(String email);

    List<Teacher> findByLastNameContainingIgnoreCase(String lastName);

    List<Teacher> findByDepartmentId(Long departmentId);

    boolean existsByTeacherNumber(String teacherNumber);

    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM teachers WHERE is_deleted = 1", nativeQuery = true)
    List<Teacher> findSoftDeletedTeachers();

    @Modifying
    @Query(value = "UPDATE teachers SET is_deleted = 0, deleted_at = NULL WHERE id = ?", nativeQuery = true)
    void restoreTeacherById(Long id);
}
