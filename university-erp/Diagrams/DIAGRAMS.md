# University ERP System - Architecture Diagrams & Visual Documentation

**Authors:**
- **Agrim Upadhyay** (2024046)
- **Saksham Verma** (2024497)

**Course:** Advanced Programming
**Institution:** IIIT Delhi
**Submission Date:** November 2025

---

## Table of Contents

1. [System Architecture Diagram](#1-system-architecture-diagram)
2. [Use Case Diagrams](#2-use-case-diagrams)
3. [Entity Relationship Diagram](#3-entity-relationship-diagram)
4. [Flow Diagrams](#4-flow-diagrams)
5. [Sequence Diagrams](#5-sequence-diagrams)
6. [Class Diagram](#6-class-diagram)
7. [Deployment Diagram](#7-deployment-diagram)
8. [Component Interaction Diagram](#8-component-interaction-diagram)

---

## 1. System Architecture Diagram

### 1.1 Layered Architecture Overview

The system follows a **clean layered architecture** with clear separation of concerns:

```
╔═══════════════════════════════════════════════════════════════════════╗
║                      PRESENTATION LAYER (UI)                          ║
║                        26 Swing Components                            ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  ┌────────────────┐  ┌────────────────┐  ┌────────────────────────┐ ║
║  │  LoginPanel    │  │ DashboardPanel │  │  Role-Based Panels     │ ║
║  │  MainFrame     │  │  SidebarPanel  │  │ (Student/Instructor/   │ ║
║  │  UIFactory     │  │ BreadcrumbPanel│  │  Admin)                │ ║
║  └────────────────┘  └────────────────┘  └────────────────────────┘ ║
║                                                                       ║
║  Components: StudentCatalogPanel, GradebookPanel, MyGradesPanel,     ║
║             UserManagementPanel, CourseManagementPanel, etc.         ║
╚═══════════════════════════════════════════════════════════════════════╝
                                  │
                                  │ User Actions & Events
                                  ▼
╔═══════════════════════════════════════════════════════════════════════╗
║                  SERVICE LAYER (Business Logic)                       ║
║                          7 Core Services                              ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────────┐ ║
║  │ StudentService   │  │ InstructorService │  │  AdminService    │ ║
║  │ • Register       │  │ • Define Components│  │ • User CRUD      │ ║
║  │ • Drop Section   │  │ • Enter Grades     │  │ • Course CRUD    │ ║
║  │ • View Grades    │  │ • Compute Finals   │  │ • Section CRUD   │ ║
║  │ • Transcript     │  │ • Statistics       │  │ • Maintenance    │ ║
║  └──────────────────┘  └───────────────────┘  └──────────────────┘ ║
║                                                                       ║
║  ┌────────────────────┐  ┌──────────────────┐  ┌─────────────────┐ ║
║  │ NotificationService│  │ MaintenanceService│  │ SemesterService │ ║
║  │ • User Notify      │  │ • Toggle Mode     │  │ • Get Current   │ ║
║  │ • Broadcast        │  │ • Check Status    │  │ • Update Year   │ ║
║  └────────────────────┘  └──────────────────┘  └─────────────────┘ ║
║                                                                       ║
║  ┌──────────────────────────────────────────────────────────────┐   ║
║  │         ACCESS CONTROL & AUTHORIZATION LAYER                  │   ║
║  │  • AccessControlService  • SessionManager  • AuthService      │   ║
║  │  • Role-Based Permissions • Maintenance Mode Enforcement      │   ║
║  └──────────────────────────────────────────────────────────────┘   ║
╚═══════════════════════════════════════════════════════════════════════╝
                                  │
                                  │ Database Operations
                                  ▼
╔═══════════════════════════════════════════════════════════════════════╗
║              DATA ACCESS LAYER (DAOs)                                 ║
║                    8 Data Access Objects                              ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────┐ ║
║  │ StudentDAO   │  │InstructorDAO │  │  CourseDAO   │  │ AuthDAO │ ║
║  └──────────────┘  └──────────────┘  └──────────────┘  └─────────┘ ║
║                                                                       ║
║  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────┐ ║
║  │ SectionDAO   │  │EnrollmentDAO │  │  GradeDAO    │  │Settings │ ║
║  └──────────────┘  └──────────────┘  └──────────────┘  └─────────┘ ║
║                                                                       ║
║  Features: CRUD Operations, Transaction Management, Locking          ║
╚═══════════════════════════════════════════════════════════════════════╝
                                  │
                                  │ SQL Queries via JDBC
                                  ▼
╔═══════════════════════════════════════════════════════════════════════╗
║                   CONNECTION POOLING LAYER                            ║
║                         HikariCP                                      ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  ┌──────────────────────────┐     ┌───────────────────────────┐     ║
║  │  Auth DB Pool            │     │  ERP DB Pool              │     ║
║  │  • Max Connections: 10   │     │  • Max Connections: 15    │     ║
║  │  • Min Idle: 2           │     │  • Min Idle: 3            │     ║
║  │  • Timeout: 30s          │     │  • Timeout: 30s           │     ║
║  └──────────────────────────┘     └───────────────────────────┘     ║
╚═══════════════════════════════════════════════════════════════════════╝
                    │                             │
                    │                             │
                    ▼                             ▼
┌──────────────────────────────┐  ┌──────────────────────────────────┐
│   university_auth (MySQL)    │  │   university_erp (MySQL)         │
├──────────────────────────────┤  ├──────────────────────────────────┤
│ • users_auth                 │  │ • students                       │
│ • password_history           │  │ • instructors                    │
│                              │  │ • courses                        │
│ Purpose: Authentication &    │  │ • sections                       │
│          User Credentials    │  │ • enrollments                    │
│                              │  │ • grades                         │
│ Security: BCrypt Hashing     │  │ • grade_components               │
│           Account Lockout    │  │ • course_prerequisites           │
│                              │  │ • notifications                  │
│                              │  │ • settings                       │
│                              │  │                                  │
│                              │  │ Purpose: Academic Data           │
└──────────────────────────────┘  └──────────────────────────────────┘
```

### 1.2 Architectural Principles

| Principle | Implementation |
|-----------|---------------|
| **Separation of Concerns** | UI, Business Logic, Data Access are independent |
| **Single Responsibility** | Each class has one clear purpose |
| **Dependency Injection** | Services receive DAO dependencies |
| **Loose Coupling** | Layers interact through interfaces |
| **High Cohesion** | Related functionality grouped together |

---

## 2. Use Case Diagrams

### 2.1 Student Use Cases

```
                             University ERP System
                    ┌──────────────────────────────────┐
                    │                                  │
        ┌───────────┼──────────────┬───────────────────┼───────────┐
        │           │              │                   │           │
        ▼           ▼              ▼                   ▼           ▼
    ┌────────┐  ┌────────┐    ┌────────┐         ┌────────┐  ┌────────┐
    │Browse  │  │Register│    │ Drop   │         │  View  │  │  View  │
    │Catalog │  │Section │    │Section │         │Timetable│ │ Grades │
    └────────┘  └────┬───┘    └───┬────┘         └────────┘  └────────┘
                     │            │
                     │            │
                «include»     «include»
                     │            │
              ┌──────┴───┐    ┌──┴────────┐
              │ Check    │    │  Check    │
              │ Capacity │    │ Deadline  │
              └──────────┘    └───────────┘
                     │
                «include»
                     │
              ┌──────┴─────────┐
              │  Check         │
              │  Prerequisites │
              └────────────────┘
                     │
                «include»
                     │
              ┌──────┴────────┐
              │   Validate    │
              │   Timetable   │
              │   Clash       │
              └───────────────┘

        ┌────────────┐
        │            │
        │  Download  │
        │ Transcript │
        │  (PDF/CSV) │
        │            │
        └────────────┘
               ▲
               │
               │
        ┌──────┴──────┐
        │             │
        │   Student   │
        │             │
        └─────────────┘
```

**Student Use Case Summary:**

| Use Case | Description | Preconditions | Postconditions |
|----------|-------------|---------------|----------------|
| Browse Catalog | View available courses and sections | Logged in | Informed of course offerings |
| Register for Section | Enroll in a course section | Before add deadline, seats available | Enrolled, notification sent |
| Drop Section | Withdraw from a section | Before drop deadline, enrolled | Enrollment status = dropped |
| View Timetable | See weekly schedule | Logged in | Schedule displayed |
| View Grades | Check component scores and final grades | Enrolled in courses | Academic progress shown |
| Download Transcript | Export academic history | Logged in | Transcript file saved |

---

### 2.2 Instructor Use Cases

```
                             University ERP System
                    ┌──────────────────────────────────┐
                    │                                  │
        ┌───────────┼──────────────┬───────────────────┼───────────┐
        │           │              │                   │           │
        ▼           ▼              ▼                   ▼           ▼
    ┌────────┐  ┌────────┐    ┌────────┐         ┌────────┐  ┌────────┐
    │  View  │  │ Define │    │ Enter  │         │Compute │  │  View  │
    │  My    │  │ Grade  │    │ Scores │         │ Final  │  │ Class  │
    │Sections│  │Compnts │    │        │         │ Grades │  │ Stats  │
    └────────┘  └────┬───┘    └───┬────┘         └───┬────┘  └────────┘
                     │            │                  │
                     │            │                  │
                «include»     «include»          «include»
                     │            │                  │
              ┌──────┴───┐    ┌──┴────────┐    ┌────┴──────┐
              │ Validate │    │ Validate  │    │  Validate │
              │ Weights  │    │   Score   │    │  Weights  │
              │ Sum=100% │    │≤ Max Score│    │  Sum=100% │
              └──────────┘    └───────────┘    └───────────┘
                                    │
                               «include»
                                    │
                             ┌──────┴────────┐
                             │   Verify      │
                             │   Section     │
                             │   Ownership   │
                             └───────────────┘

        ┌────────────┐        ┌────────────┐
        │  Import/   │        │   Export   │
        │  Export    │        │   Grade    │
        │  Grades    │        │   Report   │
        │  (CSV)     │        │   (PDF)    │
        └────────────┘        └────────────┘
               ▲                     ▲
               │                     │
               └──────────┬──────────┘
                          │
                   ┌──────┴──────┐
                   │             │
                   │ Instructor  │
                   │             │
                   └─────────────┘
```

**Instructor Use Case Summary:**

| Use Case | Description | Preconditions | Postconditions |
|----------|-------------|---------------|----------------|
| View My Sections | List all assigned sections | Logged in as instructor | Sections displayed with enrollment |
| Define Grade Components | Create grading structure | Teaching a section | Components created (weights sum to 100%) |
| Enter/Edit Scores | Input student scores | Components defined | Scores saved to database |
| Compute Final Grades | Calculate weighted averages | All scores entered | Final grades posted, students notified |
| View Class Statistics | See aggregate metrics | Scores entered | Statistics displayed |
| Import/Export Grades | Bulk grade operations via CSV | Components defined | Grades imported/exported successfully |

---

### 2.3 Admin Use Cases

```
                             University ERP System
                    ┌──────────────────────────────────┐
                    │                                  │
        ┌───────────┼──────────────┬───────────────────┼───────────┐
        │           │              │                   │           │
        ▼           ▼              ▼                   ▼           ▼
    ┌────────┐  ┌────────┐    ┌────────┐         ┌────────┐  ┌────────┐
    │ Manage │  │ Manage │    │ Manage │         │ Toggle │  │  Bulk  │
    │ Users  │  │Courses │    │Sections│         │Maint.  │  │  Set   │
    │ (CRUD) │  │ (CRUD) │    │ (CRUD) │         │ Mode   │  │Deadlines│
    └────┬───┘  └────┬───┘    └───┬────┘         └────────┘  └────────┘
         │           │            │
         │           │            │
    «include»   «include»    «include»
         │           │            │
  ┌──────┴──────┐ ┌─┴────────┐ ┌─┴──────────┐
  │Create in    │ │   Add    │ │  Assign    │
  │Auth DB +    │ │Prerequisite│ │ Instructor │
  │ERP Profile  │ │          │ │            │
  └─────────────┘ └──────────┘ └────────────┘
         │
         │
    «include»
         │
  ┌──────┴──────────┐
  │ Compensating    │
  │ Transaction     │
  │ on Failure      │
  └─────────────────┘

        ┌────────────┐        ┌────────────┐        ┌────────────┐
        │  Database  │        │   Send     │        │  Semester  │
        │  Backup /  │        │ Announce-  │        │    Year    │
        │  Restore   │        │   ments    │        │ Management │
        └────────────┘        └────────────┘        └────────────┘
               ▲                     ▲                     ▲
               │                     │                     │
               └──────────┬──────────┴─────────────────────┘
                          │
                   ┌──────┴──────┐
                   │             │
                   │    Admin    │
                   │             │
                   └─────────────┘
```

**Admin Use Case Summary:**

| Use Case | Description | Key Features |
|----------|-------------|--------------|
| Manage Users | CRUD operations on users | Two-database transaction, role assignment |
| Manage Courses | CRUD operations on courses | Prerequisites, active/inactive status |
| Manage Sections | CRUD operations on sections | Instructor assignment, deadline management |
| Toggle Maintenance | Enable/disable maintenance mode | System-wide enforcement, notifications |
| Bulk Set Deadlines | Update deadlines for a semester | Mass update, confirmation dialog |
| Database Backup/Restore | Export/import databases | mysqldump integration, safety warnings |
| Send Announcements | Broadcast messages | Role-based targeting |

---

## 3. Entity Relationship Diagram

### 3.1 Complete ERD with All Relationships

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          AUTH DATABASE                                  │
└─────────────────────────────────────────────────────────────────────────┘

╔══════════════════════════════════════════════════════════════════════╗
║                           users_auth                                 ║
╠══════════════════════════════════════════════════════════════════════╣
║ PK  user_id           INT AUTO_INCREMENT                             ║
║ UK  username          VARCHAR(50)                                    ║
║     full_name         VARCHAR(100)                                   ║
║     role              ENUM('student','instructor','admin')           ║
║     password_hash     VARCHAR(255)  [BCrypt hashed]                 ║
║     status            ENUM('active','locked','inactive')             ║
║     failed_attempts   INT DEFAULT 0                                  ║
║     locked_until      TIMESTAMP                                      ║
║     last_login        TIMESTAMP                                      ║
║     created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP            ║
╚══════════════════════════════════════════════════════════════════════╝
         │                                     │
         │ 1:1                                 │ 1:M
         │                                     │
         ▼                                     ▼
╔══════════════════════════╗         ╔════════════════════════╗
║  password_history        ║         ║  [Links to ERP DB]     ║
╠══════════════════════════╣         ║  students.user_id      ║
║ PK  history_id    INT    ║         ║  instructors.user_id   ║
║ FK  user_id       INT    ║         ╚════════════════════════╝
║     password_hash VARCHAR║
║     created_at   TIMESTAMP║
╚══════════════════════════╝


┌─────────────────────────────────────────────────────────────────────────┐
│                           ERP DATABASE                                  │
└─────────────────────────────────────────────────────────────────────────┘

╔═══════════════════════════════╗         ╔═══════════════════════════════╗
║         students              ║         ║       instructors             ║
╠═══════════════════════════════╣         ╠═══════════════════════════════╣
║ PK,FK  user_id     INT        ║         ║ PK,FK  user_id     INT        ║
║ UK     roll_no     VARCHAR(20)║         ║        department  VARCHAR(100)║
║        program     VARCHAR(100)║         ║        office      VARCHAR(50)║
║        year        INT         ║         ╚═══════════════════════════════╝
╚═══════════════════════════════╝                       │
         │                                              │ 1:M
         │ 1:M                                          │
         │                                              ▼
         │                                    ╔════════════════════════════╗
         │                                    ║       sections             ║
         │                                    ╠════════════════════════════╣
         │                                    ║ PK  section_id      INT    ║
         │                                    ║ FK  course_id       INT    ║
         │                                    ║ FK  instructor_id   INT    ║
         │                                    ║     section_code    VARCHAR║
         │                                    ║     day_time        VARCHAR║
         │                                    ║     room            VARCHAR║
         │                                    ║     capacity        INT    ║
         │                                    ║     semester        ENUM   ║
         │                                    ║     year            INT    ║
         │                                    ║     enrollment_count INT   ║
         │                                    ║     add_deadline    DATE   ║
         │                                    ║     drop_deadline   DATE   ║
         │                                    ╚════════════════════════════╝
         │                                              ▲
         │                                              │ M:1
         │                                              │
         │                                    ╔════════════════════════════╗
         │                                    ║        courses             ║
         │                                    ╠════════════════════════════╣
         │                                    ║ PK  course_id       INT    ║
         │                                    ║ UK  code            VARCHAR║
         │                                    ║     title           VARCHAR║
         │                                    ║     credits         INT    ║
         │                                    ║     description     TEXT   ║
         │                                    ║     is_active       BOOLEAN║
         │                                    ╚════════════════════════════╝
         │                                              │
         │                                              │ M:M (self-ref)
         │                                              ▼
         │                                    ╔════════════════════════════╗
         │                                    ║  course_prerequisites      ║
         │                                    ╠════════════════════════════╣
         │                                    ║ PK,FK course_id      INT   ║
         │                                    ║ PK,FK requires_course_id   ║
         │                                    ╚════════════════════════════╝
         │
         │
         ▼
╔════════════════════════════════════════════════════════════════════╗
║                          enrollments                               ║
╠════════════════════════════════════════════════════════════════════╣
║ PK  enrollment_id    INT AUTO_INCREMENT                            ║
║ FK  student_id       INT                                           ║
║ FK  section_id       INT                                           ║
║     status           ENUM('registered','dropped','completed')      ║
║     enrolled_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP           ║
║     dropped_at       TIMESTAMP                                     ║
║     final_grade      VARCHAR(2)                                    ║
║ UK  (student_id, section_id)  [Prevents duplicate enrollments]    ║
╚════════════════════════════════════════════════════════════════════╝
         │
         │ 1:M
         │
         ▼
╔══════════════════════════════╗           ╔══════════════════════════╗
║         grades               ║           ║   grade_components       ║
╠══════════════════════════════╣           ╠══════════════════════════╣
║ PK  grade_id      INT        ║           ║ PK component_id   INT    ║
║ FK  enrollment_id INT        ║◄──────────║ FK section_id     INT    ║
║ FK  component_id  INT        ║   M:1     ║    component_name VARCHAR║
║     score         DOUBLE     ║           ║    weight         DOUBLE ║
║     entered_at    TIMESTAMP  ║           ║    max_score      DOUBLE ║
║ UK  (enrollment_id,          ║           ╚══════════════════════════╝
║      component_id)           ║
╚══════════════════════════════╝


╔══════════════════════════════╗           ╔══════════════════════════╗
║      notifications           ║           ║       settings           ║
╠══════════════════════════════╣           ╠══════════════════════════╣
║ PK notification_id   INT     ║           ║ PK setting_key   VARCHAR ║
║ FK user_id          INT NULL ║           ║    setting_value TEXT    ║
║    target_role      VARCHAR  ║           ║    updated_at   TIMESTAMP║
║    message          TEXT     ║           ╚══════════════════════════╝
║    is_read          BOOLEAN  ║
║    created_at       TIMESTAMP║           Examples:
╚══════════════════════════════╝           • maintenance_mode = true
                                            • current_semester = Monsoon
Notification Types:                         • current_year = 2024
• User-specific (user_id set)
• Role broadcast (target_role set)
• System-wide (both NULL)
```

### 3.2 Cardinality Summary

| Relationship | Type | Explanation |
|--------------|------|-------------|
| users_auth → students | 1:1 | One auth user maps to one student profile |
| users_auth → instructors | 1:1 | One auth user maps to one instructor profile |
| instructors → sections | 1:M | One instructor teaches many sections |
| courses → sections | 1:M | One course has many sections (different semesters/times) |
| sections → enrollments | 1:M | One section has many student enrollments |
| students → enrollments | 1:M | One student enrolls in many sections |
| enrollments → grades | 1:M | One enrollment has many component grades |
| sections → grade_components | 1:M | One section has many grade components |
| grade_components → grades | 1:M | One component has many student scores |
| courses ↔ course_prerequisites | M:M | Courses can require other courses (self-referencing) |

### 3.3 Database Constraints

| Constraint Type | Example | Purpose |
|----------------|---------|---------|
| **Primary Keys** | `user_id`, `course_id`, `section_id` | Unique identification |
| **Foreign Keys** | `sections.course_id → courses.course_id` | Referential integrity |
| **Unique Keys** | `username`, `roll_no`, `course code` | Prevent duplicates |
| **Check Constraints** | `capacity >= 0`, `credits BETWEEN 1 AND 10` | Data validation |
| **NOT NULL** | `username NOT NULL`, `course title NOT NULL` | Required fields |
| **Cascade Deletes** | `ON DELETE CASCADE` on enrollments | Maintain consistency |

---

## 4. Flow Diagrams

### 4.1 Student Registration Flow (Complete)

```
                              ┌──────────┐
                              │  START   │
                              └────┬─────┘
                                   │
                                   ▼
                         ┌─────────────────┐
                         │ Student browses │
                         │ Course Catalog  │
                         │ (filters, search)│
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Selects desired │
                         │    section      │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Clicks "Register"│
                         │     button      │
                         └────────┬────────┘
                                  │
                                  ▼
                    ╔═════════════════════════════╗
                    ║ AccessControl: Check if     ║
                    ║ REGISTER_SECTION allowed    ║
                    ╚═════════════╦═══════════════╝
                                  │
                    ┌─────────────┴──────────────┐
                    │                            │
                   NO                          YES
                    │                            │
                    ▼                            ▼
            ┌────────────────┐         ┌────────────────┐
            │ Show Error:    │         │ Check Add      │
            │ "Access Denied"│         │ Deadline       │
            │ or "Maintenance│         └────────┬───────┘
            │ Mode Active"   │                  │
            └────────────────┘         ┌────────┴────────┐
                    │                  │                 │
                    │              PASSED            BEFORE
                    │                  │                 │
                    │                  ▼                 ▼
                    │          ┌────────────┐   ┌───────────────┐
                    │          │Show Error: │   │ BEGIN DB      │
                    │          │"Deadline   │   │ TRANSACTION   │
                    │          │ Passed"    │   └───────┬───────┘
                    │          └────────────┘           │
                    │                  │                ▼
                    │                  │       ┌────────────────┐
                    │                  │       │ SELECT section │
                    │                  │       │ FOR UPDATE     │
                    │                  │       │ (ROW LOCK)     │
                    │                  │       └───────┬────────┘
                    │                  │               │
                    │                  │               ▼
                    │                  │       ╔══════════════════╗
                    │                  │       ║ Check existing   ║
                    │                  │       ║ enrollment       ║
                    │                  │       ╚══════╦═══════════╝
                    │                  │              │
                    │                  │       ┌──────┴────────┐
                    │                  │       │               │
                    │                  │    ALREADY        NOT EXISTS
                    │                  │    ENROLLED           │
                    │                  │       │               ▼
                    │                  │       ▼       ╔═══════════════╗
                    │                  │  ┌─────────┐  ║ Check Seats   ║
                    │                  │  │ROLLBACK │  ║ Available     ║
                    │                  │  │Error:   │  ╚═══════╦═══════╝
                    │                  │  │"Already │          │
                    │                  │  │Reg'd"   │   ┌──────┴────────┐
                    │                  │  └─────────┘   │               │
                    │                  │       │      FULL          AVAILABLE
                    │                  │       │        │               │
                    │                  │       │        ▼               ▼
                    │                  │       │  ┌─────────┐   ╔══════════════╗
                    │                  │       │  │ROLLBACK │   ║ Check        ║
                    │                  │       │  │Error:   │   ║ Prerequisites║
                    │                  │       │  │"Section │   ╚══════╦═══════╝
                    │                  │       │  │ Full"   │          │
                    │                  │       │  └─────────┘   ┌──────┴──────┐
                    │                  │       │        │       │             │
                    │                  │       │        │   NOT MET        MET
                    │                  │       │        │       │             │
                    │                  │       │        │       ▼             ▼
                    │                  │       │        │  ┌─────────┐ ╔═══════════╗
                    │                  │       │        │  │ROLLBACK │ ║ Check     ║
                    │                  │       │        │  │Error:   │ ║ Timetable ║
                    │                  │       │        │  │"Prereq  │ ║ Clash     ║
                    │                  │       │        │  │Not Met" │ ╚═══╦═══════╝
                    │                  │       │        │  └─────────┘     │
                    │                  │       │        │        │    ┌────┴─────┐
                    │                  │       │        │        │    │          │
                    │                  │       │        │        │  CLASH    NO CLASH
                    │                  │       │        │        │    │          │
                    │                  │       │        │        │    ▼          ▼
                    │                  │       │        │        │ ┌───────┐ ┌─────────┐
                    │                  │       │        │        │ │ROLLBACK│ │ INSERT  │
                    │                  │       │        │        │ │Error: │ │enrollment│
                    │                  │       │        │        │ │"Time  │ │         │
                    │                  │       │        │        │ │Clash" │ └────┬────┘
                    │                  │       │        │        │ └───────┘      │
                    │                  │       │        │        │     │          ▼
                    │                  │       │        │        │     │   ┌─────────────┐
                    │                  │       │        │        │     │   │ INCREMENT   │
                    │                  │       │        │        │     │   │ enrollment_ │
                    │                  │       │        │        │     │   │ count       │
                    │                  │       │        │        │     │   └──────┬──────┘
                    │                  │       │        │        │     │          │
                    │                  │       │        │        │     │          ▼
                    │                  │       │        │        │     │   ┌─────────────┐
                    │                  │       │        │        │     │   │   COMMIT    │
                    │                  │       │        │        │     │   │ Transaction │
                    │                  │       │        │        │     │   └──────┬──────┘
                    │                  │       │        │        │     │          │
                    │                  │       │        │        │     │          ▼
                    │                  │       │        │        │     │   ┌─────────────┐
                    │                  │       │        │        │     │   │   Create    │
                    │                  │       │        │        │     │   │Notifications│
                    │                  │       │        │        │     │   │(Student +   │
                    │                  │       │        │        │     │   │Instructor)  │
                    │                  │       │        │        │     │   └──────┬──────┘
                    │                  │       │        │        │     │          │
                    └──────────────────┴───────┴────────┴────────┴─────┴──────────┘
                                                                                   │
                                                                                   ▼
                                                                          ┌────────────────┐
                                                                          │ Show Success   │
                                                                          │ "Registered!"  │
                                                                          └────────┬───────┘
                                                                                   │
                                                                                   ▼
                                                                          ┌────────────────┐
                                                                          │ Refresh Catalog│
                                                                          │ & My Courses   │
                                                                          └────────┬───────┘
                                                                                   │
                                                                                   ▼
                                                                              ┌────────┐
                                                                              │  END   │
                                                                              └────────┘
```

**Key Features:**
- **Row-Level Locking**: `SELECT ... FOR UPDATE` prevents race conditions
- **Transaction Management**: Atomic operations with rollback on failure
- **Comprehensive Validation**: Deadline, capacity, prerequisites, timetable clash
- **Access Control**: Maintenance mode and role permission checks
- **Notifications**: Automatic notification to student and instructor

---

### 4.2 Grade Computation Flow

```
                              ┌──────────┐
                              │  START   │
                              └────┬─────┘
                                   │
                                   ▼
                         ┌──────────────────┐
                         │ Instructor opens │
                         │ Section Gradebook│
                         └────────┬─────────┘
                                  │
                                  ▼
                    ╔═════════════════════════════╗
                    ║ Are Grade Components        ║
                    ║ Already Defined?            ║
                    ╚══════════╦══════════════════╝
                               │
                     ┌─────────┴──────────┐
                     │                    │
                    NO                   YES
                     │                    │
                     ▼                    │
            ┌────────────────┐            │
            │ Define Grade   │            │
            │ Components:    │            │
            │ • Name         │            │
            │ • Weight (%)   │            │
            │ • Max Score    │            │
            └───────┬────────┘            │
                    │                     │
                    ▼                     │
            ╔═══════════════════╗         │
            ║ Validate: Weights ║         │
            ║ Sum to 100%?      ║         │
            ╚═══════╦═══════════╝         │
                    │                     │
          ┌─────────┴─────────┐           │
          │                   │           │
         NO                  YES          │
          │                   │           │
          ▼                   │           │
    ┌──────────┐              │           │
    │ Show     │              │           │
    │ Error:   │              │           │
    │ "Weights │              │           │
    │ must sum │              │           │
    │ to 100%" │              │           │
    └──────────┘              │           │
          │                   │           │
          └───────────────────┴───────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │ Display Gradebook│
                    │ Spreadsheet with │
                    │ Students × Comps │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │ Instructor Enters│
                    │ Scores for each  │
                    │ Student/Component│
                    └────────┬─────────┘
                             │
                             ▼
                    ╔════════════════════╗
                    ║ Validate Each Score║
                    ║ • score >= 0       ║
                    ║ • score <= max     ║
                    ╚══════╦═════════════╝
                           │
                 ┌─────────┴──────────┐
                 │                    │
            INVALID                VALID
                 │                    │
                 ▼                    ▼
         ┌────────────┐        ┌───────────┐
         │ Show Error │        │ Save Score│
         │ "Invalid   │        │ to DB     │
         │  Score"    │        │ (Auto-save)│
         └────────────┘        └─────┬─────┘
                 │                   │
                 └───────────────────┘
                                     │
                                     ▼
                            ╔════════════════════╗
                            ║ All Scores Entered?║
                            ╚═══════╦════════════╝
                                    │
                          ┌─────────┴──────────┐
                          │                    │
                         NO                   YES
                          │                    │
                    Continue                   ▼
                    Entering          ┌────────────────┐
                    Scores            │ Instructor     │
                          │           │ Clicks "Compute│
                          │           │ Final Grades"  │
                          │           └────────┬───────┘
                          │                    │
                          │                    ▼
                          │           ╔════════════════════╗
                          │           ║ Revalidate Weights ║
                          │           ║ Sum to 100%        ║
                          │           ╚══════╦═════════════╝
                          │                  │
                          │        ┌─────────┴──────────┐
                          │        │                    │
                          │       NO                   YES
                          │        │                    │
                          │        ▼                    ▼
                          │  ┌──────────┐      ┌───────────────┐
                          │  │ Error:   │      │ FOR EACH      │
                          │  │ "Weights │      │ Student:      │
                          │  │ Invalid" │      └───────┬───────┘
                          │  └──────────┘              │
                          │        │                   ▼
                          │        │          ┌────────────────┐
                          │        │          │ Calculate      │
                          │        │          │ Weighted Avg:  │
                          │        │          │ Σ(score/max×w) │
                          │        │          └───────┬────────┘
                          │        │                  │
                          │        │                  ▼
                          │        │          ┌────────────────┐
                          │        │          │ Convert to     │
                          │        │          │ Letter Grade:  │
                          │        │          │ ≥90:A, ≥80:B   │
                          │        │          │ ≥70:C, ≥60:D   │
                          │        │          │ <60:F          │
                          │        │          └───────┬────────┘
                          │        │                  │
                          │        │                  ▼
                          │        │          ┌────────────────┐
                          │        │          │ UPDATE         │
                          │        │          │ enrollments    │
                          │        │          │ SET final_grade│
                          │        │          └───────┬────────┘
                          │        │                  │
                          │        │                  ▼
                          │        │          ┌────────────────┐
                          │        │          │ Create         │
                          │        │          │ Notification   │
                          │        │          │ for Student    │
                          │        │          │ "Grade Posted" │
                          │        │          └───────┬────────┘
                          │        │                  │
                          └────────┴──────────────────┘
                                                      │
                                                      ▼
                                             ┌────────────────┐
                                             │ Show Success   │
                                             │ "Grades        │
                                             │  Computed for  │
                                             │  N students"   │
                                             └────────┬───────┘
                                                      │
                                                      ▼
                                                 ┌────────┐
                                                 │  END   │
                                                 └────────┘
```

**Grade Computation Formula:**

```
For each student:
    final_score = 0
    For each component:
        contribution = (student_score / max_score) × component_weight
        final_score += contribution

    letter_grade = convert_to_letter(final_score)

Letter Grade Mapping:
    90-100  → A
    80-89   → B
    70-79   → C
    60-69   → D
    0-59    → F
```

---

### 4.3 Maintenance Mode Toggle Flow

```
                              ┌──────────┐
                              │  START   │
                              └────┬─────┘
                                   │
                                   ▼
                         ┌──────────────────┐
                         │ Admin navigates  │
                         │ to System        │
                         │ Settings Panel   │
                         └────────┬─────────┘
                                  │
                                  ▼
                    ╔═════════════════════════════╗
                    ║ Read Current Maintenance    ║
                    ║ Mode Status from Settings   ║
                    ║ Table                       ║
                    ╚══════════╦══════════════════╝
                               │
                     ┌─────────┴──────────┐
                     │                    │
                  CURRENTLY              CURRENTLY
                    OFF                    ON
                     │                    │
                     ▼                    ▼
            ┌────────────────┐   ┌────────────────┐
            │ Checkbox:      │   │ Checkbox:      │
            │ ☐ Enable       │   │ ☑ Enable       │
            │ Maintenance    │   │ Maintenance    │
            └───────┬────────┘   └───────┬────────┘
                    │                    │
                    ▼                    ▼
            ┌────────────────┐   ┌────────────────┐
            │ Admin checks   │   │ Admin unchecks │
            │ the box        │   │ the box        │
            └───────┬────────┘   └───────┬────────┘
                    │                    │
                    └──────────┬─────────┘
                               │
                               ▼
                    ╔═════════════════════════════╗
                    ║ Show Confirmation Dialog:   ║
                    ║                             ║
                    ║ "This will [ENABLE/DISABLE] ║
                    ║  maintenance mode.          ║
                    ║                             ║
                    ║  Students and instructors   ║
                    ║  will [be restricted/regain ║
                    ║  full access].              ║
                    ║                             ║
                    ║  Continue?"                 ║
                    ║                             ║
                    ║  [Cancel]  [Confirm]        ║
                    ╚══════════╦══════════════════╝
                               │
                     ┌─────────┴──────────┐
                     │                    │
                  CANCEL               CONFIRM
                     │                    │
                     ▼                    ▼
            ┌────────────────┐   ┌────────────────┐
            │ Revert         │   │ UPDATE settings│
            │ checkbox to    │   │ SET value =    │
            │ original state │   │ 'true'/'false' │
            │                │   │ WHERE key =    │
            │ No changes     │   │'maintenance_   │
            │ made           │   │ mode'          │
            └───────┬────────┘   └───────┬────────┘
                    │                    │
                    │                    ▼
                    │           ┌────────────────┐
                    │           │ Update         │
                    │           │ SessionManager │
                    │           │ .setMaintenance│
                    │           │ Mode(boolean)  │
                    │           └───────┬────────┘
                    │                   │
                    │                   ▼
                    │           ┌────────────────┐
                    │           │ CREATE Broadcast│
                    │           │ Notification:  │
                    │           │                │
                    │           │ "SYSTEM ALERT: │
                    │           │ Maintenance is │
                    │           │ now [ON/OFF]"  │
                    │           │                │
                    │           │ Target: All    │
                    │           │ Students &     │
                    │           │ Instructors    │
                    │           └───────┬────────┘
                    │                   │
                    │                   ▼
                    │           ╔═══════════════════╗
                    │           ║ Update All Active ║
                    │           ║ UI Windows:       ║
                    │           ║                   ║
                    │           ║ IF ON:            ║
                    │           ║  • Show Banner    ║
                    │           ║  • Disable Buttons║
                    │           ║                   ║
                    │           ║ IF OFF:           ║
                    │           ║  • Hide Banner    ║
                    │           ║  • Enable Buttons ║
                    │           ╚═══════╦═══════════╝
                    │                   │
                    │                   ▼
                    │           ┌────────────────────┐
                    │           │ Block Write Actions│
                    │           │ in Services:       │
                    │           │                    │
                    │           │ IF ON (Non-Admin): │
                    │           │  • REGISTER blocked│
                    │           │  • DROP blocked    │
                    │           │  • MANAGE_GRADES   │
                    │           │    blocked         │
                    │           │  • CHANGE_PASSWORD │
                    │           │    blocked         │
                    │           │                    │
                    │           │ IF OFF:            │
                    │           │  • All actions     │
                    │           │    allowed by role │
                    │           └────────┬───────────┘
                    │                    │
                    │                    ▼
                    │           ┌────────────────┐
                    │           │ Show Success   │
                    │           │ Message:       │
                    │           │                │
                    │           │ "Maintenance   │
                    │           │ Mode [Enabled/ │
                    │           │ Disabled]      │
                    │           │ successfully"  │
                    │           └────────┬───────┘
                    │                    │
                    └────────────────────┘
                                         │
                                         ▼
                                    ┌────────┐
                                    │  END   │
                                    └────────┘
```

**Maintenance Mode Effects:**

| User Role | Actions Blocked | Actions Allowed |
|-----------|----------------|-----------------|
| **Student** | Register, Drop, Change Password | View Catalog, Grades, Transcript, Timetable |
| **Instructor** | Enter Grades, Compute Finals, Change Password | View Sections, Grades, Statistics, Reports |
| **Admin** | **NONE** (Full Access) | **ALL** (Complete Control) |

**UI Indicators:**
- **Banner**: "⚠️ SYSTEM UNDER MAINTENANCE - View Only Mode" (Orange background)
- **Button States**: Disabled with tooltip explaining maintenance mode
- **Notifications**: All users notified immediately of status change

---

## 5. Sequence Diagrams

### 5.1 Login Authentication Sequence

```
┌────────┐       ┌──────────┐      ┌──────────┐     ┌────────┐      ┌──────────┐
│ User   │       │LoginPanel│      │AuthService│    │ AuthDAO│      │ Auth DB  │
└───┬────┘       └────┬─────┘      └────┬─────┘     └───┬────┘      └────┬─────┘
    │                 │                  │               │                │
    │ Enter username  │                  │               │                │
    │ and password    │                  │               │                │
    │────────────────>│                  │               │                │
    │                 │                  │               │                │
    │ Click           │                  │               │                │
    │ "Sign In"       │                  │               │                │
    │────────────────>│                  │               │                │
    │                 │                  │               │                │
    │                 │ login(username,  │               │                │
    │                 │ password)        │               │                │
    │                 │─────────────────>│               │                │
    │                 │                  │               │                │
    │                 │                  │getUserByUsername(username)     │
    │                 │                  │──────────────>│                │
    │                 │                  │               │                │
    │                 │                  │               │ SELECT * FROM  │
    │                 │                  │               │ users_auth     │
    │                 │                  │               │ WHERE username=?│
    │                 │                  │               │───────────────>│
    │                 │                  │               │                │
    │                 │                  │               │ User record +  │
    │                 │                  │               │ password_hash  │
    │                 │                  │               │<───────────────│
    │                 │                  │               │                │
    │                 │                  │ User object   │                │
    │                 │                  │<──────────────│                │
    │                 │                  │               │                │
    │                 │                  │ Check account  │                │
    │                 │                  │ lockout status │                │
    │                 │                  │───────────┐    │                │
    │                 │                  │           │    │                │
    │                 │                  │<──────────┘    │                │
    │                 │                  │               │                │
    │                 │                  ├───────────────────────────────┐
    │                 │                  │ IF locked_until > NOW:        │
    │                 │                  │   throw "Account locked"      │
    │                 │                  │<──────────────────────────────┘
    │                 │                  │               │                │
    │                 │                  │ verifyPassword(typed, hash)   │
    │                 │                  │──────────┐    │                │
    │                 │                  │          │    │                │
    │                 │                  │ BCrypt.  │    │                │
    │                 │                  │ checkpw()│    │                │
    │                 │                  │<─────────┘    │                │
    │                 │                  │               │                │
    │                 │                  ├───────────────────────────────┐
    │                 │                  │ IF password CORRECT:          │
    │                 │                  │<──────────────────────────────┘
    │                 │                  │               │                │
    │                 │                  │ resetFailedAttempts(userId)   │
    │                 │                  │──────────────>│                │
    │                 │                  │               │                │
    │                 │                  │               │ UPDATE         │
    │                 │                  │               │ failed_attempts│
    │                 │                  │               │ = 0            │
    │                 │                  │               │───────────────>│
    │                 │                  │               │                │
    │                 │                  │ updateLastLogin(userId)       │
    │                 │                  │──────────────>│                │
    │                 │                  │               │                │
    │                 │                  │               │ UPDATE         │
    │                 │                  │               │ last_login =   │
    │                 │                  │               │ NOW()          │
    │                 │                  │               │───────────────>│
    │                 │                  │               │                │
    │                 │ User object      │               │                │
    │                 │<─────────────────│               │                │
    │                 │                  │               │                │
    │                 │                  ├───────────────────────────────┐
    │                 │                  │ ELSE (password WRONG):        │
    │                 │                  │<──────────────────────────────┘
    │                 │                  │               │                │
    │                 │                  │ incrementFailedAttempts(userId)│
    │                 │                  │──────────────>│                │
    │                 │                  │               │                │
    │                 │                  │               │ UPDATE         │
    │                 │                  │               │ failed_attempts│
    │                 │                  │               │ = N+1          │
    │                 │                  │               │───────────────>│
    │                 │                  │               │                │
    │                 │                  │               ├────────────────┐
    │                 │                  │               │ IF attempts>=5:│
    │                 │                  │               │  SET status=   │
    │                 │                  │               │  'locked'      │
    │                 │                  │               │  SET locked_   │
    │                 │                  │               │  until=NOW()+  │
    │                 │                  │               │  15 minutes    │
    │                 │                  │               │<───────────────┘
    │                 │                  │               │                │
    │                 │ AuthException:   │               │                │
    │                 │ "Invalid         │               │                │
    │                 │  credentials"    │               │                │
    │                 │<─────────────────│               │                │
    │                 │                  │               │                │
    │                 ├──────────────────────────────────────────────────┐
    │                 │ IF login SUCCESS:                                │
    │                 │<─────────────────────────────────────────────────┘
    │                 │                  │               │                │
    │                 │ SessionManager.login(user)       │                │
    │                 │──────────────┐   │               │                │
    │                 │              │   │               │                │
    │                 │<─────────────┘   │               │                │
    │                 │                  │               │                │
    │                 │ Load user profile from ERP DB    │                │
    │                 │──────────────────────────────────────────────────>│
    │                 │                  │               │  (student/     │
    │                 │                  │               │  instructor)   │
    │                 │<──────────────────────────────────────────────────│
    │                 │                  │               │                │
    │                 │ Navigate to      │               │                │
    │                 │ Dashboard        │               │                │
    │                 │──────────────┐   │               │                │
    │                 │              │   │               │                │
    │                 │<─────────────┘   │               │                │
    │                 │                  │               │                │
    │ Dashboard shown │                  │               │                │
    │ (Role-specific) │                  │               │                │
    │<────────────────│                  │               │                │
    │                 │                  │               │                │
```

**Key Security Features:**
1. **Password Hashing**: BCrypt with 12 rounds
2. **Account Lockout**: 5 attempts → 15 minute lockout
3. **Failed Attempt Tracking**: Incremental counter
4. **Auto-Reset**: Successful login clears counter
5. **Database Separation**: Auth DB separate from ERP DB

---

### 5.2 Student Registration Sequence

```
┌─────────┐   ┌──────────┐   ┌──────────┐   ┌─────────┐   ┌────────┐
│ Student │   │Catalog   │   │Student   │   │Section  │   │ ERP DB │
│         │   │Panel     │   │Service   │   │DAO      │   │        │
└────┬────┘   └────┬─────┘   └────┬─────┘   └────┬────┘   └───┬────┘
     │             │               │              │            │
     │ Browse      │               │              │            │
     │ Catalog     │               │              │            │
     │────────────>│               │              │            │
     │             │               │              │            │
     │             │ getAvailableSections(semester, year)      │
     │             │──────────────>│              │            │
     │             │               │              │            │
     │             │               │ findAvailable│            │
     │             │               │ Sections()   │            │
     │             │               │─────────────>│            │
     │             │               │              │            │
     │             │               │              │ SELECT ... │
     │             │               │              │ WHERE      │
     │             │               │              │ enrollment │
     │             │               │              │ _count <   │
     │             │               │              │ capacity   │
     │             │               │              │───────────>│
     │             │               │              │            │
     │             │               │              │ Sections   │
     │             │               │              │<───────────│
     │             │               │              │            │
     │             │               │ Sections     │            │
     │             │               │<─────────────│            │
     │             │               │              │            │
     │             │ Section list  │              │            │
     │             │<──────────────│              │            │
     │             │               │              │            │
     │ Display     │               │              │            │
     │ in table    │               │              │            │
     │<────────────│               │              │            │
     │             │               │              │            │
     │ Select      │               │              │            │
     │ section     │               │              │            │
     │────────────>│               │              │            │
     │             │               │              │            │
     │ Click       │               │              │            │
     │ "Register"  │               │              │            │
     │────────────>│               │              │            │
     │             │               │              │            │
     │             │ registerForSection(studentId, sectionId)  │
     │             │──────────────>│              │            │
     │             │               │              │            │
     │             │               ├──────────────────────────┐
     │             │               │ Check Access Control:    │
     │             │               │ isActionAllowed(REGISTER)│
     │             │               │<─────────────────────────┘
     │             │               │              │            │
     │             │               │ BEGIN        │            │
     │             │               │ TRANSACTION  │            │
     │             │               │──────────────────────────>│
     │             │               │              │            │
     │             │               │ findByIdWithLock(sectionId)│
     │             │               │─────────────>│            │
     │             │               │              │            │
     │             │               │              │ SELECT ... │
     │             │               │              │ FOR UPDATE │
     │             │               │              │───────────>│
     │             │               │              │            │
     │             │               │              │ Section    │
     │             │               │              │ (LOCKED)   │
     │             │               │              │<───────────│
     │             │               │              │            │
     │             │               │ Section      │            │
     │             │               │<─────────────│            │
     │             │               │              │            │
     │             │               ├──────────────────────────┐
     │             │               │ Validate:                │
     │             │               │ 1. Deadline not passed   │
     │             │               │ 2. Seats available       │
     │             │               │ 3. Not already enrolled  │
     │             │               │ 4. Prerequisites met     │
     │             │               │ 5. No timetable clash    │
     │             │               │<─────────────────────────┘
     │             │               │              │            │
     │             │               │ create       │            │
     │             │               │ (enrollment) │            │
     │             │               │─────────────>│            │
     │             │               │              │            │
     │             │               │              │ INSERT INTO│
     │             │               │              │ enrollments│
     │             │               │              │───────────>│
     │             │               │              │            │
     │             │               │ incrementEnrollmentCount()│
     │             │               │─────────────>│            │
     │             │               │              │            │
     │             │               │              │ UPDATE     │
     │             │               │              │ sections   │
     │             │               │              │ SET        │
     │             │               │              │ enrollment │
     │             │               │              │ _count+1   │
     │             │               │              │───────────>│
     │             │               │              │            │
     │             │               │ COMMIT       │            │
     │             │               │──────────────────────────>│
     │             │               │              │            │
     │             │               │ createNotification()      │
     │             │               │ (Student + Instructor)    │
     │             │               │───────────────────────────>│
     │             │               │              │            │
     │             │ Success      │              │            │
     │             │<──────────────│              │            │
     │             │               │              │            │
     │ "Successfully│              │              │            │
     │  registered" │              │              │            │
     │<────────────│               │              │            │
     │             │               │              │            │
     │             │ Refresh      │              │            │
     │             │ Catalog &    │              │            │
     │             │ My Courses   │              │            │
     │             │──────────┐   │              │            │
     │             │          │   │              │            │
     │             │<─────────┘   │              │            │
     │             │               │              │            │
```

**Transaction Features:**
- **Atomicity**: All-or-nothing enrollment
- **Row Locking**: `FOR UPDATE` prevents race conditions
- **Consistency**: Enrollment count always accurate
- **Rollback**: Any validation failure rolls back transaction

---

## 6. Class Diagram

### 6.1 Domain Model Classes

```
╔═══════════════════════════════════════════════════════════════════╗
║                            User                                    ║
╠═══════════════════════════════════════════════════════════════════╣
║ - userId: int                                                      ║
║ - username: String                                                 ║
║ - fullName: String                                                 ║
║ - role: String                                                     ║
║ - status: String                                                   ║
║ - lastLogin: LocalDateTime                                         ║
║ - createdAt: LocalDateTime                                         ║
╠═══════════════════════════════════════════════════════════════════╣
║ + User(userId, username, fullName, role, status, ...)             ║
║ + getUserId(): int                                                 ║
║ + getUsername(): String                                            ║
║ + getFullName(): String                                            ║
║ + getRole(): String                                                ║
║ + getStatus(): String                                              ║
║ + setStatus(String): void                                          ║
║ + toString(): String                                               ║
╚═══════════════════════════════════════════════════════════════════╝
                    △                            △
                    │                            │
                    │ extends                    │ extends
                    │                            │
      ┌─────────────┴────────┐      ┌───────────┴────────┐
      │                      │      │                    │
╔═════════════════╗  ╔═══════════════════════╗  ╔══════════════╗
║   Student       ║  ║    Instructor         ║  ║ (Admin uses  ║
╠═════════════════╣  ╠═══════════════════════╣  ║  User class) ║
║ - userId: int   ║  ║ - userId: int         ║  ╚══════════════╝
║ - rollNo: String║  ║ - department: String  ║
║ - program: String║  ║ - office: String      ║
║ - year: int     ║  ╠═══════════════════════╣
╠═════════════════╣  ║ + Instructor(...)     ║
║ + Student(...)  ║  ║ + getUserId(): int    ║
║ + getUserId()   ║  ║ + getDepartment():    ║
║ + getRollNo()   ║  ║   String              ║
║ + getProgram()  ║  ║ + getOffice(): String ║
║ + getYear()     ║  ╚═══════════════════════╝
║ + setYear(int)  ║
╚═════════════════╝


╔══════════════════════════════════════════════════════════════════╗
║                           Course                                  ║
╠══════════════════════════════════════════════════════════════════╣
║ - courseId: int                                                   ║
║ - code: String                                                    ║
║ - title: String                                                   ║
║ - credits: int                                                    ║
║ - description: String                                             ║
║ - isActive: boolean                                               ║
╠══════════════════════════════════════════════════════════════════╣
║ + Course(courseId, code, title, credits, description, isActive)  ║
║ + getCourseId(): int                                              ║
║ + getCode(): String                                               ║
║ + getTitle(): String                                              ║
║ + getCredits(): int                                               ║
║ + getDescription(): String                                        ║
║ + isActive(): boolean                                             ║
║ + setActive(boolean): void                                        ║
║ + toString(): String                                              ║
╚══════════════════════════════════════════════════════════════════╝
                    │
                    │ 1:M
                    │ has
                    ▼
╔══════════════════════════════════════════════════════════════════╗
║                          Section                                  ║
╠══════════════════════════════════════════════════════════════════╣
║ - sectionId: int                                                  ║
║ - courseId: int                                                   ║
║ - instructorId: int                                               ║
║ - sectionCode: String                                             ║
║ - dayTime: String                                                 ║
║ - room: String                                                    ║
║ - capacity: int                                                   ║
║ - semester: String                                                ║
║ - year: int                                                       ║
║ - enrollmentCount: int                                            ║
║ - addDeadline: LocalDate                                          ║
║ - dropDeadline: LocalDate                                         ║
║ - courseCode: String        // From JOIN                          ║
║ - courseTitle: String       // From JOIN                          ║
║ - instructorName: String    // From JOIN                          ║
╠══════════════════════════════════════════════════════════════════╣
║ + Section(...)                                                    ║
║ + getSectionId(): int                                             ║
║ + getCourseId(): int                                              ║
║ + getInstructorId(): int                                          ║
║ + getSectionCode(): String                                        ║
║ + getDayTime(): String                                            ║
║ + getRoom(): String                                               ║
║ + getCapacity(): int                                              ║
║ + getSemester(): String                                           ║
║ + getYear(): int                                                  ║
║ + getEnrollmentCount(): int                                       ║
║ + hasAvailableSeats(): boolean                                    ║
║ + getAddDeadline(): LocalDate                                     ║
║ + getDropDeadline(): LocalDate                                    ║
║ + isBeforeAddDeadline(): boolean                                  ║
║ + isBeforeDropDeadline(): boolean                                 ║
╚══════════════════════════════════════════════════════════════════╝
                    │
                    │ 1:M
                    │ has
                    ▼
╔══════════════════════════════════════════════════════════════════╗
║                        Enrollment                                 ║
╠══════════════════════════════════════════════════════════════════╣
║ - enrollmentId: int                                               ║
║ - studentId: int                                                  ║
║ - sectionId: int                                                  ║
║ - status: String                                                  ║
║ - enrolledAt: LocalDateTime                                       ║
║ - droppedAt: LocalDateTime                                        ║
║ - finalGrade: String                                              ║
║ - courseCode: String        // From JOIN                          ║
║ - courseTitle: String       // From JOIN                          ║
║ - sectionCode: String       // From JOIN                          ║
╠══════════════════════════════════════════════════════════════════╣
║ + Enrollment(...)                                                 ║
║ + getEnrollmentId(): int                                          ║
║ + getStudentId(): int                                             ║
║ + getSectionId(): int                                             ║
║ + getStatus(): String                                             ║
║ + getEnrolledAt(): LocalDateTime                                  ║
║ + getDroppedAt(): LocalDateTime                                   ║
║ + getFinalGrade(): String                                         ║
║ + setStatus(String): void                                         ║
║ + setDroppedAt(LocalDateTime): void                               ║
║ + setFinalGrade(String): void                                     ║
╚══════════════════════════════════════════════════════════════════╝


╔══════════════════════════════════════════════════════════════════╗
║                      GradeComponent                               ║
╠══════════════════════════════════════════════════════════════════╣
║ - componentId: int                                                ║
║ - sectionId: int                                                  ║
║ - componentName: String                                           ║
║ - weight: double                                                  ║
║ - maxScore: double                                                ║
╠══════════════════════════════════════════════════════════════════╣
║ + GradeComponent(componentId, sectionId, componentName,           ║
║                  weight, maxScore)                                ║
║ + getComponentId(): int                                           ║
║ + getSectionId(): int                                             ║
║ + getComponentName(): String                                      ║
║ + getWeight(): double                                             ║
║ + getMaxScore(): double                                           ║
║ + setWeight(double): void                                         ║
║ + setMaxScore(double): void                                       ║
╚══════════════════════════════════════════════════════════════════╝
                    │
                    │ 1:M
                    │ has
                    ▼
╔══════════════════════════════════════════════════════════════════╗
║                           Grade                                   ║
╠══════════════════════════════════════════════════════════════════╣
║ - gradeId: int                                                    ║
║ - enrollmentId: int                                               ║
║ - componentId: int                                                ║
║ - score: double                                                   ║
║ - enteredAt: LocalDateTime                                        ║
╠══════════════════════════════════════════════════════════════════╣
║ + Grade(gradeId, enrollmentId, componentId, score, enteredAt)    ║
║ + getGradeId(): int                                               ║
║ + getEnrollmentId(): int                                          ║
║ + getComponentId(): int                                           ║
║ + getScore(): double                                              ║
║ + setScore(double): void                                          ║
║ + getEnteredAt(): LocalDateTime                                   ║
╚══════════════════════════════════════════════════════════════════╝


╔══════════════════════════════════════════════════════════════════╗
║                      Notification                                 ║
╠══════════════════════════════════════════════════════════════════╣
║ - notificationId: int                                             ║
║ - userId: Integer (nullable)                                      ║
║ - targetRole: String (nullable)                                   ║
║ - message: String                                                 ║
║ - isRead: boolean                                                 ║
║ - createdAt: LocalDateTime                                        ║
╠══════════════════════════════════════════════════════════════════╣
║ + Notification(...)                                               ║
║ + getNotificationId(): int                                        ║
║ + getUserId(): Integer                                            ║
║ + getTargetRole(): String                                         ║
║ + getMessage(): String                                            ║
║ + isRead(): boolean                                               ║
║ + setRead(boolean): void                                          ║
║ + getCreatedAt(): LocalDateTime                                   ║
║ + isUserSpecific(): boolean                                       ║
║ + isRoleBroadcast(): boolean                                      ║
╚══════════════════════════════════════════════════════════════════╝
```

### 6.2 Service Layer Classes

```
╔══════════════════════════════════════════════════════════════════╗
║                       StudentService                              ║
╠══════════════════════════════════════════════════════════════════╣
║ - sectionDAO: SectionDAO                                          ║
║ - enrollmentDAO: EnrollmentDAO                                    ║
║ - gradeDAO: GradeDAO                                              ║
║ - courseDAO: CourseDAO                                            ║
║ - notificationService: NotificationService                        ║
╠══════════════════════════════════════════════════════════════════╣
║ + getAllCourses(): List<Course>                                   ║
║ + getAvailableSections(semester, year): List<Section>             ║
║ + registerForSection(studentId, sectionId): void                  ║
║ + dropSection(studentId, enrollmentId): void                      ║
║ + getEnrolledCourses(studentId): List<Enrollment>                 ║
║ + getTimetable(studentId): List<Enrollment>                       ║
║ + getMyGrades(studentId): List<GradeInfo>                         ║
║ + getTranscriptData(studentId): List<TranscriptEntry>             ║
║ - validateRegistration(studentId, section): void                  ║
║ - checkPrerequisites(studentId, courseId): boolean                ║
║ - checkTimetableClash(studentId, newSection): boolean             ║
╚══════════════════════════════════════════════════════════════════╝


╔══════════════════════════════════════════════════════════════════╗
║                     InstructorService                             ║
╠══════════════════════════════════════════════════════════════════╣
║ - sectionDAO: SectionDAO                                          ║
║ - enrollmentDAO: EnrollmentDAO                                    ║
║ - gradeDAO: GradeDAO                                              ║
║ - notificationService: NotificationService                        ║
╠══════════════════════════════════════════════════════════════════╣
║ + getMySections(instructorId): List<Section>                      ║
║ + getGradeComponents(sectionId): List<GradeComponent>             ║
║ + defineGradeComponent(sectionId, name, weight, maxScore): boolean║
║ + updateGradeComponent(componentId, name, weight, maxScore): boolean║
║ + deleteGradeComponent(componentId): boolean                      ║
║ + getGradesForSection(sectionId): List<StudentGradeEntry>         ║
║ + saveSingleScore(enrollmentId, componentId, score): boolean      ║
║ + computeFinalGrades(sectionId): boolean                          ║
║ + getSectionStatistics(sectionId): Map<String, Map<String, Double>>║
║ + verifySectionOwnership(instructorId, sectionId): boolean        ║
║ - scoreToLetterGrade(score): String                               ║
╚══════════════════════════════════════════════════════════════════╝


╔══════════════════════════════════════════════════════════════════╗
║                        AdminService                               ║
╠══════════════════════════════════════════════════════════════════╣
║ - authDAO: AuthDAO                                                ║
║ - studentDAO: StudentDAO                                          ║
║ - instructorDAO: InstructorDAO                                    ║
║ - courseDAO: CourseDAO                                            ║
║ - sectionDAO: SectionDAO                                          ║
║ - notificationService: NotificationService                        ║
╠══════════════════════════════════════════════════════════════════╣
║ + getAllUsers(): List<User>                                       ║
║ + createUser(fullName, username, role, password, ...): boolean    ║
║ + updateUser(userId, fullName, status): boolean                   ║
║ + deleteUser(userId): boolean                                     ║
║ + getAllCourses(): List<Course>                                   ║
║ + createCourse(code, title, credits, desc): int                   ║
║ + updateCourse(courseId, code, title, credits, desc): boolean     ║
║ + deleteCourse(courseId): boolean                                 ║
║ + getCourseEnrollmentCount(courseId): int                         ║
║ + getAllSections(): List<Section>                                 ║
║ + createSection(courseId, instructorId, ...): boolean             ║
║ + updateSection(sectionId, ...): boolean                          ║
║ + deleteSection(sectionId): boolean                               ║
║ + bulkSetDeadlines(semester, year, addDeadline, dropDeadline): int║
║ + createAnnouncement(targetRole, message): boolean                ║
║ - createStudentProfile(userId, username, program, year): boolean  ║
║ - createInstructorProfile(userId, username): boolean              ║
║ - deleteAuthUserCompensation(userId): void                        ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## 7. Deployment Diagram

### 7.1 Physical Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT MACHINE                              │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │              Java Runtime Environment (JRE 21)                │ │
│  │                                                               │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │       University ERP Desktop Application               │ │ │
│  │  │                                                         │ │ │
│  │  │  ╔═══════════════════════════════════════════════════╗ │ │ │
│  │  │  ║         Presentation Layer (Swing UI)             ║ │ │ │
│  │  │  ║  26 Panels | FlatLaf Theme | MigLayout           ║ │ │ │
│  │  │  ╚═══════════════════════════════════════════════════╝ │ │ │
│  │  │                          │                              │ │ │
│  │  │  ╔═══════════════════════▼═══════════════════════════╗ │ │ │
│  │  │  ║         Service Layer (Business Logic)            ║ │ │ │
│  │  │  ║  7 Services | Access Control | Session Manager   ║ │ │ │
│  │  │  ╚═══════════════════════════════════════════════════╝ │ │ │
│  │  │                          │                              │ │ │
│  │  │  ╔═══════════════════════▼═══════════════════════════╗ │ │ │
│  │  │  ║         Data Access Layer (DAOs)                  ║ │ │ │
│  │  │  ║  8 DAOs | JDBC PreparedStatements                ║ │ │ │
│  │  │  ╚═══════════════════════════════════════════════════╝ │ │ │
│  │  │                          │                              │ │ │
│  │  │  ╔═══════════════════════▼═══════════════════════════╗ │ │ │
│  │  │  ║       Connection Pool Manager (HikariCP)          ║ │ │ │
│  │  │  ║  Auth Pool (Max 10) | ERP Pool (Max 15)          ║ │ │ │
│  │  │  ╚═══════════════════════════════════════════════════╝ │ │ │
│  │  │                                                         │ │ │
│  │  │  Libraries:                                             │ │ │
│  │  │  • FlatLaf 3.2.1      • jBCrypt 0.4                    │ │ │
│  │  │  • HikariCP 5.0.1     • OpenPDF 1.3.30                 │ │ │
│  │  │  • MySQL Driver 8.0.33• OpenCSV 5.7.1                  │ │ │
│  │  │  • MigLayout 11.0     • JFreeChart 1.5.4               │ │ │
│  │  │                                                         │ │ │
│  │  │  JAR: university-erp-1.0.0.jar                         │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               │ JDBC over TCP/IP
                               │ (localhost:3306)
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                        DATABASE SERVER                              │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │                    MySQL 8.0+ Server                          │ │
│  │                                                               │ │
│  │  ┌──────────────────────────┐    ┌──────────────────────────┐│ │
│  │  │  university_auth DB      │    │  university_erp DB       ││ │
│  │  ├──────────────────────────┤    ├──────────────────────────┤│ │
│  │  │ Tables:                  │    │ Tables:                  ││ │
│  │  │ • users_auth (2 cols)    │    │ • students (4 cols)      ││ │
│  │  │ • password_history       │    │ • instructors (3 cols)   ││ │
│  │  │   (4 cols)               │    │ • courses (6 cols)       ││ │
│  │  │                          │    │ • course_prerequisites   ││ │
│  │  │ Port: 3306               │    │ • sections (12 cols)     ││ │
│  │  │ User: university_user    │    │ • enrollments (7 cols)   ││ │
│  │  │ Charset: utf8mb4         │    │ • grade_components       ││ │
│  │  │                          │    │ • grades (5 cols)        ││ │
│  │  │ Purpose:                 │    │ • notifications (6 cols) ││ │
│  │  │ • Authentication         │    │ • settings (3 cols)      ││ │
│  │  │ • Password Management    │    │ • backup_logs            ││ │
│  │  │ • Account Security       │    │                          ││ │
│  │  │                          │    │ Port: 3306               ││ │
│  │  │ Security:                │    │ User: university_user    ││ │
│  │  │ • BCrypt Hashing         │    │ Charset: utf8mb4         ││ │
│  │  │ • Account Lockout        │    │                          ││ │
│  │  │ • Password History       │    │ Purpose:                 ││ │
│  │  └──────────────────────────┘    │ • Academic Data          ││ │
│  │                                  │ • Course Management      ││ │
│  │  Storage Engine: InnoDB          │ • Enrollment Tracking    ││ │
│  │  Transaction Support: Yes        │ • Grade Management       ││ │
│  │  Foreign Keys: CASCADE           └──────────────────────────┘│ │
│  │  Connection Pooling: HikariCP                                 │ │
│  │                                                               │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                      FILE SYSTEM (Backups)                          │
├─────────────────────────────────────────────────────────────────────┤
│  backups/                                                           │
│  ├── university_erp_backup_20241120_153045.sql   (2.3 MB)          │
│  ├── university_erp_backup_20241119_120000.sql   (2.1 MB)          │
│  └── university_erp_backup_20241118_180000.sql   (2.0 MB)          │
│                                                                     │
│  Backup Method: mysqldump (both databases)                          │
│  Restore Method: mysql CLI                                          │
└─────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                   EXPORTED FILES (User Downloads)                   │
├─────────────────────────────────────────────────────────────────────┤
│  User's Downloads/                                                  │
│  ├── Alice_Johnson_Transcript.pdf                                  │
│  ├── Bob_Williams_Transcript.csv                                   │
│  ├── CS101_SectionA_Grades.csv                                     │
│  └── CS101_SectionA_ClassReport.pdf                                │
│                                                                     │
│  Export Libraries: OpenPDF 1.3.30, OpenCSV 5.7.1                   │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.2 Network Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  DEVELOPMENT/SINGLE-USER DEPLOYMENT                          │
│                                                              │
│  ┌────────────────┐                  ┌──────────────────┐   │
│  │ Desktop App    │    localhost     │ MySQL Server     │   │
│  │ (JVM Process)  │◄────────────────►│ (Port 3306)      │   │
│  │                │   TCP/IP JDBC    │                  │   │
│  └────────────────┘   Connection     └──────────────────┘   │
│                                                              │
│  Connection Details:                                         │
│  • Host: localhost (127.0.0.1)                              │
│  • Port: 3306                                               │
│  • Protocol: JDBC over TCP/IP                               │
│  • Auth Pool: Max 10 connections                            │
│  • ERP Pool: Max 15 connections                             │
│  • Timeout: 30 seconds                                      │
│  • Idle Timeout: 10 minutes                                 │
└──────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│  PRODUCTION/MULTI-USER DEPLOYMENT (Future Enhancement)       │
│                                                              │
│  ┌────────────────┐     ┌────────────────┐                  │
│  │ Client App 1   │     │ Client App 2   │                  │
│  │ (Student)      │     │ (Instructor)   │                  │
│  └────────┬───────┘     └────────┬───────┘                  │
│           │                      │                          │
│           │      ┌───────────────┴───────────┐              │
│           │      │                           │              │
│           ▼      ▼                           ▼              │
│  ┌─────────────────────────────────────────────────┐        │
│  │           REST API Server (Java)                │        │
│  │  • Spring Boot                                  │        │
│  │  • JWT Authentication                           │        │
│  │  • Load Balancing                               │        │
│  └────────────────────┬────────────────────────────┘        │
│                       │                                     │
│                       ▼                                     │
│              ┌─────────────────┐                            │
│              │  MySQL Server   │                            │
│              │  (Dedicated)    │                            │
│              └─────────────────┘                            │
│                                                              │
│  Future Enhancements:                                        │
│  • Convert to client-server with REST API                   │
│  • Web frontend (React/Angular)                             │
│  • Mobile apps (iOS/Android)                                │
│  • Centralized database server                              │
│  • Load balancing for scalability                           │
│  • Redis caching layer                                      │
└──────────────────────────────────────────────────────────────┘
```

---

## 8. Component Interaction Diagram

### 8.1 Complete System Interaction Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                           USER LAYER                                 │
└────────┬─────────────────────────────────────────────────────┬───────┘
         │                                                     │
    ┌────▼────┐                                          ┌────▼────┐
    │ Student │                                          │ Instructor │
    └────┬────┘                                          └────┬────┘
         │                                                     │
         │  Actions:                                          │  Actions:
         │  • Register/Drop                                   │  • Enter Grades
         │  • View Grades                                     │  • Compute Finals
         │  • Download Transcript                             │  • View Stats
         │                                                     │
         └──────────────────────┬─────────────────────────────┘
                                │
                                │
┌───────────────────────────────▼──────────────────────────────────────┐
│                          UI LAYER (26 Components)                    │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────────┐              │
│  │ LoginPanel  │  │  Main      │  │   Dashboard     │              │
│  │             │─►│  Frame     │─►│   Panel         │              │
│  └─────────────┘  │  (Shell)   │  └─────────────────┘              │
│                   │            │                                    │
│  ┌─────────────┐  │ CardLayout │  ┌─────────────────┐              │
│  │  Student    │◄─┤  Content   │─►│   Instructor    │              │
│  │  Panels     │  │  Switching │  │   Panels        │              │
│  │  (Catalog,  │  │            │  │   (Gradebook,   │              │
│  │   Grades,   │  │            │  │    Stats)       │              │
│  │   Timetable)│  │            │  └─────────────────┘              │
│  └─────────────┘  │            │                                    │
│                   │            │  ┌─────────────────┐              │
│  ┌─────────────┐  │            │  │    Admin        │              │
│  │   Common    │◄─┤            │─►│    Panels       │              │
│  │ Components  │  │            │  │   (User Mgmt,   │              │
│  │ (Sidebar,   │  │            │  │    Course Mgmt) │              │
│  │ Breadcrumb) │  └────────────┘  └─────────────────┘              │
│  └─────────────┘                                                    │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             │ Service Method Calls
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER (7 Services)                       │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │           ACCESS CONTROL CHECKPOINT                          │   │
│  │  • Check: AccessControlService.isActionAllowed(action)      │   │
│  │  • Validate: Role permissions + Maintenance mode            │   │
│  │  • Block if unauthorized or maintenance active              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                             │                                        │
│                             │ If Authorized                          │
│                             ▼                                        │
│  ┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ StudentService  │  │InstructorService │  │  AdminService    │  │
│  │                 │  │                  │  │                  │  │
│  │ • register()    │  │ • defineComponent│  │ • createUser()   │  │
│  │ • drop()        │  │ • enterScores()  │  │ • createCourse() │  │
│  │ • getGrades()   │  │ • computeFinal() │  │ • createSection()│  │
│  │ • transcript()  │  │ • getStats()     │  │ • toggleMaint()  │  │
│  └────────┬────────┘  └────────┬─────────┘  └────────┬─────────┘  │
│           │                    │                      │             │
│           │                    │                      │             │
│  ┌────────┴──────────┐  ┌──────┴────────────┐  ┌────┴──────────┐  │
│  │NotificationService│  │MaintenanceService │  │SemesterService│  │
│  │ • notify()        │  │ • toggle()        │  │ • getCurrent()│  │
│  │ • broadcast()     │  │ • isActive()      │  │ • promote()   │  │
│  └───────────────────┘  └───────────────────┘  └───────────────┘  │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             │ DAO Method Calls
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                   DATA ACCESS LAYER (8 DAOs)                         │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────┐ │
│  │ StudentDAO   │  │InstructorDAO │  │  CourseDAO   │  │AuthDAO │ │
│  │              │  │              │  │              │  │        │ │
│  │ • findById() │  │ • findById() │  │ • getAll()   │  │• login │ │
│  │ • update()   │  │ • update()   │  │ • create()   │  │• hash  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └────────┘ │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────┐ │
│  │ SectionDAO   │  │EnrollmentDAO │  │  GradeDAO    │  │Settings│ │
│  │              │  │              │  │              │  │  DAO   │ │
│  │• findByIdWith│  │ • create()   │  │ • save()     │  │• get() │ │
│  │  Lock()      │  │ • update()   │  │ • getByEnr() │  │• set() │ │
│  │• increment() │  │ • delete()   │  │ • getBySect()│  │        │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └────────┘ │
│                                                                      │
│  Features:                                                           │
│  • PreparedStatements (SQL injection protection)                    │
│  • Transaction Management (BEGIN/COMMIT/ROLLBACK)                   │
│  • Row-Level Locking (SELECT ... FOR UPDATE)                        │
│  • Exception Handling with Logging                                  │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             │ SQL Queries via JDBC
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│              CONNECTION POOL LAYER (HikariCP)                        │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌────────────────────────────┐     ┌──────────────────────────┐   │
│  │  Auth DB Connection Pool   │     │  ERP DB Connection Pool  │   │
│  │                            │     │                          │   │
│  │  Max Connections: 10       │     │  Max Connections: 15     │   │
│  │  Min Idle: 2               │     │  Min Idle: 3             │   │
│  │  Connection Timeout: 30s   │     │  Connection Timeout: 30s │   │
│  │  Idle Timeout: 10 min      │     │  Idle Timeout: 10 min    │   │
│  │                            │     │                          │   │
│  │  Pool Name: AuthDB-Pool    │     │  Pool Name: ERP-DB-Pool  │   │
│  └────────────┬───────────────┘     └───────────┬──────────────┘   │
│               │                                 │                   │
│               │  getConnection()                │  getConnection()  │
│               │  close() → returns to pool      │  close() → returns│
└───────────────┴─────────────────────────────────┴───────────────────┘
                │                                 │
                │                                 │
                ▼                                 ▼
┌──────────────────────────┐     ┌────────────────────────────────────┐
│  university_auth (MySQL) │     │  university_erp (MySQL)            │
├──────────────────────────┤     ├────────────────────────────────────┤
│ • users_auth             │     │ • students                         │
│ • password_history       │     │ • instructors                      │
│                          │     │ • courses                          │
│ Purpose: Authentication  │     │ • sections                         │
│ Security: BCrypt hashing │     │ • enrollments                      │
│           Account lockout│     │ • grades                           │
└──────────────────────────┘     │ • grade_components                 │
                                 │ • course_prerequisites             │
                                 │ • notifications                    │
                                 │ • settings                         │
                                 │                                    │
                                 │ Purpose: Academic & Operational    │
                                 └────────────────────────────────────┘
```

**Key Interaction Patterns:**

1. **Top-Down Flow**: User → UI → Service → DAO → Database
2. **Access Control Gate**: Every service call checks permissions
3. **Connection Pooling**: Efficient database connection reuse
4. **Two-Database Strategy**: Separation of auth and business data
5. **Notification Broadcasting**: Automatic notifications on key events

---

## Summary

This comprehensive diagram documentation provides:

1. **Complete System Architecture** - Layered design with clear separation
2. **Detailed Use Cases** - All user interactions across three roles
3. **Complete ERD** - All 13 tables with relationships and constraints
4. **Process Flows** - Step-by-step registration, grading, and maintenance flows
5. **Sequence Diagrams** - Interaction patterns for authentication and registration
6. **Class Diagrams** - Domain models and service architecture
7. **Deployment Architecture** - Physical deployment and network topology
8. **Component Interactions** - Complete system interaction flow

These diagrams collectively demonstrate:
- **Clean architectural design** with proper layering
- **Comprehensive functionality** across all user roles
- **Robust data modeling** with referential integrity
- **Secure authentication** with dual-database separation
- **Professional implementation** following industry best practices

---

**End of Diagrams Document**
