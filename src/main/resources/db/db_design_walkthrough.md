# Student Management System - Database Design Document

This document details the database architecture designed in **Sprint 1** for the enterprise-grade Student Management System. The design adheres to Relational Database Management Systems (RDBMS) best practices, Third Normal Form (3NF) standards, data security rules, and performance optimization.

---

## 🏛️ Normalization (Third Normal Form - 3NF)

To eliminate data redundancy, update anomalies, and transaction errors, the schema is normalized to **3NF**:

### 1. First Normal Form (1NF)
- Every column contains atomic (indivisible) values.
- There are no repeating groups of columns.
- Each table has a defined primary key (`id`).

### 2. Second Normal Form (2NF)
- The schema is in 1NF.
- All non-key attributes are fully functionally dependent on the primary key.
- Shared attributes (like department details) are factored out into a dedicated `departments` table rather than repeating names and codes inside the `students` or `courses` tables.

### 3. Third Normal Form (3NF)
- The schema is in 2NF.
- There are no transitive dependencies (where a non-key column depends on another non-key column).
- For example, Student attributes depend only on the student primary key. The department relationship is managed strictly via foreign key `department_id`.

---

## 📊 Database Schema Details

The database contains four tables:

### 1. `departments`
Acts as a parent boundary table for college courses and students.
- **Key Columns**: `id` (PK), `code` (Unique Code, e.g., 'CS'), `name` (Department name).

### 2. `students`
Holds profile identifiers and contact information for students.
- **Key Columns**: `id` (PK), `student_number` (Unique registration ID), `first_name`, `last_name`, `email` (Unique), `phone`, `date_of_birth`, `department_id` (FK referencing `departments`).
- **Dependencies**: Depends on `departments`.

### 3. `courses`
Holds curricular course data.
- **Key Columns**: `id` (PK), `code` (Unique, e.g., 'CS101'), `title`, `credits`, `department_id` (FK referencing `departments`).
- **Dependencies**: Depends on `departments`.

### 4. `enrollments`
Resolves the many-to-many relationship between `students` and `courses` (a student enrolls in many courses; a course has many students).
- **Key Columns**: `id` (PK), `student_id` (FK referencing `students`), `course_id` (FK referencing `courses`), `enrollment_date`, `grade` (Nullable), `status` (String enum).
- **Dependencies**: Depends on both `students` and `courses`.

---

## 🛠️ Indexes and Performance Strategy

Database index selection balances select query speed against write performance (inserts/updates).

### Foreign Key Indexes
In MySQL (InnoDB engine), indexes are automatically created for columns defined in foreign key constraints. We explicitly define them in the schema script to guarantee indexing on:
- `students.department_id`
- `courses.department_id`
- `enrollments.student_id`
- `enrollments.course_id`

*Reasoning*: These columns are repeatedly used in `JOIN` statements (e.g., matching students to departments or fetching enrollments). Indexing prevents full-table scans.

### Search Indexes
- **`idx_students_last_name`**: Created on `students(last_name)`.
  *Reasoning*: A common administrative operation is searching students by surname.
- **`idx_enrollments_status`**: Created on `enrollments(status)`.
  *Reasoning*: Useful for filtering active/completed enrollments and compiling academic reports.

---

## 🧹 Soft Delete Strategy

To preserve audit history and prevent data loss, physical deletion is prohibited for key business entities. Instead, a **Soft Delete** pattern is implemented:

1. **Columns**:
   - `is_deleted TINYINT(1) DEFAULT 0` (Boolean flag: `0` for active, `1` for deleted)
   - `deleted_at TIMESTAMP NULL DEFAULT NULL` (Timestamp marking deletion time)
   
2. **Implementation Example**:
   To delete student #1:
   ```sql
   UPDATE students 
   SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP 
   WHERE id = 1;
   ```
   
3. **Fetching Active Records**:
   All standard queries must filter out deleted items:
   ```sql
   SELECT * FROM students WHERE is_deleted = 0;
   ```

4. **Unique Constraints & Soft Delete**:
   A common issue with soft deletes is unique constraints (e.g., student enrolls in a course, drops it (soft delete), and attempts to re-enroll). If a unique constraint is defined as `UNIQUE(student_id, course_id)`, the second enrollment will fail because the soft-deleted record is still present.
   - **Solution**: We define the unique constraint in the `enrollments` table as:
     `CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id, is_deleted)`
     This allows only **one active** enrollment (where `is_deleted = 0`) at a time. If an enrollment is soft-deleted (setting `is_deleted = 1`), a new active enrollment (`is_deleted = 0`) can be created without violating the constraint.

---

## ⏱️ Audit Trails (Enterprise Logging)

Each table contains four audit fields to track resource updates:
- `created_at`: Records database insertion time.
- `created_by`: Identifies the actor or API service that generated the record.
- `updated_at`: Automatically updates to the current timestamp whenever the row is modified.
- `updated_by`: Identifies the actor or API service that performed the update.

In the next sprints, Spring Boot's JPA Auditing (`AuditorAware<String>`) will automatically populate these fields using the active Security Context.

---

## 🔮 Future-Ready Design Decisions

1. **Surrogate Keys (`id`)**: BigInt Auto-Increment is used for all tables as the primary key. This isolates physical keys from business key changes (e.g., changing student registration numbers or course codes).
2. **Referential Integrity Constraints**:
   - `ON DELETE RESTRICT` is used for `fk_students_departments` and `fk_courses_departments`. You cannot delete a department if it contains active students or courses.
   - `ON DELETE CASCADE` is used on `fk_enrollments_students`. If a student profile is completely removed, their associated enrollment logs are cascades. (Although soft-deletes are the norm, this provides cascading integrity for hard purges).
