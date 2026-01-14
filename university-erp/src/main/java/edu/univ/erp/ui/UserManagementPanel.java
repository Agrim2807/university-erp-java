package edu.univ.erp.ui;

import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementPanel {
    private JPanel mainPanel;
    private AdminService adminService;
    
    private JList<User> userList;
    private DefaultListModel<User> userListModel;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private List<User> allUsersCache;
    private JPanel listContainer; 

    
    private static final String[] BRANCHES = {
        "Computer Science and Engineering (CSE)",
        "Electronics and Communications Engineering (ECE)",
        "Computer Science and Applied Mathematics (CSAM)",
        "Computer Science and Design (CSD)",
        "Computer Science and Social Sciences (CSSS)",
        "Computer Science and Biosciences (CSB)",
        "Computer Science and Artificial Intelligence (CSAI)"
    };

    public UserManagementPanel() {
        this.adminService = new AdminService();
        initializePanel();
    }

    private void initializePanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.COLOR_BACKGROUND);

        mainPanel.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent event) { refreshUserList(); }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });



        JPanel topPanel = new JPanel(new MigLayout("fillx, insets 20 30 10 30", "[grow][]"));
        topPanel.setOpaque(false);
        topPanel.add(UIFactory.createHeader("User Management"), "wrap");

        searchField = UIFactory.createInput("Search users...");
        searchField.addActionListener(e -> filterList(searchField.getText()));

        sortCombo = new JComboBox<>(new String[]{"Sort: Name", "Sort: Role", "Sort: Status", "Sort: Username"});
        sortCombo.setFont(ThemeManager.FONT_BODY);
        sortCombo.addActionListener(e -> applySortAndFilter());

        topPanel.add(searchField, "growx, wmin 300");
        topPanel.add(UIFactory.createSecondaryButton("Search", () -> filterList(searchField.getText())));
        topPanel.add(sortCombo, "w 180!");
        mainPanel.add(topPanel, BorderLayout.NORTH);

        
        listContainer = new JPanel(new BorderLayout());
        listContainer.setOpaque(false);
        listContainer.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListRenderer());
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        userList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) showUserDetails(userList.getSelectedValue());
            }
        });

        JScrollPane scroll = new JScrollPane(userList);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
        listContainer.add(scroll, BorderLayout.CENTER);
        mainPanel.add(listContainer, BorderLayout.CENTER);

        
        JPanel bottomPanel = new JPanel(new MigLayout("fillx, insets 15 30 20 30", "[grow][]"));
        bottomPanel.setOpaque(false);
        bottomPanel.setBackground(ThemeManager.COLOR_BACKGROUND);

        JButton addBtn = UIFactory.createPrimaryButton("+ Add User", () -> openUserDialog(null));
        JButton editBtn = UIFactory.createSecondaryButton("Edit Selected", () -> {
            User u = userList.getSelectedValue();
            if(u != null) openUserDialog(u);
            else MainFrame.getInstance().showWarning("Select a user to edit.");
        });
        JButton delBtn = UIFactory.createDangerButton("Delete", this::deleteUser);

        
        bottomPanel.add(new JLabel(""), "growx"); 
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(delBtn);
        btnPanel.add(editBtn);
        btnPanel.add(addBtn);
        bottomPanel.add(btnPanel, "wrap");

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void openUserDialog(User user) {
        boolean isEdit = (user != null);
        JDialog dialog = new JDialog(MainFrame.getInstance(), isEdit ? "Edit User" : "Add New User", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel form = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[label]10[grow, fill]"));
        form.setBackground(Color.WHITE);
        
        JTextField usernameField = UIFactory.createInput("Username");
        JTextField fullNameField = UIFactory.createInput("Full Name");
        JPasswordField passwordField = UIFactory.createPasswordInput(isEdit ? "Leave empty to keep" : "Password");
        
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"student", "instructor", "admin"});
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"active", "locked"});
        
        JComboBox<String> branchCombo = new JComboBox<>(BRANCHES);
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        JLabel branchLbl = UIFactory.createLabel("Branch");
        JLabel yearLbl = UIFactory.createLabel("Year");
        
        roleCombo.addActionListener(e -> {
            boolean isStudent = "student".equals(roleCombo.getSelectedItem());
            branchCombo.setVisible(isStudent);
            branchLbl.setVisible(isStudent);
            yearSpinner.setVisible(isStudent);
            yearLbl.setVisible(isStudent);
            dialog.pack(); 
        });

        if (isEdit) {
            usernameField.setText(user.getUsername());
            usernameField.setEditable(false);
            fullNameField.setText(user.getFullName());
            roleCombo.setSelectedItem(user.getRole());
            roleCombo.setEnabled(false); 
            statusCombo.setSelectedItem(user.getStatus());
            
            if ("student".equals(user.getRole())) {
                Student s = adminService.getStudentProfile(user.getUserId());
                if (s != null) {
                    branchCombo.setSelectedItem(s.getProgram());
                    yearSpinner.setValue(s.getYear());
                }
            }
        } else {
            roleCombo.setSelectedItem("student");
            statusCombo.setSelectedItem("active");
        }
        
        roleCombo.getActionListeners()[0].actionPerformed(null);

        form.add(UIFactory.createLabel("Full Name")); form.add(fullNameField, "h 40!");
        form.add(UIFactory.createLabel("Username")); form.add(usernameField, "h 40!");
        form.add(UIFactory.createLabel("Role")); form.add(roleCombo, "h 40!");
        form.add(UIFactory.createLabel("Status")); form.add(statusCombo, "h 40!");
        
        form.add(branchLbl, "hidemode 3"); form.add(branchCombo, "h 40!, hidemode 3");
        form.add(yearLbl, "hidemode 3"); form.add(yearSpinner, "h 40!, hidemode 3");
        
        form.add(UIFactory.createLabel(isEdit ? "New Password" : "Password")); 
        form.add(passwordField, "h 40!");

        JButton saveBtn = UIFactory.createPrimaryButton(isEdit ? "Save Changes" : "Create User", () -> {
            if (usernameField.getText().isEmpty() || fullNameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Username required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isEdit && new String(passwordField.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Password required for new users.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    try {
                        String role = (String) roleCombo.getSelectedItem();
                        String pass = new String(passwordField.getPassword());
                        String prog = "student".equals(role) ? (String) branchCombo.getSelectedItem() : null;
                        int yr = "student".equals(role) ? (int) yearSpinner.getValue() : 0;

                        if (!isEdit) {
                            return adminService.createUser(fullNameField.getText(), usernameField.getText(),
                                role, pass, prog, yr);
                        } else {
                            return adminService.updateUserFull(user.getUserId(), fullNameField.getText(), role, 
                                (String)statusCombo.getSelectedItem(), pass, prog, yr);
                        }
                    } catch (Exception e) { 
                        e.printStackTrace();
                        return false; 
                    }
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            MainFrame.getInstance().showSuccess("User Saved.");
                            refreshUserList();
                            dialog.dispose();
                        } else MainFrame.getInstance().showError("Operation Failed.");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);
        actions.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setMinimumSize(new Dimension(450, 400)); 
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void refreshUserList() {
        listContainer.removeAll();
        listContainer.add(new SkeletonPanel(), BorderLayout.CENTER);
        listContainer.revalidate(); listContainer.repaint();

        new SwingWorker<List<User>, Void>() {
            @Override protected List<User> doInBackground() { return adminService.getAllUsers(); }
            @Override protected void done() {
                try {
                    allUsersCache = get();
                    listContainer.removeAll();
                    JScrollPane scroll = new JScrollPane(userList);
                    scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER));
                    listContainer.add(scroll, BorderLayout.CENTER);
                    filterList(searchField.getText());
                    listContainer.revalidate(); listContainer.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void filterList(String query) {
        applySortAndFilter();
    }

    private void applySortAndFilter() {
        if (allUsersCache == null) return;
        userListModel.clear();

        // Filter
        String query = searchField.getText().toLowerCase();
        List<User> filtered = allUsersCache.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query) ||
                             u.getFullName().toLowerCase().contains(query))
                .collect(Collectors.toList());

        // Sort
        String sortOption = (String) sortCombo.getSelectedItem();
        if ("Sort: Name".equals(sortOption)) {
            filtered.sort((u1, u2) -> u1.getFullName().compareToIgnoreCase(u2.getFullName()));
        } else if ("Sort: Role".equals(sortOption)) {
            filtered.sort((u1, u2) -> u1.getRole().compareToIgnoreCase(u2.getRole()));
        } else if ("Sort: Status".equals(sortOption)) {
            filtered.sort((u1, u2) -> u1.getStatus().compareToIgnoreCase(u2.getStatus()));
        } else if ("Sort: Username".equals(sortOption)) {
            filtered.sort((u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
        }

        // Populate list
        for (User u : filtered) {
            userListModel.addElement(u);
        }
    }

    private void deleteUser() {
        User user = userList.getSelectedValue();
        if (user == null) { MainFrame.getInstance().showWarning("Select a user to delete."); return; }
        if (ConfirmDialog.confirmDelete(mainPanel, user.getUsername() + " (" + user.getFullName() + ")")) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return adminService.deleteUser(user.getUserId(), user.getRole());
                }
                @Override protected void done() {
                    try {
                        if (get()) { MainFrame.getInstance().showSuccess("Deleted."); refreshUserList(); }
                        else MainFrame.getInstance().showError("Failed.");
                    } catch (Exception e) { MainFrame.getInstance().showError(getCleanErrorMessage(e)); }
                }
            }.execute();
        }
    }

    
    private void showUserDetails(User user) {
        if (user == null) return;

        JDialog dialog = new JDialog(MainFrame.getInstance(), "User Details", true);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right]15[grow, fill]"));
        form.setBackground(Color.WHITE);

        
        addDetailRow(form, "Full Name", user.getFullName());
        addDetailRow(form, "Username", user.getUsername());
        addDetailRow(form, "Role", user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1));
        addDetailRow(form, "Status", user.getStatus().substring(0, 1).toUpperCase() + user.getStatus().substring(1));

        
        if ("student".equals(user.getRole())) {
            Student s = adminService.getStudentProfile(user.getUserId());
            if (s != null) {
                addDetailRow(form, "Branch", s.getProgram() != null ? s.getProgram() : "Not Assigned");
                addDetailRow(form, "Year", String.valueOf(s.getYear()));
                addDetailRow(form, "Roll No", s.getRollNo() != null ? s.getRollNo() : "N/A");
            }
        }

        
        String lastLogin = "Never";
        if (user.getLastLogin() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            lastLogin = user.getLastLogin().format(formatter);
        }
        addDetailRow(form, "Last Login", lastLogin);

        JButton closeBtn = UIFactory.createSecondaryButton("Close", dialog::dispose);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);
        actions.add(closeBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 300));
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lblComponent = UIFactory.createLabel(label + ":");
        lblComponent.setFont(ThemeManager.FONT_BODY_BOLD);
        JLabel valueComponent = new JLabel(value != null ? value : "N/A");
        valueComponent.setFont(ThemeManager.FONT_BODY);
        panel.add(lblComponent);
        panel.add(valueComponent, "h 30!");
    }

    private static class UserListRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel p = new JPanel(new BorderLayout(10, 5));
            p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            if (value instanceof User) {
                User u = (User) value;
                JLabel name = new JLabel(u.getFullName());
                name.setFont(ThemeManager.FONT_BODY_BOLD);
                
                JLabel roleLabel = new JLabel(u.getUsername() + " • " + u.getRole().toUpperCase() + " • " + u.getStatus());
                roleLabel.setFont(ThemeManager.FONT_LABEL);
                roleLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
                
                p.add(name, BorderLayout.NORTH);
                p.add(roleLabel, BorderLayout.SOUTH);
                
                if (isSelected) {
                    p.setBackground(new Color(241, 245, 249));
                    name.setForeground(ThemeManager.getCurrentAccent());
                } else {
                    p.setBackground(Color.WHITE);
                    name.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);
                }
            }
            return p;
        }
    }

    
    private String getCleanErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "An unexpected error occurred.";

        
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            msg = e.getCause().getMessage();
        }

        
        if (msg.contains(": ")) {
            int colonIdx = msg.indexOf(": ");
            String prefix = msg.substring(0, colonIdx);
            if (prefix.contains(".") && !prefix.contains(" ")) {
                msg = msg.substring(colonIdx + 2);
            }
        }
        return msg;
    }

    public JPanel getPanel() { return mainPanel; }
}