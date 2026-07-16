-- ============================================================================
-- Sprint 1 & 14: Student Management System Seed Data (DML)
-- Target Database: MySQL 8.x
-- ============================================================================

-- Disable foreign key checks to allow seeding without strict constraint order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE courses;
TRUNCATE TABLE students;
TRUNCATE TABLE teachers;
TRUNCATE TABLE departments;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Seed Users (Passwords are BCrypt encoded for 'password')
-- ============================================================================
INSERT INTO users (id, username, password, role, created_by, updated_by) VALUES
(1, 'admin', '$2a$10$g/VbrhsO7CpsbnzImceF/OwZ2ZYzzWMbNUm9/TM9S43Ra5mNSeSHK', 'ROLE_ADMIN', 'ADMIN_SEED', 'ADMIN_SEED'),
(2, 'student', '$2a$10$g/VbrhsO7CpsbnzImceF/OwZ2ZYzzWMbNUm9/TM9S43Ra5mNSeSHK', 'ROLE_STUDENT', 'ADMIN_SEED', 'ADMIN_SEED');

-- ============================================================================
-- Seed Departments
-- ============================================================================
INSERT INTO departments (id, code, name, created_by, updated_by) VALUES
(1, 'CS', 'Computer Science & Engineering', 'ADMIN_SEED', 'ADMIN_SEED'),
(2, 'ME', 'Mechanical Engineering', 'ADMIN_SEED', 'ADMIN_SEED'),
(3, 'BA', 'Business Administration', 'ADMIN_SEED', 'ADMIN_SEED');

-- ============================================================================
-- Seed Courses
-- ============================================================================
INSERT INTO courses (id, code, title, credits, department_id, created_by, updated_by) VALUES
(1, 'CS101', 'Introduction to Java Programming', 4, 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(2, 'CS201', 'Database Management Systems', 3, 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(3, 'ME101', 'Applied Thermodynamics', 4, 2, 'ADMIN_SEED', 'ADMIN_SEED'),
(4, 'BA101', 'Principles of Financial Accounting', 3, 3, 'ADMIN_SEED', 'ADMIN_SEED');

-- ============================================================================
-- Seed Students
-- ============================================================================
INSERT INTO students (id, student_number, first_name, last_name, email, phone, date_of_birth, department_id, created_by, updated_by) VALUES
(1, 'STD-2026-0001', 'John', 'Doe', 'john.doe@university.edu', '+1-555-0101', '2004-05-15', 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(2, 'STD-2026-0002', 'Jane', 'Smith', 'jane.smith@university.edu', '+1-555-0102', '2005-09-22', 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(3, 'STD-2026-0003', 'Bob', 'Johnson', 'bob.johnson@university.edu', '+1-555-0102', '2003-11-10', 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(4, 'STD-2026-0004', 'Alice', 'Brown', 'alice.brown@university.edu', '+1-555-0103', '2004-02-28', 3, 'ADMIN_SEED', 'ADMIN_SEED');

-- ============================================================================
-- Seed Teachers
-- ============================================================================
INSERT INTO teachers (id, teacher_number, first_name, last_name, email, phone, specialization, department_id, created_by, updated_by) VALUES
(1, 'TCH-2026-0001', 'Alan', 'Turing', 'alan.turing@university.edu', '+1-555-0201', 'Theory of Computation', 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(2, 'TCH-2026-0002', 'Grace', 'Hopper', 'grace.hopper@university.edu', '+1-555-0202', 'Compiler Design', 1, 'ADMIN_SEED', 'ADMIN_SEED'),
(3, 'TCH-2026-0003', 'Nikola', 'Tesla', 'nikola.tesla@university.edu', '+1-555-0203', 'Electromagnetism', 2, 'ADMIN_SEED', 'ADMIN_SEED'),
(4, 'TCH-2026-0004', 'Adam', 'Smith', 'adam.smith@university.edu', '+1-555-0204', 'Macroeconomics', 3, 'ADMIN_SEED', 'ADMIN_SEED');

-- ============================================================================
-- Seed Enrollments
-- ============================================================================
INSERT INTO enrollments (id, student_id, course_id, enrollment_date, grade, status, created_by, updated_by) VALUES
-- John Doe (CS Student)
(1, 1, 1, '2026-01-10', 'A', 'COMPLETED', 'REGISTRAR', 'REGISTRAR'),
(2, 1, 2, '2026-07-01', NULL, 'ENROLLED', 'REGISTRAR', 'REGISTRAR'),

-- Jane Smith (CS Student)
(3, 2, 1, '2026-07-01', NULL, 'ENROLLED', 'REGISTRAR', 'REGISTRAR'),

-- Bob Johnson (ME Student)
(4, 3, 3, '2026-01-15', 'B+', 'COMPLETED', 'REGISTRAR', 'REGISTRAR'),

-- Alice Brown (BA Student)
(5, 4, 4, '2026-07-01', NULL, 'ENROLLED', 'REGISTRAR', 'REGISTRAR');
