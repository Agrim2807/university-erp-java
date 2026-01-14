package edu.univ.erp.data;

import edu.univ.erp.domain.Notification;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class NotificationDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDAO.class);

    public List<Notification> findByUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        }
        return notifications;
    }

    public List<Notification> findUnreadByUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        }
        return notifications;
    }

    public List<Notification> findByRole(String role) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE target_role = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        }
        return notifications;
    }

    public boolean create(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, target_role, message, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (notification.getUserId() > 0) {
                stmt.setInt(1, notification.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (notification.getTargetRole() != null && !notification.getTargetRole().isEmpty()) {
                stmt.setString(2, notification.getTargetRole());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setString(3, notification.getMessage());
            stmt.setBoolean(4, notification.isRead());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        notification.setNotificationId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE notification_id = ?";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean markAllAsReadForUser(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countUnreadByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean deleteOldNotifications(int daysOld) throws SQLException {
        String sql = "DELETE FROM notifications WHERE created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConfig.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, daysOld);
            return stmt.executeUpdate() > 0;
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        Integer userIdObj = rs.wasNull() ? null : userId;

        return new Notification(
            rs.getInt("notification_id"),
            userIdObj,
            rs.getString("target_role"),
            rs.getString("message"),
            rs.getBoolean("is_read"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
