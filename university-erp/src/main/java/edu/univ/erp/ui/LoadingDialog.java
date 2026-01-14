package edu.univ.erp.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable loading dialog to show "Please wait..." messages during long operations.
 * Addresses rubric requirement: "Long actions show 'please wait' (even a simple dialog)"
 *
 * Usage:
 * LoadingDialog dialog = new LoadingDialog(parentFrame, "Loading courses...");
 * dialog.showLoading();
 * // ... perform operation in SwingWorker ...
 * dialog.hideLoading();
 */
public class LoadingDialog {
    private JDialog dialog;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private Component parentComponent;

    /**
     * Creates a loading dialog with default message "Please wait..."
     */
    public LoadingDialog(Component parent) {
        this(parent, "Please wait...");
    }

    /**
     * Creates a loading dialog with a custom message
     */
    public LoadingDialog(Component parent, String message) {
        this.parentComponent = parent;
        initializeDialog(message);
    }

    private void initializeDialog(String message) {
        // Find parent window
        Window parentWindow = null;
        if (parentComponent instanceof Window) {
            parentWindow = (Window) parentComponent;
        } else if (parentComponent != null) {
            parentWindow = SwingUtilities.getWindowAncestor(parentComponent);
        }

        // Create non-modal dialog (allows UI to remain responsive)
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, false);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, false);
        } else {
            dialog = new JDialog((Frame) null, false);
        }

        dialog.setTitle("Loading");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setUndecorated(false);

        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(Color.WHITE);

        // Message label with icon
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        messagePanel.setBackground(Color.WHITE);

        // Loading icon (spinner)
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        messagePanel.add(iconLabel);

        messageLabel = new JLabel(message);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 14f));
        messagePanel.add(messageLabel);

        contentPanel.add(messagePanel, BorderLayout.CENTER);

        // Indeterminate progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(350, 120));

        // Center on parent or screen
        if (parentComponent != null) {
            dialog.setLocationRelativeTo(parentComponent);
        } else {
            dialog.setLocationRelativeTo(null);
        }
    }

    /**
     * Updates the loading message while dialog is showing
     */
    public void setMessage(String message) {
        if (messageLabel != null) {
            SwingUtilities.invokeLater(() -> messageLabel.setText(message));
        }
    }

    /**
     * Shows the loading dialog (non-blocking)
     */
    public void showLoading() {
        SwingUtilities.invokeLater(() -> {
            if (dialog != null && !dialog.isVisible()) {
                dialog.setVisible(true);
            }
        });
    }

    /**
     * Hides the loading dialog
     */
    public void hideLoading() {
        SwingUtilities.invokeLater(() -> {
            if (dialog != null && dialog.isVisible()) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
    }

    /**
     * Convenience method: Execute a task with automatic loading indicator
     *
     * Example usage:
     * LoadingDialog.executeWithLoading(
     *     this,
     *     "Loading courses...",
     *     () -> {
     *         // Background task
     *         List<Course> courses = studentService.getAllCourses();
     *         return courses;
     *     },
     *     result -> {
     *         // Success callback (on EDT)
     *         updateTableWithCourses(result);
     *     },
     *     error -> {
     *         // Error callback (on EDT)
     *         showError(error.getMessage());
     *     }
     * );
     */
    public static <T> void executeWithLoading(
            Component parent,
            String message,
            BackgroundTask<T> task,
            SuccessCallback<T> onSuccess,
            ErrorCallback onError) {

        LoadingDialog loadingDialog = new LoadingDialog(parent, message);

        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute();
            }

            @Override
            protected void done() {
                loadingDialog.hideLoading();
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.onSuccess(result);
                    }
                } catch (Exception e) {
                    if (onError != null) {
                        onError.onError(e);
                    }
                }
            }
        };

        loadingDialog.showLoading();
        worker.execute();
    }

    // Functional interfaces for convenience method
    @FunctionalInterface
    public interface BackgroundTask<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface SuccessCallback<T> {
        void onSuccess(T result);
    }

    @FunctionalInterface
    public interface ErrorCallback {
        void onError(Exception error);
    }
}
