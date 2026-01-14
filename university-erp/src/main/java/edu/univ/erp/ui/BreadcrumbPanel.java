package edu.univ.erp.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;


public class BreadcrumbPanel extends JPanel {
    private final LinkedList<String> path;

    public BreadcrumbPanel() {
        this.path = new LinkedList<>();
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));
        setBackground(ThemeManager.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0)); 
    }

    public void updatePath(String currentPanelKey) {
        removeAll();
        
        
        addLink("Home", () -> MainFrame.getInstance().navigateToDashboard());
        addSeparator();

        
        String readableName = getReadableName(currentPanelKey);
        
        
        
        JLabel current = new JLabel(readableName);
        current.setFont(ThemeManager.FONT_BODY_BOLD);
        current.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        add(current);

        revalidate();
        repaint();
    }

    private void addLink(String text, Runnable action) {
        JLabel link = new JLabel(text);
        link.setFont(ThemeManager.FONT_BODY);
        link.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.run();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                link.setForeground(ThemeManager.getCurrentAccent());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                link.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
            }
        });
        add(link);
    }

    private void addSeparator() {
        JLabel sep = new JLabel("/");
        sep.setFont(ThemeManager.FONT_BODY);
        sep.setForeground(ThemeManager.COLOR_BORDER);
        add(sep);
    }

    private String getReadableName(String key) {
        switch (key) {
            case MainFrame.DASHBOARD_PANEL: return "Dashboard";
            case MainFrame.STUDENT_CATALOG_PANEL: return "Course Catalog";
            case MainFrame.STUDENT_COURSES_PANEL: return "My Courses";
            case MainFrame.STUDENT_GRADES_PANEL: return "Grades";
            case MainFrame.STUDENT_TRANSCRIPT_PANEL: return "Transcript";
            case MainFrame.STUDENT_TIMETABLE_PANEL: return "Timetable";
            case MainFrame.INSTRUCTOR_SECTIONS_PANEL: return "My Sections";
            case MainFrame.INSTRUCTOR_GRADEBOOK_PANEL: return "Gradebook";
            case MainFrame.INSTRUCTOR_CLASS_STATS_PANEL: return "Class Analytics";
            case MainFrame.INSTRUCTOR_REPORTS_PANEL: return "Reports";
            case MainFrame.ADMIN_USER_MGMT_PANEL: return "User Management";
            case MainFrame.ADMIN_COURSE_MGMT_PANEL: return "Course Management";
            case MainFrame.ADMIN_SECTION_MGMT_PANEL: return "Section Management";
            case MainFrame.ADMIN_SETTINGS_PANEL: return "System Settings";
            default: return "Page";
        }
    }
}