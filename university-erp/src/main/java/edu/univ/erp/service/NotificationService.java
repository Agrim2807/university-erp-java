package edu.univ.erp.service;

import edu.univ.erp.domain.Notification;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public List<Notification> getNotificationsForUser(int userId, String userRole) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications " +
                     "WHERE (user_id = ? OR target_role = ?) AND is_read = false " +
                     "ORDER BY created_at DESC " +
                     "LIMIT 50";

        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, userRole);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                            rs.getInt("notification_id"),
                            (Integer) rs.getObject("user_id"),
                            rs.getString("target_role"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching notifications for user {}: {}", userId, e.getMessage());
        }
        return notifications;
    }

    public boolean createNotification(Integer userId, String targetRole, String message) {
        String sql = "INSERT INTO notifications (user_id, target_role, message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }

            if (targetRole != null) {
                stmt.setString(2, targetRole);
            } else {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            }

            stmt.setString(3, message);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error creating notification: {}", e.getMessage());
            return false;
        }
    }
    
    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE notification_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error marking notification {} as read: {}", notificationId, e.getMessage());
        }
    }

    public boolean createBroadcastNotification(String targetRole, String message) {
        return createNotification(null, targetRole, message);
    }

    public boolean createUserNotification(int userId, String message) {
        return createNotification(userId, null, message);
    }

    public boolean createBroadcastNotificationToAll(String message) {
        boolean studentSuccess = createNotification(null, "student", message);
        boolean instructorSuccess = createNotification(null, "instructor", message);
        return studentSuccess && instructorSuccess;
    }

    
    public boolean markAllAsRead(int userId, String userRole) {
        String sql = "UPDATE notifications SET is_read = true WHERE (user_id = ? OR target_role = ?) AND is_read = false";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, userRole);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Error marking all notifications as read for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}