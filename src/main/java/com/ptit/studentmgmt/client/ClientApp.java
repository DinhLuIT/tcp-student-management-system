package com.ptit.studentmgmt.client;

import com.ptit.studentmgmt.client.ui.ConnectServerDialog;
import com.ptit.studentmgmt.client.ui.MainClientFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ClientApp {
    public static void main(String[] args) {
        // Cố gắng nạp FlatLaf Light Theme nếu có thư viện
        try {
            Class<?> flatLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLaf.getMethod("setup").invoke(null);
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            ConnectServerDialog serverDialog = new ConnectServerDialog(null);
            serverDialog.setVisible(true);

            if (serverDialog.isConnected()) {
                MainClientFrame mainFrame = new MainClientFrame();
                mainFrame.setVisible(true);

                // Tự động mở hộp thoại cấu hình CSDL sau khi nối Server thành công
                SwingUtilities.invokeLater(mainFrame::openDBConfigDialog);
            } else {
                System.exit(0);
            }
        });
    }
}
