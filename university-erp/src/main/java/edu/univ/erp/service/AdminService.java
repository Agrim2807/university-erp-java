package edu.univ.erp.service;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.util.DatabaseConfig;
import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.auth.SessionManager; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private NotificationService notificationService;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;

    
    public static class AdminServiceException extends RuntimeException {
        public AdminServiceException(String message) {
            super(message);
        }
    }

    public AdminService() {
        this.notificationService = new NotificationService();
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
    }

    

    public boolean createUser(String fullName, String username, String role, String password, String program, int year) throws AdminServiceException { 
        Connection authConn = null;
        int newUserId = -1;

        try {
            authConn = DatabaseConfig.getAuthConnection();
            authConn.setAutoCommit(false); 

            String authSql = "INSERT INTO users_auth (username, full_name, role, password_hash) VALUES (?, ?, ?, ?)";

            try (PreparedStatement authStmt = authConn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                authStmt.setString(1, username);
                authStmt.setString(2, fullName);
                authStmt.setString(3, role);
                authStmt.setString(4, PasswordUtil.hashPassword(password));

                if (authStmt.executeUpdate() == 0) {
                    throw new SQLException("Creating auth user failed, no rows affected.");
                }

                try (ResultSet rs = authStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newUserId = rs.getInt(1);
                    } else {
                        throw new SQLException("Creating auth user failed, no ID obtained.");
                    }
                }
            }
            authConn.commit(); 

            boolean profileCreated = false;
            if ("student".equals(role)) {
                profileCreated = createStudentProfile(newUserId, username, program, year);
            } else if ("instructor".equals(role)) {
                profileCreated = createInstructorProfile(newUserId, username);
            } else if ("admin".equals(role)) {
                 profileCreated = true; 
            }

            if (!profileCreated) {
                 throw new SQLException("Failed to create corresponding ERP profile.");
            }

            logger.info("Successfully created new user: {} (ID: {}) with role: {}", username, newUserId, role);
            return true;

        } catch (SQLException e) {
            if (authConn != null) {
                try {
                    authConn.rollback();
                    logger.warn("Auth transaction rolled back during user creation.");
                } catch (SQLException ex) {
                     logger.error("Failed to rollback auth transaction during user creation failure!", ex);
                }
            }
            if (newUserId != -1 && e.getMessage().contains("ERP profile")) {
                logger.warn("Rolling back auth user creation due to ERP profile failure for userId: {}", newUserId);
                deleteAuthUserCompensation(newUserId);
            }

            logger.error("Failed to create user: {}", e.getMessage());
            String userMessage = "Failed to create user.";
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                userMessage = "Username already exists. Please choose a different username.";
            }
            throw new AdminServiceException(userMessage);
        } finally {
            if (authConn != null) {
                try {
                    authConn.setAutoCommit(true); 
                    authConn.close();
                } catch (SQLException e) { logger.error("Failed to close auth connection", e); }
            }
        }
    }

    public boolean updateUserFull(int userId, String fullName, String role, String status, String password, String program, int year) throws AdminServiceException {
        Connection authConn = null;
        Connection erpConn = null;
        
        try {
            authConn = DatabaseConfig.getAuthConnection();
            authConn.setAutoCommit(false);

            
            StringBuilder authSql = new StringBuilder("UPDATE users_auth SET full_name = ?, status = ?");
            if (password != null && !password.isEmpty()) {
                authSql.append(", password_hash = ?");
            }
            authSql.append(" WHERE user_id = ?");

            try (PreparedStatement stmt = authConn.prepareStatement(authSql.toString())) {
                stmt.setString(1, fullName);
                stmt.setString(2, status);
                int paramIndex = 3;
                if (password != null && !password.isEmpty()) {
                    stmt.setString(paramIndex++, PasswordUtil.hashPassword(password));
                }
                stmt.setInt(paramIndex, userId);
                stmt.executeUpdate();
            }
            
            authConn.commit();

            
            if ("student".equals(role)) {
                erpConn = DatabaseConfig.getERPConnection();
                erpConn.setAutoCommit(false);
                
                if (getStudentProfile(userId) != null) {
                    String studentSql = "UPDATE students SET program = ?, year = ? WHERE user_id = ?";
                    try (PreparedStatement stmt = erpConn.prepareStatement(studentSql)) {
                        stmt.setString(1, program);
                        stmt.setInt(2, year);
                        stmt.setInt(3, userId);
                        stmt.executeUpdate();
                    }
                } else {
                    createStudentProfile(userId, "", program, year);
                }
                erpConn.commit();
            }
            
            return true;

        } catch (SQLException e) {
            if (authConn != null) try { authConn.rollback(); } catch (SQLException ex) {}
            if (erpConn != null) try { erpConn.rollback(); } catch (SQLException ex) {}
            logger.error("Failed to update user {}", userId, e);
            throw new AdminServiceException("Failed to update user. Please try again.");
        } finally {
            if (authConn != null) try { authConn.setAutoCommit(true); authConn.close(); } catch (SQLException e) {}
            if (erpConn != null) try { erpConn.setAutoCommit(true); erpConn.close(); } catch (SQLException e) {}
        }
    }
    
    private void deleteAuthUserCompensation(int userId) {
        logger.info("Attempting compensation: Deleting auth user {}", userId);
        String sql = "DELETE FROM university_auth.users_auth WHERE user_id = ?";
         try (Connection compConn = DatabaseConfig.getAuthConnection();
              PreparedStatement stmt = compConn.prepareStatement(sql)) {
             stmt.setInt(1, userId);
             int rows = stmt.executeUpdate();
             if (rows > 0) {
                 logger.info("Compensation successful: Deleted auth user {}", userId);
             } else {
                  logger.warn("Compensation failed: Auth user {} not found or already deleted.", userId);
             }
         } catch (SQLException e) {
             logger.error("CRITICAL: Compensation failed! Could not delete auth user {} after ERP profile failure.", userId, e);
         }
    }

    private boolean createStudentProfile(int userId, String username, String program, int year) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        String defaultRollNo = "B-" + userId;
        String finalProgram = (program != null && !program.isEmpty()) ? program : "Not Assigned";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, defaultRollNo);
            stmt.setString(3, finalProgram);
            stmt.setInt(4, year);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating student profile for userId: {}", userId, e);
            return false;
        }
    }

    private boolean createInstructorProfile(int userId, String username) {
        String sql = "INSERT INTO instructors (user_id, department, office) VALUES (?, ?, ?)";
        String defaultDept = "Not Assigned";
        String defaultOffice = "N/A";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, defaultDept);
            stmt.setString(3, defaultOffice);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating instructor profile for userId: {}", userId, e);
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name, role, status, last_login, created_at " +
                     "FROM university_auth.users_auth ORDER BY user_id";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("status"),
                    rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null,
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
        }
        return users;
    }

    public boolean setUserStatus(int userId, String status) {
        String sql = "UPDATE university_auth.users_auth SET status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for user {}", userId, e);
            return false;
        }
    }
    
    public boolean hasAssignedSections(int instructorId) {
        String sql = "SELECT COUNT(*) FROM sections WHERE instructor_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking assigned sections for instructor {}", instructorId, e);
        }
        return false;
    }

    public boolean deleteUser(int userIdToDelete, String userRole) throws AdminServiceException {
        if (userIdToDelete == SessionManager.getCurrentUserId()) {
            throw new AdminServiceException("Admin cannot delete their own account.");
        }

        
        if ("instructor".equals(userRole)) {
            String deleteSectionsSql = "DELETE FROM sections WHERE instructor_id = ?";
            try (Connection erpConn = DatabaseConfig.getERPConnection();
                 PreparedStatement stmt = erpConn.prepareStatement(deleteSectionsSql)) {
                stmt.setInt(1, userIdToDelete);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new AdminServiceException("Failed to clean up instructor sections.");
            }
        }

        String profileSql = null;
        if ("student".equals(userRole)) {
            
            decrementEnrollmentCountsForStudent(userIdToDelete);
            profileSql = "DELETE FROM students WHERE user_id = ?";
        } else if ("instructor".equals(userRole)) {
            profileSql = "DELETE FROM instructors WHERE user_id = ?";
        }

        if (profileSql != null) {
            try (Connection erpConn = DatabaseConfig.getERPConnection();
                 PreparedStatement profileStmt = erpConn.prepareStatement(profileSql)) {
                profileStmt.setInt(1, userIdToDelete);
                profileStmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error deleting profile", e);
            }
        }

        
        boolean authDeleted = false;
        String authSql = "DELETE FROM university_auth.users_auth WHERE user_id = ?";
        try (Connection authConn = DatabaseConfig.getAuthConnection();
             PreparedStatement authStmt = authConn.prepareStatement(authSql)) {
            authStmt.setInt(1, userIdToDelete);
            authDeleted = (authStmt.executeUpdate() > 0);
        } catch (SQLException e) {
            throw new AdminServiceException("Failed to delete user from auth database.");
        }
        return authDeleted;
    }
    
    
    private void decrementEnrollmentCountsForStudent(int studentId) {
        String findSql = "SELECT section_id FROM enrollments WHERE student_id = ? AND status = 'registered'";
        String updateSql = "UPDATE sections SET enrollment_count = enrollment_count - 1 WHERE section_id = ? AND enrollment_count > 0";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement findStmt = conn.prepareStatement(findSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            conn.setAutoCommit(false); 

            findStmt.setInt(1, studentId);
            try (ResultSet rs = findStmt.executeQuery()) {
                while (rs.next()) {
                    int sectionId = rs.getInt("section_id");
                    updateStmt.setInt(1, sectionId);
                    updateStmt.executeUpdate();
                }
            }

            conn.commit();
            logger.info("Decremented enrollment counts for deleted student ID: {}", studentId);

        } catch (SQLException e) {
            logger.error("Error decrementing enrollment counts for student {}. Data may be inconsistent.", studentId, e);
        }
    }


    public boolean createAnnouncement(String targetRole, String message) {
        String announcementMessage = "Announcement: " + message;
        if ("All".equals(targetRole)) return notificationService.createBroadcastNotificationToAll(announcementMessage);
        else return notificationService.createBroadcastNotification(targetRole, announcementMessage);
    }

    
    public boolean createCourse(String code, String title, int credits, String description) throws SQLException {
        Course course = new Course(0, code, title, credits, description, true);
        return courseDAO.create(course) > 0;
    }

    public List<Course> getAllCourses() {
        try { return courseDAO.findAllIncludingInactive(); } 
        catch (SQLException e) { return new ArrayList<>(); }
    }

    public boolean updateCourse(int courseId, String code, String title, int credits, String description) throws SQLException {
        Course course = new Course(courseId, code, title, credits, description, true);
        return courseDAO.update(course);
    }

    public boolean deleteCourse(int courseId) throws SQLException {
        return courseDAO.delete(courseId);
    }

    public int getCourseEnrollmentCount(int courseId) {
        String sql = "SELECT COALESCE(SUM(s.enrollment_count), 0) as total_enrollments " +
                     "FROM sections s WHERE s.course_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_enrollments");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching enrollment count for course {}", courseId, e);
        }
        return 0;
    }
    
    public List<User> getAllInstructors() {
        List<User> instructors = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name FROM university_auth.users_auth WHERE role = 'instructor' AND status = 'active' ORDER BY full_name";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"), "instructor", null, null, null));
            }
        } catch (SQLException e) { logger.error("Error fetching instructors", e); }
        return instructors;
    }

    public boolean createSection(int courseId, int instructorId, String sectionCode, String dayTime, String room, int capacity, String semester, int year, LocalDate addDeadline, LocalDate dropDeadline) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, section_code, day_time, room, capacity, semester, year, enrollment_count, add_deadline, drop_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId); stmt.setInt(2, instructorId); stmt.setString(3, sectionCode); stmt.setString(4, dayTime);
            stmt.setString(5, room); stmt.setInt(6, capacity); stmt.setString(7, semester); stmt.setInt(8, year);
            stmt.setDate(9, addDeadline != null ? java.sql.Date.valueOf(addDeadline) : null);
            stmt.setDate(10, dropDeadline != null ? java.sql.Date.valueOf(dropDeadline) : null);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id ORDER BY c.code, s.section_code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Section s = new Section(rs.getInt("section_id"), rs.getInt("course_id"), rs.getInt("instructor_id"),
                    rs.getString("section_code"), rs.getString("day_time"), rs.getString("room"),
                    rs.getInt("capacity"), rs.getString("semester"), rs.getInt("year"), rs.getInt("enrollment_count"));
                s.setCourseCode(rs.getString("course_code"));
                s.setCourseTitle(rs.getString("course_title"));
                s.setInstructorName(rs.getString("instructor_name"));
                Date ad = rs.getDate("add_deadline"); if(ad!=null) s.setAddDeadline(ad.toLocalDate());
                Date dd = rs.getDate("drop_deadline"); if(dd!=null) s.setDropDeadline(dd.toLocalDate());
                sections.add(s);
            }
        } catch (SQLException e) { logger.error("Error fetching sections", e); }
        return sections;
    }

    public boolean updateSection(int sectionId, int courseId, int instructorId, String sectionCode, String dayTime, String room, int capacity, String semester, int year, LocalDate addDeadline, LocalDate dropDeadline) throws SQLException {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, section_code = ?, day_time = ?, room = ?, capacity = ?, semester = ?, year = ?, add_deadline = ?, drop_deadline = ? WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId); stmt.setInt(2, instructorId); stmt.setString(3, sectionCode); stmt.setString(4, dayTime);
            stmt.setString(5, room); stmt.setInt(6, capacity); stmt.setString(7, semester); stmt.setInt(8, year);
            stmt.setDate(9, addDeadline != null ? java.sql.Date.valueOf(addDeadline) : null);
            stmt.setDate(10, dropDeadline != null ? java.sql.Date.valueOf(dropDeadline) : null);
            stmt.setInt(11, sectionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId); return stmt.executeUpdate() > 0;
        }
    }
    
    public int bulkUpdateDeadlines(String semester, int year, LocalDate addDeadline, LocalDate dropDeadline) throws SQLException {
        String sql = "UPDATE sections SET add_deadline = ?, drop_deadline = ? WHERE semester = ? AND year = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, addDeadline != null ? java.sql.Date.valueOf(addDeadline) : null);
            stmt.setDate(2, dropDeadline != null ? java.sql.Date.valueOf(dropDeadline) : null);
            stmt.setString(3, semester);
            stmt.setInt(4, year);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error bulk updating deadlines: {}", e.getMessage());
            throw e;
        }
    }

    

    public Student getStudentProfile(int userId) {
        try {
            StudentDAO studentDAO = new StudentDAO();
            return studentDAO.findById(userId);
        } catch (SQLException e) {
            logger.error("Error fetching student profile for userId: {}", userId, e);
            return null;
        }
    }

    public boolean updateStudentProfile(Student student) {
        try {
            StudentDAO studentDAO = new StudentDAO();
            boolean success = studentDAO.update(student);
            if (success) {
                logger.info("Successfully updated student profile for userId: {}", student.getUserId());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error updating student profile for userId: {}", student.getUserId(), e);
            return false;
        }
    }
    
    public int incrementAllStudentYears() {
        String sql = "UPDATE students SET year = year + 1 WHERE year < 5"; 
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error incrementing student years", e);
            return 0;
        }
    }
}