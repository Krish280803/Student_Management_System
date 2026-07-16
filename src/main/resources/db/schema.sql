-- ============================================================================
-- Sprint 1 & 14: Student Management System Database Schema (DDL)
-- Target Database: MySQL 8.x
-- ============================================================================

-- Disable foreign key checks temporarily to drop tables in any order
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS exam_results;
DROP TABLE IF EXISTS exams;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Table: users
-- Purpose: Holds user authentication records and roles for RBAC
-- ============================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL, -- BCrypt encoded password
    role VARCHAR(30) NOT NULL, -- ROLE_ADMIN, ROLE_STUDENT
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    -- Constraints
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: departments
-- Purpose: Enforces 3NF by normalising departments
-- ============================================================================
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    -- Constraints
    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uq_departments_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: students
-- Purpose: Holds student bio records, contact metadata, and department link
-- ============================================================================
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT,
    student_number VARCHAR(30) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    date_of_birth DATE NOT NULL,
    department_id BIGINT NOT NULL,
    photo_path VARCHAR(255) NULL, -- Photo storage support
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    -- Constraints
    CONSTRAINT pk_students PRIMARY KEY (id),
    CONSTRAINT uq_students_student_number UNIQUE (student_number),
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT fk_students_departments FOREIGN KEY (department_id) 
        REFERENCES departments (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: teachers
-- Purpose: Holds teacher bio records, contact metadata, hire date, and department link
-- ============================================================================
CREATE TABLE teachers (
    id BIGINT AUTO_INCREMENT,
    teacher_number VARCHAR(30) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    hire_date DATE NOT NULL,
    department_id BIGINT NOT NULL,
    photo_path VARCHAR(255) NULL, -- Photo storage support
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    -- Constraints
    CONSTRAINT pk_teachers PRIMARY KEY (id),
    CONSTRAINT uq_teachers_teacher_number UNIQUE (teacher_number),
    CONSTRAINT uq_teachers_email UNIQUE (email),
    CONSTRAINT fk_teachers_departments FOREIGN KEY (department_id) 
        REFERENCES departments (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: courses
-- Purpose: Stores academic course profiles offering credits
-- ============================================================================
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    credits INT NOT NULL,
    department_id BIGINT NOT NULL,
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    -- Constraints
    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT uq_courses_code UNIQUE (code),
    CONSTRAINT fk_courses_departments FOREIGN KEY (department_id) 
        REFERENCES departments (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: enrollments
-- Purpose: Resolves many-to-many relationship between students and courses
-- ============================================================================
CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    grade VARCHAR(5) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED', -- ENROLLED, COMPLETED, DROPPED
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    -- Constraints
    CONSTRAINT pk_enrollments PRIMARY KEY (id),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id, is_deleted),
    CONSTRAINT fk_enrollments_students FOREIGN KEY (student_id) 
        REFERENCES students (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_enrollments_courses FOREIGN KEY (course_id) 
        REFERENCES courses (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ============================================================================

-- Users Table Indexes
CREATE INDEX idx_users_username ON users (username);

-- Students Table Indexes
CREATE INDEX idx_students_last_name ON students (last_name);
CREATE INDEX idx_students_department_id ON students (department_id);

-- Teachers Table Indexes
CREATE INDEX idx_teachers_last_name ON teachers (last_name);
CREATE INDEX idx_teachers_department_id ON teachers (department_id);

-- Courses Table Indexes
CREATE INDEX idx_courses_department_id ON courses (department_id);

-- Enrollments Table Indexes
CREATE INDEX idx_enrollments_student_id ON enrollments (student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);
CREATE INDEX idx_enrollments_status ON enrollments (status);

-- ============================================================================
-- Table: exams
-- Purpose: Stores scheduled exams
-- ============================================================================
CREATE TABLE exams (
    id BIGINT AUTO_INCREMENT,
    exam_name VARCHAR(100) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    exam_date DATE NOT NULL,
    room VARCHAR(50) NOT NULL,
    max_marks INT NOT NULL DEFAULT 100,
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    CONSTRAINT pk_exams PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: exam_results
-- Purpose: Stores student marks and grades for exams
-- ============================================================================
CREATE TABLE exam_results (
    id BIGINT AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    marks_obtained DOUBLE NOT NULL,
    grade VARCHAR(5) NOT NULL,
    
    -- Soft Delete Support
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    
    -- Enterprise Audit Columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    CONSTRAINT pk_exam_results PRIMARY KEY (id),
    CONSTRAINT fk_exam_results_exams FOREIGN KEY (exam_id) 
        REFERENCES exams (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_exam_results_students FOREIGN KEY (student_id) 
        REFERENCES students (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for Exam Department Tables
CREATE INDEX idx_exams_exam_date ON exams (exam_date);
CREATE INDEX idx_exam_results_exam_id ON exam_results (exam_id);
CREATE INDEX idx_exam_results_student_id ON exam_results (student_id);
