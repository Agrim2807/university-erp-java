-- ============================================================================
-- UNIVERSITY ERP SYSTEM - ERP DATABASE SETUP
-- File: erp_setup.sql
-- Description: Creates and populates the university_erp database
-- Run this script AFTER auth_setup.sql
-- ============================================================================
-- Prerequisites: auth_setup.sql must be executed first
-- ============================================================================

-- ============================================================================
-- DATABASE CREATION
-- ============================================================================
DROP DATABASE IF EXISTS university_erp;
CREATE DATABASE university_erp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE university_erp;

-- ============================================================================
-- TABLE: students
-- Student profile information linked to auth database
-- ============================================================================
CREATE TABLE students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(20) UNIQUE NOT NULL,
    program VARCHAR(100) NOT NULL,
    year INT NOT NULL CHECK (year >= 1 AND year <= 6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_roll_no (roll_no),
    INDEX idx_program (program)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: instructors
-- Instructor profile information linked to auth database
-- ============================================================================
CREATE TABLE instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    office VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_department (department)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: courses
-- Course catalog with code, title, credits
-- ============================================================================
CREATE TABLE courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL CHECK (credits > 0 AND credits <= 6),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_code (code),
    INDEX idx_active (is_active)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: course_prerequisites
-- Prerequisite relationships between courses (Optional feature)
-- ============================================================================
CREATE TABLE course_prerequisites (
    prerequisite_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    requires_course_id INT NOT NULL,

    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (requires_course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_prereq (course_id, requires_course_id),
    CONSTRAINT check_different_courses CHECK (course_id != requires_course_id),

    INDEX idx_course_prereq (course_id),
    INDEX idx_required_course (requires_course_id)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: sections
-- Course sections with schedule, capacity, and MANDATORY deadlines
-- NOTE: add_deadline and drop_deadline are NOT NULL (enforced at DB level)
-- CHECK constraint ensures drop_deadline >= add_deadline for data integrity
-- ============================================================================
CREATE TABLE sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    instructor_id INT NOT NULL,
    section_code VARCHAR(10) NOT NULL,
    day_time VARCHAR(100) NOT NULL,
    room VARCHAR(50),
    capacity INT NOT NULL CHECK (capacity > 0 AND capacity <= 500),
    semester ENUM('Monsoon', 'Winter', 'Summer') NOT NULL,
    year INT NOT NULL,
    enrollment_count INT DEFAULT 0,
    add_deadline DATE NOT NULL,
    drop_deadline DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_deadline_order CHECK (drop_deadline >= add_deadline),

    UNIQUE KEY unique_section (course_id, section_code, semester, year),
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id),

    INDEX idx_semester_year (semester, year),
    INDEX idx_instructor (instructor_id)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: enrollments
-- Student registrations in course sections
-- ============================================================================
CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status ENUM('registered', 'dropped', 'completed') DEFAULT 'registered',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dropped_at DATETIME NULL,
    final_grade VARCHAR(4) NULL DEFAULT NULL,

    UNIQUE KEY unique_enrollment (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE,

    INDEX idx_student_status (student_id, status),
    INDEX idx_section_status (section_id, status)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: grade_components
-- Assessment components for each section (Quiz, Midterm, Final, etc.)
-- ============================================================================
CREATE TABLE grade_components (
    component_id INT AUTO_INCREMENT PRIMARY KEY,
    section_id INT NOT NULL,
    component_name VARCHAR(50) NOT NULL,
    weight DECIMAL(5,2) NOT NULL CHECK (weight >= 0 AND weight <= 100),
    max_score DECIMAL(5,2) NOT NULL CHECK (max_score > 0),

    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE,

    INDEX idx_section (section_id)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: grades
-- Individual student scores for each assessment component
-- ============================================================================
CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    component_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL CHECK (score >= 0),
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_grade (enrollment_id, component_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (component_id) REFERENCES grade_components(component_id) ON DELETE CASCADE,

    INDEX idx_enrollment (enrollment_id)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: settings
-- System configuration (maintenance mode, current semester, etc.)
-- ============================================================================
CREATE TABLE settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: notifications
-- User notifications and system announcements (Bonus feature +2 points)
-- ============================================================================
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    target_role ENUM('student', 'instructor', 'admin') NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES university_auth.users_auth(user_id) ON DELETE CASCADE,

    INDEX idx_user_read (user_id, is_read),
    INDEX idx_role (target_role)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: backup_logs
-- Database backup history (Bonus feature +2 points)
-- ============================================================================
CREATE TABLE backup_logs (
    backup_id INT AUTO_INCREMENT PRIMARY KEY,
    backup_type ENUM('full', 'partial') DEFAULT 'full',
    backup_path VARCHAR(500) NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('success', 'failed') DEFAULT 'success',
    notes TEXT,

    FOREIGN KEY (created_by) REFERENCES university_auth.users_auth(user_id),

    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB;

-- ============================================================================
-- SAMPLE DATA: Instructors
-- user_id must match users_auth table
-- ============================================================================
INSERT INTO instructors (user_id, department, office) VALUES
(2, 'Computer Science', 'Room CS-101'),
(5, 'Mathematics', 'Room MATH-201');

-- ============================================================================
-- SAMPLE DATA: Students
-- user_id must match users_auth table
-- ============================================================================
INSERT INTO students (user_id, roll_no, program, year) VALUES
(3, 'B-1001', 'Computer Science and Engineering', 1),
(4, 'B-1002', 'Electronics and Communication Engineering', 1);

-- ============================================================================
-- SAMPLE DATA: Courses
-- ============================================================================
INSERT INTO courses (course_id, code, title, credits, description) VALUES
(101, 'CS101', 'Introduction to Programming', 4, 'Fundamentals of programming using Python and Java'),
(102, 'PHY101', 'Physics I', 3, 'Classical mechanics and thermodynamics'),
(103, 'MATH101', 'Calculus I', 4, 'Differential and integral calculus'),
(104, 'CS201', 'Data Structures', 4, 'Arrays, linked lists, trees, graphs, and algorithms'),
(105, 'ENG101', 'Technical Communication', 2, 'Written and oral communication skills');

-- ============================================================================
-- SAMPLE DATA: Course Prerequisites
-- ============================================================================
INSERT INTO course_prerequisites (course_id, requires_course_id) VALUES
(102, 101),   -- PHY101 requires CS101 (for computational physics)
(104, 101);   -- CS201 requires CS101

-- ============================================================================
-- SAMPLE DATA: Sections - Monsoon 2025
-- ============================================================================
INSERT INTO sections (section_id, course_id, instructor_id, section_code, day_time, room, capacity, semester, year, add_deadline, drop_deadline, enrollment_count) VALUES
(501, 101, 2, 'A', 'Mon/Wed 10:00-11:50', 'CS-105', 50, 'Monsoon', 2025, '2025-12-15', '2025-12-20', 2),
(502, 102, 2, 'B', 'Tue/Thu 14:00-15:20', 'PHY-201', 30, 'Monsoon', 2025, '2025-12-15', '2025-12-20', 1),
(506, 103, 5, 'A', 'Mon/Wed/Fri 09:00-09:50', 'MATH-101', 60, 'Monsoon', 2025, '2025-12-15', '2025-12-20', 0),
(507, 105, 5, 'A', 'Fri 14:00-15:50', 'LH-301', 100, 'Monsoon', 2025, '2025-12-15', '2025-12-20', 0);

-- ============================================================================
-- SAMPLE DATA: Sections - Winter 2025
-- ============================================================================
INSERT INTO sections (section_id, course_id, instructor_id, section_code, day_time, room, capacity, semester, year, add_deadline, drop_deadline, enrollment_count) VALUES
(503, 101, 2, 'B', 'Tue/Thu 09:00-10:50', 'CS-106', 45, 'Winter', 2025, '2026-02-15', '2026-02-28', 0),
(504, 102, 2, 'A', 'Mon/Wed 14:00-15:20', 'PHY-202', 35, 'Winter', 2025, '2026-02-15', '2026-02-28', 0),
(505, 103, 5, 'A', 'Mon/Wed/Fri 11:00-11:50', 'MATH-102', 60, 'Winter', 2025, '2026-02-15', '2026-02-28', 0),
(508, 104, 2, 'A', 'Tue/Thu 10:00-11:50', 'CS-107', 40, 'Winter', 2025, '2026-02-15', '2026-02-28', 0),
(509, 105, 5, 'B', 'Fri 10:00-11:50', 'LH-302', 80, 'Winter', 2025, '2026-02-15', '2026-02-28', 0);

-- ============================================================================
-- SAMPLE DATA: Grade Components
-- Grading scheme: Quiz (20%) + Midterm (30%) + End-Sem (50%) = 100%
-- ============================================================================

-- CS101 Section A (Monsoon 2025)
INSERT INTO grade_components (component_id, section_id, component_name, weight, max_score) VALUES
(1, 501, 'Quiz', 20.00, 20.00),
(2, 501, 'Midterm', 30.00, 100.00),
(3, 501, 'End-Sem', 50.00, 100.00);

-- PHY101 Section B (Monsoon 2025) - Lab (25%) + Midterm (25%) + Final (50%)
INSERT INTO grade_components (component_id, section_id, component_name, weight, max_score) VALUES
(4, 502, 'Lab', 25.00, 50.00),
(5, 502, 'Midterm', 25.00, 100.00),
(6, 502, 'Final', 50.00, 100.00);

-- MATH101 Section A (Monsoon 2025)
INSERT INTO grade_components (component_id, section_id, component_name, weight, max_score) VALUES
(7, 506, 'Assignment', 20.00, 100.00),
(8, 506, 'Midterm', 30.00, 100.00),
(9, 506, 'Final', 50.00, 100.00);

-- ============================================================================
-- SAMPLE DATA: Enrollments
-- stu1 (Alice) enrolled in CS101
-- stu2 (Bob) enrolled in CS101 and PHY101
-- ============================================================================
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
(1, 3, 501, 'registered'),   -- Alice in CS101-A
(2, 4, 501, 'registered'),   -- Bob in CS101-A
(3, 4, 502, 'registered');   -- Bob in PHY101-B

-- ============================================================================
-- SAMPLE DATA: Grades
-- Sample grades for Alice in CS101 to demonstrate grading functionality
-- ============================================================================
INSERT INTO grades (enrollment_id, component_id, score) VALUES
(1, 1, 18.0),   -- Alice: Quiz 18/20
(1, 2, 85.0),   -- Alice: Midterm 85/100
(1, 3, 78.0);   -- Alice: End-Sem 78/100

-- ============================================================================
-- SAMPLE DATA: System Settings
-- ============================================================================
INSERT INTO settings (setting_key, setting_value) VALUES
('maintenance_mode', 'false'),
('current_semester', 'Monsoon'),
('current_year', '2025');

-- ============================================================================
-- SAMPLE DATA: Welcome Notifications
-- ============================================================================
INSERT INTO notifications (user_id, target_role, message) VALUES
(NULL, 'student', 'Welcome to the new semester! Course registration is now open.'),
(NULL, 'instructor', 'Reminder: Please set up your grade components for all sections.'),
(3, NULL, 'You have been successfully enrolled in CS101 - Section A.');

-- ============================================================================
-- GRANT PRIVILEGES
-- ============================================================================
GRANT ALL PRIVILEGES ON university_erp.* TO 'university_user'@'localhost';
FLUSH PRIVILEGES;

-- ============================================================================
-- VERIFICATION
-- ============================================================================
SELECT 'ERP Database Setup Complete!' AS Status;
SELECT '--- Courses ---' AS Info;
SELECT code, title, credits FROM courses ORDER BY code;
SELECT '--- Sections ---' AS Info;
SELECT s.section_id, c.code, s.section_code, s.semester, s.year, s.capacity, s.enrollment_count
FROM sections s JOIN courses c ON s.course_id = c.course_id ORDER BY s.semester, c.code;
SELECT '--- Enrollments ---' AS Info;
SELECT e.enrollment_id, st.roll_no, c.code AS course, s.section_code, e.status
FROM enrollments e
JOIN students st ON e.student_id = st.user_id
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id;

COMMIT;
