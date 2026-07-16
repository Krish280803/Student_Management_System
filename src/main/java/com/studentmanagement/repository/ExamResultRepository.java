package com.studentmanagement.repository;

import com.studentmanagement.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByExamId(Long examId);

    List<ExamResult> findByStudentId(Long studentId);

    Optional<ExamResult> findByExamIdAndStudentId(Long examId, Long studentId);

    @Query(value = "SELECT * FROM exam_results WHERE is_deleted = 1", nativeQuery = true)
    List<ExamResult> findSoftDeletedExamResults();

    @Modifying
    @Query(value = "UPDATE exam_results SET is_deleted = 0, deleted_at = NULL WHERE id = ?", nativeQuery = true)
    void restoreExamResultById(Long id);
}
