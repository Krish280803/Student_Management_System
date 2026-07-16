package com.studentmanagement.repository;

import com.studentmanagement.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByExamNameAndCourseName(String examName, String courseName);

    @Query(value = "SELECT * FROM exams WHERE is_deleted = 1", nativeQuery = true)
    List<Exam> findSoftDeletedExams();

    @Modifying
    @Query(value = "UPDATE exams SET is_deleted = 0, deleted_at = NULL WHERE id = ?", nativeQuery = true)
    void restoreExamById(Long id);
}
