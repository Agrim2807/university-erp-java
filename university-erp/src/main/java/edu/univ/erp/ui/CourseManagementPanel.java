package edu.univ.erp.ui;

import edu.univ.erp.domain.Course;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CourseManagementPanel {
    private JPanel mainPanel;
    private AdminService adminService;
    private CourseDAO courseDAO;

    private JList<Course> courseList;
    private DefaultListModel<Course> listModel;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private List<Course> allCoursesCache;
    private JPanel listContainer;

    public CourseManagementPanel() {
        this.adminService = new AdminService();
        this.courseDAO = new CourseDAO();
        initializePanel();
    }

    private void initializePanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.COLOR_BACKGROUND);

        mainPanel.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent event) { refreshList(); }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });

        
        JPanel topPanel = new JPanel(new MigLayout("fillx, insets 20 30 10 30", "[grow][]"));
        topPanel.setOpaque(false);
        topPanel.add(UIFactory.createHeader("Course Management"), "wrap");

        searchField = UIFactory.createInput("Search courses...");
        searchField.addActionListener(e -> applySortAndFilter());
        topPanel.add(searchField, "growx, wmin 300");
        topPanel.add(UIFactory.createSecondaryButton("Search", this::applySortAndFilter));

        sortCombo = new JComboBox<>(new String[]{"Sort: Code", "Sort: Title", "Sort: Credits"});
        sortCombo.setFont(ThemeManager.FONT_BODY);
        sortCombo.addActionListener(e -> applySortAndFilter());
        topPanel.add(sortCombo, "w 180!");

        mainPanel.add(topPanel, BorderLayout.NORTH);

        
        listContainer = new JPanel(new BorderLayout());
        listContainer.setOpaque(false);
        listContainer.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        listModel = new DefaultListModel<>();
        courseList = new JList<>(listModel);
        courseList.setCellRenderer(new CourseListRenderer());
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) showCourseDetails(courseList.getSelectedValue());
            }
        });

        JScrollPane scroll = new JScrollPane(courseList);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        listContainer.add(scroll, BorderLayout.CENTER);
        mainPanel.add(listContainer, BorderLayout.CENTER);

        
        JPanel bottom = new JPanel(new MigLayout("fillx, insets 15 30 20 30", "[grow][]"));
        bottom.setOpaque(false);

        JButton prereqBtn = UIFactory.createSecondaryButton("Prerequisites", this::openPrerequisiteDialog);
        JButton delBtn = UIFactory.createDangerButton("Delete", this::deleteCourse);
        JButton editBtn = UIFactory.createSecondaryButton("Edit", () -> {
            if(courseList.getSelectedValue() != null) openCourseDialog(courseList.getSelectedValue());
            else MainFrame.getInstance().showWarning("Select a course.");
        });
        JButton addBtn = UIFactory.createPrimaryButton("+ New Course", () -> openCourseDialog(null));

        bottom.add(new JLabel(""), "growx"); 
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(prereqBtn);
        btnPanel.add(delBtn);
        btnPanel.add(editBtn);
        btnPanel.add(addBtn);
        bottom.add(btnPanel, "wrap");

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    private void openCourseDialog(Course course) {
        boolean isEdit = (course != null);
        JDialog dialog = new JDialog(MainFrame.getInstance(), isEdit ? "Edit Course" : "New Course", true);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new MigLayout("fillx, insets 20", "[label]10[grow, fill]"));
        form.setBackground(Color.WHITE);

        JTextField codeField = UIFactory.createInput("Code");
        JTextField titleField = UIFactory.createInput("Title");

        // Credits spinner with validation (DB constraint: 1-6)
        SpinnerNumberModel creditsModel = new SpinnerNumberModel(3, 1, 6, 1);
        JSpinner creditsSpinner = new JSpinner(creditsModel);

        // Prevent manual typing of invalid values - force user to use arrows
        JComponent editor = creditsSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setEditable(false); // Disable manual typing, use arrows only
        }

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        JCheckBox activeCheck = new JCheckBox("Active");
        activeCheck.setOpaque(false);

        if (isEdit) {
            codeField.setText(course.getCode());
            titleField.setText(course.getTitle());
            creditsSpinner.setValue(course.getCredits());
            descArea.setText(course.getDescription());
            activeCheck.setSelected(course.isActive());
        } else {
            activeCheck.setSelected(true);
        }

        form.add(UIFactory.createLabel("Code")); form.add(codeField, "wrap, h 40!");
        form.add(UIFactory.createLabel("Title")); form.add(titleField, "wrap, h 40!");
        form.add(UIFactory.createLabel("Credits")); form.add(creditsSpinner, "wrap, h 40!");
        form.add(UIFactory.createLabel("Description")); form.add(new JScrollPane(descArea), "wrap");
        form.add(new JLabel()); form.add(activeCheck, "wrap");

        JButton save = UIFactory.createPrimaryButton("Save", () -> {
             if (codeField.getText().isEmpty() || titleField.getText().isEmpty()) {
                MainFrame.getInstance().showWarning("Code and Title are required."); return;
             }

             // Validate credits (must be 1-6 as per database CHECK constraint)
             int credits = (int) creditsSpinner.getValue();
             if (credits < 1 || credits > 6) {
                MainFrame.getInstance().showError("Credits must be between 1 and 6.");
                return;
             }

             new SwingWorker<Boolean, Void>() {
                 @Override protected Boolean doInBackground() throws Exception {
                     if (!isEdit) return adminService.createCourse(codeField.getText(), titleField.getText(), (int)creditsSpinner.getValue(), descArea.getText());
                     else return adminService.updateCourse(course.getCourseId(), codeField.getText(), titleField.getText(), (int)creditsSpinner.getValue(), descArea.getText());
                 }
                 @Override protected void done() {
                     try {
                         if(get()) { MainFrame.getInstance().showSuccess("Saved."); refreshList(); dialog.dispose(); }
                         else MainFrame.getInstance().showError("Failed.");
                     } catch(Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                 }
             }.execute();
        });

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acts.setBackground(Color.WHITE);
        acts.add(save);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(acts, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void openPrerequisiteDialog() {
        Course selected = courseList.getSelectedValue();
        if (selected == null) {
            MainFrame.getInstance().showWarning("Select a course to manage its prerequisites.");
            return;
        }

        JDialog dialog = new JDialog(MainFrame.getInstance(), "Prerequisites: " + selected.getCode(), true);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel(new MigLayout("fill, insets 20", "[grow][grow]", "[][grow][]"));
        content.setBackground(Color.WHITE);

        
        JLabel currentLabel = UIFactory.createLabel("Current Prerequisites");
        currentLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        DefaultListModel<Course> prereqModel = new DefaultListModel<>();
        JList<Course> prereqList = new JList<>(prereqModel);
        prereqList.setCellRenderer(new SimpleCourseCellRenderer());

        
        JLabel availLabel = UIFactory.createLabel("Available Courses");
        availLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        DefaultListModel<Course> availModel = new DefaultListModel<>();
        JList<Course> availList = new JList<>(availModel);
        availList.setCellRenderer(new SimpleCourseCellRenderer());

        
        new SwingWorker<Void, Void>() {
            List<Course> currentPrereqs;
            List<Course> allCourses;
            @Override protected Void doInBackground() throws Exception {
                
                currentPrereqs = courseDAO.getPrerequisites(selected.getCourseId());
                allCourses = adminService.getAllCourses();
                return null;
            }
            @Override protected void done() {
                try {
                    prereqModel.clear();
                    availModel.clear();

                    
                    java.util.Set<Integer> prereqIds = new java.util.HashSet<>();
                    for (Course p : currentPrereqs) {
                        prereqModel.addElement(p);
                        prereqIds.add(p.getCourseId());
                    }

                    
                    for (Course c : allCourses) {
                        if (c.getCourseId() != selected.getCourseId() && !prereqIds.contains(c.getCourseId())) {
                            availModel.addElement(c);
                        }
                    }
                } catch (Exception e) {
                    MainFrame.getInstance().showError("Failed to load prerequisites.");
                }
            }
        }.execute();

        
        JButton addBtn = UIFactory.createPrimaryButton("< Add", () -> {
            Course sel = availList.getSelectedValue();
            if (sel == null) {
                MainFrame.getInstance().showWarning("Select a course to add as prerequisite.");
                return;
            }
            
            for (int i = 0; i < prereqModel.size(); i++) {
                if (prereqModel.getElementAt(i).getCourseId() == sel.getCourseId()) {
                    MainFrame.getInstance().showWarning("This course is already a prerequisite.");
                    return;
                }
            }
            new SwingWorker<Boolean, Void>() {
                private String errorMessage = null;
                @Override protected Boolean doInBackground() throws Exception {
                    try {
                        return courseDAO.addPrerequisite(selected.getCourseId(), sel.getCourseId());
                    } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                        errorMessage = "This course is already a prerequisite.";
                        return false;
                    } catch (java.sql.SQLException e) {
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                            errorMessage = "This course is already a prerequisite.";
                        } else {
                            errorMessage = "Database error: " + e.getMessage();
                        }
                        return false;
                    }
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            prereqModel.addElement(sel);
                            availModel.removeElement(sel);
                            MainFrame.getInstance().showSuccess("Prerequisite added.");
                        } else if (errorMessage != null) {
                            MainFrame.getInstance().showWarning(errorMessage);
                        }
                    } catch (Exception e) {
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        if (msg != null && msg.toLowerCase().contains("duplicate")) {
                            MainFrame.getInstance().showWarning("This course is already a prerequisite.");
                        } else {
                            MainFrame.getInstance().showError("Failed to add prerequisite. Please try again.");
                        }
                    }
                }
            }.execute();
        });

        
        JButton removeBtn = UIFactory.createDangerButton("Remove >", () -> {
            Course sel = prereqList.getSelectedValue();
            if (sel == null) return;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    return courseDAO.removePrerequisite(selected.getCourseId(), sel.getCourseId());
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            availModel.addElement(sel);
                            prereqModel.removeElement(sel);
                            MainFrame.getInstance().showSuccess("Prerequisite removed.");
                        }
                    } catch (Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        });

        content.add(currentLabel);
        content.add(availLabel, "wrap");

        JScrollPane prereqScroll = new JScrollPane(prereqList);
        prereqScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        content.add(prereqScroll, "grow");

        JScrollPane availScroll = new JScrollPane(availList);
        availScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        content.add(availScroll, "grow, wrap");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        content.add(btnPanel, "span 2, align center");

        dialog.add(content, BorderLayout.CENTER);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void refreshList() {
        listContainer.removeAll();
        listContainer.add(new SkeletonPanel(), BorderLayout.CENTER);
        listContainer.revalidate(); listContainer.repaint();

        new SwingWorker<List<Course>, Void>() {
            @Override protected List<Course> doInBackground() { return adminService.getAllCourses(); }
            @Override protected void done() {
                try {
                    allCoursesCache = get();
                    listContainer.removeAll();
                    JScrollPane scroll = new JScrollPane(courseList);
                    scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
                    listContainer.add(scroll, BorderLayout.CENTER);
                    applySortAndFilter();
                    listContainer.revalidate(); listContainer.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void applySortAndFilter() {
        if (allCoursesCache == null) return;
        listModel.clear();

        // Filter
        String q = searchField.getText().toLowerCase();
        List<Course> filtered = allCoursesCache.stream()
                .filter(c -> c.getCode().toLowerCase().contains(q) || c.getTitle().toLowerCase().contains(q))
                .collect(Collectors.toList());

        // Sort
        String sortOption = (String) sortCombo.getSelectedItem();
        if ("Sort: Code".equals(sortOption)) {
            filtered.sort((c1, c2) -> c1.getCode().compareToIgnoreCase(c2.getCode()));
        } else if ("Sort: Title".equals(sortOption)) {
            filtered.sort((c1, c2) -> c1.getTitle().compareToIgnoreCase(c2.getTitle()));
        } else if ("Sort: Credits".equals(sortOption)) {
            filtered.sort((c1, c2) -> Integer.compare(c2.getCredits(), c1.getCredits()));
        }

        // Populate list
        for (Course c : filtered) {
            listModel.addElement(c);
        }
    }

    private void deleteCourse() {
        Course c = courseList.getSelectedValue();
        if (c == null) { MainFrame.getInstance().showWarning("Select a course."); return; }

        // Check enrollment count
        int enrollmentCount = adminService.getCourseEnrollmentCount(c.getCourseId());
        String warningMsg = "Are you sure you want to delete this course?";
        ConfirmDialog.DialogType dialogType = ConfirmDialog.DialogType.WARNING;

        if (enrollmentCount > 0) {
            warningMsg = "WARNING: This course has sections with " + enrollmentCount +
                         " total enrollment(s). Deleting will cascade delete all sections, enrollments, and grades.";
            dialogType = ConfirmDialog.DialogType.DANGER;
        }

        if (ConfirmDialog.show(mainPanel, "Confirm Deletion", warningMsg, dialogType)) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception { return adminService.deleteCourse(c.getCourseId()); }
                @Override protected void done() {
                    try {
                        if(get()) { MainFrame.getInstance().showSuccess("Deleted."); refreshList(); }
                        else MainFrame.getInstance().showError("Failed.");
                    } catch(Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        }
    }

    
    private void showCourseDetails(Course course) {
        if (course == null) return;

        JDialog dialog = new JDialog(MainFrame.getInstance(), "Course Details", true);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right]15[grow, fill]"));
        form.setBackground(Color.WHITE);

        
        addDetailRow(form, "Code", course.getCode());
        addDetailRow(form, "Title", course.getTitle());
        addDetailRow(form, "Credits", String.valueOf(course.getCredits()));
        addDetailRow(form, "Status", course.isActive() ? "Active" : "Inactive");

        
        JLabel descLabel = UIFactory.createLabel("Description:");
        descLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        JTextArea descArea = new JTextArea(course.getDescription() != null ? course.getDescription() : "No description");
        descArea.setEditable(false);
        // --- FIX: Make non-focusable to prevent cursor appearing ---
        descArea.setFocusable(false);
        // -----------------------------------------------------------
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(ThemeManager.FONT_BODY);
        descArea.setBackground(new Color(248, 250, 252));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(300, 80));
        form.add(descLabel);
        form.add(descScroll, "h 80!");

        
        try {
            java.util.List<Course> prereqs = courseDAO.getPrerequisites(course.getCourseId());
            String prereqStr = prereqs.isEmpty() ? "None" : prereqs.stream()
                .map(p -> p.getCode())
                .collect(java.util.stream.Collectors.joining(", "));
            addDetailRow(form, "Prerequisites", prereqStr);
        } catch (SQLException e) {
            addDetailRow(form, "Prerequisites", "Error loading");
        }

        JButton closeBtn = UIFactory.createSecondaryButton("Close", dialog::dispose);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);
        actions.add(closeBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 350));
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lblComponent = UIFactory.createLabel(label + ":");
        lblComponent.setFont(ThemeManager.FONT_BODY_BOLD);
        JLabel valueComponent = new JLabel(value != null ? value : "N/A");
        valueComponent.setFont(ThemeManager.FONT_BODY);
        panel.add(lblComponent);
        panel.add(valueComponent, "h 30!");
    }

    private static class CourseListRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel p = new JPanel(new BorderLayout(10,5));
            p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            if (value instanceof Course) {
                Course c = (Course) value;
                JLabel title = new JLabel(c.getCode() + " - " + c.getTitle());
                title.setFont(ThemeManager.FONT_BODY_BOLD);
                JLabel meta = new JLabel(c.getCredits() + " Credits - " + (c.isActive()?"Active":"Inactive"));
                meta.setFont(ThemeManager.FONT_LABEL);
                meta.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
                p.add(title, BorderLayout.NORTH);
                p.add(meta, BorderLayout.SOUTH);
                if(isSelected) {
                    p.setBackground(new Color(241, 245, 249));
                    title.setForeground(ThemeManager.getCurrentAccent());
                } else {
                    p.setBackground(Color.WHITE);
                    title.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                }
            }
            return p;
        }
    }

    private static class SimpleCourseCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Course) {
                Course c = (Course) value;
                setText(c.getCode() + " - " + c.getTitle());
            }
            return this;
        }
    }

    
    private String getCleanErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "An unexpected error occurred.";

        if (e.getCause() != null && e.getCause().getMessage() != null) {
            msg = e.getCause().getMessage();
        }

        if (msg.contains(": ")) {
            int colonIdx = msg.indexOf(": ");
            String prefix = msg.substring(0, colonIdx);
            if (prefix.contains(".") && !prefix.contains(" ")) {
                msg = msg.substring(colonIdx + 2);
            }
        }
        return msg;
    }

    public JPanel getPanel() { return mainPanel; }
}