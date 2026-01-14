package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuthDAO.class);
    
    public User authenticateUser(String username, String password) {
        logger.debug("Attempting authentication for user: {}", username);
        
        String sql = "SELECT user_id, username, full_name, role, password_hash, status, last_login, created_at " +
                    "FROM users_auth WHERE username = ? AND status = 'active'";
        
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    logger.debug("Found user with hash: {}", storedHash != null ? "[HASH_PRESENT]" : "[NULL_HASH]");
                    
                    if (storedHash != null && PasswordUtil.verifyPassword(password, storedHash)) {
                        logger.debug("Password verification SUCCESS for user: {}", username);
                        
                        updateLastLogin(rs.getInt("user_id"));
                        
                        User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"), 
                            rs.getString("role"),
                            rs.getString("status"),
                            rs.getTimestamp("last_login") != null ? 
                                rs.getTimestamp("last_login").toLocalDateTime() : null,
                            rs.getTimestamp("created_at").toLocalDateTime()
                        );
                        
                        logger.info("User authenticated successfully: {}", username);
                        return user;
                    } else {
                        logger.warn("Password verification FAILED for user: {}", username);
                    }
                } else {
                    logger.warn("User not found or inactive: {}", username);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during authentication for user: " + username, e);
        }
        
        return null;
    }
    
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Error updating last login for user: " + userId, e);
        }
    }
    
    public boolean isUsernameAvailable(String username) {
        String sql = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking username availability: " + username, e);
        }
        
        return false;
    }
}