package edu.univ.erp.service;

import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    
    public Map<String, Object> getAdminDashboardData() {
        Map<String, Object> data = new HashMap<>();

        try (Connection authConn = DatabaseConfig.getAuthConnection();
             Connection erpConn = DatabaseConfig.getERPConnection()) {

            
            String userSql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN role = 'student' THEN 1 ELSE 0 END) as students, " +
                "SUM(CASE WHEN role = 'instructor' THEN 1 ELSE 0 END) as instructors, " +
                "SUM(CASE WHEN role = 'admin' THEN 1 ELSE 0 END) as admins, " +
                "SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as active " +
                "FROM users_auth";
            try (PreparedStatement stmt = authConn.prepareStatement(userSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    data.put("totalUsers", rs.getInt("total"));
                    data.put("totalStudents", rs.getInt("students"));
                    data.put("totalInstructors", rs.getInt("instructors"));
                    data.put("totalAdmins", rs.getInt("admins"));
                    data.put("activeUsers", rs.getInt("active"));
                }
            }

            
            String courseSql = "SELECT COUNT(*) as total FROM courses WHERE is_active = true";
            try (PreparedStatement stmt = erpConn.prepareStatement(courseSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) data.put("totalCourses", rs.getInt("total"));
            }

            String sectionSql = "SELECT COUNT(*) as total, SUM(enrollment_count) as enrolled FROM sections";
            try (PreparedStatement stmt = erpConn.prepareStatement(sectionSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    data.put("totalSections", rs.getInt("total"));
                    data.put("totalEnrollments", rs.getInt("enrolled"));
                }
            }

            
            String maintenanceSql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'";
            try (PreparedStatement stmt = erpConn.prepareStatement(maintenanceSql);
                 ResultSet rs = stmt.executeQuery()) {
                data.put("maintenanceMode", rs.next() && "true".equals(rs.getString(1)));
            }

        } catch (SQLException e) {
            logger.error("Error fetching admin dashboard data", e);
        }

        return data;
    }

    
    public Map<String, Object> getStudentDashboardData(int studentId) {
        Map<String, Object> data = new HashMap<>();

        try (Connection conn = DatabaseConfig.getERPConnection()) {

            
            String enrolledSql = "SELECT COUNT(*) as count FROM enrollments WHERE student_id = ? AND status = 'registered'";
            try (PreparedStatement stmt = conn.prepareStatement(enrolledSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) data.put("enrolledCourses", rs.getInt("count"));
                }
            }

            
            String pendingSql = "SELECT COUNT(*) as count FROM enrollments WHERE student_id = ? AND status = 'registered' AND final_grade IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(pendingSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) data.put("pendingGrades", rs.getInt("count"));
                }
            }

            
            String completedSql = "SELECT COUNT(*) as count FROM enrollments WHERE student_id = ? AND final_grade IS NOT NULL";
            try (PreparedStatement stmt = conn.prepareStatement(completedSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) data.put("completedCourses", rs.getInt("count"));
                }
            }

            
            String cgpaSql = "SELECT final_grade FROM enrollments WHERE student_id = ? AND final_grade IS NOT NULL";
            try (PreparedStatement stmt = conn.prepareStatement(cgpaSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    double totalPoints = 0;
                    int count = 0;
                    while (rs.next()) {
                        totalPoints += gradeToPoint(rs.getString("final_grade"));
                        count++;
                    }
                    data.put("cgpa", count > 0 ? String.format("%.2f", totalPoints / count) : "N/A");
                    data.put("totalCredits", count * 3); 
                }
            }

            
            String nextClassSql = "SELECT c.code, s.section_code, s.day_time, s.room " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "WHERE e.student_id = ? AND e.status = 'registered' " +
                "ORDER BY s.day_time LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(nextClassSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        data.put("nextCourse", rs.getString("code") + " - Sec " + rs.getString("section_code"));
                        data.put("nextClassTime", rs.getString("day_time"));
                        data.put("nextClassRoom", rs.getString("room"));
                    } else {
                        data.put("nextCourse", "No Classes");
                        data.put("nextClassTime", "Not enrolled in any courses");
                        data.put("nextClassRoom", "");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching student dashboard data for {}", studentId, e);
        }

        return data;
    }

    
    public Map<String, Object> getInstructorDashboardData(int instructorId) {
        Map<String, Object> data = new HashMap<>();

        try (Connection conn = DatabaseConfig.getERPConnection()) {

            
            String sectionsSql = "SELECT COUNT(*) as count FROM sections WHERE instructor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sectionsSql)) {
                stmt.setInt(1, instructorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) data.put("totalSections", rs.getInt("count"));
                }
            }

            
            String studentsSql = "SELECT SUM(s.enrollment_count) as count FROM sections s WHERE s.instructor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(studentsSql)) {
                stmt.setInt(1, instructorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) data.put("totalStudents", rs.getInt("count"));
                }
            }

            
            String pendingSql = "SELECT COUNT(DISTINCT s.section_id) as count FROM sections s " +
                "JOIN enrollments e ON s.section_id = e.section_id " +
                "WHERE s.instructor_id = ? AND e.status = 'registered' AND e.final_grade IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(pendingSql)) {
                stmt.setInt(1, instructorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) data.put("pendingGrades", rs.getInt("count"));
                }
            }

            
            String actionSql = "SELECT c.code, s.section_code, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id AND e.status = 'registered' AND e.final_grade IS NULL) as pending " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "WHERE s.instructor_id = ? " +
                "HAVING pending > 0 " +
                "ORDER BY pending DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(actionSql)) {
                stmt.setInt(1, instructorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        data.put("actionCourse", rs.getString("code") + " Section " + rs.getString("section_code"));
                        data.put("actionPending", rs.getInt("pending") + " students need grades");
                    } else {
                        data.put("actionCourse", "All Grades Submitted");
                        data.put("actionPending", "Great job! No pending grades.");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching instructor dashboard data for {}", instructorId, e);
        }

        return data;
    }

    private double gradeToPoint(String grade) {
        if (grade == null) return 0;
        switch (grade) {
            case "A": return 4.0;
            case "B": return 3.0;
            case "C": return 2.0;
            case "D": return 1.0;
            default: return 0.0;
        }
    }
}
