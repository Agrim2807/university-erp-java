package edu.univ.erp.access;

import edu.univ.erp.auth.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccessControlService {
    private static final Logger logger = LoggerFactory.getLogger(AccessControlService.class);

    public static final String VIEW_GRADES = "view_grades";
    public static final String REGISTER_SECTION = "register_section";
    public static final String DROP_SECTION = "drop_section";
    public static final String CHANGE_PASSWORD = "change_password"; 
    public static final String MANAGE_GRADES = "manage_grades";
    public static final String MANAGE_USERS = "manage_users";
    public static final String MANAGE_COURSES = "manage_courses";
    public static final String MANAGE_SECTIONS = "manage_sections";
    public static final String TOGGLE_MAINTENANCE = "toggle_maintenance";

    public static boolean isActionAllowed(String action) {
        if (!SessionManager.isLoggedIn()) {
            logger.warn("Access denied: User not logged in for action: {}", action);
            return false;
        }

        
        if (SessionManager.isAdmin()) {
            return true;
        }

        
        if (SessionManager.isMaintenanceMode()) {
            
            if (REGISTER_SECTION.equals(action) ||
                DROP_SECTION.equals(action) ||
                MANAGE_GRADES.equals(action) ||
                CHANGE_PASSWORD.equals(action)) 
            {
                logger.warn("Access denied: Maintenance mode active for action: {} by user {}", action, SessionManager.getCurrentUsername());
                return false;
            }
            
        }

        String role = SessionManager.getCurrentRole();
        boolean allowed = checkPermission(role, action);

        if (!allowed) {
            logger.warn("Access denied: Role {} cannot perform action: {}", role, action);
        }

        return allowed;
    }

    private static boolean checkPermission(String role, String action) {
        if ("student".equals(role)) {
            return checkStudentPermission(action);
        } else if ("instructor".equals(role)) {
            return checkInstructorPermission(action);
        } else if ("admin".equals(role)) {
            return checkAdminPermission(action);
        }
        return false;
    }

    private static boolean checkStudentPermission(String action) {
        if (VIEW_GRADES.equals(action) ||
            REGISTER_SECTION.equals(action) ||
            DROP_SECTION.equals(action) ||
            CHANGE_PASSWORD.equals(action)) { 
            return true;
        }
        return false;
    }

    private static boolean checkInstructorPermission(String action) {
        if (VIEW_GRADES.equals(action) ||
            MANAGE_GRADES.equals(action) ||
            CHANGE_PASSWORD.equals(action)) { 
            return true;
        }
        return false;
    }

    private static boolean checkAdminPermission(String action) {
        
        return true;
    }

    public static String getAccessDeniedMessage(String action) {
        if (!SessionManager.isLoggedIn()) {
            return "Please log in to perform this action.";
        }

        if (SessionManager.isMaintenanceMode() && !SessionManager.isAdmin()) {
             
             if (REGISTER_SECTION.equals(action) ||
                 DROP_SECTION.equals(action) ||
                 MANAGE_GRADES.equals(action) ||
                 CHANGE_PASSWORD.equals(action))
             {
                 return "System is in maintenance mode. This action is temporarily unavailable.";
             }
        }

        String role = SessionManager.getCurrentRole();
        return String.format("Access denied: Your role (%s) cannot perform this action.", role);
    }

    public static boolean canViewStudentData(int targetStudentId) {
        if (SessionManager.isAdmin()) {
            return true;
        }

        if (SessionManager.isStudent()) {
            return SessionManager.getCurrentUserId() == targetStudentId;
        }

        
        return SessionManager.isInstructor();
    }

    public static boolean canManageSection(int sectionInstructorId) {
        if (SessionManager.isAdmin()) {
            return true;
        }

        if (SessionManager.isInstructor()) {
            return SessionManager.getCurrentUserId() == sectionInstructorId;
        }

        return false;
    }
}