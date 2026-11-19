package com.ptit.studentmgmt.server.ui;

import com.ptit.studentmgmt.common.dto.StudentResultDTO;
import com.ptit.studentmgmt.server.dao.DBConnectionManager;
import com.ptit.studentmgmt.server.network.TcpServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ServerDashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final TcpServer tcpServer;
    private JTextField txtPort;
    private JButton btnStartStop;
    private JLabel lblStatus;
    private JTextArea txtLogs;
    private DefaultTableModel tableModel;

    public ServerDashboardFrame() {
        this.tcpServer = new TcpServer();
        initUI();
        setupListeners();
    }

    private void initUI() {
        setTitle("Quản Lý Server Socket - Đồ Án Lập Trình Mạng PTIT");
        setSize(950, 680);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Header Toolbar Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("Cấu hình & Trạng thái Server"));

        topPanel.add(new JLabel("Cổng lắng nghe (Port):"));
        txtPort = new JTextField("8888", 6);
        topPanel.add(txtPort);

        btnStartStop = new JButton("Khởi động Server");
        btnStartStop.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnStartStop.setBackground(new Color(40, 167, 69));
        btnStartStop.setForeground(Color.WHITE);
        btnStartStop.setFocusPainted(false);
        topPanel.add(btnStartStop);

        lblStatus = new JLabel("Trạng thái: ĐÃ DỪNG");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setForeground(Color.RED);
        topPanel.add(lblStatus);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. Center Panel: SplitPane (Nửa trên là Danh sách SV đã giải mã, Nửa dưới là Terminal Log)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Bảng dữ liệu Sinh viên trên Server
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Dữ liệu CSDL đã Giải mã DES & Điểm Trung Bình"));

        String[] columnNames = {"Mã SV", "Họ và Tên", "Điểm Toán", "Điểm Văn", "Điểm Tiếng Anh", "Điểm TB", "Xếp Loại"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblStudents = new JTable(tableModel);
        tblStudents.setRowHeight(24);
        tblStudents.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tablePanel.add(new JScrollPane(tblStudents), BorderLayout.CENTER);

        // Nút thao tác Server
        JPanel tableActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnDecryptAndCalc = new JButton("Đọc, Giải mã DES & Tính ĐTB từ CSDL");
        btnDecryptAndCalc.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnDecryptAndCalc.setBackground(new Color(0, 123, 255));
        btnDecryptAndCalc.setForeground(Color.WHITE);
        btnDecryptAndCalc.setFocusPainted(false);
        btnDecryptAndCalc.addActionListener(e -> loadAndDisplayDecryptedData());
        tableActionPanel.add(btnDecryptAndCalc);
        tablePanel.add(tableActionPanel, BorderLayout.SOUTH);

        // Khu vực Log Console
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(BorderFactory.createTitledBorder("Nhật ký Kết nối & Gói tin Real-time"));

        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLogs.setBackground(new Color(30, 30, 30));
        txtLogs.setForeground(new Color(0, 255, 128)); // Màu xanh lá terminal
        JScrollPane scrollLog = new JScrollPane(txtLogs);
        logPanel.add(scrollLog, BorderLayout.CENTER);

        JPanel logBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        JButton btnClearLog = new JButton("Xóa Log");
        btnClearLog.addActionListener(e -> txtLogs.setText(""));
        logBtnPanel.add(btnClearLog);
        logPanel.add(logBtnPanel, BorderLayout.SOUTH);

        splitPane.setTopComponent(tablePanel);
        splitPane.setBottomComponent(logPanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void setupListeners() {
        // Log callback
        tcpServer.setLogListener(msg -> SwingUtilities.invokeLater(() -> {
            txtLogs.append(msg + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        }));

        btnStartStop.addActionListener(e -> {
            if (!tcpServer.isRunning()) {
                startServer();
            } else {
                stopServer();
            }
        });
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            if (port < 1024 || port > 65535) {
                JOptionPane.showMessageDialog(this, "Port phải nằm trong khoảng từ 1024 đến 65535!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            tcpServer.start(port);
            lblStatus.setText("Trạng thái: ĐANG CHẠY (Port: " + port + ")");
            lblStatus.setForeground(new Color(40, 167, 69));
            btnStartStop.setText("Dừng Server");
            btnStartStop.setBackground(new Color(220, 53, 69));
            txtPort.setEnabled(false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số cổng (Port) không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể khởi động Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        tcpServer.stop();
        lblStatus.setText("Trạng thái: ĐÃ DỪNG");
        lblStatus.setForeground(Color.RED);
        btnStartStop.setText("Khởi động Server");
        btnStartStop.setBackground(new Color(40, 167, 69));
        txtPort.setEnabled(true);
    }

    private void loadAndDisplayDecryptedData() {
        if (!DBConnectionManager.isConnected()) {
            JOptionPane.showMessageDialog(this, "CSDL SQL Server chưa được kết nối! Hãy để Client kết nối CSDL trước hoặc cấu hình trong DAO.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<List<StudentResultDTO>, Void>() {
            @Override
            protected List<StudentResultDTO> doInBackground() throws Exception {
                return tcpServer.getStudentDAO().getAllStudentsWithCalculatedAverage();
            }

            @Override
            protected void done() {
                try {
                    List<StudentResultDTO> list = get();
                    tableModel.setRowCount(0);
                    for (StudentResultDTO s : list) {
                        tableModel.addRow(new Object[]{
                                s.getStudentId(),
                                s.getFullName(),
                                s.getMathScore(),
                                s.getLiteratureScore(),
                                s.getEnglishScore(),
                                s.getAverageScore(),
                                s.getAcademicRank()
                        });
                    }
                    tcpServer.log(">>> [SERVER UI] Đã tải và hiển thị " + list.size() + " sinh viên đã giải mã DES lên bảng!");
                    // Phát thông báo cập nhật kết quả cho các Client
                    tcpServer.triggerDecryptionAndBroadcast();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ServerDashboardFrame.this, "Lỗi đọc & giải mã dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
