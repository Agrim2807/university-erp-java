package edu.univ.erp.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;

public class UIFactory {

    public static JButton createPrimaryButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeManager.FONT_BODY_BOLD);
        btn.setBackground(ThemeManager.getCurrentAccent());
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("JButton.buttonType", "roundRect");
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    public static JButton createSecondaryButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        
        
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        if (!font.getFamily().contains("Emoji")) {
            font = new Font("Dialog", Font.PLAIN, 14); 
        }
        btn.setFont(font);
        
        btn.setBackground(Color.WHITE);
        btn.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        
        
        
        btn.putClientProperty("JButton.buttonType", "roundRect");
        
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }
    
    public static JButton createDangerButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeManager.FONT_BODY_BOLD);
        btn.setBackground(ThemeManager.COLOR_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    public static JTextField createInput(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(ThemeManager.FONT_BODY);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JTextField.showClearButton", true);
        return field;
    }
    
    public static JPasswordField createPasswordInput(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(ThemeManager.FONT_BODY);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JPasswordField.showRevealButton", true);
        return field;
    }

    public static JLabel createHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_DISPLAY);
        label.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        return label;
    }
    
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        
        Font baseFont = ThemeManager.FONT_LABEL;
        Map<TextAttribute, Object> attributes = new HashMap<>(baseFont.getAttributes());
        attributes.put(TextAttribute.TRACKING, 0.05); 
        label.setFont(baseFont.deriveFont(attributes));
        
        return label;
    }
}