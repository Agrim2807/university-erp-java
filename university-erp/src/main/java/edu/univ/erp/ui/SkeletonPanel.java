package edu.univ.erp.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;


public class SkeletonPanel extends JPanel {
    private float pulse = 0.5f;
    private boolean increasing = true;
    private final Timer animator;

    public SkeletonPanel() {
        setBackground(Color.WHITE);
        
        animator = new Timer(16, (ActionEvent e) -> {
            if (increasing) {
                pulse += 0.02f;
                if (pulse >= 0.9f) increasing = false;
            } else {
                pulse -= 0.02f;
                if (pulse <= 0.5f) increasing = true;
            }
            repaint();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        animator.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        animator.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        int brightness = (int) (240 + (15 * pulse)); 
        Color skeletonColor = new Color(brightness, brightness, brightness); 
        
        g2.setColor(skeletonColor);

        
        g2.fill(new RoundRectangle2D.Double(30, 30, 200, 32, 10, 10));

        
        int y = 100;
        for (int i = 0; i < 5; i++) {
            
            g2.fill(new RoundRectangle2D.Double(30, y, getWidth() - 60, 60, 12, 12));
            y += 80;
        }

        g2.dispose();
    }
}