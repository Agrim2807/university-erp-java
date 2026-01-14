package edu.univ.erp.ui;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;


public class ClassStatsPanel {
    private JPanel panel;
    private JPanel contentArea;
    private JLabel titleLabel;
    private InstructorService service;

    public ClassStatsPanel() {
        service = new InstructorService();
        initializePanel();
    }

    private void initializePanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.COLOR_BACKGROUND);

        
        JPanel header = new JPanel(new MigLayout("fillx, insets 20 30 10 30", "[]20[grow]", "[center]"));
        header.setOpaque(false);

        
        JButton backBtn = UIFactory.createSecondaryButton("\u2190 Back to Gradebook", () ->
            MainFrame.getInstance().showPanel(MainFrame.INSTRUCTOR_GRADEBOOK_PANEL));

        titleLabel = UIFactory.createHeader("Class Statistics");
        header.add(backBtn, "aligny center");
        header.add(titleLabel, "growx, aligny center");
        panel.add(header, BorderLayout.NORTH);

        
        contentArea = new JPanel(new MigLayout("wrap 1, fillx, insets 0 30 30 30, gap 20", "[grow, fill]"));
        contentArea.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(ThemeManager.COLOR_BACKGROUND);
        
        panel.add(scroll, BorderLayout.CENTER);
    }

    public void loadStats(Section s) {
        if (s == null) return;
        titleLabel.setText("Statistics: " + s.getCourseCode() + " - " + s.getSectionCode());
        contentArea.removeAll();
        contentArea.add(new SkeletonPanel(), "growx, h 300!"); 
        contentArea.revalidate();
        contentArea.repaint();
        
        new SwingWorker<Map<String, Map<String, Double>>, Void>() {
            @Override protected Map<String, Map<String, Double>> doInBackground() {
                
                
                return service.getSectionStatistics(s.getSectionId());
            }
            @Override protected void done() {
                try {
                    Map<String, Map<String, Double>> stats = get();
                    contentArea.removeAll();
                    
                    if (stats.isEmpty()) {
                        contentArea.add(new JLabel("No grades recorded yet to analyze."), "align center");
                    } else {
                        for (String comp : stats.keySet()) {
                            
                            
                            contentArea.add(createChartCard(comp, stats.get(comp)), "growx, h 300!");
                        }
                    }
                    contentArea.revalidate();
                    contentArea.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    MainFrame.getInstance().showError("Failed to load analytics.");
                }
            }
        }.execute();
    }

    private JPanel createChartCard(String title, Map<String, Double> data) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        card.putClientProperty("Component.arc", 12);

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        
        dataset.addValue(data.get("Average"), "Score", "Average");
        dataset.addValue(data.get("Highest"), "Score", "Highest");
        dataset.addValue(data.get("Lowest"), "Score", "Lowest");

        
        JFreeChart chart = ChartFactory.createBarChart(
            title + " Performance", 
            "Metric",               
            "Score",                
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );

        
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(ThemeManager.COLOR_BORDER);
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ThemeManager.COLOR_INSTRUCTOR); 
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter()); 
        renderer.setShadowVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        card.add(chartPanel, BorderLayout.CENTER);
        
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(new Color(248, 250, 252));
        footer.add(createStatLabel("Avg: " + String.format("%.1f", data.get("Average"))));
        footer.add(createStatLabel("Max: " + String.format("%.0f", data.get("MaxPossible"))));
        footer.add(createStatLabel("Count: " + String.format("%.0f", data.get("Count"))));
        
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }
    
    private JLabel createStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeManager.FONT_LABEL);
        lbl.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        return lbl;
    }

    public JPanel getPanel() { return panel; }
}