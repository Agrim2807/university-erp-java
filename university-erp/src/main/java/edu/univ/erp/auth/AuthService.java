package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DatabaseConfig;
import edu.univ.erp.access.AccessControlService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    public User login(String username, String password) throws AuthException {
        User user = null;
        
        String sql = "SELECT user_id, username, full_name, role, password_hash, status, failed_attempts, locked_until FROM university_auth.users_auth WHERE username = ?";

        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    
                    user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"), 
                            rs.getString("role"),
                            rs.getString("status"),
                            null,
                            null
                    );

                    LocalDateTime lockedUntil = rs.getTimestamp("locked_until") != null ? rs.getTimestamp("locked_until").toLocalDateTime() : null;
                    if ("locked".equals(user.getStatus()) && lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
                        logger.warn("Login attempt failed for locked user: {}", username);
                        throw new AuthException("Account is locked due to too many failed attempts. Try again later.");
                    }
                     if ("locked".equals(user.getStatus()) && (lockedUntil == null || LocalDateTime.now().isAfter(lockedUntil))) {
                         logger.info("Account lock expired for user {}, resetting status.", username);
                         resetLockout(conn, user.getUserId());
                         user.setStatus("active");
                     }

                    String storedHash = rs.getString("password_hash");
                    int failedAttempts = rs.getInt("failed_attempts");

                    if (PasswordUtil.verifyPassword(password, storedHash)) {
                        if (failedAttempts > 0) {
                            resetFailedAttempts(conn, user.getUserId());
                        }
                        updateLastLogin(conn, user.getUserId());
                        SessionManager.login(user);
                        logger.info("User logged in successfully: {}", username);
                        return user;
                    } else {
                        handleFailedLoginAttempt(conn, user.getUserId(), failedAttempts + 1);
                        logger.warn("Login attempt failed for user: {}", username);
                        throw new AuthException("Incorrect username or password.");
                    }
                } else {
                    logger.warn("Login attempt for non-existent user: {}", username);
                    throw new AuthException("Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during login for user: {}", username, e);
            throw new AuthException("Database error during login. Please try again later.");
        }
    }

    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE university_auth.users_auth SET last_login = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void handleFailedLoginAttempt(Connection conn, int userId, int newAttemptCount) throws SQLException, AuthException {
        String sql;
        if (newAttemptCount >= MAX_FAILED_ATTEMPTS) {
            sql = "UPDATE university_auth.users_auth SET status = 'locked', failed_attempts = ?, locked_until = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newAttemptCount);
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES)));
                stmt.setInt(3, userId);
                stmt.executeUpdate();
                logger.warn("User account locked due to excessive failed attempts: user_id={}", userId);
                throw new AuthException("Account locked due to too many failed attempts.");
            }
        } else {
            sql = "UPDATE university_auth.users_auth SET failed_attempts = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newAttemptCount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        }
    }

     private void resetLockout(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE university_auth.users_auth SET status = 'active', failed_attempts = 0, locked_until = NULL WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE university_auth.users_auth SET failed_attempts = 0, locked_until = NULL WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public boolean changePassword(String currentPassword, String newPassword) throws AuthException {
        if (!AccessControlService.isActionAllowed(AccessControlService.CHANGE_PASSWORD)) {
            logger.warn("Password change attempt denied during maintenance mode for user: {}", SessionManager.getCurrentUsername());
            throw new AuthException(AccessControlService.getAccessDeniedMessage(AccessControlService.CHANGE_PASSWORD));
        }

        if (!SessionManager.isLoggedIn()) {
            throw new AuthException("User must be logged in to change password.");
        }
        int userId = SessionManager.getCurrentUserId();
        String username = SessionManager.getCurrentUsername();

        boolean success = PasswordUtil.changePassword(userId, currentPassword, newPassword);

        if (success) {
            logger.info("Password successfully changed for user: {}", username);
        } else {
            logger.warn("Password change failed for user: {} (Incorrect current password or history constraint)", username);
        }
        return success;
    }


    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}