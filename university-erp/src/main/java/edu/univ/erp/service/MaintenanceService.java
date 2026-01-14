package edu.univ.erp.service;

import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class MaintenanceService {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private static final String MAINTENANCE_KEY = "maintenance_mode";
    
    public static boolean isMaintenanceMode() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, MAINTENANCE_KEY);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "true".equalsIgnoreCase(rs.getString("setting_value"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking maintenance mode", e);
        }
        
        return false;
    }
    
    public static boolean setMaintenanceMode(boolean enabled) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE setting_value = ?";
        
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String value = enabled ? "true" : "false";
            stmt.setString(1, MAINTENANCE_KEY);
            stmt.setString(2, value);
            stmt.setString(3, value);
            
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                logger.info("Maintenance mode set to: {}", enabled);
                
                try {
                    String message;
                    if (enabled) {
                        message = "SYSTEM ALERT: Maintenance Mode is now ON. Functionality is limited.";
                    } else {
                        message = "SYSTEM ALERT: Maintenance Mode is now OFF. All services are restored.";
                    }
                    NotificationService notificationService = new NotificationService();
                    notificationService.createBroadcastNotification("student", message);
                    notificationService.createBroadcastNotification("instructor", message);
                } catch (Exception e) {
                    logger.error("Failed to create maintenance mode notifications: {}", e.getMessage());
                }
            }
            
            return success;
            
        } catch (SQLException e) {
            logger.error("Error setting maintenance mode to: " + enabled, e);
            return false;
        }
    }
}