package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class InstructorDAO {
    private static final Logger logger = LoggerFactory.getLogger(InstructorDAO.class);

    public Instructor findById(int userId) throws SQLException {
        String sql = "SELECT * FROM instructors WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInstructor(rs);
                }
            }
        }
        return null;
    }

    public List<Instructor> findAll() throws SQLException {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT * FROM instructors ORDER BY department";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(mapResultSetToInstructor(rs));
            }
        }
        return instructors;
    }

    public boolean create(Instructor instructor) throws SQLException {
        String sql = "INSERT INTO instructors (user_id, department, office) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructor.getUserId());
            stmt.setString(2, instructor.getDepartment());
            stmt.setString(3, instructor.getOffice());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean update(Instructor instructor) throws SQLException {
        String sql = "UPDATE instructors SET department = ?, office = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, instructor.getDepartment());
            stmt.setString(2, instructor.getOffice());
            stmt.setInt(3, instructor.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM instructors WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Instructor mapResultSetToInstructor(ResultSet rs) throws SQLException {
        return new Instructor(
            rs.getInt("user_id"),
            rs.getString("department"),
            rs.getString("office")
        );
    }
}
