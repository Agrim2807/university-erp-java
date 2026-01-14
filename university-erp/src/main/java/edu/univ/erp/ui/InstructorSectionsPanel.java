package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class InstructorSectionsPanel {
    private JPanel mainPanel;
    private JPanel cardsContainer;
    private InstructorService instructorService;
    private GradebookPanel gradebookPanelInstance;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private List<Section> allSectionsCache;

    public InstructorSectionsPanel(GradebookPanel gradebookPanel) {
        this.gradebookPanelInstance = gradebookPanel;
        this.instructorService = new InstructorService();
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

        JPanel textContainer = new JPanel(new MigLayout("insets 0, gap 0, wrap 1"));
        textContainer.setOpaque(false);
        textContainer.add(UIFactory.createHeader("My Sections"));
        textContainer.add(UIFactory.createLabel("Select a section to manage grades"));

        header.add(textContainer, "wrap");

        searchField = UIFactory.createInput("Search sections...");
        searchField.addActionListener(e -> applySortAndFilter());
        header.add(searchField, "growx, wmin 300");
        header.add(UIFactory.createSecondaryButton("Search", this::applySortAndFilter));

        sortCombo = new JComboBox<>(new String[]{"Sort: Code", "Sort: Semester", "Sort: Enrollment"});
        sortCombo.setFont(ThemeManager.FONT_BODY);
        sortCombo.addActionListener(e -> applySortAndFilter());
        header.add(sortCombo, "w 180!");

        mainPanel.add(header, BorderLayout.NORTH);

        
        cardsContainer = new JPanel(new MigLayout("wrap 3, fillx, insets 10 30 30 30, gap 20", "[grow][grow][grow]"));
        cardsContainer.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(scroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        cardsContainer.removeAll();

        for(int i=0; i<3; i++) {
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
                return instructorService.getMySections(SessionManager.getCurrentUserId());
            }
            @Override
            protected void done() {
                try {
                    allSectionsCache = get();
                    applySortAndFilter();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void applySortAndFilter() {
        if (allSectionsCache == null) return;
        cardsContainer.removeAll();

        // Filter
        String query = searchField.getText().toLowerCase();
        List<Section> filtered = allSectionsCache.stream()
                .filter(s -> {
                    if (query.isEmpty()) return true;
                    String courseCode = s.getCourseCode() != null ? s.getCourseCode().toLowerCase() : "";
                    String sectionCode = s.getSectionCode() != null ? s.getSectionCode().toLowerCase() : "";
                    return courseCode.contains(query) || sectionCode.contains(query);
                })
                .collect(Collectors.toList());

        // Sort
        String sortOption = (String) sortCombo.getSelectedItem();
        if ("Sort: Code".equals(sortOption)) {
            filtered.sort((s1, s2) -> s1.getCourseCode().compareToIgnoreCase(s2.getCourseCode()));
        } else if ("Sort: Semester".equals(sortOption)) {
            filtered.sort((s1, s2) -> {
                int semCompare = s1.getSemester().compareToIgnoreCase(s2.getSemester());
                return semCompare != 0 ? semCompare : Integer.compare(s2.getYear(), s1.getYear());
            });
        } else if ("Sort: Enrollment".equals(sortOption)) {
            filtered.sort((s1, s2) -> Integer.compare(s2.getEnrollmentCount(), s1.getEnrollmentCount()));
        }

        if (filtered.isEmpty()) {
            cardsContainer.add(new EmptyStatePanel("No Sections", "You are not assigned to any sections yet.", null, null), "span, align center, gaptop 50");
        } else {
            for (Section s : filtered) {
                cardsContainer.add(createSectionCard(s), "grow");
            }
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createSectionCard(Section section) {
        JPanel card = new JPanel(new MigLayout("fill, insets 20, wrap 1"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        card.putClientProperty("Component.arc", 12);

        JLabel code = new JLabel(section.getCourseCode() + " - " + section.getSectionCode());
        code.setFont(ThemeManager.FONT_TITLE);
        code.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        
        JLabel term = new JLabel(section.getSemester() + " " + section.getYear());
        term.setFont(ThemeManager.FONT_LABEL);
        term.setForeground(ThemeManager.getCurrentAccent());

        
        Font iconFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        
        JLabel time = new JLabel("🕒 " + section.getDayTime());
        time.setFont(iconFont);
        time.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        
        JLabel room = new JLabel("📍 " + section.getRoom());
        room.setFont(iconFont);
        room.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        
        JProgressBar enrollBar = new JProgressBar(0, section.getCapacity());
        enrollBar.setValue(section.getEnrollmentCount());
        enrollBar.setString(section.getEnrollmentCount() + " / " + section.getCapacity() + " Students");
        enrollBar.setStringPainted(true);
        enrollBar.putClientProperty("JProgressBar.largeHeight", true);
        enrollBar.setForeground(ThemeManager.getCurrentAccent());

        JButton manageBtn = UIFactory.createPrimaryButton("Open Gradebook", () -> openGradebook(section));

        card.add(term);
        card.add(code, "gapbottom 10");
        card.add(time);
        card.add(room, "gapbottom 15");
        card.add(enrollBar, "growx, gapbottom 15");
        card.add(manageBtn, "growx");

        return card;
    }

    private void openGradebook(Section section) {
        if (gradebookPanelInstance != null) {
            gradebookPanelInstance.loadSection(section);
            MainFrame.getInstance().showPanel(MainFrame.INSTRUCTOR_GRADEBOOK_PANEL);
        }
    }

    public JPanel getPanel() { return mainPanel; }
}