package edu.univ.erp.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;


public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String title, String message, String buttonText, Runnable action) {
        setLayout(new MigLayout("insets 0, align center center, wrap 1"));
        setBackground(Color.WHITE); 
        setOpaque(false);

        
        add(new IconPanel(), "w 120!, h 120!, gapbottom 20, align center");

        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeManager.FONT_TITLE);
        titleLbl.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        add(titleLbl, "align center, gapbottom 5");

        JLabel msgLbl = new JLabel("<html><div style='text-align: center; width: 300px; color: #64748b;'>" + message + "</div></html>");
        msgLbl.setFont(ThemeManager.FONT_BODY);
        add(msgLbl, "align center, gapbottom 20");

        
        if (buttonText != null && action != null) {
            JButton btn = UIFactory.createPrimaryButton(buttonText, action);
            add(btn, "align center, h 40!");
        }
    }

    
    private static class IconPanel extends JPanel {
        public IconPanel() { setOpaque(false); }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            
            g2.setColor(new Color(241, 245, 249)); 
            g2.fill(new RoundRectangle2D.Double(10, 30, 100, 80, 15, 15));
            
            g2.setColor(new Color(226, 232, 240));
            g2.fill(new RoundRectangle2D.Double(30, 10, 60, 20, 10, 10)); 

            
            g2.setColor(new Color(224, 231, 255, 150)); 
            g2.fill(new Ellipse2D.Double(60, 60, 50, 50));
            
            g2.setColor(ThemeManager.getCurrentAccent());
            g2.setStroke(new BasicStroke(3));
            g2.draw(new Ellipse2D.Double(60, 60, 50, 50));
            
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
            g2.drawString("?", 77, 96);
        }
    }
}