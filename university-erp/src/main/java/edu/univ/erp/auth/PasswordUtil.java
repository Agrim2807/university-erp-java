package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import edu.univ.erp.util.DatabaseConfig;

public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_ROUNDS = 12;
    private static final int PASSWORD_HISTORY_LIMIT = 5;
    
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            logger.warn("Password verification failed: null password or hash");
            return false;
        }

        
        if (hashedPassword.trim().isEmpty()) {
            logger.warn("Password verification failed: empty hash");
            return false;
        }

        try {
            boolean result = BCrypt.checkpw(plainPassword, hashedPassword);
            logger.debug("Password verification result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Password verification error: " + e.getMessage(), e);
            return false;
        }
    }
    
    public static void main(String[] args) {
        String[] passwords = {"admin123", "inst123", "stu123"};
        for (String pwd : passwords) {
            String hash = hashPassword(pwd);
            System.out.println("Password: " + pwd + " -> Hash: " + hash);
            System.out.println("Verify: " + verifyPassword(pwd, hash));
            System.out.println("---");
        }
    }
    
    public static boolean isPasswordInHistory(int userId, String newPassword) {
        String sql = "SELECT password_hash FROM password_history " +
                    "WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, PASSWORD_HISTORY_LIMIT);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String oldHash = rs.getString("password_hash");
                    if (verifyPassword(newPassword, oldHash)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking password history for user: " + userId, e);
        }
        
        return false;
    }
    
    public static void addToPasswordHistory(int userId, String passwordHash) {
        String sql = "INSERT INTO password_history (user_id, password_hash) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Error adding to password history for user: " + userId, e);
        }
    }
    
    public static boolean changePassword(int userId, String currentPassword, String newPassword) {
        String currentHashSql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(currentHashSql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String currentHash = rs.getString("password_hash");
                    if (!verifyPassword(currentPassword, currentHash)) {
                        return false;
                    }
                }
            }
            
            if (isPasswordInHistory(userId, newPassword)) {
                return false;
            }
            
            String newHash = hashPassword(newPassword);
            String updateSql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
            
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newHash);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
                
                addToPasswordHistory(userId, newHash);
                
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error changing password for user: " + userId, e);
            return false;
        }
    }
}