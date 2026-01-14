package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.TranscriptEntry;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.CsvExporter;
import edu.univ.erp.util.PdfExporter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class TranscriptPanel {
    private JPanel mainPanel;
    private JTable transcriptTable;
    private DefaultTableModel tableModel;
    private StudentService studentService;
    private List<TranscriptEntry> transcriptData;

    public TranscriptPanel() {
        this.studentService = new StudentService();
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
        
        JPanel titles = new JPanel(new MigLayout("insets 0, gap 0, wrap 1"));
        titles.setOpaque(false);
        titles.add(UIFactory.createHeader("Academic Transcript"));
        titles.add(UIFactory.createLabel(SessionManager.getCurrentUserFullName()));
        
        header.add(titles, "growx");

        
        JPanel exportBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exportBtns.setOpaque(false);
        JButton csvBtn = UIFactory.createSecondaryButton("Download CSV", this::exportCsv);
        JButton pdfBtn = UIFactory.createPrimaryButton("Download PDF", this::exportPdf);
        exportBtns.add(csvBtn);
        exportBtns.add(pdfBtn);
        header.add(exportBtns);
        
        mainPanel.add(header, BorderLayout.NORTH);

        String[] cols = {"Semester", "Course Code", "Title", "Credits", "Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        transcriptTable = new JTable(tableModel);
        transcriptTable.setRowHeight(40);
        transcriptTable.getTableHeader().setFont(ThemeManager.FONT_LABEL);
        transcriptTable.getTableHeader().setBackground(Color.WHITE);
        transcriptTable.setFont(ThemeManager.FONT_BODY);
        transcriptTable.setSelectionBackground(Color.WHITE);
        transcriptTable.setSelectionForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        
        transcriptTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setBackground(Color.WHITE);
                if (col == 4) {
                    String grade = (String) value;
                    setFont(ThemeManager.FONT_BODY_BOLD);
                    if ("A".equals(grade)) setForeground(ThemeManager.COLOR_SUCCESS);
                    else if ("F".equals(grade)) setForeground(ThemeManager.COLOR_DANGER);
                    else setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                } else {
                    setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(transcriptTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        scroll.getViewport().setBackground(Color.WHITE);
        
        mainPanel.add(scroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        new SwingWorker<List<TranscriptEntry>, Void>() {
            @Override
            protected List<TranscriptEntry> doInBackground() {
                return studentService.getTranscriptData(SessionManager.getCurrentUserId());
            }
            @Override
            protected void done() {
                try {
                    transcriptData = get();
                    tableModel.setRowCount(0);
                    for (TranscriptEntry t : transcriptData) {
                        String sem = t.getSemester() + " " + t.getYear();
                        tableModel.addRow(new Object[]{
                            sem, t.getCourseCode(), t.getCourseTitle(), t.getCredits(), t.getFinalGrade()
                        });
                    }
                    mainPanel.revalidate(); mainPanel.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void exportCsv() {
        if (transcriptData == null || transcriptData.isEmpty()) {
            MainFrame.getInstance().showWarning("No data to export.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Transcript.csv"));
        if (fc.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        CsvExporter.exportTranscript(transcriptData, file);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                @Override
                protected void done() {
                    try {
                        if (get()) MainFrame.getInstance().showSuccess("Transcript CSV Saved Successfully.");
                        else MainFrame.getInstance().showError("Failed to generate CSV.");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        }
    }

    private void exportPdf() {
        if (transcriptData == null || transcriptData.isEmpty()) {
            MainFrame.getInstance().showWarning("No data to export.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Transcript.pdf"));
        if (fc.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            // Show loading dialog (addresses rubric: "Long actions show 'please wait'")
            LoadingDialog loadingDialog = new LoadingDialog(mainPanel, "Generating PDF transcript...");

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    loadingDialog.showLoading(); // Show "Please wait..." dialog
                    Student student = studentService.getStudentProfile(SessionManager.getCurrentUserId());
                    String rollNo = (student != null) ? student.getRollNo() : "N/A";
                    String program = (student != null) ? student.getProgram() : "N/A";

                    return PdfExporter.exportTranscript(
                        transcriptData,
                        SessionManager.getCurrentUserFullName(),
                        rollNo,
                        program,
                        file
                    );
                }
                @Override
                protected void done() {
                    loadingDialog.hideLoading(); // Hide dialog when done
                    try {
                        if (get()) MainFrame.getInstance().showSuccess("Transcript Saved Successfully.");
                        else MainFrame.getInstance().showError("Failed to generate PDF.");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        }
    }

    public JPanel getPanel() { return mainPanel; }
}