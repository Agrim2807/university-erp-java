-- ============================================================================
-- UNIVERSITY ERP SYSTEM - AUTHENTICATION DATABASE SETUP
-- File: auth_setup.sql
-- Description: Creates and populates the university_auth database
-- Run this script FIRST before erp_setup.sql
-- ============================================================================
-- Default Password for all users: admin123
-- ============================================================================

-- Drop and recreate database user
DROP USER IF EXISTS 'university_user'@'localhost';
CREATE USER 'university_user'@'localhost' IDENTIFIED BY 'SecurePass123!';

-- ============================================================================
-- DATABASE CREATION
-- Drop ERP first (has FK references to auth), then auth
-- ============================================================================
DROP DATABASE IF EXISTS university_erp;
DROP DATABASE IF EXISTS university_auth;
CREATE DATABASE university_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE university_auth;

-- ============================================================================
-- TABLE: users_auth
-- Core authentication table storing user credentials and login state
-- Links to ERP database via user_id
-- ============================================================================
CREATE TABLE users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('student', 'instructor', 'admin') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status ENUM('active', 'locked') DEFAULT 'active',
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    last_login DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- ============================================================================
-- TABLE: password_history
-- Stores previous password hashes to prevent password reuse (last 5)
-- Bonus feature for Change Password & Lockout (+3 points)
-- ============================================================================
CREATE TABLE password_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users_auth(user_id) ON DELETE CASCADE,
    INDEX idx_user_history (user_id, created_at DESC)
) ENGINE=InnoDB;

-- ============================================================================
-- SAMPLE DATA: Users
-- Password for ALL users: admin123 (BCrypt hashed with 10 rounds)
-- ============================================================================

-- Admin User
INSERT INTO users_auth (user_id, username, full_name, role, password_hash, status) VALUES
(1, 'admin1', 'System Administrator', 'admin', '$2a$10$5N4PxgiExe9IzecAUcf8TuHYSteb5Unj7Qa6v89PnE5nIvq935iQO', 'active');

-- Instructor Users
INSERT INTO users_auth (user_id, username, full_name, role, password_hash, status) VALUES
(2, 'inst1', 'Dr. John Smith', 'instructor', '$2a$10$5N4PxgiExe9IzecAUcf8TuHYSteb5Unj7Qa6v89PnE5nIvq935iQO', 'active'),
(5, 'inst2', 'Dr. Sarah Wilson', 'instructor', '$2a$10$5N4PxgiExe9IzecAUcf8TuHYSteb5Unj7Qa6v89PnE5nIvq935iQO', 'active');

-- Student Users
INSERT INTO users_auth (user_id, username, full_name, role, password_hash, status) VALUES
(3, 'stu1', 'Alice Johnson', 'student', '$2a$10$5N4PxgiExe9IzecAUcf8TuHYSteb5Unj7Qa6v89PnE5nIvq935iQO', 'active'),
(4, 'stu2', 'Bob Williams', 'student', '$2a$10$5N4PxgiExe9IzecAUcf8TuHYSteb5Unj7Qa6v89PnE5nIvq935iQO', 'active');

-- ============================================================================
-- GRANT PRIVILEGES
-- ============================================================================
GRANT ALL PRIVILEGES ON university_auth.* TO 'university_user'@'localhost';
FLUSH PRIVILEGES;

-- ============================================================================
-- VERIFICATION
-- ============================================================================
SELECT 'Auth Database Setup Complete!' AS Status;
SELECT user_id, username, full_name, role, status FROM users_auth ORDER BY user_id;

COMMIT;
