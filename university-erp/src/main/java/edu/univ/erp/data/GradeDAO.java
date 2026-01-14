package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.GradeInfo;
import edu.univ.erp.domain.StudentGradeEntry;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeDAO {
    private static final Logger logger = LoggerFactory.getLogger(GradeDAO.class);

    public List<GradeComponent> findComponentsBySection(int sectionId) throws SQLException {
        List<GradeComponent> components = new ArrayList<>();
        String sql = "SELECT * FROM grade_components WHERE section_id = ? ORDER BY component_id";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    components.add(mapResultSetToComponent(rs));
                }
            }
        }
        return components;
    }

    public int createComponent(GradeComponent component) throws SQLException {
        String sql = "INSERT INTO grade_components (section_id, component_name, weight, max_score) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, component.getSectionId());
            stmt.setString(2, component.getComponentName());
            stmt.setDouble(3, component.getWeight());
            stmt.setDouble(4, component.getMaxScore());

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

    public boolean updateComponent(GradeComponent component) throws SQLException {
        String sql = "UPDATE grade_components SET component_name = ?, weight = ?, max_score = ? WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, component.getComponentName());
            stmt.setDouble(2, component.getWeight());
            stmt.setDouble(3, component.getMaxScore());
            stmt.setInt(4, component.getComponentId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteComponent(int componentId) throws SQLException {
        String sql = "DELETE FROM grade_components WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, componentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public double getTotalWeightForSection(int sectionId) throws SQLException {
        String sql = "SELECT SUM(weight) as total_weight FROM grade_components WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_weight");
                }
            }
        }
        return 0.0;
    }

    public Grade findGrade(int enrollmentId, int componentId) throws SQLException {
        String sql = "SELECT * FROM grades WHERE enrollment_id = ? AND component_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.setInt(2, componentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGrade(rs);
                }
            }
        }
        return null;
    }

    public List<Grade> findGradesByEnrollment(int enrollmentId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE enrollment_id = ? ORDER BY component_id";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        }
        return grades;
    }

    public boolean saveScore(int enrollmentId, int componentId, double score) throws SQLException {
        String sql = "INSERT INTO grades (enrollment_id, component_id, score) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE score = VALUES(score)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.setInt(2, componentId);
            stmt.setDouble(3, score);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean batchSaveScores(Connection conn, Map<Integer, Map<Integer, Double>> scoresToUpdate) throws SQLException {
        String sql = "INSERT INTO grades (enrollment_id, component_id, score) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE score = VALUES(score)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int count = 0;
            for (Map.Entry<Integer, Map<Integer, Double>> studentEntry : scoresToUpdate.entrySet()) {
                int enrollmentId = studentEntry.getKey();
                for (Map.Entry<Integer, Double> scoreEntry : studentEntry.getValue().entrySet()) {
                    int componentId = scoreEntry.getKey();
                    double score = scoreEntry.getValue();

                    stmt.setInt(1, enrollmentId);
                    stmt.setInt(2, componentId);
                    stmt.setDouble(3, score);
                    stmt.addBatch();
                    count++;

                    if (count % 100 == 0) {
                        stmt.executeBatch();
                        stmt.clearBatch();
                    }
                }
            }
            stmt.executeBatch();
            return true;
        }
    }

    public List<StudentGradeEntry> getGradesForSection(int sectionId) throws SQLException {
        List<StudentGradeEntry> studentGrades = new ArrayList<>();
        Map<Integer, StudentGradeEntry> studentMap = new HashMap<>();

        
        String sql = "SELECT s.user_id as student_id, e.enrollment_id, u.full_name as student_name, " +
                     "st.roll_no, e.final_grade, g.component_id, g.score " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.student_id = s.user_id " +
                     "JOIN university_auth.users_auth u ON s.user_id = u.user_id " +
                     "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                     "JOIN students st ON e.student_id = st.user_id " +
                     "WHERE e.section_id = ? AND e.status = 'registered' " +
                     "ORDER BY u.full_name, g.component_id";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    StudentGradeEntry entry = studentMap.get(studentId);

                    if (entry == null) {
                        entry = new StudentGradeEntry(
                            studentId,
                            rs.getInt("enrollment_id"),
                            rs.getString("student_name"),
                            rs.getString("roll_no"),
                            rs.getString("final_grade")
                        );
                        studentMap.put(studentId, entry);
                        studentGrades.add(entry);
                    }

                    int componentId = rs.getInt("component_id");
                    if (!rs.wasNull()) {
                        entry.addScore(componentId, rs.getDouble("score"));
                    }
                }
            }
        }
        return studentGrades;
    }

    public List<GradeInfo> getGradesForStudent(int studentId) throws SQLException {
        List<GradeInfo> grades = new ArrayList<>();
        
        String sql = "SELECT c.code as course_code, s.section_code, " +
                     "gc.component_name, gc.max_score, gc.weight, g.score, e.final_grade " +
                     "FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "LEFT JOIN grade_components gc ON s.section_id = gc.section_id " +
                     "LEFT JOIN grades g ON gc.component_id = g.component_id AND e.enrollment_id = g.enrollment_id " +
                     "WHERE e.student_id = ? AND (e.status = 'registered' OR e.status = 'completed') " +
                     "ORDER BY c.code, gc.component_id";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double score = rs.getDouble("score");
                    if (rs.wasNull()) {
                        score = 0.0;
                    }
                    grades.add(new GradeInfo(
                        rs.getString("course_code"),
                        rs.getString("section_code"),
                        rs.getString("component_name"),
                        score,
                        rs.getDouble("max_score"),
                        rs.getDouble("weight"),
                        rs.getString("final_grade")
                    ));
                }
            }
        }
        return grades;
    }

    public Map<String, Double> getComponentStatistics(int componentId) throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT AVG(score) as average, MAX(score) as highest, MIN(score) as lowest, COUNT(*) as count " +
                     "FROM grades WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, componentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("average", rs.getDouble("average"));
                    stats.put("highest", rs.getDouble("highest"));
                    stats.put("lowest", rs.getDouble("lowest"));
                    stats.put("count", (double) rs.getInt("count"));
                }
            }
        }
        return stats;
    }

    private GradeComponent mapResultSetToComponent(ResultSet rs) throws SQLException {
        return new GradeComponent(
            rs.getInt("component_id"),
            rs.getInt("section_id"),
            rs.getString("component_name"),
            rs.getDouble("weight"),
            rs.getDouble("max_score")
        );
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        return new Grade(
            rs.getInt("grade_id"),
            rs.getInt("enrollment_id"),
            rs.getInt("component_id"),
            rs.getDouble("score"),
            rs.getTimestamp("entered_at") != null ? rs.getTimestamp("entered_at").toLocalDateTime() : java.time.LocalDateTime.now()
        );
    }
}