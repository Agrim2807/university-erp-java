package edu.univ.erp.data;

import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class SettingsDAO {
    private static final Logger logger = LoggerFactory.getLogger(SettingsDAO.class);

    public static final String MAINTENANCE_MODE = "maintenance_mode";
    public static final String CURRENT_SEMESTER = "current_semester";
    public static final String CURRENT_YEAR = "current_year";

    public String get(String key) throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        }
        return null;
    }

    public String get(String key, String defaultValue) throws SQLException {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public boolean set(String key, String value) throws SQLException {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean setMultiple(Connection conn, Map<String, String> settings) throws SQLException {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                stmt.setString(1, entry.getKey());
                stmt.setString(2, entry.getValue());
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            return results.length > 0;
        }
    }

    public Map<String, String> getAll() throws SQLException {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return settings;
    }

    public boolean delete(String key) throws SQLException {
        String sql = "DELETE FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            return stmt.executeUpdate() > 0;
        }
    }

    

    public boolean isMaintenanceMode() throws SQLException {
        String value = get(MAINTENANCE_MODE, "false");
        return "true".equalsIgnoreCase(value);
    }

    public boolean setMaintenanceMode(boolean enabled) throws SQLException {
        return set(MAINTENANCE_MODE, enabled ? "true" : "false");
    }

    public String getCurrentSemester() throws SQLException {
        return get(CURRENT_SEMESTER, "Monsoon");
    }

    public int getCurrentYear() throws SQLException {
        String yearStr = get(CURRENT_YEAR, String.valueOf(java.time.Year.now().getValue()));
        try {
            return Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid year value in settings: {}", yearStr);
            return java.time.Year.now().getValue();
        }
    }

    public boolean setCurrentSemesterAndYear(Connection conn, String semester, int year) throws SQLException {
        Map<String, String> settings = new HashMap<>();
        settings.put(CURRENT_SEMESTER, semester);
        settings.put(CURRENT_YEAR, String.valueOf(year));
        return setMultiple(conn, settings);
    }
}
