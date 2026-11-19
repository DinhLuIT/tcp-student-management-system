package com.ptit.studentmgmt.client.ui;

import com.ptit.studentmgmt.client.network.ClientSocketManager;
import com.ptit.studentmgmt.common.dto.DBConfigDTO;
import com.ptit.studentmgmt.common.dto.ServerResponseDTO;
import com.ptit.studentmgmt.common.protocol.CommandCode;
import com.ptit.studentmgmt.common.protocol.ProtocolPacket;
import com.ptit.studentmgmt.common.util.JsonUtil;

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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class ConnectDatabaseDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDbName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnSend;
    private JLabel lblStatus;
    private boolean dbConnected = false;

    public ConnectDatabaseDialog(Frame owner) {
        super(owner, "Cấu Hình Kết Nối CSDL SQL Server", true);
        initUI();
    }

    private void initUI() {
        setSize(460, 340);
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("NHẬP THÔNG SỐ CSDL SQL SERVER", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(new Color(0, 102, 204));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 10));
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        formPanel.add(new JLabel("Host CSDL (Server):"));
        txtHost = new JTextField("localhost");
        formPanel.add(txtHost);

        formPanel.add(new JLabel("Cổng CSDL (Port):"));
        txtPort = new JTextField("1433");
        formPanel.add(txtPort);

        formPanel.add(new JLabel("Tên Database:"));
        txtDbName = new JTextField("StudentManagementDB");
        formPanel.add(txtDbName);

        formPanel.add(new JLabel("Username (sa):"));
        txtUsername = new JTextField("sa");
        formPanel.add(txtUsername);

        formPanel.add(new JLabel("Mật khẩu SQL:"));
        txtPassword = new JPasswordField("");
        formPanel.add(txtPassword);

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        lblStatus = new JLabel("Nhập thông số và gửi lên Server để kết nối CSDL", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblStatus.setForeground(Color.GRAY);
        bottomPanel.add(lblStatus, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnSend = new JButton("Gửi Lên Server");
        btnSend.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSend.setBackground(new Color(0, 123, 255));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);

        JButton btnCancel = new JButton("Đóng");
        btnCancel.setFocusPainted(false);

        btnPanel.add(btnSend);
        btnPanel.add(btnCancel);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        add(panel);

        btnSend.addActionListener(e -> sendDBConfigToServer());
        btnCancel.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(btnSend);
    }

    private void sendDBConfigToServer() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String dbName = txtDbName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (host.isEmpty() || dbName.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Host, Tên Database và Username!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cổng CSDL không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DBConfigDTO config = new DBConfigDTO(host, port, dbName, username, password);
        String jsonPayload = JsonUtil.toJson(config);

        btnSend.setEnabled(false);
        lblStatus.setText("Đang gửi yêu cầu kết nối CSDL lên Server...");
        lblStatus.setForeground(new Color(0, 102, 204));

        new SwingWorker<ServerResponseDTO, Void>() {
            @Override
            protected ServerResponseDTO doInBackground() throws Exception {
                ClientSocketManager.getInstance().sendPacket(CommandCode.CMD_DB_CONNECT_REQ, jsonPayload);
                ProtocolPacket respPacket = ClientSocketManager.getInstance().readPacket();
                return JsonUtil.parseServerResponse(respPacket.getPayload());
            }

            @Override
            protected void done() {
                try {
                    ServerResponseDTO response = get();
                    if (response.isSuccess()) {
                        dbConnected = true;
                        lblStatus.setText("Server kết nối CSDL thành công!");
                        lblStatus.setForeground(new Color(40, 167, 69));
                        JOptionPane.showMessageDialog(ConnectDatabaseDialog.this,
                                response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        lblStatus.setText("Kết nối CSDL thất bại!");
                        lblStatus.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(ConnectDatabaseDialog.this,
                                "Server thông báo lỗi: " + response.getMessage(),
                                "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    lblStatus.setText("Lỗi: " + e.getMessage());
                    lblStatus.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(ConnectDatabaseDialog.this,
                            "Lỗi giao tiếp với Server: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnSend.setEnabled(true);
                }
            }
        }.execute();
    }

    public boolean isDbConnected() {
        return dbConnected;
    }
}
