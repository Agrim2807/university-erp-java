package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static User currentUser = null;
    private static boolean maintenanceMode = false;
    
    public static void login(User user) {
        currentUser = user;
        logger.info("User logged into session: {} with role: {}", user.getUsername(), user.getRole());
    }
    
    public static void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
        }
        currentUser = null;
        maintenanceMode = false;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    
    public static String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }
    
    
    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
    
    public static boolean isInstructor() {
        return currentUser != null && "instructor".equals(currentUser.getRole());
    }
    
    public static boolean isStudent() {
        return currentUser != null && "student".equals(currentUser.getRole());
    }
    
    public static void setMaintenanceMode(boolean mode) {
        maintenanceMode = mode;
    }
    
    public static boolean isMaintenanceMode() {
        return maintenanceMode;
    }
    
    public static boolean canPerformAction() {
        if (currentUser == null) return false;
        
        if ("admin".equals(currentUser.getRole())) {
            return true;
        }
        
        return !maintenanceMode;
    }
}