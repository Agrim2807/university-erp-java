package edu.univ.erp;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccessControlTest {

    @BeforeEach
    void setUp() {
        
        SessionManager.logout();
    }

    @Test
    @DisplayName("Test student can view grades")
    @Order(1)
    void testStudentCanViewGrades() {
        
        User student = new User(3, "stu1", "Test Student", "student", "active", null, null);
        SessionManager.login(student);

        assertTrue(AccessControlService.isActionAllowed(AccessControlService.VIEW_GRADES),
                  "Student should be able to view grades");
    }

    @Test
    @DisplayName("Test student can register for sections")
    @Order(2)
    void testStudentCanRegister() {
        User student = new User(3, "stu1", "Test Student", "student", "active", null, null);
        SessionManager.login(student);

        assertTrue(AccessControlService.isActionAllowed(AccessControlService.REGISTER_SECTION),
                  "Student should be able to register for sections");
    }

    @Test
    @DisplayName("Test student cannot manage courses")
    @Order(3)
    void testStudentCannotManageCourses() {
        User student = new User(3, "stu1", "Test Student", "student", "active", null, null);
        SessionManager.login(student);

        assertFalse(AccessControlService.isActionAllowed(AccessControlService.MANAGE_COURSES),
                   "Student should NOT be able to manage courses");
    }

    @Test
    @DisplayName("Test instructor can manage grades")
    @Order(4)
    void testInstructorCanManageGrades() {
        User instructor = new User(2, "inst1", "Test Instructor", "instructor", "active", null, null);
        SessionManager.login(instructor);

        assertTrue(AccessControlService.isActionAllowed(AccessControlService.MANAGE_GRADES),
                  "Instructor should be able to manage grades");
    }

    @Test
    @DisplayName("Test instructor cannot register for sections")
    @Order(5)
    void testInstructorCannotRegister() {
        User instructor = new User(2, "inst1", "Test Instructor", "instructor", "active", null, null);
        SessionManager.login(instructor);

        assertFalse(AccessControlService.isActionAllowed(AccessControlService.REGISTER_SECTION),
                   "Instructor should NOT be able to register for sections");
    }

    @Test
    @DisplayName("Test admin has full access")
    @Order(6)
    void testAdminFullAccess() {
        User admin = new User(1, "admin1", "Test Admin", "admin", "active", null, null);
        SessionManager.login(admin);

        assertTrue(AccessControlService.isActionAllowed(AccessControlService.MANAGE_COURSES));
        assertTrue(AccessControlService.isActionAllowed(AccessControlService.MANAGE_USERS));
        assertTrue(AccessControlService.isActionAllowed(AccessControlService.MANAGE_SECTIONS));
        assertTrue(AccessControlService.isActionAllowed(AccessControlService.TOGGLE_MAINTENANCE));
    }

    @Test
    @DisplayName("Test maintenance mode blocks student registration")
    @Order(7)
    void testMaintenanceModeBlocksStudentActions() {
        User student = new User(3, "stu1", "Test Student", "student", "active", null, null);
        SessionManager.login(student);
        SessionManager.setMaintenanceMode(true);

        assertFalse(AccessControlService.isActionAllowed(AccessControlService.REGISTER_SECTION),
                   "Maintenance mode should block student registration");

        assertFalse(AccessControlService.isActionAllowed(AccessControlService.DROP_SECTION),
                   "Maintenance mode should block student drop");
    }

    @Test
    @DisplayName("Test maintenance mode allows viewing during maintenance")
    @Order(8)
    void testMaintenanceModeAllowsViewing() {
        User student = new User(3, "stu1", "Test Student", "student", "active", null, null);
        SessionManager.login(student);
        SessionManager.setMaintenanceMode(true);

        assertTrue(AccessControlService.isActionAllowed(AccessControlService.VIEW_GRADES),
                  "Maintenance mode should still allow viewing grades");
    }

    @Test
    @DisplayName("Test maintenance mode does not block admin")
    @Order(9)
    void testMaintenanceModeDoesNotBlockAdmin() {
        User admin = new User(1, "admin1", "Test Admin", "admin", "active", null, null);
        SessionManager.login(admin);
        SessionManager.setMaintenanceMode(true);

        assertTrue(AccessControlService.isActionAllowed(AccessControlService.MANAGE_COURSES),
                  "Maintenance mode should NOT block admin");
        assertTrue(AccessControlService.isActionAllowed(AccessControlService.MANAGE_USERS),
                  "Maintenance mode should NOT block admin");
    }

    @Test
    @DisplayName("Test no logged-in user denies all actions")
    @Order(10)
    void testNoUserDeniesActions() {
        
        assertFalse(AccessControlService.isActionAllowed(AccessControlService.VIEW_GRADES),
                   "No logged-in user should deny all actions");
    }

    @Test
    @DisplayName("Test student can only view own data")
    @Order(11)
    void testStudentCanOnlyViewOwnData() {
        User student = new User(3, "stu1", "Test Student", "student", "active", null, null);
        SessionManager.login(student);

        assertTrue(AccessControlService.canViewStudentData(3),
                  "Student should be able to view own data");

        assertFalse(AccessControlService.canViewStudentData(4),
                   "Student should NOT be able to view other student's data");
    }

    @Test
    @DisplayName("Test admin can view any student data")
    @Order(12)
    void testAdminCanViewAnyStudentData() {
        User admin = new User(1, "admin1", "Test Admin", "admin", "active", null, null);
        SessionManager.login(admin);

        assertTrue(AccessControlService.canViewStudentData(3));
        assertTrue(AccessControlService.canViewStudentData(4));
    }

    @Test
    @DisplayName("Test instructor can only manage own sections")
    @Order(13)
    void testInstructorCanOnlyManageOwnSections() {
        User instructor = new User(2, "inst1", "Test Instructor", "instructor", "active", null, null);
        SessionManager.login(instructor);

        assertTrue(AccessControlService.canManageSection(2),
                  "Instructor should manage own sections");

        assertFalse(AccessControlService.canManageSection(99),
                   "Instructor should NOT manage other instructor's sections");
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
        SessionManager.setMaintenanceMode(false);
    }
}
