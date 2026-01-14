package edu.univ.erp.service;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.StudentGradeEntry;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InstructorService {

    private static final Logger logger = LoggerFactory.getLogger(InstructorService.class);
    private NotificationService notificationService;

    public static class InstructorServiceException extends Exception {
        public InstructorServiceException(String message) {
            super(message);
        }
    }

    public InstructorService() {
        this.notificationService = new NotificationService();
    }

    
    private void verifySectionOwnership(int sectionId) throws InstructorServiceException {
        String sql = "SELECT instructor_id FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int sectionInstructorId = rs.getInt("instructor_id");
                    int currentUserId = SessionManager.getCurrentUserId();
                    if (!SessionManager.isAdmin() && sectionInstructorId != currentUserId) {
                        logger.warn("Instructor {} attempted to access section {} owned by instructor {}",
                            currentUserId, sectionId, sectionInstructorId);
                        throw new InstructorServiceException("Access Denied: This is not your section.");
                    }
                } else {
                    throw new InstructorServiceException("Section not found.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error verifying section ownership for section {}", sectionId, e);
            throw new InstructorServiceException("Database error verifying section ownership.");
        }
    }

    public List<Section> getMySections(int instructorId) {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "WHERE s.instructor_id = ? " +
                     "ORDER BY c.code, s.section_code";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorId);
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
                    sections.add(section);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching sections for instructor {}", instructorId, e);
        }
        return sections;
    }

    public List<GradeComponent> getGradeComponents(int sectionId) {
        List<GradeComponent> components = new ArrayList<>();
        String sql = "SELECT * FROM grade_components WHERE section_id = ? ORDER BY component_id";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    components.add(new GradeComponent(
                        rs.getInt("component_id"), rs.getInt("section_id"),
                        rs.getString("component_name"), rs.getDouble("weight"),
                        rs.getDouble("max_score")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching grade components for section {}", sectionId, e);
        }
        return components;
    }

    public List<StudentGradeEntry> getGradesForSection(int sectionId) {
        List<StudentGradeEntry> studentGrades = new ArrayList<>();
        Map<Integer, StudentGradeEntry> studentMap = new HashMap<>();

        String sql = "SELECT s.user_id as student_id, e.enrollment_id, u.full_name as student_name, s.roll_no, e.final_grade, " +
                     "g.component_id, g.score " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.student_id = s.user_id " +
                     "JOIN university_auth.users_auth u ON s.user_id = u.user_id " +
                     "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
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
        } catch (SQLException e) {
            logger.error("Error fetching student grades for section {}", sectionId, e);
        }
        return studentGrades;
    }

    public boolean defineGradeComponent(int sectionId, String name, double weight, double maxScore) throws InstructorServiceException, SQLException {
        if (!AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES)) {
            throw new InstructorServiceException("Access Denied or Maintenance Mode is ON.");
        }

        
        verifySectionOwnership(sectionId);

        String sql = "INSERT INTO grade_components (section_id, component_name, weight, max_score) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            stmt.setString(2, name);
            stmt.setDouble(3, weight);
            stmt.setDouble(4, maxScore);

            return stmt.executeUpdate() > 0;
        }
    }

    
    public boolean updateGradeComponent(int componentId, String name, double weight, double maxScore) throws InstructorServiceException, SQLException {
        if (!AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES)) {
            throw new InstructorServiceException("Access Denied or Maintenance Mode is ON.");
        }

        String sql = "UPDATE grade_components SET component_name = ?, weight = ?, max_score = ? WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setDouble(2, weight);
            stmt.setDouble(3, maxScore);
            stmt.setInt(4, componentId);

            return stmt.executeUpdate() > 0;
        }
    }

    
    public boolean deleteGradeComponent(int componentId) throws InstructorServiceException, SQLException {
        if (!AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES)) {
            throw new InstructorServiceException("Access Denied or Maintenance Mode is ON.");
        }

        
        String sql = "DELETE FROM grade_components WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, componentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean saveSingleScore(int enrollmentId, int componentId, double score) throws InstructorServiceException, SQLException {
         if (!AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES)) {
            throw new InstructorServiceException("Access Denied or Maintenance Mode is ON.");
         }

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

    
    public boolean batchUpdateScores(Map<Integer, Map<Integer, Double>> scoresToUpdate) throws InstructorServiceException, SQLException {
        if (!AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES)) {
            throw new InstructorServiceException("Access Denied or Maintenance Mode is ON.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getERPConnection();
            conn.setAutoCommit(false);
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
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error saving grades batch", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Failed to rollback save transaction", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close connection after saving grades", ex);
                }
            }
        }
    }

    public boolean computeFinalGrades(int sectionId) throws InstructorServiceException, SQLException {
        if (!AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES)) {
            throw new InstructorServiceException("Access Denied or Maintenance Mode is ON.");
        }

        
        verifySectionOwnership(sectionId);

        List<GradeComponent> components = getGradeComponents(sectionId);
        double totalWeight = components.stream().mapToDouble(GradeComponent::getWeight).sum();
        if (Math.abs(totalWeight - 100.0) > 0.01) {
            throw new InstructorServiceException("Cannot compute: Component weights do not add up to 100 (Current: " + totalWeight + ")");
        }

        List<StudentGradeEntry> students = getGradesForSection(sectionId);

        Connection conn = null;
        String updateSql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
        Section sectionDetails = null;

        try {
            conn = DatabaseConfig.getERPConnection();
            conn.setAutoCommit(false);

            sectionDetails = getSectionDetails(conn, sectionId);
            String courseCode = (sectionDetails != null) ? sectionDetails.getCourseCode() : "Unknown Course";

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                for (StudentGradeEntry student : students) {
                    double finalScore = 0.0;
                    boolean allGradesIn = true;

                    for (GradeComponent comp : components) {
                        Double score = student.getScores().get(comp.getComponentId());
                        if (score == null) {
                            allGradesIn = false;
                            break;
                        }
                        finalScore += (score / comp.getMaxScore()) * comp.getWeight();
                    }

                    if (allGradesIn) {
                        String letterGrade = scoreToLetterGrade(finalScore);
                        stmt.setString(1, letterGrade);
                        stmt.setInt(2, student.getEnrollmentId());
                        stmt.addBatch();
                        logger.info("Student {} ({}) final score: {} -> {}",
                            student.getStudentName(), student.getEnrollmentId(), finalScore, letterGrade);
                        
                        notificationService.createUserNotification(student.getStudentId(), 
                            "Your final grade for " + courseCode + " has been posted: " + letterGrade);

                    } else {
                        logger.warn("Skipping final grade for {} ({}), missing component scores.",
                            student.getStudentName(), student.getEnrollmentId());
                    }
                }
                stmt.executeBatch();
            }

            conn.commit();
            
            notificationService.createBroadcastNotification("admin", 
                "Final grades for section " + courseCode + " - " + sectionDetails.getSectionCode() + " have been computed and saved.");

            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    private Section getSectionDetails(Connection conn, int sectionId) {
        String sql = "SELECT s.instructor_id, c.code as course_code, s.section_code " +
                     "FROM sections s JOIN courses c ON s.course_id = c.course_id " +
                     "WHERE s.section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Section section = new Section(sectionId, 0, rs.getInt("instructor_id"), rs.getString("section_code"), null, null, 0, null, 0, 0);
                    section.setCourseCode(rs.getString("course_code"));
                    return section;
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching section details for {}: {}", sectionId, e.getMessage());
        }
        return null;
    }

    private String scoreToLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    public Map<String, Map<String, Double>> getSectionStatistics(int sectionId) {
        Map<String, Map<String, Double>> componentStats = new HashMap<>();
        List<GradeComponent> components = getGradeComponents(sectionId);
        List<StudentGradeEntry> students = getGradesForSection(sectionId);

        if (components.isEmpty() || students.isEmpty()) {
            return componentStats;
        }

        for (GradeComponent comp : components) {
            List<Double> scores = new ArrayList<>();
            for (StudentGradeEntry student : students) {
                Double score = student.getScores().get(comp.getComponentId());
                if (score != null) {
                    scores.add(score);
                }
            }

            if (!scores.isEmpty()) {
                double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
                double average = sum / scores.size();
                double highest = scores.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
                double lowest = scores.stream().mapToDouble(Double::doubleValue).min().getAsDouble();

                Map<String, Double> stats = new HashMap<>();
                stats.put("Average", average);
                stats.put("Highest", highest);
                stats.put("Lowest", lowest);
                stats.put("MaxPossible", comp.getMaxScore());
                stats.put("Count", (double) scores.size());

                componentStats.put(comp.getComponentName(), stats);
            }
        }
        return componentStats;
    }

    public List<StudentGradeEntry> generateClassReportData(int sectionId) {
        return getGradesForSection(sectionId);
    }
}