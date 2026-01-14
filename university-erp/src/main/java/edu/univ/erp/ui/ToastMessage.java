package edu.univ.erp.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;


public class ToastMessage extends JPanel {
    private final String message;
    private final boolean isSuccess;
    private float alpha = 0.0f; 

    public ToastMessage(String message, boolean isSuccess) {
        this.message = message;
        this.isSuccess = isSuccess;
        
        setOpaque(false);
        setLayout(new MigLayout("insets 10 20 10 20"));
        
        
        JLabel icon = new JLabel(isSuccess ? "✓" : "✕");
        icon.setForeground(Color.WHITE);
        Font iconFont = new Font("Segoe UI Emoji", Font.BOLD, 16);
        if (!iconFont.getFamily().contains("Emoji")) {
            iconFont = new Font("Segoe UI", Font.BOLD, 16); 
        }
        icon.setFont(iconFont);
        
        JLabel text = new JLabel(message);
        text.setForeground(Color.WHITE);
        text.setFont(ThemeManager.FONT_BODY_BOLD);
        
        add(icon, "gapright 10");
        add(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        
        Color bg = isSuccess ? ThemeManager.COLOR_SUCCESS : ThemeManager.COLOR_DANGER;
        g2.setColor(bg);
        
        
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        
        super.paintComponent(g2);
        g2.dispose();
    }
    
    public void setAlpha(float value) {
        this.alpha = Math.min(1.0f, Math.max(0.0f, value));
        repaint();
    }
}