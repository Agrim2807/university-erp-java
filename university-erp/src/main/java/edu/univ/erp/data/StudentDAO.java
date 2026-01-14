package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

    public Student findById(int userId) throws SQLException {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_no";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    public boolean create(Student student) throws SQLException {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, student.getUserId());
            stmt.setString(2, student.getRollNo());
            stmt.setString(3, student.getProgram());
            stmt.setInt(4, student.getYear());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, program = ?, year = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getRollNo());
            stmt.setString(2, student.getProgram());
            stmt.setInt(3, student.getYear());
            stmt.setInt(4, student.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM students WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existsByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE roll_no = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public String getFullNameById(int userId) throws SQLException {
        
        String sql = "SELECT full_name FROM university_auth.users_auth WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        }
        return "Unknown";
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("user_id"),
            rs.getString("roll_no"),
            rs.getString("program"),
            rs.getInt("year")
        );
    }
}