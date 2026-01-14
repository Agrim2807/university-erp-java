package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.service.MaintenanceService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    
    
    private JPanel rootPanel;
    private CardLayout rootLayout;
    private JPanel appShellPanel;
    private SidebarPanel sidebarPanel;
    private JPanel contentAreaWrapper; 
    private BreadcrumbPanel breadcrumbPanel;
    private JPanel contentPanel;
    private CardLayout contentLayout;
    
    private JLabel maintenanceBanner;
    private Map<String, JPanel> panelCache = new HashMap<>();
    
    
    private AuthService authService;

    
    public static final String LOGIN_VIEW = "LOGIN";
    public static final String APP_VIEW = "APP";
    
    
    public static final String DASHBOARD_PANEL = "DASHBOARD";
    public static final String STUDENT_CATALOG_PANEL = "STUDENT_CATALOG";
    public static final String STUDENT_COURSES_PANEL = "STUDENT_COURSES";
    public static final String STUDENT_GRADES_PANEL = "STUDENT_GRADES";
    public static final String STUDENT_TRANSCRIPT_PANEL = "STUDENT_TRANSCRIPT";
    public static final String STUDENT_TIMETABLE_PANEL = "STUDENT_TIMETABLE";
    public static final String INSTRUCTOR_SECTIONS_PANEL = "INSTRUCTOR_SECTIONS";
    public static final String INSTRUCTOR_GRADEBOOK_PANEL = "INSTRUCTOR_GRADEBOOK";
    public static final String INSTRUCTOR_CLASS_STATS_PANEL = "INSTRUCTOR_CLASS_STATS";
    public static final String INSTRUCTOR_REPORTS_PANEL = "INSTRUCTOR_REPORTS";
    public static final String ADMIN_USER_MGMT_PANEL = "ADMIN_USER_MGMT";
    public static final String ADMIN_COURSE_MGMT_PANEL = "ADMIN_COURSE_MGMT";
    public static final String ADMIN_SECTION_MGMT_PANEL = "ADMIN_SECTION_MGMT";
    public static final String ADMIN_SETTINGS_PANEL = "ADMIN_SETTINGS";

    
    public DashboardPanel dashboardPanelInstance = null;
    public GradebookPanel gradebookPanelInstance = null;
    public InstructorSectionsPanel instructorSectionsPanelInstance = null;
    public StudentCatalogPanel studentCatalogPanelInstance = null;
    public MyCoursesPanel myCoursesPanelInstance = null;
    public MyGradesPanel myGradesPanelInstance = null;
    public TranscriptPanel transcriptPanelInstance = null;
    public TimetablePanel timetablePanelInstance = null;
    public ClassStatsPanel classStatsPanelInstance = null;
    public ClassReportPanel classReportPanelInstance = null;

    private MainFrame() {
        this.authService = new AuthService();
        ThemeManager.setupTheme();
        initializeFrame();
        
        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);
        
        rootPanel.add(new LoginPanel().getPanel(), LOGIN_VIEW);
        
        appShellPanel = new JPanel(new MigLayout("fill, insets 0", "[250!, fill][grow, fill]", "[grow, fill]"));
        
        contentAreaWrapper = new JPanel(new BorderLayout());
        contentAreaWrapper.setBackground(ThemeManager.COLOR_BACKGROUND);
        
        breadcrumbPanel = new BreadcrumbPanel();
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(ThemeManager.COLOR_BACKGROUND);
        
        contentAreaWrapper.add(breadcrumbPanel, BorderLayout.NORTH);
        contentAreaWrapper.add(contentPanel, BorderLayout.CENTER);
        
        appShellPanel.add(new JPanel(), "cell 0 0"); 
        appShellPanel.add(contentAreaWrapper, "cell 1 0");
        
        rootPanel.add(appShellPanel, APP_VIEW);
        
        maintenanceBanner = new JLabel("⚠️ SYSTEM UNDER MAINTENANCE - View Only Mode");
        maintenanceBanner.setOpaque(true);
        maintenanceBanner.setBackground(ThemeManager.COLOR_WARNING);
        maintenanceBanner.setForeground(Color.WHITE);
        maintenanceBanner.setHorizontalAlignment(SwingConstants.CENTER);
        maintenanceBanner.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        maintenanceBanner.setVisible(false);

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.add(maintenanceBanner, BorderLayout.NORTH);
        basePanel.add(rootPanel, BorderLayout.CENTER);
        
        setContentPane(basePanel);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { revalidate(); }
        });
        
        pack();
        setLocationRelativeTo(null);
    }

    public static MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    private void initializeFrame() {
        setTitle("University ERP System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1280, 800));
        setMinimumSize(new Dimension(1024, 768));
    }

    public void navigateToDashboard() {
        if (appShellPanel.getComponentCount() > 0) appShellPanel.remove(0);
        this.sidebarPanel = new SidebarPanel(this);
        appShellPanel.add(this.sidebarPanel, "cell 0 0, grow", 0);
        
        rootLayout.show(rootPanel, APP_VIEW);
        showPanel(DASHBOARD_PANEL);
        updateMaintenanceBanner();

        SwingUtilities.invokeLater(() -> {
            appShellPanel.revalidate();
            appShellPanel.repaint();
            this.revalidate();
            this.repaint();
        });
    }

    public void navigateToLogin() {
        SessionManager.logout();
        panelCache.clear();
        contentPanel.removeAll();
        rootLayout.show(rootPanel, LOGIN_VIEW);
    }

    public void showPanel(String panelName) {
        if (!panelCache.containsKey(panelName)) {
            JPanel p = createPanel(panelName);
            panelCache.put(panelName, p);
            contentPanel.add(p, panelName);
        }
        refreshPanelData(panelName);
        if (breadcrumbPanel != null) breadcrumbPanel.updatePath(panelName);
        contentLayout.show(contentPanel, panelName);
    }

    private JPanel createPanel(String panelName) {
        switch (panelName) {
            case DASHBOARD_PANEL:
                if (dashboardPanelInstance == null) dashboardPanelInstance = new DashboardPanel();
                return dashboardPanelInstance.getPanel();
            case STUDENT_CATALOG_PANEL:
                if (studentCatalogPanelInstance == null) studentCatalogPanelInstance = new StudentCatalogPanel();
                return studentCatalogPanelInstance.getPanel();
            case STUDENT_COURSES_PANEL:
                if (myCoursesPanelInstance == null) myCoursesPanelInstance = new MyCoursesPanel();
                return myCoursesPanelInstance.getPanel();
            case STUDENT_GRADES_PANEL:
                if (myGradesPanelInstance == null) myGradesPanelInstance = new MyGradesPanel();
                return myGradesPanelInstance.getPanel();
            case STUDENT_TRANSCRIPT_PANEL:
                if (transcriptPanelInstance == null) transcriptPanelInstance = new TranscriptPanel();
                return transcriptPanelInstance.getPanel();
            case STUDENT_TIMETABLE_PANEL:
                if (timetablePanelInstance == null) timetablePanelInstance = new TimetablePanel();
                return timetablePanelInstance.getPanel();
            case ADMIN_USER_MGMT_PANEL: return new UserManagementPanel().getPanel();
            case ADMIN_COURSE_MGMT_PANEL: return new CourseManagementPanel().getPanel();
            case ADMIN_SECTION_MGMT_PANEL: return new SectionManagementPanel().getPanel();
            case ADMIN_SETTINGS_PANEL: return new SystemSettingsPanel().getPanel();
            case INSTRUCTOR_SECTIONS_PANEL:
                 if (gradebookPanelInstance == null) gradebookPanelInstance = new GradebookPanel();
                 if (instructorSectionsPanelInstance == null) instructorSectionsPanelInstance = new InstructorSectionsPanel(gradebookPanelInstance);
                 return instructorSectionsPanelInstance.getPanel();
            case INSTRUCTOR_GRADEBOOK_PANEL:
                 if (gradebookPanelInstance == null) gradebookPanelInstance = new GradebookPanel();
                 return gradebookPanelInstance.getPanel();
            case INSTRUCTOR_CLASS_STATS_PANEL:
                 if (classStatsPanelInstance == null) classStatsPanelInstance = new ClassStatsPanel();
                 return classStatsPanelInstance.getPanel();
            case INSTRUCTOR_REPORTS_PANEL:
                 if (classReportPanelInstance == null) classReportPanelInstance = new ClassReportPanel();
                 return classReportPanelInstance.getPanel();
            default: return new JPanel();
        }
    }

    private void refreshPanelData(String panelName) {
        SwingUtilities.invokeLater(() -> {
            if (panelName.equals(DASHBOARD_PANEL) && dashboardPanelInstance != null) dashboardPanelInstance.updateUserInfo();
            if (panelName.equals(STUDENT_CATALOG_PANEL) && studentCatalogPanelInstance != null) studentCatalogPanelInstance.refreshData();
            if (panelName.equals(STUDENT_COURSES_PANEL) && myCoursesPanelInstance != null) myCoursesPanelInstance.refreshData();
            if (panelName.equals(STUDENT_GRADES_PANEL) && myGradesPanelInstance != null) myGradesPanelInstance.refreshData();
            if (panelName.equals(STUDENT_TRANSCRIPT_PANEL) && transcriptPanelInstance != null) transcriptPanelInstance.refreshData();
            if (panelName.equals(STUDENT_TIMETABLE_PANEL) && timetablePanelInstance != null) timetablePanelInstance.refreshData();
            if (panelName.equals(INSTRUCTOR_SECTIONS_PANEL) && instructorSectionsPanelInstance != null) instructorSectionsPanelInstance.refreshData();
        });
    }

    public void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "Change Password", true);
        JPanel p = new JPanel(new MigLayout("insets 20, fill"));
        p.setBackground(Color.WHITE);

        JPasswordField oldPass = UIFactory.createPasswordInput("Current Password");
        JPasswordField newPass = UIFactory.createPasswordInput("New Password");
        JPasswordField confirmPass = UIFactory.createPasswordInput("Confirm New Password");
        
        JButton save = UIFactory.createPrimaryButton("Update Password", () -> {
            String oldP = new String(oldPass.getPassword());
            String newP = new String(newPass.getPassword());
            String confP = new String(confirmPass.getPassword());
            
            if(oldP.isEmpty() || newP.isEmpty()) {
                showWarning("All fields required."); return;
            }
            if(!newP.equals(confP)) {
                showWarning("New passwords do not match."); return;
            }
            
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() {
                    try {
                        if(authService.changePassword(oldP, newP)) return "Success";
                        else return "Incorrect current password or used recently.";
                    } catch(Exception e) { return e.getMessage(); }
                }
                @Override protected void done() {
                    try {
                        String res = get();
                        if("Success".equals(res)) {
                            showSuccess("Password Changed Successfully.");
                            dialog.dispose();
                        } else showError(res);
                    } catch(Exception e) { showError(e.getMessage()); }
                }
            }.execute();
        });
        
        p.add(UIFactory.createLabel("Current Password"), "wrap");
        p.add(oldPass, "growx, h 40!, wrap 10");
        p.add(UIFactory.createLabel("New Password"), "wrap");
        p.add(newPass, "growx, h 40!, wrap 10");
        p.add(UIFactory.createLabel("Confirm Password"), "wrap");
        p.add(confirmPass, "growx, h 40!, wrap 20");
        p.add(save, "growx");
        
        dialog.add(p);
        dialog.pack();
        dialog.setSize(350, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void updateMaintenanceBanner() {
        boolean mode = MaintenanceService.isMaintenanceMode();
        SessionManager.setMaintenanceMode(mode);
        maintenanceBanner.setVisible(mode);
    }
    
    public JPanel getPanelInstance(String panelName) { return panelCache.get(panelName); }

    private void showToastInternal(String msg, boolean success) {
        JLayeredPane layeredPane = getLayeredPane();
        if (layeredPane == null) return;
        
        ToastMessage toast = new ToastMessage(msg, success);
        toast.setSize(toast.getPreferredSize());
        
        int x = getWidth() - toast.getWidth() - 40;
        int y = getHeight() - toast.getHeight() - 60;
        toast.setLocation(x, y);
        
        layeredPane.add(toast, JLayeredPane.POPUP_LAYER);
        
        new Thread(() -> {
            try {
                for (float i = 0; i <= 1.0; i += 0.1) { toast.setAlpha(i); Thread.sleep(20); }
                Thread.sleep(3000);
                for (float i = 1.0f; i >= 0; i -= 0.1) { toast.setAlpha(i); Thread.sleep(20); }
                layeredPane.remove(toast);
                layeredPane.repaint();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void showSuccess(String msg) { SwingUtilities.invokeLater(() -> showToastInternal(msg, true)); }
    public void showError(String msg) { SwingUtilities.invokeLater(() -> showToastInternal(msg, false)); }
    public void showWarning(String msg) {
        SwingUtilities.invokeLater(() -> ConfirmDialog.showWarning(this, msg));
    }

    
    public void showInfo(String msg) {
        SwingUtilities.invokeLater(() -> ConfirmDialog.showInfo(this, msg));
    }
}