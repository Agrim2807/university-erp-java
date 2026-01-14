package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.GradeInfo;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.TableUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class MyGradesPanel {
    private JPanel panel;
    private JPanel contentArea;
    
    private JList<String> courseList;
    private DefaultListModel<String> courseListModel;
    private JTable componentsTable;
    private DefaultTableModel componentsTableModel;
    private JLabel totalLabel;
    
    private StudentService studentService;
    private Map<String, List<GradeInfo>> gradesByCourse;

    public MyGradesPanel() {
        this.studentService = new StudentService();
        initializePanel();
    }

    private void initializePanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.COLOR_BACKGROUND);

        
        panel.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent event) { refreshData(); }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });

        JPanel header = new JPanel(new MigLayout("fillx, insets 20 30 10 30"));
        header.setOpaque(false);
        header.add(UIFactory.createHeader("My Grades"), "split 2, growx");
        panel.add(header, BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        panel.add(contentArea, BorderLayout.CENTER);
    }

    private JSplitPane createSplitView() {
        courseListModel = new DefaultListModel<>();
        courseList = new JList<>(courseListModel);
        courseList.setFont(ThemeManager.FONT_BODY);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setFixedCellHeight(40);
        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) displayGrades(courseList.getSelectedValue());
        });
        
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        left.add(new JScrollPane(courseList), BorderLayout.CENTER);

        String[] cols = {"Component", "Score", "Max", "Weight", "%"};
        componentsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        componentsTable = new JTable(componentsTableModel);
        componentsTable.setFont(ThemeManager.FONT_BODY);
        componentsTable.setRowHeight(35);
        componentsTable.getTableHeader().setFont(ThemeManager.FONT_LABEL);
        componentsTable.getTableHeader().setBackground(new Color(248, 250, 252));

        // Enable sorting
        TableUtils.enableSorting(componentsTable);

        totalLabel = new JLabel("Select a course to view details");
        totalLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        right.add(new JScrollPane(componentsTable), BorderLayout.CENTER);
        right.add(totalLabel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(250);
        split.setBorder(null);
        split.setOpaque(false);
        return split;
    }

    public void refreshData() {
        contentArea.removeAll();
        contentArea.add(new SkeletonPanel(), BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();

        new SwingWorker<List<GradeInfo>, Void>() {
            @Override protected List<GradeInfo> doInBackground() {
                try { Thread.sleep(400); } catch (Exception e) {}
                return studentService.getMyGrades(SessionManager.getCurrentUserId());
            }
            @Override protected void done() {
                try {
                    List<GradeInfo> all = get();
                    contentArea.removeAll();

                    if (all == null || all.isEmpty()) {
                        contentArea.add(new EmptyStatePanel(
                            "No Grades Available",
                            "You don't have any graded courses yet.",
                            "Go to Dashboard",
                            () -> MainFrame.getInstance().navigateToDashboard()
                        ), BorderLayout.CENTER);
                    } else {
                        gradesByCourse = all.stream().collect(Collectors.groupingBy(GradeInfo::getCourseCode));
                        JSplitPane split = createSplitView();
                        new ArrayList<>(gradesByCourse.keySet()).stream().sorted().forEach(courseListModel::addElement);
                        contentArea.add(split, BorderLayout.CENTER);
                        if (!courseListModel.isEmpty()) courseList.setSelectedIndex(0);
                    }
                    contentArea.revalidate();
                    contentArea.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void displayGrades(String courseCode) {
        if (courseCode == null) return;
        componentsTableModel.setRowCount(0);
        List<GradeInfo> grades = gradesByCourse.get(courseCode);
        
        double totalScore = 0;
        String finalGrade = "N/A";

        for (GradeInfo g : grades) {
            if (g.getComponentName() != null) {
                double pct = (g.getMaxScore() > 0) ? (g.getScore() / g.getMaxScore()) * 100 : 0;
                componentsTableModel.addRow(new Object[]{
                    g.getComponentName(), g.getScore(), g.getMaxScore(), g.getWeight() + "%", String.format("%.1f%%", pct)
                });
                totalScore += pct * (g.getWeight() / 100.0);
            }
            if (g.getFinalGrade() != null) finalGrade = g.getFinalGrade();
        }
        totalLabel.setText(String.format("  Total Weighted Score: %.2f%%  |  Final Grade: %s", totalScore, finalGrade));
    }

    public JPanel getPanel() { return panel; }
}