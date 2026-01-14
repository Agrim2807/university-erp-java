package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.SemesterService;
import edu.univ.erp.service.StudentService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.List;

public class StudentCatalogPanel {
    private JPanel mainPanel;
    private JPanel cardsContainer;
    private StudentService studentService;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private List<Section> allSections; 

    public StudentCatalogPanel() {
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
        header.add(UIFactory.createHeader("Course Catalog"), "wrap");

        searchField = UIFactory.createInput("Search by Course Name or Code...");
        searchField.addActionListener(e -> applySortAndFilter());
        header.add(searchField, "growx, wmin 300");
        header.add(UIFactory.createSecondaryButton("Search", this::applySortAndFilter));

        sortCombo = new JComboBox<>(new String[]{"Sort: Code", "Sort: Credits", "Sort: Availability"});
        sortCombo.setFont(ThemeManager.FONT_BODY);
        sortCombo.addActionListener(e -> applySortAndFilter());
        header.add(sortCombo, "w 180!");

        mainPanel.add(header, BorderLayout.NORTH);

        
        cardsContainer = new JPanel(new MigLayout("wrap 3, fillx, insets 10 30 30 30, gap 20", "[grow][grow][grow]"));
        cardsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        // Show skeleton loading cards (better UX than dialog for card view)
        cardsContainer.removeAll();
        for(int i=0; i<6; i++) {
            SkeletonPanel skeleton = new SkeletonPanel();
            skeleton.setPreferredSize(new Dimension(300, 150));
            skeleton.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
            cardsContainer.add(skeleton, "grow, h 180!");
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();

        new SwingWorker<List<Section>, Void>() {
            @Override
            protected List<Section> doInBackground() {
                try { Thread.sleep(400); } catch (Exception e) {}
                String semester = SemesterService.getCurrentSemester();
                int year = SemesterService.getCurrentYear();

                int studentId = SessionManager.getCurrentUserId();
                return studentService.getAvailableSectionsForStudent(semester, year, studentId);
            }

            @Override
            protected void done() {
                try {
                    allSections = get();
                    applySortAndFilter();
                } catch (Exception e) {
                    e.printStackTrace();
                    MainFrame.getInstance().showError("Failed to load catalog.");
                }
            }
        }.execute();
    }

    private void applySortAndFilter() {
        if (allSections == null) return;

        String query = searchField.getText().trim().toLowerCase();
        cardsContainer.removeAll();

        // Filter
        List<Section> filtered = allSections.stream()
            .filter(s -> {
                if (query.isEmpty()) return true;

                String code = s.getCourseCode() != null ? s.getCourseCode().toLowerCase() : "";
                String title = s.getCourseTitle() != null ? s.getCourseTitle().toLowerCase() : "";
                String instructor = s.getInstructorName() != null ? s.getInstructorName().toLowerCase() : "";
                return code.contains(query) || title.contains(query) || instructor.contains(query);
            })
            .collect(java.util.stream.Collectors.toList());

        // Sort
        String sortOption = (String) sortCombo.getSelectedItem();
        if ("Sort: Code".equals(sortOption)) {
            filtered.sort((s1, s2) -> s1.getCourseCode().compareToIgnoreCase(s2.getCourseCode()));
        } else if ("Sort: Credits".equals(sortOption)) {
            filtered.sort((s1, s2) -> Integer.compare(s2.getCapacity(), s1.getCapacity()));
        } else if ("Sort: Availability".equals(sortOption)) {
            filtered.sort((s1, s2) -> {
                int avail1 = s1.getCapacity() - s1.getEnrollmentCount();
                int avail2 = s2.getCapacity() - s2.getEnrollmentCount();
                return Integer.compare(avail2, avail1);
            });
        }

        if (filtered.isEmpty()) {
            String msg = query.isEmpty() ? "No courses available for this semester." :
                "No courses found matching \"" + searchField.getText().trim() + "\"";
            JLabel empty = new JLabel(msg);
            empty.setFont(ThemeManager.FONT_TITLE);
            empty.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
            cardsContainer.add(empty, "span, align center, gaptop 50");
        } else {
            for (Section s : filtered) {
                cardsContainer.add(createCourseCard(s), "grow");
            }
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createCourseCard(Section section) {
        JPanel card = new JPanel(new MigLayout("fill, insets 15, wrap 1"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        card.putClientProperty("Component.arc", 12);

        JLabel creditsTag = new JLabel(section.getCourseCode() + " • " + 4 + " Credits");
        creditsTag.setFont(ThemeManager.FONT_LABEL);
        creditsTag.setForeground(ThemeManager.getCurrentAccent());
        
        JLabel title = new JLabel("<html><body style='width: 200px'>" + section.getCourseTitle() + "</body></html>");
        title.setFont(ThemeManager.FONT_TITLE.deriveFont(16f));
        title.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        
        JLabel details = new JLabel("<html>Sec " + section.getSectionCode() + "<br/>" + 
                                    "Instr: " + section.getInstructorName() + "<br/>" + 
                                    section.getDayTime() + " • " + section.getRoom() + "</html>");
        details.setFont(ThemeManager.FONT_BODY);
        details.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        int filled = section.getEnrollmentCount();
        int capacity = section.getCapacity();
        int percentage = (int) ((double) filled / capacity * 100);
        
        JProgressBar capacityBar = new JProgressBar(0, capacity);
        capacityBar.setValue(filled);
        capacityBar.setStringPainted(true);
        capacityBar.setString(filled + "/" + capacity + " Seats Taken");
        capacityBar.putClientProperty("JProgressBar.largeHeight", true);
        
        if (percentage >= 90) capacityBar.setForeground(ThemeManager.COLOR_DANGER);
        else if (percentage >= 75) capacityBar.setForeground(ThemeManager.COLOR_WARNING);
        else capacityBar.setForeground(ThemeManager.COLOR_SUCCESS);

        JButton registerBtn = UIFactory.createPrimaryButton("Register", () -> registerAction(section));
        if (percentage >= 100) {
            registerBtn.setEnabled(false);
            registerBtn.setText("Full");
            registerBtn.setBackground(ThemeManager.COLOR_TEXT_SECONDARY);
        }

        card.add(creditsTag);
        card.add(title, "gapbottom 10");
        card.add(details, "gapbottom 15");
        card.add(capacityBar, "growx, gapbottom 10");
        card.add(registerBtn, "growx");

        return card;
    }

    private void registerAction(Section section) {
        if (ConfirmDialog.confirmAction(mainPanel, "Register for Course",
            "Register for " + section.getCourseCode() + " - " + section.getSectionCode() + "?")) {
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    try {
                        studentService.registerForSection(SessionManager.getCurrentUserId(), section.getSectionId());
                        return "Success";
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                }
                @Override
                protected void done() {
                    try {
                        String result = get();
                        if ("Success".equals(result)) {
                            MainFrame.getInstance().showSuccess("Successfully registered!");
                            refreshData();
                        } else {
                            MainFrame.getInstance().showError(result);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        }
    }

    public JPanel getPanel() { return mainPanel; }
}