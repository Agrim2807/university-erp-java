package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {
    private static final Logger logger = LoggerFactory.getLogger(SectionDAO.class);

    public Section findById(int sectionId) throws SQLException {
        
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE s.section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSection(rs, true);
                }
            }
        }
        return null;
    }

    public Section findByIdWithLock(Connection conn, int sectionId) throws SQLException {
        
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE s.section_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSection(rs, true);
                }
            }
        }
        return null;
    }

    public List<Section> findAll() throws SQLException {
        List<Section> sections = new ArrayList<>();
        
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "ORDER BY s.semester, s.year DESC, c.code, s.section_code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sections.add(mapResultSetToSection(rs, true));
            }
        }
        return sections;
    }

    public List<Section> findAvailableSections() throws SQLException {
        List<Section> sections = new ArrayList<>();
        
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE s.enrollment_count < s.capacity " +
                     "ORDER BY c.code, s.section_code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sections.add(mapResultSetToSection(rs, true));
            }
        }
        return sections;
    }

    public List<Section> findByInstructor(int instructorId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE s.instructor_id = ? " +
                     "ORDER BY c.code, s.section_code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapResultSetToSection(rs, true));
                }
            }
        }
        return sections;
    }

    public List<Section> findByCourse(int courseId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, u.full_name as instructor_name " +
                     "FROM sections s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "JOIN university_auth.users_auth u ON s.instructor_id = u.user_id " +
                     "WHERE s.course_id = ? " +
                     "ORDER BY s.semester, s.year DESC, s.section_code";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapResultSetToSection(rs, true));
                }
            }
        }
        return sections;
    }

    public int create(Section section) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, section_code, day_time, room, capacity, semester, year, drop_deadline, add_deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, section.getCourseId());
            stmt.setInt(2, section.getInstructorId());
            stmt.setString(3, section.getSectionCode());
            stmt.setString(4, section.getDayTime());
            stmt.setString(5, section.getRoom());
            stmt.setInt(6, section.getCapacity());
            stmt.setString(7, section.getSemester());
            stmt.setInt(8, section.getYear());

            if (section.getDropDeadline() != null) {
                stmt.setDate(9, Date.valueOf(section.getDropDeadline()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            if (section.getAddDeadline() != null) {
                stmt.setDate(10, Date.valueOf(section.getAddDeadline()));
            } else {
                stmt.setNull(10, Types.DATE);
            }

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

    public boolean update(Section section) throws SQLException {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, section_code = ?, " +
                     "day_time = ?, room = ?, capacity = ?, semester = ?, year = ?, " +
                     "drop_deadline = ?, add_deadline = ? WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, section.getCourseId());
            stmt.setInt(2, section.getInstructorId());
            stmt.setString(3, section.getSectionCode());
            stmt.setString(4, section.getDayTime());
            stmt.setString(5, section.getRoom());
            stmt.setInt(6, section.getCapacity());
            stmt.setString(7, section.getSemester());
            stmt.setInt(8, section.getYear());

            if (section.getDropDeadline() != null) {
                stmt.setDate(9, Date.valueOf(section.getDropDeadline()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            if (section.getAddDeadline() != null) {
                stmt.setDate(10, Date.valueOf(section.getAddDeadline()));
            } else {
                stmt.setNull(10, Types.DATE);
            }

            stmt.setInt(11, section.getSectionId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getEnrollmentCount(int sectionId) throws SQLException {
        String sql = "SELECT enrollment_count FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_count");
                }
            }
        }
        return 0;
    }

    public boolean hasAvailableSeats(Connection conn, int sectionId) throws SQLException {
        String sql = "SELECT capacity, enrollment_count FROM sections WHERE section_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_count") < rs.getInt("capacity");
                }
            }
        }
        return false;
    }

    public boolean updateEnrollmentCount(Connection conn, int sectionId, int change) throws SQLException {
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

    public boolean isDropDeadlinePassed(int sectionId) throws SQLException {
        String sql = "SELECT drop_deadline FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dropDeadline = rs.getDate("drop_deadline");
                    if (dropDeadline == null) {
                        return false;
                    }
                    return LocalDate.now().isAfter(dropDeadline.toLocalDate());
                }
            }
        }
        return true;
    }

    public boolean isAddDeadlinePassed(int sectionId) throws SQLException {
        String sql = "SELECT add_deadline FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date addDeadline = rs.getDate("add_deadline");
                    if (addDeadline == null) {
                        return false;
                    }
                    return LocalDate.now().isAfter(addDeadline.toLocalDate());
                }
            }
        }
        return true;
    }

    private Section mapResultSetToSection(ResultSet rs, boolean includeJoinedData) throws SQLException {
        Section section = new Section(
            rs.getInt("section_id"),
            rs.getInt("course_id"),
            rs.getInt("instructor_id"),
            rs.getString("section_code"),
            rs.getString("day_time"),
            rs.getString("room"),
            rs.getInt("capacity"),
            rs.getString("semester"),
            rs.getInt("year"),
            rs.getInt("enrollment_count")
        );

        Date dropDeadline = rs.getDate("drop_deadline");
        if (dropDeadline != null) {
            section.setDropDeadline(dropDeadline.toLocalDate());
        }

        Date addDeadline = rs.getDate("add_deadline");
        if (addDeadline != null) {
            section.setAddDeadline(addDeadline.toLocalDate());
        }

        if (includeJoinedData) {
            section.setCourseCode(rs.getString("course_code"));
            section.setCourseTitle(rs.getString("course_title"));
            section.setInstructorName(rs.getString("instructor_name"));
        }

        return section;
    }
}