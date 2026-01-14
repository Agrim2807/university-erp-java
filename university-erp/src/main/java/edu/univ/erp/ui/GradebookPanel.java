package edu.univ.erp.ui;

import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.StudentGradeEntry;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.InstructorServiceException;
import edu.univ.erp.util.GradeCsvHandler;
import edu.univ.erp.util.TableUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradebookPanel {
    private JPanel mainPanel;
    private JTable gradebookTable;
    private GradebookTableModel tableModel;
    
    private JButton saveBtn;
    private JButton computeBtn;
    private JButton importBtn;
    private JButton exportCsvBtn; 
    private JButton statsBtn;
    private JButton reportBtn;
    private JButton componentsBtn;
    
    private JLabel sectionTitle;
    private JLabel statusLabel;

    private InstructorService instructorService;
    private Section currentSection;
    private List<GradeComponent> components = new ArrayList<>();
    private List<StudentGradeEntry> students = new ArrayList<>();

    public GradebookPanel() {
        this.instructorService = new InstructorService();
        initializePanel();
    }

    private void initializePanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.COLOR_BACKGROUND);

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel(new MigLayout("fillx, insets 20 30 10 30", "[]20[grow][]", "[center]10[]"));
        topPanel.setOpaque(false);

        JButton backBtn = UIFactory.createSecondaryButton("\u2190 Back to Sections", () ->
            MainFrame.getInstance().showPanel(MainFrame.INSTRUCTOR_SECTIONS_PANEL));

        sectionTitle = UIFactory.createHeader("Select a Section");
        statusLabel = UIFactory.createLabel("Ready");

        // Initialize Buttons
        componentsBtn = UIFactory.createSecondaryButton("Components", this::showManageComponentsDialog);
        importBtn = UIFactory.createSecondaryButton("Import CSV", this::importGradesFromCsv);
        statsBtn = UIFactory.createSecondaryButton("Analytics", this::showStatsPanel);
        
        // These two will go to the bottom
        exportCsvBtn = UIFactory.createSecondaryButton("Export CSV", this::exportGradesToCsv);
        reportBtn = UIFactory.createSecondaryButton("Export PDF", this::showReportPanel);

        computeFinalGradeButton();
        saveBtn = UIFactory.createPrimaryButton("Save Changes", this::saveAllGrades);

        // Add operational buttons to Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.add(componentsBtn);
        toolbar.add(importBtn);
        toolbar.add(statsBtn);
        toolbar.add(computeBtn);
        toolbar.add(saveBtn);

        topPanel.add(backBtn, "aligny center");
        topPanel.add(sectionTitle, "growx, aligny center");
        topPanel.add(statusLabel, "aligny center, wrap");
        topPanel.add(toolbar, "span, growx, alignright");

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- CENTER TABLE ---
        tableModel = new GradebookTableModel();
        gradebookTable = new JTable(tableModel);
        
        gradebookTable.setRowHeight(35);
        gradebookTable.setShowVerticalLines(true);
        gradebookTable.setShowHorizontalLines(true);
        gradebookTable.setGridColor(ThemeManager.COLOR_BORDER);
        gradebookTable.setFont(ThemeManager.FONT_BODY);
        gradebookTable.getTableHeader().setFont(ThemeManager.FONT_LABEL);
        gradebookTable.getTableHeader().setBackground(new Color(248, 250, 252));
        gradebookTable.setSelectionBackground(new Color(224, 242, 254));
        gradebookTable.setSelectionForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        
        gradebookTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                }
                return c;
            }
        });

        // Enable sorting
        TableUtils.enableSorting(gradebookTable);

        JScrollPane scrollPane = new JScrollPane(gradebookTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30)); // Reduced bottom padding slightly
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM PANEL (EXPORTS) ---
        JPanel bottomPanel = new JPanel(new MigLayout("fillx, insets 15 30 20 30", "[grow][]"));
        bottomPanel.setOpaque(false);

        JPanel exportGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exportGroup.setOpaque(false);
        exportGroup.add(exportCsvBtn);
        exportGroup.add(reportBtn);

        bottomPanel.add(new JLabel(""), "growx"); // Spacer to push buttons to the right
        bottomPanel.add(exportGroup);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setActionsEnabled(false);
    }
    
    private void computeFinalGradeButton() {
        computeBtn = new JButton("Compute Finals");
        computeBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        computeBtn.setBackground(ThemeManager.COLOR_SUCCESS);
        computeBtn.setForeground(Color.WHITE);
        computeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        computeBtn.putClientProperty("JButton.buttonType", "roundRect");
        computeBtn.addActionListener(e -> computeFinalGrades());
    }

    public void loadSection(Section section) {
        this.currentSection = section;
        if (section == null) {
            sectionTitle.setText("No Section Selected");
            statusLabel.setText("Select a section from the sidebar");
            tableModel.setData(new ArrayList<>(), new ArrayList<>());
            setActionsEnabled(false);
            return;
        }

        sectionTitle.setText(section.getCourseCode() + " - " + section.getSectionCode());
        statusLabel.setText("Loading gradebook data...");
        setActionsEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    components = instructorService.getGradeComponents(section.getSectionId());
                    students = instructorService.getGradesForSection(section.getSectionId());
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }
            @Override
            protected void done() {
                tableModel.setData(components, students);
                statusLabel.setText(students.size() + " Students • " + components.size() + " Assessments");
                setActionsEnabled(true);
            }
        }.execute();
    }

    private void setActionsEnabled(boolean enabled) {
        saveBtn.setEnabled(enabled);
        computeBtn.setEnabled(enabled);
        importBtn.setEnabled(enabled);
        exportCsvBtn.setEnabled(enabled);
        statsBtn.setEnabled(enabled);
        reportBtn.setEnabled(enabled);
        componentsBtn.setEnabled(enabled);
    }

    

    private void showManageComponentsDialog() {
        JDialog dialog = new JDialog(MainFrame.getInstance(), "Manage Grade Components", true);
        dialog.setLayout(new BorderLayout());
        
        
        DefaultListModel<GradeComponent> listModel = new DefaultListModel<>();
        components.forEach(listModel::addElement);
        JList<GradeComponent> compList = new JList<>(listModel);
        compList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                GradeComponent c = (GradeComponent) value;
                setText(c.getComponentName() + " (" + c.getWeight() + "% / Max " + c.getMaxScore() + ")");
                return this;
            }
        });
        
        
        JPanel form = new JPanel(new MigLayout("fillx, insets 20", "[label]10[grow, fill]"));
        form.setBackground(Color.WHITE);
        
        JTextField nameField = UIFactory.createInput("Name (e.g. Quiz 1)");
        JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 100.0, 1.0));
        JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 1.0));
        
        form.add(UIFactory.createLabel("Name")); form.add(nameField, "wrap, h 40!");
        form.add(UIFactory.createLabel("Weight %")); form.add(weightSpinner, "wrap, h 40!");
        form.add(UIFactory.createLabel("Max Score")); form.add(maxSpinner, "wrap, h 40!");

        JButton addBtn = UIFactory.createPrimaryButton("Add Component", () -> {
            // --- VALIDATION START ---
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                MainFrame.getInstance().showWarning("Component name cannot be empty.");
                return;
            }

            double newWeight = (double) weightSpinner.getValue();
            double currentTotal = components.stream().mapToDouble(GradeComponent::getWeight).sum();
            
            if (currentTotal + newWeight > 100.0) {
                MainFrame.getInstance().showWarning("Total weight cannot exceed 100%. Current total: " + String.format("%.1f", currentTotal) + "%");
                return;
            }
            // --- VALIDATION END ---

            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    return instructorService.defineGradeComponent(currentSection.getSectionId(), 
                        name, newWeight, (double) maxSpinner.getValue());
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            loadSection(currentSection); 
                            dialog.dispose();
                            showManageComponentsDialog(); 
                        }
                    } catch (Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        });

        JButton delBtn = UIFactory.createDangerButton("Delete Selected", () -> {
            GradeComponent sel = compList.getSelectedValue();
            if (sel == null) return;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    return instructorService.deleteGradeComponent(sel.getComponentId());
                }
                @Override protected void done() {
                     try {
                        if (get()) {
                            loadSection(currentSection);
                            dialog.dispose();
                            showManageComponentsDialog();
                        }
                    } catch (Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Current Components"));
        left.add(new JScrollPane(compList), BorderLayout.CENTER);
        left.add(delBtn, BorderLayout.SOUTH);
        
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("New Component"));
        right.add(form, BorderLayout.CENTER);
        right.add(addBtn, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(250);
        
        dialog.add(split, BorderLayout.CENTER);
        dialog.pack();
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void saveAllGrades() {
        if (gradebookTable.isEditing()) gradebookTable.getCellEditor().stopCellEditing();

        
        for (int r = 0; r < students.size(); r++) {
            StudentGradeEntry student = students.get(r);
            for (int c = 2; c < tableModel.getColumnCount() - 1; c++) {
                int compIdx = c - 2;
                if (compIdx < components.size()) {
                    Object val = tableModel.getValueAt(r, c);
                    if (val instanceof Double) {
                        double score = (Double) val;
                        GradeComponent comp = components.get(compIdx);
                        if (score < 0) {
                            MainFrame.getInstance().showError("Invalid score for " + student.getStudentName() + " in " + comp.getComponentName() + ": Score cannot be negative.");
                            return;
                        }
                        if (score > comp.getMaxScore()) {
                            MainFrame.getInstance().showError("Invalid score for " + student.getStudentName() + " in " + comp.getComponentName() + ": Score exceeds maximum (" + comp.getMaxScore() + ").");
                            return;
                        }
                    }
                }
            }
        }

        Map<Integer, Map<Integer, Double>> scoresToUpdate = new HashMap<>();
        for (int r = 0; r < students.size(); r++) {
            StudentGradeEntry student = students.get(r);
            Map<Integer, Double> sScores = new HashMap<>();
            for (int c = 2; c < tableModel.getColumnCount() - 1; c++) {
                int compIdx = c - 2;
                if (compIdx < components.size()) {
                    Object val = tableModel.getValueAt(r, c);
                    if (val instanceof Double) sScores.put(components.get(compIdx).getComponentId(), (Double)val);
                }
            }
            if (!sScores.isEmpty()) scoresToUpdate.put(student.getEnrollmentId(), sScores);
        }

        statusLabel.setText("Saving...");
        saveBtn.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return instructorService.batchUpdateScores(scoresToUpdate);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        MainFrame.getInstance().showSuccess("Grades saved successfully.");
                        statusLabel.setText("All changes saved.");
                    } else {
                        MainFrame.getInstance().showError("Save failed.");
                        statusLabel.setText("Save failed.");
                    }
                } catch (Exception e) {
                    MainFrame.getInstance().showError(getCleanErrorMessage(e));
                    statusLabel.setText("Save failed.");
                }
                saveBtn.setEnabled(true);
            }
        }.execute();
    }

    private void computeFinalGrades() {
        if (!ConfirmDialog.confirmAction(mainPanel, "Compute Final Grades",
            "Compute final grades for all students? This will overwrite existing final grades.")) {
            return;
        }

        statusLabel.setText("Computing...");

        new SwingWorker<Boolean, Void>() {
            Exception caughtException = null;
            @Override
            protected Boolean doInBackground() {
                try {
                    return instructorService.computeFinalGrades(currentSection.getSectionId());
                } catch (Exception e) {
                    caughtException = e;
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        MainFrame.getInstance().showSuccess("Final grades computed & posted!");
                        loadSection(currentSection);
                    } else {
                        String errorMsg = caughtException != null ? getCleanErrorMessage(caughtException) : "Computation failed.";
                        MainFrame.getInstance().showError(errorMsg);
                        statusLabel.setText("Computation failed.");
                    }
                } catch (Exception e) {
                    MainFrame.getInstance().showError(getCleanErrorMessage(e));
                    statusLabel.setText("Computation failed.");
                }
            }
        }.execute();
    }

    private void importGradesFromCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Grades CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        if (fc.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
             Map<String, Integer> rollMap = new HashMap<>();
             students.forEach(s -> rollMap.put(s.getRollNo(), s.getEnrollmentId()));
             
             GradeCsvHandler.CsvImportResult result = GradeCsvHandler.importGradesFromCsv(file, components, rollMap);
             if(result.isSuccess()) {
                 
                  new SwingWorker<Boolean, Void>() {
                    @Override protected Boolean doInBackground() throws Exception {
                        return instructorService.batchUpdateScores(result.getImportedScores());
                    }
                    @Override protected void done() {
                        try { if(get()) { MainFrame.getInstance().showSuccess("Imported " + result.getRecordsProcessed() + " records."); loadSection(currentSection); } } 
                        catch(Exception e) { e.printStackTrace(); }
                    }
                  }.execute();
             } else {
                 MainFrame.getInstance().showError(result.getErrors().toString());
             }
        }
    }

    private void exportGradesToCsv() {
        if (students == null || students.isEmpty()) {
            MainFrame.getInstance().showWarning("No data to export.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Grades CSV");
        fc.setSelectedFile(new File(currentSection.getCourseCode() + "_" + currentSection.getSectionCode() + "_Grades.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fc.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return GradeCsvHandler.exportGradesToCsv(students, components, file);
                }
                @Override protected void done() {
                    try {
                        if (get()) MainFrame.getInstance().showSuccess("Grades exported successfully.");
                        else MainFrame.getInstance().showError("Failed to export grades.");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        }
    }

    private void showStatsPanel() {
        if (currentSection == null) {
            MainFrame.getInstance().showWarning("Please select a section first.");
            return;
        }
        
        MainFrame.getInstance().showPanel(MainFrame.INSTRUCTOR_CLASS_STATS_PANEL);
        if (MainFrame.getInstance().classStatsPanelInstance != null) {
            MainFrame.getInstance().classStatsPanelInstance.loadStats(currentSection);
        }
    }

    private void showReportPanel() {
        if (currentSection == null) {
            MainFrame.getInstance().showWarning("Please select a section first.");
            return;
        }
        
        MainFrame.getInstance().showPanel(MainFrame.INSTRUCTOR_REPORTS_PANEL);
        if (MainFrame.getInstance().classReportPanelInstance != null) {
            MainFrame.getInstance().classReportPanelInstance.loadReport(currentSection);
        }
    }

    public JPanel getPanel() { return mainPanel; }

    
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
}