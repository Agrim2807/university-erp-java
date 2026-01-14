package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.service.StudentService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimetablePanel {
    private JPanel mainPanel;
    private WeeklyScheduleView scheduleView;
    private StudentService studentService;

    public TimetablePanel() {
        studentService = new StudentService();
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

        JPanel header = new JPanel(new MigLayout("insets 20 30 20 30"));
        header.setOpaque(false);
        header.add(UIFactory.createHeader("My Weekly Schedule"));
        
        scheduleView = new WeeklyScheduleView();
        JScrollPane scrollPane = new JScrollPane(scheduleView);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        new SwingWorker<List<Enrollment>, Void>() {
            @Override
            protected List<Enrollment> doInBackground() {
                return studentService.getMyRegisteredSections(SessionManager.getCurrentUserId());
            }
            @Override
            protected void done() {
                try {
                    scheduleView.setEnrollments(get());
                    mainPanel.revalidate(); mainPanel.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    public JPanel getPanel() { return mainPanel; }

    private static class WeeklyScheduleView extends JPanel {
        private List<Enrollment> enrollments = new ArrayList<>();
        private final int START_HOUR = 8; 
        private final int END_HOUR = 18;  
        private final int HEADER_HEIGHT = 40;
        private final int TIME_COL_WIDTH = 60;
        
        private final Color[] BLOCK_COLORS = {
            new Color(199, 210, 254), 
            new Color(167, 243, 208), 
            new Color(253, 230, 138), 
            new Color(251, 207, 232)  
        };

        public WeeklyScheduleView() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 600));
        }

        public void setEnrollments(List<Enrollment> data) {
            this.enrollments = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int dayWidth = (width - TIME_COL_WIDTH) / 5;
            int hourHeight = (height - HEADER_HEIGHT) / (END_HOUR - START_HOUR);

            g2.setFont(ThemeManager.FONT_LABEL);
            g2.setColor(ThemeManager.COLOR_TEXT_SECONDARY);
            
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
            for (int i = 0; i < 5; i++) {
                int x = TIME_COL_WIDTH + (i * dayWidth);
                g2.drawString(days[i], x + dayWidth/2 - 10, 25);
                g2.setColor(ThemeManager.COLOR_BORDER);
                g2.drawLine(x, 0, x, height);
            }
            
            for (int h = START_HOUR; h <= END_HOUR; h++) {
                int y = HEADER_HEIGHT + (h - START_HOUR) * hourHeight;
                g2.setColor(ThemeManager.COLOR_TEXT_SECONDARY);
                g2.drawString(String.format("%02d:00", h), 10, y + 5);
                g2.setColor(new Color(241, 245, 249)); 
                g2.drawLine(TIME_COL_WIDTH, y, width, y);
            }

            if (enrollments == null) return;
            
            int colorIdx = 0;
            Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})");
            Pattern dayPattern = Pattern.compile("(Mon|Tue|Wed|Thu|Fri)");

            for (Enrollment e : enrollments) {
                String info = e.getSectionInfo(); 
                Matcher mTime = timePattern.matcher(info);
                Matcher mDay = dayPattern.matcher(info);

                if (mTime.find()) {
                    double start = Integer.parseInt(mTime.group(1)) + (Integer.parseInt(mTime.group(2)) / 60.0);
                    double end = Integer.parseInt(mTime.group(3)) + (Integer.parseInt(mTime.group(4)) / 60.0);
                    
                    int y = HEADER_HEIGHT + (int)((start - START_HOUR) * hourHeight);
                    int h = (int)((end - start) * hourHeight);

                    Color blockColor = BLOCK_COLORS[colorIdx % BLOCK_COLORS.length];
                    colorIdx++;

                    while (mDay.find()) {
                        int dayIdx = getDayIndex(mDay.group(1));
                        if (dayIdx >= 0) {
                            int x = TIME_COL_WIDTH + (dayIdx * dayWidth);
                            g2.setColor(blockColor);
                            g2.fill(new RoundRectangle2D.Float(x + 2, y, dayWidth - 4, h, 10, 10));
                            g2.setColor(ThemeManager.COLOR_TEXT_PRIMARY);
                            g2.setFont(ThemeManager.FONT_LABEL);
                            g2.drawString(e.getCourseCode(), x + 10, y + 20);
                            g2.setFont(ThemeManager.FONT_BODY.deriveFont(10f));
                            g2.drawString(e.getSectionInfo().split(",")[2].trim(), x + 10, y + 35); 
                        }
                    }
                }
            }
        }

        private int getDayIndex(String day) {
            switch (day) {
                case "Mon": return 0;
                case "Tue": return 1;
                case "Wed": return 2;
                case "Thu": return 3;
                case "Fri": return 4;
                default: return -1;
            }
        }
    }
}