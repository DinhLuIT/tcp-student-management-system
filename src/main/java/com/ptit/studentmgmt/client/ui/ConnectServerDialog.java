package com.ptit.studentmgmt.client.ui;

import com.ptit.studentmgmt.client.network.ClientSocketManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class ConnectServerDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTextField txtHost;
    private JTextField txtPort;
    private JButton btnConnect;
    private JLabel lblStatus;
    private boolean connected = false;

    public ConnectServerDialog(Frame owner) {
        super(owner, "Kết Nối Server TCP", true);
        initUI();
    }

    private void initUI() {
        setSize(420, 240);
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Tiêu đề
        JLabel lblTitle = new JLabel("CẤU HÌNH KẾT NỐI SERVER", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(new Color(0, 102, 204));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 12));
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        formPanel.add(new JLabel("Địa chỉ IP / Host Server:"));
        txtHost = new JTextField("127.0.0.1");
        formPanel.add(txtHost);

        formPanel.add(new JLabel("Cổng kết nối (Port):"));
        txtPort = new JTextField("8888");
        formPanel.add(txtPort);

        panel.add(formPanel, BorderLayout.CENTER);

        // Phía dưới: Trạng thái và Nút bấm
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        lblStatus = new JLabel("Vui lòng nhập IP và Port của Server", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblStatus.setForeground(Color.GRAY);
        bottomPanel.add(lblStatus, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnConnect = new JButton("Kết Nối");
        btnConnect.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnConnect.setBackground(new Color(40, 167, 69));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);

        JButton btnExit = new JButton("Thoát");
        btnExit.setFocusPainted(false);

        btnPanel.add(btnConnect);
        btnPanel.add(btnExit);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        add(panel);

        // Sự kiện
        btnConnect.addActionListener(e -> attemptConnect());
        btnExit.addActionListener(e -> {
            connected = false;
            dispose();
            System.exit(0);
        });

        // Nhấn Enter để kết nối
        getRootPane().setDefaultButton(btnConnect);
    }

    private void attemptConnect() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Địa chỉ Host không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cổng Port phải là số nguyên từ 1 đến 65535!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnConnect.setEnabled(false);
        txtHost.setEnabled(false);
        txtPort.setEnabled(false);
        lblStatus.setText("Đang kết nối tới " + host + ":" + port + "...");
        lblStatus.setForeground(new Color(0, 102, 204));

        // Thực hiện kết nối qua SwingWorker để không đơ UI
        new SwingWorker<Boolean, Void>() {
            private String errorMessage = "";

            @Override
            protected Boolean doInBackground() {
                try {
                    ClientSocketManager.getInstance().connect(host, port, 5000);
                    return true;
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        connected = true;
                        lblStatus.setText("Kết nối thành công!");
                        lblStatus.setForeground(new Color(40, 167, 69));
                        JOptionPane.showMessageDialog(ConnectServerDialog.this,
                                "Kết nối Server thành công tại " + host + ":" + port + "!",
                                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        lblStatus.setText("Kết nối thất bại. Vui lòng nhập lại!");
                        lblStatus.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(ConnectServerDialog.this,
                                "Không thể kết nối đến Server (" + host + ":" + port + ")!\nLý do: " + errorMessage +
                                "\nVui lòng kiểm tra Server đã khởi động chưa và nhập lại thông tin.",
                                "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    lblStatus.setText("Lỗi: " + e.getMessage());
                } finally {
                    btnConnect.setEnabled(true);
                    txtHost.setEnabled(true);
                    txtPort.setEnabled(true);
                }
            }
        }.execute();
    }

    public boolean isConnected() {
        return connected;
    }
}
