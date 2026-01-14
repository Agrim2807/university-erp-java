package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Notification;
import edu.univ.erp.service.DashboardService;
import edu.univ.erp.service.NotificationService;
import edu.univ.erp.service.SemesterService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final NotificationService notificationService;
    private final DashboardService dashboardService;

    public DashboardPanel() {
        this.notificationService = new NotificationService();
        this.dashboardService = new DashboardService();

        // Main layout
        setLayout(new MigLayout("fill, insets 30 30 30 30", "[grow, fill]20[340!]", "[grow, fill]"));
        setBackground(ThemeManager.COLOR_BACKGROUND);

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                revalidate();
                repaint();
            }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });
    }

    public void updateUserInfo() {
        removeAll();

        // Left Panel (Widgets)
        JPanel leftPanel = new JPanel(new MigLayout("fillx, insets 0, wrap 1", "[grow, fill]", "[]20[]20[]"));
        leftPanel.setOpaque(false);

        leftPanel.add(createHeaderSection(), "growx");

        if (SessionManager.isStudent()) {
            leftPanel.add(createStudentWidgets(), "growx");
        } else if (SessionManager.isInstructor()) {
            leftPanel.add(createInstructorWidgets(), "growx");
        } else if (SessionManager.isAdmin()) {
            leftPanel.add(createAdminWidgets(), "growx");
        }

        add(leftPanel, "grow, aligny top");

        // Right Panel (Notifications)
        JPanel rightPanel = new JPanel(new MigLayout("fill, insets 0", "[grow, fill]", "[grow, fill]"));
        rightPanel.setOpaque(false);

        rightPanel.add(createNotificationWidget(), "grow");

        add(rightPanel, "growy, width 340!");

        revalidate();
        repaint();
    }

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new MigLayout("insets 0, gap 0"));
        panel.setOpaque(false);

        String fullName = SessionManager.getCurrentUserFullName();
        String displayName = "User";
        if (fullName != null) {
            String[] parts = fullName.split(" ");
            displayName = parts[0];
            if (parts.length > 1 && "System".equalsIgnoreCase(displayName)) {
                displayName = fullName;
            }
        }

        JLabel greeting = new JLabel("Welcome back, " + displayName);
        greeting.setFont(ThemeManager.FONT_DISPLAY);
        greeting.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

        JLabel subtext = new JLabel("Here's what's happening in " + SemesterService.getCurrentSemesterDisplay());
        subtext.setFont(ThemeManager.FONT_BODY);
        subtext.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        panel.add(greeting, "wrap");
        panel.add(subtext);
        return panel;
    }

    // --- Student Widgets ---
    private JPanel createStudentWidgets() {
        JPanel container = new JPanel(new MigLayout("fillx, insets 0, gap 15, wrap 1", "[grow, fill]", "[]"));
        container.setOpaque(false);

        container.add(new SkeletonPanel(), "grow, h 150!");
        container.add(new SkeletonPanel(), "grow, h 150!");

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                return dashboardService.getStudentDashboardData(SessionManager.getCurrentUserId());
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    container.removeAll();

                    String nextCourse = (String) data.getOrDefault("nextCourse", "No Classes");
                    String nextTime = (String) data.getOrDefault("nextClassTime", "");
                    String nextRoom = (String) data.getOrDefault("nextClassRoom", "");
                    String subtitle = nextRoom.isEmpty() ? nextTime : nextRoom + " - " + nextTime;

                    container.add(createCardWidget("Next Class", nextCourse, subtitle, ThemeManager.COLOR_INSTRUCTOR), "growx");

                    String cgpa = (String) data.getOrDefault("cgpa", "N/A");
                    int enrolled = (int) data.getOrDefault("enrolledCourses", 0);
                    int pending = (int) data.getOrDefault("pendingGrades", 0);

                    container.add(createProgressWidget(cgpa, enrolled, pending), "growx");
                    container.revalidate();
                    container.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        return container;
    }

    private JPanel createProgressWidget(String cgpa, int enrolled, int pending) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        card.putClientProperty("Component.arc", 12);

        JLabel title = new JLabel("Academic Progress");
        title.setFont(ThemeManager.FONT_LABEL);
        title.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        final String finalCgpa = cgpa;
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2.setColor(new Color(241, 245, 249));
                g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Arc2D.Double(x, y, size, size, 0, 360, Arc2D.OPEN));

                double cgpaValue = 0;
                try {
                    cgpaValue = Double.parseDouble(finalCgpa);
                } catch (NumberFormatException e) {
                    cgpaValue = 0;
                }
                int arcAngle = (int) ((cgpaValue / 4.0) * 360);

                g2.setColor(ThemeManager.COLOR_STUDENT);
                g2.draw(new Arc2D.Double(x, y, size, size, 90, -arcAngle, Arc2D.OPEN));

                g2.setColor(ThemeManager.COLOR_TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(finalCgpa, getWidth() / 2 - fm.stringWidth(finalCgpa) / 2, getHeight() / 2 + fm.getAscent() / 2 - 2);
            }
        };
        chart.setPreferredSize(new Dimension(100, 100));
        chart.setOpaque(false);

        JPanel footer = new JPanel(new MigLayout("insets 5 15 10 15, fillx", "[grow][grow]"));
        footer.setBackground(new Color(248, 250, 252));

        JLabel enrolledLabel = new JLabel(enrolled + " Enrolled");
        enrolledLabel.setFont(ThemeManager.FONT_LABEL);
        enrolledLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        JLabel pendingLabel = new JLabel(pending + " Pending Grades");
        pendingLabel.setFont(ThemeManager.FONT_LABEL);
        pendingLabel.setForeground(pending > 0 ? ThemeManager.COLOR_WARNING : ThemeManager.COLOR_TEXT_SECONDARY);

        footer.add(enrolledLabel);
        footer.add(pendingLabel, "align right");

        card.add(title, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    // --- Instructor Widgets ---
    private JPanel createInstructorWidgets() {
        JPanel container = new JPanel(new MigLayout("fillx, insets 0, gap 15, wrap 1", "[grow, fill]", "[]"));
        container.setOpaque(false);

        container.add(new SkeletonPanel(), "grow, h 150!");
        container.add(new SkeletonPanel(), "grow, h 150!");

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                return dashboardService.getInstructorDashboardData(SessionManager.getCurrentUserId());
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    container.removeAll();

                    String actionCourse = (String) data.getOrDefault("actionCourse", "No Sections");
                    String actionPending = (String) data.getOrDefault("actionPending", "");
                    int pending = (int) data.getOrDefault("pendingGrades", 0);

                    Color actionColor = pending > 0 ? ThemeManager.COLOR_WARNING : ThemeManager.COLOR_SUCCESS;
                    container.add(createCardWidget("Action Item", actionCourse, actionPending, actionColor), "growx");

                    int sections = (int) data.getOrDefault("totalSections", 0);
                    int students = (int) data.getOrDefault("totalStudents", 0);
                    container.add(createStatsCard("Teaching Overview",
                        new String[]{"Sections", "Students", "Pending"},
                        new String[]{String.valueOf(sections), String.valueOf(students), String.valueOf(pending)}), "growx");

                    container.revalidate();
                    container.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        return container;
    }

    // --- Admin Widgets ---
    private JPanel createAdminWidgets() {
        JPanel container = new JPanel(new MigLayout("fillx, insets 0, gap 15, wrap 1", "[grow, fill]", "[]"));
        container.setOpaque(false);

        container.add(new SkeletonPanel(), "grow, h 140!");
        container.add(new SkeletonPanel(), "grow, h 140!");
        container.add(new SkeletonPanel(), "grow, h 140!");

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                return dashboardService.getAdminDashboardData();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    container.removeAll();

                    int totalUsers = (int) data.getOrDefault("totalUsers", 0);
                    int students = (int) data.getOrDefault("totalStudents", 0);
                    int instructors = (int) data.getOrDefault("totalInstructors", 0);
                    container.add(createHorizontalStatsPanel("User Statistics",
                        new String[]{"Total Users", "Students", "Instructors"},
                        new String[]{String.valueOf(totalUsers), String.valueOf(students), String.valueOf(instructors)},
                        ThemeManager.COLOR_ADMIN), "growx");

                    int courses = (int) data.getOrDefault("totalCourses", 0);
                    int sections = (int) data.getOrDefault("totalSections", 0);
                    int enrollments = (int) data.getOrDefault("totalEnrollments", 0);
                    container.add(createHorizontalStatsPanel("Academic Statistics",
                        new String[]{"Courses", "Sections", "Enrollments"},
                        new String[]{String.valueOf(courses), String.valueOf(sections), String.valueOf(enrollments)},
                        ThemeManager.COLOR_INSTRUCTOR), "growx");

                    boolean maintenance = (boolean) data.getOrDefault("maintenanceMode", false);
                    String lastBackup = getLastBackupTime();
                    container.add(createSystemHealthPanel(maintenance,
                        (int) data.getOrDefault("activeUsers", 0), lastBackup), "growx");

                    container.revalidate();
                    container.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        return container;
    }

    private JPanel createHorizontalStatsPanel(String title, String[] labels, String[] values, Color accent) {
        JPanel card = new JPanel(new MigLayout("fill, insets 15 20 15 20", "[grow]", "[]10[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER),
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent)));
        card.putClientProperty("Component.arc", 12);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(ThemeManager.FONT_LABEL);
        titleLabel.setForeground(accent);
        card.add(titleLabel, "wrap");

        JPanel statsRow = new JPanel(new MigLayout("insets 0, gap 30", "[grow][grow][grow]"));
        statsRow.setOpaque(false);

        for (int i = 0; i < labels.length && i < values.length; i++) {
            JPanel stat = new JPanel(new MigLayout("insets 0, wrap 1"));
            stat.setOpaque(false);

            JLabel valueLabel = new JLabel(values[i]);
            valueLabel.setFont(ThemeManager.FONT_DISPLAY);
            valueLabel.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

            JLabel labelLabel = new JLabel(labels[i]);
            labelLabel.setFont(ThemeManager.FONT_LABEL);
            labelLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

            stat.add(valueLabel);
            stat.add(labelLabel);
            statsRow.add(stat, "grow");
        }

        card.add(statsRow, "grow");
        return card;
    }

    private JPanel createSystemHealthPanel(boolean maintenanceMode, int activeUsers, String lastBackup) {
        JPanel card = new JPanel(new MigLayout("fill, insets 15 20 15 20", "[grow]", "[]10[]"));
        card.setBackground(Color.WHITE);
        Color statusColor = maintenanceMode ? ThemeManager.COLOR_WARNING : ThemeManager.COLOR_SUCCESS;
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER),
            BorderFactory.createMatteBorder(0, 4, 0, 0, statusColor)));
        card.putClientProperty("Component.arc", 12);

        JLabel titleLabel = new JLabel("SYSTEM HEALTH");
        titleLabel.setFont(ThemeManager.FONT_LABEL);
        titleLabel.setForeground(statusColor);
        card.add(titleLabel, "wrap");

        JPanel content = new JPanel(new MigLayout("insets 0, gap 30", "[grow][grow][grow]"));
        content.setOpaque(false);

        JPanel statusPanel = new JPanel(new MigLayout("insets 0, wrap 1"));
        statusPanel.setOpaque(false);
        String statusText = maintenanceMode ? "Maintenance ON" : "Operational";
        JLabel statusValue = new JLabel(statusText);
        statusValue.setFont(ThemeManager.FONT_TITLE.deriveFont(16f));
        statusValue.setForeground(statusColor);
        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(ThemeManager.FONT_LABEL);
        statusLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        statusPanel.add(statusValue);
        statusPanel.add(statusLabel);
        content.add(statusPanel, "grow");

        JPanel activePanel = new JPanel(new MigLayout("insets 0, wrap 1"));
        activePanel.setOpaque(false);
        JLabel activeValue = new JLabel(String.valueOf(activeUsers));
        activeValue.setFont(ThemeManager.FONT_DISPLAY);
        activeValue.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        JLabel activeLabel = new JLabel("Active Users");
        activeLabel.setFont(ThemeManager.FONT_LABEL);
        activeLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        activePanel.add(activeValue);
        activePanel.add(activeLabel);
        content.add(activePanel, "grow");

        JPanel backupPanel = new JPanel(new MigLayout("insets 0, wrap 1"));
        backupPanel.setOpaque(false);
        JLabel backupValue = new JLabel(lastBackup);
        backupValue.setFont(ThemeManager.FONT_BODY_BOLD);
        backupValue.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        JLabel backupLabel = new JLabel("Last Backup");
        backupLabel.setFont(ThemeManager.FONT_LABEL);
        backupLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        backupPanel.add(backupValue);
        backupPanel.add(backupLabel);
        content.add(backupPanel, "grow");

        card.add(content, "grow");
        return card;
    }

    private String getLastBackupTime() {
        try {
            java.io.File backupDir = new java.io.File("backups/");
            if (!backupDir.exists() || !backupDir.isDirectory()) {
                return "Never";
            }
            java.io.File[] backups = backupDir.listFiles((dir, name) ->
                name.startsWith("university_erp_backup_") && name.endsWith(".sql"));
            if (backups == null || backups.length == 0) {
                return "Never";
            }
            java.io.File mostRecent = backups[0];
            for (java.io.File f : backups) {
                if (f.lastModified() > mostRecent.lastModified()) {
                    mostRecent = f;
                }
            }
            java.time.Instant instant = java.time.Instant.ofEpochMilli(mostRecent.lastModified());
            java.time.LocalDateTime dt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
            return dt.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private JPanel createStatsCard(String title, String[] labels, String[] values) {
        JPanel card = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]10[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        card.putClientProperty("Component.arc", 12);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(ThemeManager.FONT_LABEL);
        titleLabel.setForeground(ThemeManager.getCurrentAccent());
        card.add(titleLabel, "wrap");

        JPanel statsGrid = new JPanel(new MigLayout("insets 0, gap 15", "[grow][grow][grow]"));
        statsGrid.setOpaque(false);

        for (int i = 0; i < labels.length && i < values.length; i++) {
            JPanel stat = new JPanel(new MigLayout("insets 0, wrap 1, align center"));
            stat.setOpaque(false);

            JLabel valueLabel = new JLabel(values[i]);
            valueLabel.setFont(ThemeManager.FONT_DISPLAY);
            valueLabel.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel labelLabel = new JLabel(labels[i]);
            labelLabel.setFont(ThemeManager.FONT_LABEL);
            labelLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
            labelLabel.setHorizontalAlignment(SwingConstants.CENTER);

            stat.add(valueLabel, "align center");
            stat.add(labelLabel, "align center");
            statsGrid.add(stat, "grow");
        }

        card.add(statsGrid, "grow");
        return card;
    }

    private JPanel createCardWidget(String category, String titleText, String subText, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 15, fill"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER),
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent)));

        JLabel cat = new JLabel(category.toUpperCase());
        cat.setFont(ThemeManager.FONT_LABEL);
        cat.setForeground(accent);

        JLabel title = new JLabel(titleText);
        title.setFont(ThemeManager.FONT_TITLE.deriveFont(16f));
        title.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

        JLabel sub = new JLabel(subText);
        sub.setFont(ThemeManager.FONT_BODY);
        sub.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        card.add(cat, "wrap");
        card.add(title, "wrap, gaptop 5");
        card.add(sub, "gaptop 2");

        return card;
    }

    // --- UPDATED NOTIFICATION WIDGET ---
    private JPanel createNotificationWidget() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 15 15 10 15", "[grow][]"));
        headerPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Notifications");
        title.setFont(ThemeManager.FONT_TITLE);

        DefaultListModel<Notification> listModel = new DefaultListModel<>();

        JButton clearBtn = new JButton("Clear All");
        clearBtn.setFont(ThemeManager.FONT_LABEL);
        clearBtn.setForeground(ThemeManager.COLOR_DANGER);
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            notificationService.markAllAsRead(SessionManager.getCurrentUserId(), SessionManager.getCurrentRole());
            listModel.clear();
            MainFrame.getInstance().showSuccess("All notifications cleared.");
        });

        headerPanel.add(title, "growx");
        headerPanel.add(clearBtn);

        JList<Notification> list = new JList<>(listModel);
        list.setCellRenderer(new NotificationRenderer());
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0 && index < listModel.size()) {
                        Notification n = listModel.getElementAt(index);
                        showNotificationDialog(n);
                        notificationService.markAsRead(n.getNotificationId());
                        listModel.removeElementAt(index);
                    }
                }
            }
        });

        new SwingWorker<List<Notification>, Void>() {
            @Override
            protected List<Notification> doInBackground() {
                return notificationService.getNotificationsForUser(SessionManager.getCurrentUserId(), SessionManager.getCurrentRole());
            }
            @Override
            protected void done() {
                try {
                    List<Notification> data = get();
                    listModel.clear();
                    for (Notification n : data) listModel.addElement(n);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        // CHANGE 1: Enable horizontal scrolling
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void showNotificationDialog(Notification notification) {
        JDialog dialog = new JDialog(MainFrame.getInstance(), "Notification", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(MainFrame.getInstance());

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 20", "[grow, fill]", "[]15[grow]15[]"));
        contentPanel.setBackground(Color.WHITE);

        JLabel timeLabel = new JLabel(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")));
        timeLabel.setFont(ThemeManager.FONT_LABEL);
        timeLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        contentPanel.add(timeLabel, "wrap");

        JTextArea messageArea = new JTextArea(notification.getMessage());
        messageArea.setFont(ThemeManager.FONT_BODY);
        messageArea.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setCaret(new javax.swing.text.DefaultCaret() {
            @Override public void paint(Graphics g) {  }
        });
        messageArea.setBackground(Color.WHITE);
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane msgScroll = new JScrollPane(messageArea);
        msgScroll.setBorder(null);
        contentPanel.add(msgScroll, "grow, wrap");

        JButton closeBtn = UIFactory.createPrimaryButton("Close", () -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(closeBtn);
        contentPanel.add(btnPanel, "growx");

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // --- UPDATED RENDERER ---
    private static class NotificationRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // CHANGE 2: Remove 'wmax 300' so panel can expand horizontally
            JPanel p = new JPanel(new MigLayout("insets 10 12 10 12, fillx, wrap 1", "[grow, fill]", "[]5[]"));
            p.setBackground(isSelected ? new Color(241, 245, 249) : Color.WHITE);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

            if (value instanceof Notification) {
                Notification n = (Notification) value;

                // CHANGE 3: Remove 'style=width:...' and 'wmax' constraint.
                // This allows the label to expand to its natural text width, triggering the scrollbar.
                JLabel msg = new JLabel("<html><body>" + escapeHtml(n.getMessage()) + "</body></html>");
                msg.setFont(ThemeManager.FONT_BODY);
                msg.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                msg.setVerticalAlignment(SwingConstants.TOP);

                JLabel time = new JLabel(n.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
                time.setFont(ThemeManager.FONT_LABEL);
                time.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

                p.add(msg, "growx"); // Removed 'wmax 300'

                JPanel footer = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
                footer.setOpaque(false);
                footer.add(time, "growx");

                if (!n.isRead()) {
                    JLabel dot = new JLabel("\u25CF");
                    dot.setForeground(ThemeManager.COLOR_STUDENT);
                    dot.setToolTipText("Unread");
                    footer.add(dot, "align right");
                }
                p.add(footer, "growx");
            }
            return p;
        }

        private String escapeHtml(String text) {
            if (text == null) return "";
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    public JPanel getPanel() { return this; }
}