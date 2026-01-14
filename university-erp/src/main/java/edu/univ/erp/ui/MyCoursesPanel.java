package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.TableUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyCoursesPanel {
    private JPanel mainPanel;
    private JPanel contentArea;
    private JPanel bottomPanel; // Store reference to hide when empty
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private StudentService studentService;
    private List<Enrollment> myEnrollments;

    public MyCoursesPanel() {
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

        JPanel header = new JPanel(new MigLayout("fillx, insets 20 30 10 30"));
        header.setOpaque(false);
        header.add(UIFactory.createHeader("My Courses"), "wrap");
        mainPanel.add(header, BorderLayout.NORTH);


        // Bottom panel with Drop button (hidden when no courses)
        bottomPanel = new JPanel(new MigLayout("fillx, insets 15 30 20 30", "[grow][]"));
        bottomPanel.setOpaque(false);
        bottomPanel.add(new JLabel(""), "growx");
        bottomPanel.add(UIFactory.createDangerButton("Drop Selected", this::performDrop));
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setVisible(false); // Initially hidden until courses load

        contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        
        String[] cols = {"Code", "Section", "Instructor", "Status", "Drop Deadline"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        coursesTable = new JTable(tableModel);
        coursesTable.setFont(ThemeManager.FONT_BODY);
        coursesTable.setRowHeight(40);
        coursesTable.getTableHeader().setFont(ThemeManager.FONT_LABEL);
        coursesTable.getTableHeader().setBackground(Color.WHITE);
        coursesTable.setSelectionBackground(new Color(241, 245, 249));
        coursesTable.setSelectionForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        coursesTable.setShowVerticalLines(false);
        coursesTable.setIntercellSpacing(new Dimension(0, 0));
        
        coursesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, value, isSel, hasF, r, c);
                if (!isSel) comp.setBackground(Color.WHITE);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.COLOR_BORDER));


                if (c == 0 || c == 2 || c == 3 || c == 4) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (c == 3) {
                    String status = (String) value;
                    setFont(ThemeManager.FONT_BODY_BOLD);
                    if ("registered".equalsIgnoreCase(status)) setForeground(ThemeManager.COLOR_SUCCESS);
                    else if ("dropped".equalsIgnoreCase(status)) setForeground(ThemeManager.COLOR_DANGER);
                    else setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                } else {
                    setFont(ThemeManager.FONT_BODY);
                    setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                }
                return comp;
            }
        });

        // Enable sorting
        TableUtils.enableSorting(coursesTable);

        mainPanel.add(contentArea, BorderLayout.CENTER);
    }

    public void refreshData() {
        contentArea.removeAll();
        contentArea.add(new SkeletonPanel(), BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();

        new SwingWorker<List<Enrollment>, Void>() {
            @Override protected List<Enrollment> doInBackground() {
                try { Thread.sleep(400); } catch (Exception e) {}
                return studentService.getMyRegisteredSections(SessionManager.getCurrentUserId());
            }
            @Override protected void done() {
                try {
                    myEnrollments = get();
                    contentArea.removeAll();

                    if (myEnrollments == null || myEnrollments.isEmpty()) {
                        // No courses in current semester - hide Drop button
                        bottomPanel.setVisible(false);
                        EmptyStatePanel empty = new EmptyStatePanel(
                            "No Active Courses",
                            "You haven't registered for any courses this semester yet.",
                            "Browse Catalog",
                            () -> MainFrame.getInstance().showPanel(MainFrame.STUDENT_CATALOG_PANEL)
                        );
                        contentArea.add(empty, BorderLayout.CENTER);
                    } else {
                        // Has courses in current semester - show Drop button
                        bottomPanel.setVisible(true);
                        tableModel.setRowCount(0);
                        for (Enrollment e : myEnrollments) {
                            tableModel.addRow(new Object[]{
                                e.getCourseCode(), e.getSectionInfo(), e.getStudentName(),
                                e.getStatus(), e.getDropDeadline()
                            });
                        }
                        JScrollPane scroll = new JScrollPane(coursesTable);
                        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
                        scroll.getViewport().setBackground(Color.WHITE);
                        contentArea.add(scroll, BorderLayout.CENTER);
                    }
                    contentArea.revalidate();
                    contentArea.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void performDrop() {
        if (coursesTable.getSelectedRow() == -1) {
            MainFrame.getInstance().showWarning("Select a course to drop.");
            return;
        }
        Enrollment e = myEnrollments.get(coursesTable.convertRowIndexToModel(coursesTable.getSelectedRow()));

        if (ConfirmDialog.confirmAction(mainPanel, "Drop Course", "Are you sure you want to drop " + e.getCourseCode() + "?")) {
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() {
                    try {
                        studentService.dropSection(SessionManager.getCurrentUserId(), e.getEnrollmentId());
                        return "Success";
                    } catch (Exception ex) { return ex.getMessage(); }
                }
                @Override protected void done() {
                    try {
                        String res = get();
                        if ("Success".equals(res)) {
                            MainFrame.getInstance().showSuccess("Dropped successfully.");
                            refreshData();
                        } else MainFrame.getInstance().showError(res);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        }
    }

    public JPanel getPanel() { return mainPanel; }
}