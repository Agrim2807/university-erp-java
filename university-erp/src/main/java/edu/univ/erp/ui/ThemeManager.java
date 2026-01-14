package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    
    
    public static final Color COLOR_STUDENT = Color.decode("#10B981");
    public static final Color COLOR_INSTRUCTOR = Color.decode("#3B82F6");
    public static final Color COLOR_ADMIN = Color.decode("#8B5CF6");
    
    
    public static final Color COLOR_TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);
    public static final Color COLOR_SURFACE = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240); 
    
    
    public static final Color COLOR_SUCCESS = new Color(34, 197, 94); 
    public static final Color COLOR_WARNING = new Color(245, 158, 11);
    public static final Color COLOR_DANGER = new Color(239, 68, 68);  

    
    public static final Font FONT_DISPLAY = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14); 
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 11);

    private static Color currentAccentColor = COLOR_ADMIN;

    public static void setupTheme() {
        try {
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("Button.margin", new Insets(8, 16, 8, 16));
            
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRole(String role) {
        if (role == null) {
            currentAccentColor = COLOR_ADMIN;
            return;
        }
        switch (role.toLowerCase()) {
            case "student": currentAccentColor = COLOR_STUDENT; break;
            case "instructor": currentAccentColor = COLOR_INSTRUCTOR; break;
            case "admin": currentAccentColor = COLOR_ADMIN; break;
            default: currentAccentColor = COLOR_ADMIN;
        }
    }

    public static Color getCurrentAccent() {
        return currentAccentColor;
    }
}