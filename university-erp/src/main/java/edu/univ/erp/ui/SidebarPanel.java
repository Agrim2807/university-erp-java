package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class SidebarPanel extends JPanel {
    private final MainFrame mainFrame;

    public SidebarPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[60!]10[80!]20[grow]20[90!]"));
        setBackground(ThemeManager.COLOR_SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.COLOR_BORDER));

        add(createBrandPanel(), "grow");
        add(createUserProfile(), "grow, pad 0 15 0 15");
        add(createNavigationMenu(), "grow, pushy, aligny top, pad 0 10 0 10");
        add(createFooter(), "grow");
    }

    private JPanel createBrandPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15 20 15 20"));
        p.setBackground(ThemeManager.getCurrentAccent());
        
        JLabel title = new JLabel("University ERP");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(Color.WHITE);
        
        p.add(title);
        return p;
    }

    private JPanel createUserProfile() {
        User user = SessionManager.getCurrentUser();
        String name = (user != null) ? user.getFullName() : "Guest";
        String role = (user != null) ? user.getRole() : "Visitor";

        JPanel card = new JPanel(new MigLayout("insets 10", "[40!]10[grow]"));
        card.setBackground(ThemeManager.COLOR_BACKGROUND);
        card.putClientProperty("Component.arc", 12);

        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCurrentAccent());
                g2.fill(new Ellipse2D.Double(0, 0, 40, 40));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String initial = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1);
                FontMetrics fm = g2.getFontMetrics();
                int x = (40 - fm.stringWidth(initial)) / 2;
                int y = ((40 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        card.add(avatar, "w 40!, h 40!");
        
        JPanel textParams = new JPanel(new MigLayout("insets 0, wrap 1, gap 0"));
        textParams.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        JLabel roleLbl = new JLabel(role != null ? role.toUpperCase() : "UNKNOWN");
        roleLbl.setFont(ThemeManager.FONT_LABEL);
        roleLbl.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        
        textParams.add(nameLbl);
        textParams.add(roleLbl);
        card.add(textParams);
        return card;
    }

    private JPanel createNavigationMenu() {
        JPanel menu = new JPanel(new MigLayout("fillx, wrap 1, insets 0, gap 5"));
        menu.setOpaque(false);

        String role = SessionManager.getCurrentRole();
        if ("student".equals(role)) {
            menu.add(createMenuItem("Dashboard", MainFrame.DASHBOARD_PANEL, "🏠"));
            menu.add(createMenuItem("Course Catalog", MainFrame.STUDENT_CATALOG_PANEL, "📚"));
            menu.add(createMenuItem("My Courses", MainFrame.STUDENT_COURSES_PANEL, "🎒"));
            menu.add(createMenuItem("My Grades", MainFrame.STUDENT_GRADES_PANEL, "📊"));
            menu.add(createMenuItem("Transcript", MainFrame.STUDENT_TRANSCRIPT_PANEL, "📄"));
            menu.add(createMenuItem("Timetable", MainFrame.STUDENT_TIMETABLE_PANEL, "📅"));
        } else if ("instructor".equals(role)) {
            menu.add(createMenuItem("Dashboard", MainFrame.DASHBOARD_PANEL, "🏠"));
            menu.add(createMenuItem("My Sections", MainFrame.INSTRUCTOR_SECTIONS_PANEL, "👨‍🏫"));
        } else if ("admin".equals(role)) {
            menu.add(createMenuItem("Dashboard", MainFrame.DASHBOARD_PANEL, "🏠"));
            menu.add(createMenuItem("Users", MainFrame.ADMIN_USER_MGMT_PANEL, "👥"));
            menu.add(createMenuItem("Courses", MainFrame.ADMIN_COURSE_MGMT_PANEL, "📚"));
            menu.add(createMenuItem("Sections", MainFrame.ADMIN_SECTION_MGMT_PANEL, "🏫"));
            menu.add(createMenuItem("System Settings", MainFrame.ADMIN_SETTINGS_PANEL, "⚙️"));
        }
        return menu;
    }

    private JButton createMenuItem(String text, String panelKey, String emoji) {
        JButton btn = new JButton(emoji + "   " + text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        if (!font.getFamily().contains("Emoji")) font = ThemeManager.FONT_BODY;
        btn.setFont(font);
        
        btn.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        btn.setBackground(ThemeManager.COLOR_SURFACE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(ThemeManager.COLOR_BACKGROUND);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ThemeManager.COLOR_SURFACE);
            }
        });

        btn.addActionListener(e -> mainFrame.showPanel(panelKey));
        return btn;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new MigLayout("fillx, insets 0 20 10 20, wrap 1"));
        footer.setOpaque(false);

        
        JButton changePass = UIFactory.createSecondaryButton("Change Password",
            () -> mainFrame.showChangePasswordDialog());

        
        JButton logout = UIFactory.createSecondaryButton("Logout",
            () -> mainFrame.navigateToLogin());
        logout.setForeground(ThemeManager.COLOR_DANGER); 
        logout.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_DANGER)); 

        footer.add(changePass, "growx, h 35!");
        footer.add(logout, "growx, h 35!, gaptop 5");
        return footer;
    }
}