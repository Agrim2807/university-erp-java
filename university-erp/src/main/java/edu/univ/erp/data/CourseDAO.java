package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    public Course findById(int courseId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }
        return null;
    }

    public Course findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM courses WHERE code = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }
        return null;
    }

    public List<Course> findAll() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE is_active = true ORDER BY code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        return courses;
    }

    public List<Course> findAllIncludingInactive() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        return courses;
    }

    public int create(Course course) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits, description, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());
            stmt.setString(4, course.getDescription());
            stmt.setBoolean(5, course.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ?, description = ?, is_active = ? WHERE course_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());
            stmt.setString(4, course.getDescription());
            stmt.setBoolean(5, course.isActive());
            stmt.setInt(6, course.getCourseId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existsByCode(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM courses WHERE code = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    
    public List<Course> getPrerequisites(int courseId) throws SQLException {
        List<Course> prerequisites = new ArrayList<>();
        String sql = "SELECT c.* FROM courses c " +
                     "JOIN course_prerequisites cp ON c.course_id = cp.requires_course_id " +
                     "WHERE cp.course_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prerequisites.add(mapResultSetToCourse(rs));
                }
            }
        }
        return prerequisites;
    }

    
    public boolean addPrerequisite(int courseId, int prerequisiteCourseId) throws SQLException {
        String sql = "INSERT INTO course_prerequisites (course_id, requires_course_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, prerequisiteCourseId);
            return stmt.executeUpdate() > 0;
        }
    }

    
    public boolean removePrerequisite(int courseId, int prerequisiteCourseId) throws SQLException {
        String sql = "DELETE FROM course_prerequisites WHERE course_id = ? AND requires_course_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, prerequisiteCourseId);
            return stmt.executeUpdate() > 0;
        }
    }

    
    public boolean hasCompletedPrerequisites(int studentId, int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) as missing FROM course_prerequisites cp " +
                     "WHERE cp.course_id = ? " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM enrollments e " +
                     "    JOIN sections s ON e.section_id = s.section_id " +
                     "    WHERE e.student_id = ? " +
                     "    AND s.course_id = cp.requires_course_id " +
                     "    AND (e.status = 'completed' OR e.status = 'registered') " +
                     "    AND e.final_grade IS NOT NULL " +
                     "    AND e.final_grade NOT IN ('F', 'W', 'I', '')" +
                     ")";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("missing") == 0;
                }
            }
        }
        return false;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        return new Course(
            rs.getInt("course_id"),
            rs.getString("code"),
            rs.getString("title"),
            rs.getInt("credits"),
            rs.getString("description"),
            rs.getBoolean("is_active")
        );
    }
}
