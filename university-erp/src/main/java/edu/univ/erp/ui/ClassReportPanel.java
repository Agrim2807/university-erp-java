package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.StudentGradeEntry;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.util.PdfExporter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ClassReportPanel {
    private JPanel panel;
    private JTable table;
    private DefaultTableModel model;
    private JLabel titleLabel;
    private InstructorService service;
    private Section currentSection;
    private List<StudentGradeEntry> data;
    private List<GradeComponent> components;

    public ClassReportPanel() {
        service = new InstructorService();
        initializePanel();
    }

    private void initializePanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.COLOR_BACKGROUND);

        JPanel header = new JPanel(new MigLayout("fillx, insets 20 30 10 30", "[]20[grow][]", "[center]"));
        header.setOpaque(false);

        
        JButton backBtn = UIFactory.createSecondaryButton("\u2190 Back to Gradebook", () ->
            MainFrame.getInstance().showPanel(MainFrame.INSTRUCTOR_GRADEBOOK_PANEL));

        titleLabel = UIFactory.createHeader("Class Report");
        JButton pdfBtn = UIFactory.createPrimaryButton("Export PDF", this::exportPdf);

        header.add(backBtn, "aligny center");
        header.add(titleLabel, "growx, aligny center");
        header.add(pdfBtn, "aligny center");

        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Roll No", "Name", "Final Grade"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setFont(ThemeManager.FONT_BODY);
        table.setRowHeight(30);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        panel.add(scroll, BorderLayout.CENTER);
    }

    public void loadReport(Section s) {
        this.currentSection = s;
        if (s == null) return;
        titleLabel.setText("Report: " + s.getCourseCode() + " - " + s.getSectionCode());
        
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                data = service.generateClassReportData(s.getSectionId());
                components = service.getGradeComponents(s.getSectionId());
                return null;
            }
            @Override protected void done() {
                model.setRowCount(0);
                for (StudentGradeEntry e : data) {
                    model.addRow(new Object[]{e.getRollNo(), e.getStudentName(), e.getFinalGrade()});
                }
            }
        }.execute();
    }

    private void exportPdf() {
        if (data == null || data.isEmpty()) { MainFrame.getInstance().showWarning("No data."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Report.pdf"));
        if (fc.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
            PdfExporter.exportSectionGradeReport(data, components, currentSection.getCourseCode(), 
                currentSection.getSectionCode(), SessionManager.getCurrentUserFullName(), fc.getSelectedFile());
            MainFrame.getInstance().showSuccess("Exported.");
        }
    }

    public JPanel getPanel() { return panel; }
}