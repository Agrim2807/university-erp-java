package edu.univ.erp.service;

import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SemesterService {
    private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);
    private static final String SEMESTER_KEY = "current_semester";
    private static final String YEAR_KEY = "current_year";

    public static String getCurrentSemester() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, SEMESTER_KEY);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting current semester", e);
        }

        return "Monsoon"; 
    }

    public static int getCurrentYear() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, YEAR_KEY);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.parseInt(rs.getString("setting_value"));
                }
            }
        } catch (SQLException | NumberFormatException e) {
            logger.error("Error getting current year", e);
        }

        return java.time.Year.now().getValue(); 
    }

    public static String getCurrentSemesterDisplay() {
        return getCurrentSemester() + " " + getCurrentYear();
    }

    public static boolean setCurrentSemester(String semester, int year) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getERPConnection();
            conn.setAutoCommit(false);

            
            String semesterSql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                                "ON DUPLICATE KEY UPDATE setting_value = ?";
            try (PreparedStatement stmt = conn.prepareStatement(semesterSql)) {
                stmt.setString(1, SEMESTER_KEY);
                stmt.setString(2, semester);
                stmt.setString(3, semester);
                stmt.executeUpdate();
            }

            
            String yearSql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE setting_value = ?";
            try (PreparedStatement stmt = conn.prepareStatement(yearSql)) {
                String yearStr = String.valueOf(year);
                stmt.setString(1, YEAR_KEY);
                stmt.setString(2, yearStr);
                stmt.setString(3, yearStr);
                stmt.executeUpdate();
            }

            conn.commit();
            logger.info("Current semester set to: {} {}", semester, year);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back semester update", ex);
                }
            }
            logger.error("Error setting current semester", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }
}