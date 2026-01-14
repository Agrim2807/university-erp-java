package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.SemesterService;
import edu.univ.erp.util.DatabaseBackupRestore;
import net.miginfocom.swing.MigLayout;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.Year;

public class SystemSettingsPanel {
    private JPanel mainPanel;
    private AdminService adminService;
    private JCheckBox maintenanceToggle; 

    public SystemSettingsPanel() {
        this.adminService = new AdminService();
        initializePanel();
        loadData();
    }

    private void initializePanel() {
        mainPanel = new JPanel(new MigLayout("fillx, insets 30, wrap 2", "[grow][grow]"));
        mainPanel.setBackground(ThemeManager.COLOR_BACKGROUND);

        mainPanel.add(UIFactory.createHeader("System Settings"), "span, wrap 20");

        
        mainPanel.add(createMaintenanceCard(), "grow");

        
        mainPanel.add(createSemesterCard(), "grow");

        
        mainPanel.add(createAnnouncementCard(), "span, grow");

        
        mainPanel.add(createBackupCard(), "span, grow");
    }

    private JPanel createCard(String title) {
        JPanel p = new JPanel(new MigLayout("fill, insets 20, wrap 1"));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        p.putClientProperty("Component.arc", 12);
        JLabel t = new JLabel(title);
        t.setFont(ThemeManager.FONT_TITLE.deriveFont(16f));
        t.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
        p.add(t, "gapbottom 15");
        return p;
    }

    private JPanel createMaintenanceCard() {
        JPanel p = createCard("System Status");
        
        maintenanceToggle = new JCheckBox("Maintenance Mode");
        maintenanceToggle.setFont(ThemeManager.FONT_BODY_BOLD);
        
        JLabel help = new JLabel("<html>When active, students and instructors have Read-Only access.<br>Admins retain full control.</html>");
        help.setFont(ThemeManager.FONT_BODY);
        help.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        JButton saveBtn = UIFactory.createPrimaryButton("Update Status", () -> {
            boolean on = maintenanceToggle.isSelected();
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return MaintenanceService.setMaintenanceMode(on); }
                @Override protected void done() { 
                    MainFrame.getInstance().updateMaintenanceBanner(); 
                    MainFrame.getInstance().showSuccess("Maintenance Mode Updated"); 
                }
            }.execute();
        });

        p.add(maintenanceToggle);
        p.add(help);
        p.add(saveBtn, "gaptop 10");
        return p;
    }

    private JPanel createSemesterCard() {
        JPanel p = createCard("Current Term");
        JComboBox<String> sem = new JComboBox<>(new String[]{"Monsoon", "Winter", "Summer"});
        JSpinner year = new JSpinner(new SpinnerNumberModel(Year.now().getValue(), 2020, 2030, 1));
        
        
        sem.setSelectedItem(SemesterService.getCurrentSemester());
        year.setValue(SemesterService.getCurrentYear());

        JButton save = UIFactory.createPrimaryButton("Save Term", () -> {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { 
                    return SemesterService.setCurrentSemester((String)sem.getSelectedItem(), (int)year.getValue()); 
                }
                @Override protected void done() { MainFrame.getInstance().showSuccess("Term Updated"); }
            }.execute();
        });

        p.add(UIFactory.createLabel("Semester")); p.add(sem, "growx");
        p.add(UIFactory.createLabel("Year")); p.add(year, "growx");
        p.add(save, "gaptop 10");
        return p;
    }

    private JPanel createAnnouncementCard() {
        JPanel p = createCard("Announcements");
        JTextArea msg = new JTextArea(3, 30);
        msg.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        JComboBox<String> role = new JComboBox<>(new String[]{"All", "student", "instructor"});
        
        JButton send = UIFactory.createPrimaryButton("Broadcast", () -> {
            if(msg.getText().isEmpty()) return;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { 
                    return adminService.createAnnouncement((String)role.getSelectedItem(), msg.getText()); 
                }
                @Override protected void done() { 
                    MainFrame.getInstance().showSuccess("Sent."); 
                    msg.setText(""); 
                }
            }.execute();
        });

        p.add(new JScrollPane(msg), "growx");
        JPanel ctrls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ctrls.setOpaque(false);
        ctrls.add(new JLabel("Target: ")); ctrls.add(role); ctrls.add(Box.createHorizontalStrut(10)); ctrls.add(send);
        p.add(ctrls);
        return p;
    }

    private JPanel createBackupCard() {
        JPanel p = createCard("Data Management");
        JButton backup = UIFactory.createPrimaryButton("Backup Database", () -> {
            
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() { DatabaseBackupRestore.quickBackup(); return null; }
                @Override protected void done() { MainFrame.getInstance().showSuccess("Backup Created."); }
            }.execute();
        });
        
        JButton restore = UIFactory.createDangerButton("Restore Database", () -> {
            JFileChooser fc = new JFileChooser("backups/");
            fc.setFileFilter(new FileNameExtensionFilter("SQL", "sql"));
            if(fc.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                DatabaseBackupRestore.quickRestore(f.getAbsolutePath());
                MainFrame.getInstance().showSuccess("Restored.");
                MainFrame.getInstance().navigateToLogin();
            }
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.add(backup);
        row.add(restore);
        p.add(row);
        return p;
    }

    private void loadData() {
        maintenanceToggle.setSelected(MaintenanceService.isMaintenanceMode());
    }

    public JPanel getPanel() { return mainPanel; }
}