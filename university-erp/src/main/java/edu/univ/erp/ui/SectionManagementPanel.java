package edu.univ.erp.ui;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class SectionManagementPanel {
    private JPanel mainPanel;
    private AdminService adminService;
    
    private JList<Section> sectionList;
    private DefaultListModel<Section> listModel;
    private JPanel listContainer;
    private JTextField searchField;
    private JComboBox<String> sortCombo;

    private List<Course> coursesCache;
    private List<User> instructorsCache;
    private List<Section> allSectionsCache;

    public SectionManagementPanel() {
        this.adminService = new AdminService();
        initializePanel();
    }

    private void initializePanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.COLOR_BACKGROUND);

        mainPanel.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent event) { refreshData(); }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });


        JPanel header = new JPanel(new MigLayout("fillx, insets 20 30 10 30", "[grow][]"));
        header.setOpaque(false);
        header.add(UIFactory.createHeader("Section Management"), "wrap");

        searchField = UIFactory.createInput("Search sections...");
        searchField.addActionListener(e -> applySortAndFilter());
        header.add(searchField, "growx, wmin 300");
        header.add(UIFactory.createSecondaryButton("Search", this::applySortAndFilter));

        sortCombo = new JComboBox<>(new String[]{"Sort: Course", "Sort: Semester", "Sort: Instructor"});
        sortCombo.setFont(ThemeManager.FONT_BODY);
        sortCombo.addActionListener(e -> applySortAndFilter());
        header.add(sortCombo, "w 180!");

        mainPanel.add(header, BorderLayout.NORTH);

        
        listContainer = new JPanel(new BorderLayout());
        listContainer.setOpaque(false);
        listContainer.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        
        listModel = new DefaultListModel<>();
        sectionList = new JList<>(listModel);
        sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionList.setCellRenderer(new SectionRenderer());
        sectionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) showSectionDetails(sectionList.getSelectedValue());
            }
        });
        
        JScrollPane scroll = new JScrollPane(sectionList);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        listContainer.add(scroll, BorderLayout.CENTER);
        mainPanel.add(listContainer, BorderLayout.CENTER);
        
        
        JPanel bottom = new JPanel(new MigLayout("fillx, insets 15 30 20 30", "[grow][]"));
        bottom.setOpaque(false);

        JButton delBtn = UIFactory.createDangerButton("Delete", this::deleteSection);
        JButton editBtn = UIFactory.createSecondaryButton("Edit", () -> {
            if(sectionList.getSelectedValue() != null) openSectionDialog(sectionList.getSelectedValue());
            else MainFrame.getInstance().showWarning("Select a section.");
        });
        JButton addBtn = UIFactory.createPrimaryButton("+ New Section", () -> openSectionDialog(null));

        bottom.add(new JLabel(""), "growx"); 
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(delBtn);
        btnPanel.add(editBtn);
        btnPanel.add(addBtn);
        bottom.add(btnPanel, "wrap");

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    private void openSectionDialog(Section s) {
        boolean isEdit = (s != null);
        JDialog dialog = new JDialog(MainFrame.getInstance(), isEdit ? "Edit Section" : "New Section", true);
        dialog.setLayout(new BorderLayout());
        
        
        JPanel form = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[label]10[grow, fill]"));
        form.setBackground(Color.WHITE);
        
        JComboBox<Course> courseCombo = new JComboBox<>(new Vector<>(coursesCache));
        JComboBox<User> instrCombo = new JComboBox<>(new Vector<>(instructorsCache));
        JTextField secCode = UIFactory.createInput("A");
        JTextField time = UIFactory.createInput("Mon/Wed 10:00-11:30");
        JTextField room = UIFactory.createInput("Room 101");
        JSpinner cap = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
        JComboBox<String> sem = new JComboBox<>(new String[]{"Monsoon", "Winter", "Summer"});
        JSpinner year = new JSpinner(new SpinnerNumberModel(Year.now().getValue(), 2020, 2030, 1));
        DatePicker addDate = new DatePicker();
        DatePicker dropDate = new DatePicker();

        if (isEdit) {
            for(int i=0; i<courseCombo.getItemCount(); i++) 
                if(courseCombo.getItemAt(i).getCourseId() == s.getCourseId()) courseCombo.setSelectedIndex(i);
            for(int i=0; i<instrCombo.getItemCount(); i++)
                if(instrCombo.getItemAt(i).getUserId() == s.getInstructorId()) instrCombo.setSelectedIndex(i);
                
            secCode.setText(s.getSectionCode());
            time.setText(s.getDayTime());
            room.setText(s.getRoom());
            cap.setValue(s.getCapacity());
            sem.setSelectedItem(s.getSemester());
            year.setValue(s.getYear());
            if(s.getAddDeadline()!=null) addDate.setDate(s.getAddDeadline());
            if(s.getDropDeadline()!=null) dropDate.setDate(s.getDropDeadline());
        }

        form.add(UIFactory.createLabel("Course")); form.add(courseCombo, "h 40!");
        form.add(UIFactory.createLabel("Instructor")); form.add(instrCombo, "h 40!");
        form.add(UIFactory.createLabel("Section Code")); form.add(secCode, "h 40!");
        form.add(UIFactory.createLabel("Day/Time")); form.add(time, "h 40!");
        form.add(UIFactory.createLabel("Room")); form.add(room, "h 40!");
        form.add(UIFactory.createLabel("Capacity")); form.add(cap, "h 40!");
        form.add(UIFactory.createLabel("Semester")); form.add(sem, "h 40!");
        form.add(UIFactory.createLabel("Year")); form.add(year, "h 40!");
        form.add(UIFactory.createLabel("Add By")); form.add(addDate, "h 40!");
        form.add(UIFactory.createLabel("Drop By")); form.add(dropDate, "h 40!");

        JButton save = UIFactory.createPrimaryButton("Save", () -> {
            Course c = (Course) courseCombo.getSelectedItem();
            User i = (User) instrCombo.getSelectedItem();

            
            if(c == null) { MainFrame.getInstance().showWarning("Please select a course."); return; }
            if(i == null) { MainFrame.getInstance().showWarning("Please select an instructor."); return; }
            if(secCode.getText().trim().isEmpty()) { MainFrame.getInstance().showWarning("Section code cannot be empty."); return; }
            if(time.getText().trim().isEmpty()) { MainFrame.getInstance().showWarning("Day/Time cannot be empty."); return; }
            if((int)cap.getValue() <= 0) { MainFrame.getInstance().showWarning("Capacity must be greater than 0."); return; }

            
            String timeStr = time.getText().trim();
            if (!validateTimeFormat(timeStr)) {
                MainFrame.getInstance().showWarning("Invalid time format. Use format: Day/Day HH:MM-HH:MM (e.g., Mon/Wed 10:00-11:30)");
                return;
            }
            if (!validateTimeNotExceed1800(timeStr)) {
                MainFrame.getInstance().showWarning("Class time cannot exceed 18:00. Please adjust the schedule.");
                return;
            }

            
            LocalDate addDl = addDate.getDate();
            LocalDate dropDl = dropDate.getDate();
            if(addDl != null && dropDl != null && dropDl.isBefore(addDl)) {
                MainFrame.getInstance().showWarning("Drop deadline must be after add deadline.");
                return;
            }

            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    if(!isEdit) return adminService.createSection(c.getCourseId(), i.getUserId(), secCode.getText().trim(), time.getText().trim(), room.getText().trim(), (int)cap.getValue(), (String)sem.getSelectedItem(), (int)year.getValue(), addDl, dropDl);
                    else return adminService.updateSection(s.getSectionId(), c.getCourseId(), i.getUserId(), secCode.getText().trim(), time.getText().trim(), room.getText().trim(), (int)cap.getValue(), (String)sem.getSelectedItem(), (int)year.getValue(), addDl, dropDl);
                }
                @Override protected void done() {
                    try { if(get()) { MainFrame.getInstance().showSuccess("Section saved successfully."); refreshData(); dialog.dispose(); } else MainFrame.getInstance().showError("Failed to save section."); } catch(Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        });

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acts.setBackground(Color.WHITE);
        acts.add(save);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(acts, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setMinimumSize(new Dimension(500, 600)); 
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void refreshData() {
        listContainer.removeAll();
        listContainer.add(new SkeletonPanel(), BorderLayout.CENTER);
        listContainer.revalidate(); listContainer.repaint();

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                coursesCache = adminService.getAllCourses();
                instructorsCache = adminService.getAllInstructors();
                allSectionsCache = adminService.getAllSections();
                return null;
            }
            @Override protected void done() {
                listContainer.removeAll();
                JScrollPane scroll = new JScrollPane(sectionList);
                scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
                listContainer.add(scroll, BorderLayout.CENTER);
                applySortAndFilter();
                listContainer.revalidate(); listContainer.repaint();
            }
        }.execute();
    }

    private void applySortAndFilter() {
        if (allSectionsCache == null) return;
        listModel.clear();

        // Filter
        String query = searchField.getText().toLowerCase();
        List<Section> filtered = allSectionsCache.stream()
                .filter(s -> {
                    if (query.isEmpty()) return true;
                    String courseCode = s.getCourseCode() != null ? s.getCourseCode().toLowerCase() : "";
                    String sectionCode = s.getSectionCode() != null ? s.getSectionCode().toLowerCase() : "";
                    String instructor = s.getInstructorName() != null ? s.getInstructorName().toLowerCase() : "";
                    return courseCode.contains(query) || sectionCode.contains(query) || instructor.contains(query);
                })
                .collect(Collectors.toList());

        // Sort
        String sortOption = (String) sortCombo.getSelectedItem();
        if ("Sort: Course".equals(sortOption)) {
            filtered.sort((s1, s2) -> s1.getCourseCode().compareToIgnoreCase(s2.getCourseCode()));
        } else if ("Sort: Semester".equals(sortOption)) {
            filtered.sort((s1, s2) -> {
                int semCompare = s1.getSemester().compareToIgnoreCase(s2.getSemester());
                return semCompare != 0 ? semCompare : Integer.compare(s2.getYear(), s1.getYear());
            });
        } else if ("Sort: Instructor".equals(sortOption)) {
            filtered.sort((s1, s2) -> s1.getInstructorName().compareToIgnoreCase(s2.getInstructorName()));
        }

        // Populate list
        for (Section s : filtered) {
            listModel.addElement(s);
        }
    }

    private void deleteSection() {
        Section s = sectionList.getSelectedValue();
        if(s == null) { MainFrame.getInstance().showWarning("Please select a section to delete."); return; }

        
        String warningMsg = "Are you sure you want to delete this section?";
        ConfirmDialog.DialogType dialogType = ConfirmDialog.DialogType.WARNING;
        if(s.getEnrollmentCount() > 0) {
            warningMsg = "WARNING: This section has " + s.getEnrollmentCount() +
                         " enrolled student(s). Deleting will remove all enrollments and grades.";
            dialogType = ConfirmDialog.DialogType.DANGER;
        }

        if(ConfirmDialog.show(mainPanel, "Confirm Deletion", warningMsg, dialogType)) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception { return adminService.deleteSection(s.getSectionId()); }
                @Override protected void done() {
                    try {
                        if(get()) { MainFrame.getInstance().showSuccess("Section deleted successfully."); refreshData(); }
                        else MainFrame.getInstance().showError("Failed to delete section.");
                    } catch(Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        }
    }

    private static class SectionRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
            JPanel p = new JPanel(new BorderLayout(10,5));
            p.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            if(value instanceof Section) {
                Section s = (Section) value;
                JLabel t = new JLabel(s.getCourseCode() + " - Sec " + s.getSectionCode());
                t.setFont(ThemeManager.FONT_BODY_BOLD);
                JLabel d = new JLabel(s.getSemester() + " " + s.getYear() + " • " + s.getInstructorName());
                d.setFont(ThemeManager.FONT_LABEL);
                d.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
                p.add(t, BorderLayout.NORTH); p.add(d, BorderLayout.SOUTH);
                if(isSelected) { p.setBackground(new Color(241,245,249)); t.setForeground(ThemeManager.getCurrentAccent()); }
                else { p.setBackground(Color.WHITE); t.setForeground(ThemeManager.COLOR_TEXT_PRIMARY); }
            }
            return p;
        }
    }

    
    private void showSectionDetails(Section section) {
        if (section == null) return;

        JDialog dialog = new JDialog(MainFrame.getInstance(), "Section Details", true);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right]15[grow, fill]"));
        form.setBackground(Color.WHITE);

        
        addDetailRow(form, "Course Code", section.getCourseCode());
        addDetailRow(form, "Course Title", section.getCourseTitle());
        addDetailRow(form, "Section Code", section.getSectionCode());
        addDetailRow(form, "Instructor", section.getInstructorName());
        addDetailRow(form, "Day/Time", section.getDayTime());
        addDetailRow(form, "Room", section.getRoom());
        addDetailRow(form, "Capacity", section.getEnrollmentCount() + " / " + section.getCapacity());
        addDetailRow(form, "Semester", section.getSemester() + " " + section.getYear());
        addDetailRow(form, "Add Deadline", section.getAddDeadline() != null ? section.getAddDeadline().toString() : "Not set");
        addDetailRow(form, "Drop Deadline", section.getDropDeadline() != null ? section.getDropDeadline().toString() : "Not set");

        JButton closeBtn = UIFactory.createSecondaryButton("Close", dialog::dispose);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);
        actions.add(closeBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 400));
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

    public JPanel getPanel() { return mainPanel; }

    
    private boolean validateTimeFormat(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return false;

        
        String[] parts = timeStr.split(" ");
        if (parts.length < 2) return false;

        String timePart = parts[1];
        
        if (!timePart.matches("\\d{1,2}:\\d{2}(-\\d{1,2}:\\d{2})?")) {
            return false;
        }
        return true;
    }

    
    private boolean validateTimeNotExceed1800(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return true;

        try {
            String[] parts = timeStr.split(" ");
            if (parts.length < 2) return true;

            String timePart = parts[1];
            String endTime;

            if (timePart.contains("-")) {
                endTime = timePart.split("-")[1];
            } else {
                
                String startTime = timePart;
                int startMinutes = parseTimeToMinutes(startTime);
                int endMinutes = startMinutes + 60;
                return endMinutes <= 18 * 60; 
            }

            int endMinutes = parseTimeToMinutes(endTime);
            return endMinutes <= 18 * 60; 
        } catch (Exception e) {
            return true; 
        }
    }

    
    private int parseTimeToMinutes(String time) {
        if (time == null || !time.contains(":")) return 0;
        String[] parts = time.trim().split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    
    private String getCleanErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "An unexpected error occurred.";

        
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            msg = e.getCause().getMessage();
        }

        
        if (msg.contains("Duplicate entry") && msg.contains("sections.unique_section")) {
            return "This section already exists. A section with this Course, Code, Semester, and Year is already defined.";
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
}