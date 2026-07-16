package com.studentmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "exams")
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE exams SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_name", nullable = false, length = 100)
    private String examName;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "room", nullable = false, length = 50)
    private String room;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;
}
