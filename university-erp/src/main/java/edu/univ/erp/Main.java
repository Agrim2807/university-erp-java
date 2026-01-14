package edu.univ.erp;

import edu.univ.erp.ui.MainFrame;
import edu.univ.erp.ui.ThemeManager;
import edu.univ.erp.util.DatabaseConfig;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ThemeManager.setupTheme();
                
                MainFrame mainFrame = MainFrame.getInstance();
                mainFrame.setVisible(true);
                
                mainFrame.updateMaintenanceBanner();
                
                System.out.println("University ERP System started successfully");
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConfig.closeDataSources();
            System.out.println("Application shutdown complete");
        }));
    }
}