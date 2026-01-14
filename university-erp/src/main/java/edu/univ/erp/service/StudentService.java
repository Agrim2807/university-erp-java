package edu.univ.erp.service;

import edu.univ.erp.domain.*; 
import edu.univ.erp.util.DatabaseConfig;
import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.StudentDAO; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private NotificationService notificationService;
    private StudentDAO studentDAO; 

    public static class StudentServiceException extends Exception {
        public StudentServiceException(String message) {
            super(message);
        }
    }

    public StudentService() {
        this.notificationService = new NotificationService();
        this.studentDAO = new StudentDAO(); 
    }

    
    public Student getStudentProfile(int userId) {
        try {
            return studentDAO.findById(userId);
        } catch (SQLException e) {
            logger.error("Error fetching student profile", e);
            return null;
        }
    }
    

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, code, title, credits, description, is_active FROM courses WHERE is_active = true";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(new Course(
                    rs.getInt("course_id"), rs.getString("code"), rs.getString("title"),
                    rs.getInt("credits"), rs.getString("description"), rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) { logger.error("Error fetching all courses", e); }
        return courses;
    }

    public List<Section> getAvailableSections(String semester, int year) {
        return getAvailableSectionsForStudent(semester, year, -1); 
    }

    
    public List<Section> getAvailableSectionsForStudent(String semester, int year, int studentId) {
        List<Section> sections = new ArrayList<>();
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT s.section_id, s.course_id, s.instructor_id, s.section_code, ")
                  .append("s.day_time, s.room, s.capacity, s.semester, s.year, s.enrollment_count, ")
                  .append("s.add_deadline, s.drop_deadline, ")
                  .append("c.code as course_code, c.title as course_title, u.full_name as instructor_name ")
                  .append("FROM sections s ")
                  .append("JOIN courses c ON s.course_id = c.course_id ")
                  .append("JOIN university_auth.users_auth u ON s.instructor_id = u.user_id ")
                  .append("WHERE s.enrollment_count < s.capacity AND s.semester = ? AND s.year = ? ");

        
        
        if (studentId > 0) {
            sqlBuilder.append("AND s.section_id NOT IN ")
                      .append("(SELECT e.section_id FROM enrollments e WHERE e.student_id = ? AND e.status = 'registered') ")
                      .append("AND s.course_id NOT IN ")
                      .append("(SELECT sec.course_id FROM enrollments e2 ")
                      .append(" JOIN sections sec ON e2.section_id = sec.section_id ")
                      .append(" WHERE e2.student_id = ? AND e2.status = 'registered') ");
        }
        sqlBuilder.append("ORDER BY c.code, s.section_code");

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            stmt.setString(1, semester);
            stmt.setInt(2, year);
            if (studentId > 0) {
                stmt.setInt(3, studentId);
                stmt.setInt(4, studentId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Section section = new Section(
                        rs.getInt("section_id"), rs.getInt("course_id"), rs.getInt("instructor_id"),
                        rs.getString("section_code"), rs.getString("day_time"), rs.getString("room"),
                        rs.getInt("capacity"), rs.getString("semester"), rs.getInt("year"),
                        rs.getInt("enrollment_count")
                    );
                    section.setCourseCode(rs.getString("course_code"));
                    section.setCourseTitle(rs.getString("course_title"));
                    section.setInstructorName(rs.getString("instructor_name"));

                    Date addDeadline = rs.getDate("add_deadline");
                    if (addDeadline != null) {
                        section.setAddDeadline(addDeadline.toLocalDate());
                    }
                    Date dropDeadline = rs.getDate("drop_deadline");
                    if (dropDeadline != null) {
                        section.setDropDeadline(dropDeadline.toLocalDate());
                    }

                    sections.add(section);
                }
            }
        } catch (SQLException e) { logger.error("Error fetching available sections", e); }
        return sections;
    }

    public void registerForSection(int studentId, int sectionId) throws SQLException, StudentServiceException {
        if (!AccessControlService.isActionAllowed(AccessControlService.REGISTER_SECTION)) {
            throw new StudentServiceException("Access Denied: You cannot register for courses right now.");
        }

        Connection conn = null;
        Section sectionDetails = null;

        try {
            conn = DatabaseConfig.getERPConnection();
            conn.setAutoCommit(false);

            SectionDAO sectionDAO = new SectionDAO();
            sectionDetails = sectionDAO.findByIdWithLock(conn, sectionId);
            if (sectionDetails == null) {
                throw new StudentServiceException("Section details not found. Registration failed.");
            }

            CourseDAO courseDAO = new CourseDAO();
            if (!courseDAO.hasCompletedPrerequisites(studentId, sectionDetails.getCourseId())) {
                throw new StudentServiceException(
                    "You have not completed the required prerequisites for this course. " +
                    "Please check the course requirements."
                );
            }

            // Deadline is now mandatory (NOT NULL in database)
            if (LocalDate.now().isAfter(sectionDetails.getAddDeadline())) {
                throw new StudentServiceException(
                    "The add/registration deadline for this section has passed (" +
                    sectionDetails.getAddDeadline() + "). Registration is no longer allowed."
                );
            }

            String checkSql = "SELECT status FROM enrollments WHERE student_id = ? AND section_id = ? FOR UPDATE";
            String currentStatus = null;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, sectionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("status");
                    }
                }
            }

            if ("registered".equals(currentStatus)) {
                throw new StudentServiceException("You are already registered for this section.");
            }

            if (!hasAvailableSeats(conn, sectionId)) {
                throw new StudentServiceException("Section full.");
            }

            
            if (hasTimeTableClash(conn, studentId, sectionDetails.getDayTime(), sectionId)) {
                throw new StudentServiceException(
                    "Timetable clash: This section's schedule conflicts with another course you're registered for."
                );
            }

            if ("dropped".equals(currentStatus)) {
                String updateSql = "UPDATE enrollments SET status = 'registered', enrolled_at = ?, dropped_at = NULL WHERE student_id = ? AND section_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setInt(2, studentId);
                    stmt.setInt(3, sectionId);
                    if (stmt.executeUpdate() == 0) {
                        throw new SQLException("Re-enrollment failed, no rows affected.");
                    }
                }
                logger.info("Student {} successfully re-enrolled in section {}", studentId, sectionId);

            } else {
                String insertSql = "INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES (?, ?, 'registered', ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setInt(1, studentId);
                    stmt.setInt(2, sectionId);
                    stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    if (stmt.executeUpdate() == 0) {
                        throw new SQLException("Creating enrollment failed, no rows affected.");
                    }
                }
                 logger.info("Student {} successfully registered for section {}", studentId, sectionId);
            }

            if (!updateEnrollmentCount(conn, sectionId, 1)) {
                 throw new SQLException("Updating section count failed.");
            }

            conn.commit();
            
            try {
                String courseCode = sectionDetails.getCourseCode() != null ? sectionDetails.getCourseCode() : "Unknown Course";
                String studentName = getStudentName(studentId); 
                notificationService.createUserNotification(studentId, 
                    "Successfully registered for " + courseCode + " - " + sectionDetails.getSectionCode());
                notificationService.createUserNotification(sectionDetails.getInstructorId(), 
                    "Student " + studentName + " (ID: " + studentId + ") has enrolled in your section " + courseCode + " - " + sectionDetails.getSectionCode());
            } catch (Exception e) {
                logger.error("Failed to create notifications for registration: {}", e.getMessage());
            }

        } catch (SQLException | StudentServiceException e) {
            if (conn != null) {
                try { conn.rollback(); logger.warn("Transaction rolled back for student {} and section {}: {}", studentId, sectionId, e.getMessage()); }
                catch (SQLException ex) { logger.error("CRITICAL: Failed to rollback transaction!", ex); }
            }
            
            if (e instanceof StudentServiceException && sectionDetails != null) {
                 try {
                     notificationService.createUserNotification(studentId, 
                        "Failed to register for " + sectionDetails.getCourseCode() + ": " + e.getMessage());
                 } catch (Exception ne) {
                     logger.error("Failed to create failure notification: {}", ne.getMessage());
                 }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException e) { logger.error("Failed to close connection", e); }
            }
        }
    }


    private boolean isAlreadyEnrolled(int studentId, int sectionId) {
        String sql = "SELECT status FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "registered".equals(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking enrollment status", e);
        }
        return false;
    }

    private boolean hasAvailableSeats(Connection conn, int sectionId) throws SQLException {
        String sql = "SELECT capacity, enrollment_count FROM sections WHERE section_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("enrollment_count") < rs.getInt("capacity");
            }
        }
        return false;
    }

    private boolean updateEnrollmentCount(Connection conn, int sectionId, int change) throws SQLException {
        if (change < 0) {
            String sql = "UPDATE sections SET enrollment_count = GREATEST(0, enrollment_count + ?) WHERE section_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, change);
                stmt.setInt(2, sectionId);
                return stmt.executeUpdate() > 0;
            }
        } else {
            String sql = "UPDATE sections SET enrollment_count = enrollment_count + ? WHERE section_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, change);
                stmt.setInt(2, sectionId);
                return stmt.executeUpdate() > 0;
            }
        }
    }

    public void dropSection(int studentId, int enrollmentId) throws SQLException, StudentServiceException {
        if (!AccessControlService.isActionAllowed(AccessControlService.DROP_SECTION)) {
            throw new StudentServiceException("Access Denied: You cannot drop courses right now.");
        }

        int sectionId = -1;
        Section sectionDetails = null;
        LocalDate dropDeadline = null;

        String checkSql = "SELECT s.section_id, s.instructor_id, s.drop_deadline, c.code as course_code, s.section_code " +
                          "FROM enrollments e " +
                          "JOIN sections s ON e.section_id = s.section_id " +
                          "JOIN courses c ON s.course_id = c.course_id " +
                          "WHERE e.enrollment_id = ? AND e.student_id = ? AND e.status = 'registered'";

        try (Connection connCheck = DatabaseConfig.getERPConnection();
             PreparedStatement checkStmt = connCheck.prepareStatement(checkSql)) {
            checkStmt.setInt(1, enrollmentId);
            checkStmt.setInt(2, studentId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    sectionId = rs.getInt("section_id");
                    sectionDetails = new Section(sectionId, 0, rs.getInt("instructor_id"), rs.getString("section_code"), null, null, 0, null, 0, 0);
                    sectionDetails.setCourseCode(rs.getString("course_code"));

                    Date deadlineDate = rs.getDate("drop_deadline");
                    if (deadlineDate != null) {
                        dropDeadline = deadlineDate.toLocalDate();
                    }
                } else {
                    throw new StudentServiceException("Enrollment not found, already dropped, or does not belong to you.");
                }
            }
        } catch (SQLException e) {
             logger.error("Database error checking enrollment for drop: {}", e.getMessage());
             throw new StudentServiceException("Database error checking enrollment.");
        }

        // Deadline is now mandatory (NOT NULL in database)
        if (LocalDate.now().isAfter(dropDeadline)) {
            throw new StudentServiceException(
                "The drop deadline for this section has passed (" +
                dropDeadline + "). You can no longer drop this course."
            );
        }


        Connection conn = null;
        try {
            conn = DatabaseConfig.getERPConnection();
            conn.setAutoCommit(false);

            String dropSql = "UPDATE enrollments SET status = 'dropped', dropped_at = ? WHERE enrollment_id = ? AND status = 'registered'";
            try (PreparedStatement stmt = conn.prepareStatement(dropSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(2, enrollmentId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Dropping enrollment failed, status may have changed or row not found.");
                }
            }

            if (!updateEnrollmentCount(conn, sectionId, -1)) {
                 throw new SQLException("Updating section count failed during drop.");
            }

            conn.commit();
            logger.info("Student {} successfully dropped enrollment {}", studentId, enrollmentId);

            try {
                String courseCode = sectionDetails.getCourseCode() != null ? sectionDetails.getCourseCode() : "Unknown Course";
                String studentName = getStudentName(studentId);
                notificationService.createUserNotification(studentId, 
                    "Successfully dropped " + courseCode + " - " + sectionDetails.getSectionCode());
                notificationService.createUserNotification(sectionDetails.getInstructorId(), 
                    "Student " + studentName + " (ID: " + studentId + ") has dropped your section " + courseCode + " - " + sectionDetails.getSectionCode());
            } catch (Exception e) {
                logger.error("Failed to create notifications for drop: {}", e.getMessage());
            }

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); logger.warn("Drop transaction rolled back for student {} enrollment {}: {}", studentId, enrollmentId, e.getMessage()); }
                catch (SQLException ex) { logger.error("CRITICAL: Failed to rollback drop transaction!", ex); }
            }
            
            if (sectionDetails != null) {
                 try {
                     notificationService.createUserNotification(studentId, 
                        "Failed to drop " + sectionDetails.getCourseCode() + ": " + e.getMessage());
                 } catch (Exception ne) {
                     logger.error("Failed to create failure notification: {}", ne.getMessage());
                 }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException e) { logger.error("Failed to close connection after drop", e); }
            }
        }
    }

    public List<Enrollment> getMyRegisteredSections(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();

        // Get current semester and year - only show current semester courses
        // (timetable is semester-specific, historical courses shown in transcript)
        String currentSemester = SemesterService.getCurrentSemester();
        int currentYear = SemesterService.getCurrentYear();

        String sql = "SELECT e.*, c.code as course_code, s.section_code, s.day_time, s.room, s.drop_deadline, " +
                     "s.semester, s.year, u.full_name as instructor_name " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE e.student_id = ? AND e.status = 'registered' " +
                     "AND s.semester = ? AND s.year = ?";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, currentSemester);
            stmt.setInt(3, currentYear);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment en = new Enrollment(
                        rs.getInt("enrollment_id"), rs.getInt("student_id"), rs.getInt("section_id"),
                        rs.getString("status"), rs.getTimestamp("enrolled_at").toLocalDateTime(),
                        rs.getTimestamp("dropped_at") != null ? rs.getTimestamp("dropped_at").toLocalDateTime() : null
                    );
                    en.setCourseCode(rs.getString("course_code"));
                    String sectionCode = rs.getString("section_code");
                    String dayTime = rs.getString("day_time");
                    String room = rs.getString("room");
                    en.setSectionInfo(String.format("Sec %s, %s, %s",
                        sectionCode != null ? sectionCode : "N/A",
                        dayTime != null ? dayTime : "N/A",
                        room != null ? room : "N/A"));
                    en.setStudentName(rs.getString("instructor_name")); 

                    java.sql.Date dropDeadlineDate = rs.getDate("drop_deadline");
                    en.setDropDeadline(dropDeadlineDate != null ? dropDeadlineDate.toLocalDate() : null);

                    enrollments.add(en);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching registered sections for student {}", studentId, e);
        }
        return enrollments;
    }

    public List<GradeInfo> getMyGrades(int studentId) {
        List<GradeInfo> grades = new ArrayList<>();

        // Only show grades for current semester courses
        String currentSemester = SemesterService.getCurrentSemester();
        int currentYear = SemesterService.getCurrentYear();

        String sql = "SELECT c.code as course_code, s.section_code, s.semester, s.year, " +
                     "gc.component_name, gc.max_score, gc.weight, g.score, e.final_grade " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "LEFT JOIN grade_components gc ON s.section_id = gc.section_id " +
                     "LEFT JOIN grades g ON gc.component_id = g.component_id AND e.enrollment_id = g.enrollment_id " +
                     "WHERE e.student_id = ? AND e.status = 'registered' " +
                     "AND s.semester = ? AND s.year = ? " +
                     "ORDER BY c.code, gc.component_id";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, currentSemester);
            stmt.setInt(3, currentYear);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double score = rs.getDouble("score");
                    if (rs.wasNull()) { score = 0.0; } 
                    grades.add(new GradeInfo(
                        rs.getString("course_code"), rs.getString("section_code"),
                        rs.getString("component_name"), score,
                        rs.getDouble("max_score"), rs.getDouble("weight"),
                        rs.getString("final_grade")
                    ));
                }
            }
        } catch (SQLException e) { logger.error("Error fetching grades for student {}", studentId, e); }
        return grades;
    }

    public List<TranscriptEntry> getTranscriptData(int studentId) {
        List<TranscriptEntry> entries = new ArrayList<>();
        String sql = "SELECT c.code, c.title, c.credits, s.semester, s.year, e.final_grade, e.enrollment_id " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "WHERE e.student_id = ? AND (e.status = 'registered' OR e.status = 'completed') " + 
                     "ORDER BY s.year, s.semester, c.code";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TranscriptEntry entry = new TranscriptEntry(
                        rs.getString("code"), rs.getString("title"), rs.getInt("credits"),
                        rs.getString("semester"), rs.getInt("year"), rs.getString("final_grade")
                    );

                    int enrollmentId = rs.getInt("enrollment_id");
                    List<String> componentDetails = getComponentGradesForEnrollment(enrollmentId);
                    entry.setComponentGrades(componentDetails);

                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching transcript data for student {}", studentId, e);
        }
        return entries;
    }

    private List<String> getComponentGradesForEnrollment(int enrollmentId) {
        List<String> components = new ArrayList<>();
        String sql = "SELECT gc.component_name, g.score, gc.max_score, gc.weight " +
                     "FROM grades g " +
                     "JOIN grade_components gc ON g.component_id = gc.component_id " +
                     "WHERE g.enrollment_id = ? " +
                     "ORDER BY gc.component_id";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String componentInfo = String.format("%s: %.1f/%.0f (%.0f%%)",
                        rs.getString("component_name"),
                        rs.getDouble("score"),
                        rs.getDouble("max_score"),
                        rs.getDouble("weight"));
                    components.add(componentInfo);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching component grades for enrollment {}", enrollmentId, e);
        }
        return components;
    }
    
    private String getStudentName(int studentId) {
        String sql = "SELECT full_name FROM university_auth.users_auth WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching name for {}: {}", studentId, e.getMessage());
        }
        return "Unknown Student";
    }

    
    private boolean hasTimeTableClash(Connection conn, int studentId, String newSectionDayTime, int newSectionId) throws SQLException {
        if (newSectionDayTime == null || newSectionDayTime.trim().isEmpty()) {
            return false; 
        }

        String sql = "SELECT s.section_id, s.day_time, c.code as course_code " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "WHERE e.student_id = ? AND e.status = 'registered' AND s.section_id != ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, newSectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String existingDayTime = rs.getString("day_time");
                    if (existingDayTime != null && doTimeSlotsOverlap(newSectionDayTime, existingDayTime)) {
                        logger.warn("Timetable clash detected for student {}: {} conflicts with {}",
                            studentId, newSectionDayTime, existingDayTime);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    
    private boolean doTimeSlotsOverlap(String slot1, String slot2) {
        if (slot1 == null || slot2 == null) return false;

        
        String[] parts1 = slot1.split(" ");
        String[] parts2 = slot2.split(" ");

        if (parts1.length < 2 || parts2.length < 2) return false;

        String days1 = parts1[0].toLowerCase();
        String days2 = parts2[0].toLowerCase();
        String time1 = parts1[1];
        String time2 = parts2[1];

        
        String[] dayList1 = days1.split("/");
        String[] dayList2 = days2.split("/");

        boolean daysOverlap = false;
        for (String d1 : dayList1) {
            for (String d2 : dayList2) {
                if (d1.trim().equalsIgnoreCase(d2.trim())) {
                    daysOverlap = true;
                    break;
                }
            }
            if (daysOverlap) break;
        }

        if (!daysOverlap) return false;

        
        try {
            int start1 = parseTimeToMinutes(time1.split("-")[0]);
            int end1 = time1.contains("-") ? parseTimeToMinutes(time1.split("-")[1]) : start1 + 60; 

            int start2 = parseTimeToMinutes(time2.split("-")[0]);
            int end2 = time2.contains("-") ? parseTimeToMinutes(time2.split("-")[1]) : start2 + 60;

            
            return (start1 < end2) && (start2 < end1);
        } catch (Exception e) {
            logger.warn("Could not parse time slots for comparison: {} vs {}", slot1, slot2);
            return false;
        }
    }

    
    private int parseTimeToMinutes(String time) {
        if (time == null || !time.contains(":")) return 0;
        String[] parts = time.trim().split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}