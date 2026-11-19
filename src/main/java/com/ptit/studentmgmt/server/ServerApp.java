package com.ptit.studentmgmt.server;

import com.ptit.studentmgmt.server.ui.ServerDashboardFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ServerApp {
    public static void main(String[] args) {
        // Cố gắng kích hoạt FlatLaf Dark theme nếu có thư viện
        try {
            Class<?> flatLaf = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            flatLaf.getMethod("setup").invoke(null);
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            ServerDashboardFrame frame = new ServerDashboardFrame();
            frame.setVisible(true);
        });
    }
}
