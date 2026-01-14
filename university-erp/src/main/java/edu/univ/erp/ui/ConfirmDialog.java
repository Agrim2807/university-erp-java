package edu.univ.erp.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;


public class ConfirmDialog {

    public enum DialogType {
        INFO, WARNING, DANGER, SUCCESS
    }

    
    public static boolean show(Component parent, String title, String message, DialogType type) {
        JDialog dialog = new JDialog(getFrame(parent), title, true);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER, 1));

        final boolean[] result = {false};

        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[]0[]"));
        panel.setBackground(Color.WHITE);

        
        JPanel header = new JPanel(new MigLayout("fill, insets 20"));
        header.setBackground(getHeaderColor(type));

        JLabel icon = new JLabel(getIcon(type));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        header.add(icon, "split 2");
        header.add(titleLabel, "gapleft 15");

        
        JPanel body = new JPanel(new MigLayout("fill, insets 25", "[grow]", "[]20[]"));
        body.setBackground(Color.WHITE);

        JLabel msgLabel = new JLabel("<html><body style='width: 280px'>" + message + "</body></html>");
        msgLabel.setFont(ThemeManager.FONT_BODY);
        msgLabel.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

        
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "[grow][grow]"));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(ThemeManager.FONT_BODY);
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.putClientProperty("JButton.buttonType", "roundRect");
        cancelBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        JButton confirmBtn = new JButton(getConfirmText(type));
        confirmBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        confirmBtn.setBackground(getButtonColor(type));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.putClientProperty("JButton.buttonType", "roundRect");
        confirmBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn, "growx, h 40!");
        buttonPanel.add(confirmBtn, "growx, h 40!");

        body.add(msgLabel, "wrap, growx");
        body.add(buttonPanel, "growx");

        panel.add(header, "growx, wrap");
        panel.add(body, "grow");

        dialog.add(panel);
        dialog.pack();
        dialog.setSize(400, dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    
    public static boolean confirmDelete(Component parent, String itemName) {
        return show(parent, "Confirm Delete",
            "Are you sure you want to delete \"" + itemName + "\"? This action cannot be undone.",
            DialogType.DANGER);
    }

    
    public static boolean confirmAction(Component parent, String title, String message) {
        return show(parent, title, message, DialogType.WARNING);
    }

    private static Frame getFrame(Component component) {
        if (component == null) return MainFrame.getInstance();
        if (component instanceof Frame) return (Frame) component;
        return (Frame) SwingUtilities.getAncestorOfClass(Frame.class, component);
    }

    private static Color getHeaderColor(DialogType type) {
        switch (type) {
            case DANGER: return ThemeManager.COLOR_DANGER;
            case WARNING: return ThemeManager.COLOR_WARNING;
            case SUCCESS: return ThemeManager.COLOR_SUCCESS;
            default: return ThemeManager.getCurrentAccent();
        }
    }

    private static Color getButtonColor(DialogType type) {
        switch (type) {
            case DANGER: return ThemeManager.COLOR_DANGER;
            case WARNING: return ThemeManager.COLOR_WARNING;
            case SUCCESS: return ThemeManager.COLOR_SUCCESS;
            default: return ThemeManager.getCurrentAccent();
        }
    }

    private static String getIcon(DialogType type) {
        switch (type) {
            case DANGER: return "\u26A0"; 
            case WARNING: return "\u2753"; 
            case SUCCESS: return "\u2714"; 
            default: return "\u2139"; 
        }
    }

    private static String getConfirmText(DialogType type) {
        switch (type) {
            case DANGER: return "Delete";
            case WARNING: return "Confirm";
            case SUCCESS: return "OK";
            default: return "OK";
        }
    }

    
    public static void showMessage(Component parent, String title, String message, DialogType type) {
        JDialog dialog = new JDialog(getFrame(parent), title, true);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER, 1));

        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[]0[]"));
        panel.setBackground(Color.WHITE);

        
        JPanel header = new JPanel(new MigLayout("fill, insets 20"));
        header.setBackground(getHeaderColor(type));

        JLabel icon = new JLabel(getIcon(type));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        header.add(icon, "split 2");
        header.add(titleLabel, "gapleft 15");

        
        JPanel body = new JPanel(new MigLayout("fill, insets 25", "[grow]", "[]20[]"));
        body.setBackground(Color.WHITE);

        JLabel msgLabel = new JLabel("<html><body style='width: 280px'>" + message + "</body></html>");
        msgLabel.setFont(ThemeManager.FONT_BODY);
        msgLabel.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

        
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow]"));
        buttonPanel.setOpaque(false);

        JButton okBtn = new JButton("OK");
        okBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        okBtn.setBackground(getButtonColor(type));
        okBtn.setForeground(Color.WHITE);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.putClientProperty("JButton.buttonType", "roundRect");
        okBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okBtn, "growx, h 40!");

        body.add(msgLabel, "wrap, growx");
        body.add(buttonPanel, "growx");

        panel.add(header, "growx, wrap");
        panel.add(body, "grow");

        dialog.add(panel);
        dialog.pack();
        dialog.setSize(400, dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    
    public static void showWarning(Component parent, String message) {
        showMessage(parent, "Warning", message, DialogType.WARNING);
    }

    
    public static void showErrorMessage(Component parent, String message) {
        showMessage(parent, "Error", message, DialogType.DANGER);
    }

    
    public static void showInfo(Component parent, String message) {
        showMessage(parent, "Information", message, DialogType.INFO);
    }
}
