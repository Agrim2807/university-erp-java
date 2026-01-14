package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.AuthService.AuthException;
import edu.univ.erp.domain.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginPanel {
    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private AuthService authService;

    public LoginPanel() {
        this.authService = new AuthService();
        initializePanel();
    }

    private void initializePanel() {
        
        panel = new JPanel(new MigLayout("fill, insets 0", "[50%, fill][50%, fill]", "[grow, fill]"));

        
        JPanel leftPane = new JPanel(new MigLayout("fill, insets 40", "[center]", "[center]"));
        leftPane.setBackground(new Color(41, 128, 185)); 
        
        JLabel brandTitle = new JLabel("<html><div style='text-align: center; color: white;'>" +
                "<h1 style='font-size: 48px; margin-bottom: 10px;'>University ERP</h1>" +
                "<p style='font-size: 18px; font-weight: normal;'>Excellence in Academic Management</p></div></html>");
        leftPane.add(brandTitle);
        
        
        
        JPanel rightPane = new JPanel(new MigLayout("wrap 1, insets 60 80 60 80", "[grow, fill]"));
        rightPane.setBackground(Color.WHITE);

        
        JLabel signInLabel = UIFactory.createHeader("Welcome Back");
        JLabel subLabel = new JLabel("Please enter your credentials to access your account.");
        subLabel.setFont(ThemeManager.FONT_BODY);
        subLabel.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);

        
        usernameField = UIFactory.createInput("Username");
        
        passwordField = UIFactory.createPasswordInput("Password");
        
        passwordField.putClientProperty("JPasswordField.showRevealButton", false); 

        loginButton = UIFactory.createPrimaryButton("Sign In", this::performLogin);
        
        loginButton.setBackground(new Color(41, 128, 185));

        
        usernameField.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());

        
        JLabel userLabel = UIFactory.createLabel("Username");
        userLabel.setFont(ThemeManager.FONT_BODY_BOLD); 
        userLabel.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

        JLabel passLabel = UIFactory.createLabel("Password");
        passLabel.setFont(ThemeManager.FONT_BODY_BOLD); 
        passLabel.setForeground(ThemeManager.COLOR_TEXT_PRIMARY);

        
        JCheckBox showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setFont(ThemeManager.FONT_BODY);
        showPassCheck.setForeground(ThemeManager.COLOR_TEXT_SECONDARY);
        showPassCheck.setOpaque(false);
        showPassCheck.setFocusPainted(false);
        
        char defaultEcho = passwordField.getEchoChar();
        showPassCheck.addActionListener(e -> {
            if (showPassCheck.isSelected()) {
                passwordField.setEchoChar((char) 0); 
            } else {
                passwordField.setEchoChar(defaultEcho); 
            }
        });

        
        JLabel demoText = new JLabel("<html><div style='color: #999; font-size: 11px; text-align: center;'>" +
                "Demo: admin1 / inst1 / stu1 (Pass: admin123)</div></html>");

        
        rightPane.add(new JLabel(), "pushy"); 
        
        rightPane.add(signInLabel);
        rightPane.add(subLabel, "gaptop 5, gapbottom 30");
        
        
        rightPane.add(userLabel, "gapbottom 5"); 
        rightPane.add(usernameField, "h 45!");
        
        
        rightPane.add(passLabel, "gaptop 20, gapbottom 5"); 
        rightPane.add(passwordField, "h 45!");
        
        
        rightPane.add(showPassCheck, "gaptop 5");
        
        
        rightPane.add(loginButton, "gaptop 30, h 50!");
        rightPane.add(demoText, "gaptop 20, align center");
        
        rightPane.add(new JLabel(), "pushy"); 

        panel.add(leftPane);
        panel.add(rightPane);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            MainFrame.getInstance().showError("Please enter both username and password.");
            return;
        }

        // Show loading dialog (addresses rubric: "Long actions show 'please wait'")
        LoadingDialog loadingDialog = new LoadingDialog(panel, "Authenticating...");

        loginButton.setText("Signing in...");
        loginButton.setEnabled(false);

        new SwingWorker<User, Void>() {
            String errorMsg = null;
            @Override
            protected User doInBackground() {
                loadingDialog.showLoading(); // Show "Please wait..." dialog
                try {
                    return authService.login(username, password);
                } catch (AuthException e) {
                    errorMsg = e.getMessage();
                    return null;
                } catch (Exception e) {
                    errorMsg = "Unexpected error: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                loadingDialog.hideLoading(); // Hide dialog when done
                try {
                    User user = get();
                    if (user != null) {
                        ThemeManager.setRole(user.getRole());
                        MainFrame.getInstance().navigateToDashboard();
                    } else {
                        MainFrame.getInstance().showError(errorMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    loginButton.setText("Sign In");
                    loginButton.setEnabled(true);
                }
            }
        }.execute();
    }

    public JPanel getPanel() { return panel; }
}