# University ERP System

A **production-quality desktop ERP** built using **Java 21 (Swing)** to manage the complete academic lifecycle of a university, with a strong focus on **security, correctness, and system design**.

**Authors:**  
- Agrim Upadhyay (2024046)  
- Saksham Verma (2024497)  

**Course:** Advanced Programming, IIIT Delhi  

---

## Overview

The University ERP System supports **Students, Instructors, and Administrators** with strict **Role-Based Access Control (RBAC)**, robust security mechanisms, and a clean layered architecture.  
This is a **database-centric, transaction-safe system**, not a basic CRUD application.

---

## Key Features

### Core Functionality
- Student registration & add/drop with **deadline enforcement**
- Instructor gradebook with **weighted grade computation**
- Admin management of users, courses, sections, and settings
- **System-wide Maintenance Mode** (read-only enforcement)
- Real-time notifications (user-specific & role-based)

### Security
- **BCrypt password hashing (12 rounds)**
- **Account lockout** after 5 failed attempts
- **Password history enforcement** (last 5 passwords)
- SQL injection protection using prepared statements
- **Dual-database architecture** (Auth DB + ERP DB)

### Data & Export
- CSV import/export for grades
- PDF & CSV transcript generation
- Full database **backup & restore** (mysqldump integration)

### UI/UX
- Modern FlatLaf-based UI
- Sortable tables across the application
- Global search & filtering
- Loading indicators for long operations
- Toast notifications & styled dialogs

---

## Architecture

**Pattern:** Layered Architecture



UI (Swing Panels)
↓
Service Layer (Business Logic)
↓
DAO Layer (JDBC + HikariCP)
↓
MySQL (Auth DB | ERP DB)


### Design Principles
- Separation of concerns
- Single responsibility
- Centralized access control
- Transaction-safe operations
- Concurrency-aware enrollment & grading



## Technology Stack

| Area | Technology |
|----|-----------|
| Language | Java 21 (LTS) |
| UI | Swing + FlatLaf |
| Database | MySQL 8 |
| Pooling | HikariCP |
| Security | jBCrypt |
| Build | Maven |
| Testing | JUnit 5 |
| Export | OpenPDF, OpenCSV |

---

## Database Design

### Dual Database Setup
- **`university_auth`** → credentials, password history, lockout
- **`university_erp`** → courses, sections, enrollments, grades

### Highlights
- Strong foreign keys & constraints
- CHECK constraints for add/drop deadlines
- Transactional enrollment (`SELECT … FOR UPDATE`)
- Grade invariants enforced at service layer

---

## Role-Based Access Control

| Action | Student | Instructor | Admin |
|------|--------|------------|-------|
| Register / Drop | Yes | No | Yes |
| Manage Grades | No | Own Sections | All |
| Manage Users | No | No | Yes |
| Maintenance Toggle | No | No | Yes |
| Backup / Restore | No | No | Yes |

Maintenance Mode blocks **all write operations** for students & instructors.

---

## Grade Computation

**Formula:**
```

Final Score = Σ (score / maxScore) × weight

````

- Weights must sum to **100%**
- Letter grades: **A / B / C / D / F**
- Missing components block finalization
- Students auto-notified on grade publication

---

## Testing

- **35 unit tests**, 100% pass rate
- Covers:
  - RBAC & maintenance mode
  - BCrypt hashing & lockout logic
  - Grade computation edge cases
- All tests are **pure unit tests** (no DB dependency)

Run:
```bash
mvn test
````

---

## Installation (Quick Start)

```bash
# Setup databases
mysql -u root -p < sql_files/mysql_setup.sql

# Build
mvn clean package

# Run
java -jar target/university-erp-1.0.0.jar
```

Demo users (admin, instructor, students) are included.

---

## Why This Project Stands Out

* Not a CRUD app — emphasizes **invariants, transactions, and concurrency**
* Strong **security-first design**
* Clean separation of authentication and business data
* Realistic ERP-level system complexity
* Resume-grade **systems & backend engineering** project

---

## Future Enhancements

* GPA & analytics dashboards
* Audit logging
* Web / REST frontend
* Waitlists & scheduling optimization
* Email & push notifications

---

## License

Academic / educational use.
