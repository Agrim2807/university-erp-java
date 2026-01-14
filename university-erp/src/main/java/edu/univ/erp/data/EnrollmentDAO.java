package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);

    public Enrollment findById(int enrollmentId) throws SQLException {
        
        String sql = "SELECT e.*, c.code as course_code, s.section_code, s.day_time, s.room, u.full_name as instructor_name " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE e.enrollment_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnrollment(rs, true);
                }
            }
        }
        return null;
    }

    public List<Enrollment> findByStudent(int studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        
        String sql = "SELECT e.*, c.code as course_code, s.section_code, s.day_time, s.room, u.full_name as instructor_name " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY e.enrolled_at DESC";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs, true));
                }
            }
        }
        return enrollments;
    }

    public List<Enrollment> findActiveByStudent(int studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        
        String sql = "SELECT e.*, c.code as course_code, s.section_code, s.day_time, s.room, u.full_name as instructor_name " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE e.student_id = ? AND e.status = 'registered' " +
                     "ORDER BY c.code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs, true));
                }
            }
        }
        return enrollments;
    }

    public List<Enrollment> findBySection(int sectionId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        
        String sql = "SELECT e.*, u.full_name as student_name FROM enrollments e " +
                     "JOIN university_auth.users_auth u ON e.student_id = u.user_id " +
                     "WHERE e.section_id = ? AND e.status = 'registered' " +
                     "ORDER BY u.full_name";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = mapResultSetToEnrollment(rs, false);
                    enrollment.setStudentName(rs.getString("student_name"));
                    enrollments.add(enrollment);
                }
            }
        }
        return enrollments;
    }

    public String checkExistingEnrollment(Connection conn, int studentId, int sectionId) throws SQLException {
        String sql = "SELECT status FROM enrollments WHERE student_id = ? AND section_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }

    public int create(Connection conn, Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getSectionId());
            stmt.setString(3, enrollment.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(enrollment.getEnrolledAt()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); 
                    }
                }
            }
        }
        return -1;
    }

    public boolean updateToRegistered(Connection conn, int studentId, int sectionId) throws SQLException {
        String sql = "UPDATE enrollments SET status = 'registered', enrolled_at = ?, dropped_at = NULL " +
                     "WHERE student_id = ? AND section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, studentId);
            stmt.setInt(3, sectionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateToDropped(Connection conn, int enrollmentId) throws SQLException {
        String sql = "UPDATE enrollments SET status = 'dropped', dropped_at = ? " +
                     "WHERE enrollment_id = ? AND status = 'registered'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, enrollmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateFinalGrade(int enrollmentId, String finalGrade) throws SQLException {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, finalGrade);
            stmt.setInt(2, enrollmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int enrollmentId, String status) throws SQLException {
        String sql = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, enrollmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isEnrolled(int studentId, int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'registered'";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Enrollment findByStudentAndSection(int studentId, int sectionId) throws SQLException {
        
        String sql = "SELECT e.*, c.code as course_code, s.section_code, s.day_time, s.room, u.full_name as instructor_name " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE e.student_id = ? AND e.section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnrollment(rs, true);
                }
            }
        }
        return null;
    }

    public int countActiveEnrollments(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND status = 'registered'";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int getSectionIdByEnrollmentId(int enrollmentId) throws SQLException {
        String sql = "SELECT section_id FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("section_id");
                }
            }
        }
        return -1;
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs, boolean includeJoinedData) throws SQLException {
        Enrollment enrollment = new Enrollment(
            rs.getInt("enrollment_id"),
            rs.getInt("student_id"),
            rs.getInt("section_id"),
            rs.getString("status"),
            rs.getTimestamp("enrolled_at").toLocalDateTime(),
            rs.getTimestamp("dropped_at") != null ? rs.getTimestamp("dropped_at").toLocalDateTime() : null
        );

        if (includeJoinedData) {
            enrollment.setCourseCode(rs.getString("course_code"));
            String sectionCode = rs.getString("section_code");
            String dayTime = rs.getString("day_time");
            String room = rs.getString("room");
            enrollment.setSectionInfo(String.format("Sec %s, %s, %s",
                sectionCode != null ? sectionCode : "N/A",
                dayTime != null ? dayTime : "N/A",
                room != null ? room : "N/A"));
            enrollment.setStudentName(rs.getString("instructor_name")); 
        }

        return enrollment;
    }
}