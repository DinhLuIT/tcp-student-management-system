package com.ptit.studentmgmt.client.ui;

import com.ptit.studentmgmt.client.network.ClientSocketManager;
import com.ptit.studentmgmt.common.dto.BatchStudentDTO;
import com.ptit.studentmgmt.common.dto.ServerResponseDTO;
import com.ptit.studentmgmt.common.dto.StudentDTO;
import com.ptit.studentmgmt.common.dto.StudentResultDTO;
import com.ptit.studentmgmt.common.protocol.CommandCode;
import com.ptit.studentmgmt.common.protocol.ProtocolPacket;
import com.ptit.studentmgmt.common.util.FileParserUtil;
import com.ptit.studentmgmt.common.util.JsonUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class MainClientFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final StudentTableModel tableModel;

    // Header badge
    private JLabel lblDbStatus;

    // Form nhập từng sinh viên
    private JTextField txtStudentId;
    private JTextField txtFullName;
    private JTextField txtMathScore;
    private JTextField txtLiteratureScore;
    private JTextField txtEnglishScore;
    private JButton btnSendStudent;

    // Form đính kèm file
    private JTextField txtFilePath;
    private JButton btnUploadFile;
    private File selectedFile;

    // Thao tác bảng kết quả
    private JButton btnRefreshResults;
    private JLabel lblStatusMessage;

    // Cơ chế tự động đồng bộ thời gian thực giữa các Client
    private javax.swing.Timer autoSyncTimer;
    private boolean isSyncing = false;

    public MainClientFrame() {
        this.tableModel = new StudentTableModel();
        initUI();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Sinh Viên TCP Client - Đồ Án Lập Trình Mạng PTIT");
        setSize(1000, 720);
        setMinimumSize(new Dimension(880, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // 1. Top Panel: Trạng thái kết nối Server & CSDL
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        JLabel lblServerStatus = new JLabel("● Server: Đã kết nối (" + ClientSocketManager.getInstance().getServerHost() + ":" + ClientSocketManager.getInstance().getServerPort() + ")");
        lblServerStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblServerStatus.setForeground(new Color(40, 167, 69));

        lblDbStatus = new JLabel("● CSDL SQL Server: Chưa kết nối");
        lblDbStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblDbStatus.setForeground(Color.RED);

        statusPanel.add(lblServerStatus);
        statusPanel.add(lblDbStatus);
        topPanel.add(statusPanel, BorderLayout.WEST);

        JButton btnConfigureDb = new JButton("Cấu hình CSDL SQL...");
        btnConfigureDb.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnConfigureDb.setBackground(new Color(0, 123, 255));
        btnConfigureDb.setForeground(Color.WHITE);
        btnConfigureDb.setFocusPainted(false);
        btnConfigureDb.addActionListener(e -> openDBConfigDialog());
        topPanel.add(btnConfigureDb, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. Center Panel: SplitPane (Trái là Nhập liệu + File, Phải là Bảng Kết Quả)
        JPanel inputWrapper = new JPanel(new GridLayout(2, 1, 0, 10));

        // 2.1 Panel Nhập từng dòng sinh viên
        JPanel singleInputPanel = new JPanel(new GridBagLayout());
        singleInputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "1. Nhập Dữ Liệu Từng Sinh Viên",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), new Color(0, 102, 204)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hàng 0: MSSV
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        singleInputPanel.add(new JLabel("Mã Sinh Viên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        txtStudentId = new JTextField();
        singleInputPanel.add(txtStudentId, gbc);

        // Hàng 1: Họ tên
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        singleInputPanel.add(new JLabel("Họ và Tên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        txtFullName = new JTextField();
        singleInputPanel.add(txtFullName, gbc);

        // Hàng 2: Điểm Toán
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        singleInputPanel.add(new JLabel("Điểm Toán (0-10):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        txtMathScore = new JTextField();
        singleInputPanel.add(txtMathScore, gbc);

        // Hàng 3: Điểm Văn
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        singleInputPanel.add(new JLabel("Điểm Văn (0-10):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        txtLiteratureScore = new JTextField();
        singleInputPanel.add(txtLiteratureScore, gbc);

        // Hàng 4: Điểm Tiếng Anh
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        singleInputPanel.add(new JLabel("Điểm Tiếng Anh (0-10):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.7;
        txtEnglishScore = new JTextField();
        singleInputPanel.add(txtEnglishScore, gbc);

        // Hàng 5: Nút Gửi
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        btnSendStudent = new JButton("Gửi Sinh Viên Lên Server");
        btnSendStudent.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSendStudent.setBackground(new Color(40, 167, 69));
        btnSendStudent.setForeground(Color.WHITE);
        btnSendStudent.setFocusPainted(false);
        btnSendStudent.setEnabled(false); // Chỉ bật khi CSDL kết nối thành công!
        btnSendStudent.addActionListener(e -> sendSingleStudent());
        singleInputPanel.add(btnSendStudent, gbc);

        inputWrapper.add(singleInputPanel);

        // 2.2 Panel Đính kèm file mẫu
        JPanel fileInputPanel = new JPanel(new BorderLayout(5, 10));
        fileInputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "2. Đính Kèm File Dữ Liệu Theo Mẫu (.csv / .txt)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), new Color(0, 102, 204)));

        JPanel fileChoosePanel = new JPanel(new BorderLayout(6, 6));
        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        JButton btnBrowseFile = new JButton("Chọn File...");
        btnBrowseFile.addActionListener(e -> chooseFile());
        fileChoosePanel.add(txtFilePath, BorderLayout.CENTER);
        fileChoosePanel.add(btnBrowseFile, BorderLayout.EAST);
        fileInputPanel.add(fileChoosePanel, BorderLayout.NORTH);

        JPanel fileActionPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        btnUploadFile = new JButton("Gửi File Lên Server (Xử lý hàng loạt)");
        btnUploadFile.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnUploadFile.setBackground(new Color(23, 162, 184));
        btnUploadFile.setForeground(Color.WHITE);
        btnUploadFile.setFocusPainted(false);
        btnUploadFile.setEnabled(false); // Chỉ bật khi CSDL kết nối thành công!
        btnUploadFile.addActionListener(e -> uploadBatchFile());

        JButton btnDownloadTemplate = new JButton("Tải File Mẫu Chuẩn (sample_students.csv)");
        btnDownloadTemplate.addActionListener(e -> downloadSampleTemplate());

        fileActionPanel.add(btnUploadFile);
        fileActionPanel.add(btnDownloadTemplate);
        fileInputPanel.add(fileActionPanel, BorderLayout.CENTER);

        inputWrapper.add(fileInputPanel);

        // 2.3 Panel Bảng hiển thị kết quả từ Server
        JPanel tableResultPanel = new JPanel(new BorderLayout(5, 8));
        tableResultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "3. Bảng Kết Quả Tính Điểm Từ Server (Mã SV, Họ Tên, Điểm TB)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), new Color(40, 167, 69)));

        JTable tblResults = new JTable(tableModel);
        tblResults.setRowHeight(25);
        tblResults.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tblResults.getTableHeader().setBackground(new Color(240, 240, 240));
        tableResultPanel.add(new JScrollPane(tblResults), BorderLayout.CENTER);
        tblResults.getTableHeader().setBackground(new Color(240, 240, 240));
        tableResultPanel.add(new JScrollPane(tblResults), BorderLayout.CENTER);

        JPanel tableBottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnRefreshResults = new JButton("Làm Mới Bảng Điểm Từ Server");
        btnRefreshResults.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnRefreshResults.setFocusPainted(false);
        btnRefreshResults.setEnabled(false);
        btnRefreshResults.addActionListener(e -> requestCalculateResults());
        tableBottomBar.add(btnRefreshResults);
        tableResultPanel.add(tableBottomBar, BorderLayout.SOUTH);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputWrapper, tableResultPanel);
        horizontalSplit.setResizeWeight(0.38);
        mainPanel.add(horizontalSplit, BorderLayout.CENTER);

        // 3. Status Bar ở dưới cùng
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        lblStatusMessage = new JLabel("Chào mừng! Vui lòng cấu hình kết nối CSDL SQL Server để mở khóa tính năng nhập liệu.");
        lblStatusMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.add(lblStatusMessage);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public void openDBConfigDialog() {
        ConnectDatabaseDialog dialog = new ConnectDatabaseDialog(this);
        dialog.setVisible(true);
        if (dialog.isDbConnected()) {
            setDatabaseConnected(true);
            requestCalculateResults(); // Đọc dữ liệu hiện có từ Server
        }
    }

    public void setDatabaseConnected(boolean connected) {
        if (connected) {
            lblDbStatus.setText("● CSDL SQL Server: Đã kết nối");
            lblDbStatus.setForeground(new Color(40, 167, 69));
            btnSendStudent.setEnabled(true);
            btnUploadFile.setEnabled(true);
            btnRefreshResults.setEnabled(true);
            lblStatusMessage.setText("Kết nối CSDL thành công! Bạn có thể nhập sinh viên hoặc đính kèm file mẫu.");
            startAutoSync();
        } else {
            lblDbStatus.setText("● CSDL SQL Server: Chưa kết nối");
            lblDbStatus.setForeground(Color.RED);
            btnSendStudent.setEnabled(false);
            btnUploadFile.setEnabled(false);
            btnRefreshResults.setEnabled(false);
            stopAutoSync();
        }
    }

    private void startAutoSync() {
        stopAutoSync();
        autoSyncTimer = new javax.swing.Timer(3500, e -> {
            if (btnRefreshResults.isEnabled() && btnSendStudent.isEnabled() && btnUploadFile.isEnabled() && !isSyncing) {
                isSyncing = true;
                new SwingWorker<List<StudentResultDTO>, Void>() {
                    @Override
                    protected List<StudentResultDTO> doInBackground() {
                        try {
                            synchronized (ClientSocketManager.getInstance()) {
                                ClientSocketManager.getInstance().sendPacket(CommandCode.CMD_CALC_RESULTS_REQ, "{}");
                                ProtocolPacket packet = ClientSocketManager.getInstance().readPacket();
                                if (packet.getCommandCode() == CommandCode.CMD_CALC_RESULTS_RESP) {
                                    return JsonUtil.parseStudentResults(packet.getPayload());
                                }
                            }
                        } catch (Exception ignored) {}
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            List<StudentResultDTO> results = get();
                            if (results != null) {
                                boolean changed = (results.size() != tableModel.getRowCount());
                                if (!changed) {
                                    for (int i = 0; i < results.size(); i++) {
                                        StudentResultDTO cur = tableModel.getStudentAt(i);
                                        StudentResultDTO neu = results.get(i);
                                        if (cur == null || !cur.getStudentId().equals(neu.getStudentId())
                                                || cur.getAverageScore() != neu.getAverageScore()) {
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                if (changed) {
                                    tableModel.setStudents(results);
                                    lblStatusMessage.setText("Đồng bộ thời gian thực: Cập nhật " + results.size() + " sinh viên từ Server.");
                                }
                            }
                        } catch (Exception ignored) {
                        } finally {
                            isSyncing = false;
                        }
                    }
                }.execute();
            }
        });
        autoSyncTimer.start();
    }

    private void stopAutoSync() {
        if (autoSyncTimer != null) {
            autoSyncTimer.stop();
            autoSyncTimer = null;
        }
    }

    private void sendSingleStudent() {
        String id = txtStudentId.getText().trim();
        String name = txtFullName.getText().trim();
        String mathStr = txtMathScore.getText().trim();
        String litStr = txtLiteratureScore.getText().trim();
        String engStr = txtEnglishScore.getText().trim();

        if (id.isEmpty() || name.isEmpty() || mathStr.isEmpty() || litStr.isEmpty() || engStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double math, lit, eng;
        try {
            math = Double.parseDouble(mathStr);
            lit = Double.parseDouble(litStr);
            eng = Double.parseDouble(engStr);
            if (math < 0 || math > 10 || lit < 0 || lit > 10 || eng < 0 || eng > 10) {
                JOptionPane.showMessageDialog(this, "Điểm thi phải nằm trong thang điểm từ 0 đến 10!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Điểm thi phải là giá trị số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StudentDTO student = new StudentDTO(id, name, math, lit, eng);
        String jsonPayload = JsonUtil.toJson(student);

        btnSendStudent.setEnabled(false);
        lblStatusMessage.setText("Đang gửi sinh viên [" + id + "] lên Server...");

        new SwingWorker<Void, Void>() {
            private String errorMsg = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    synchronized (ClientSocketManager.getInstance()) {
                        ClientSocketManager.getInstance().sendPacket(CommandCode.CMD_ADD_STUDENT_REQ, jsonPayload);
                        // Đọc gói tin phản hồi lưu sinh viên
                        ProtocolPacket respPacket = ClientSocketManager.getInstance().readPacket();
                        ServerResponseDTO resp = JsonUtil.parseServerResponse(respPacket.getPayload());
                        if (!resp.isSuccess()) {
                            errorMsg = resp.getMessage();
                            return null;
                        }

                        // Đọc tiếp gói tin danh sách kết quả tính ĐTB mới nhất
                        ProtocolPacket resultsPacket = ClientSocketManager.getInstance().readPacket();
                        if (resultsPacket.getCommandCode() == CommandCode.CMD_CALC_RESULTS_RESP) {
                            List<StudentResultDTO> results = JsonUtil.parseStudentResults(resultsPacket.getPayload());
                            SwingUtilities.invokeLater(() -> tableModel.setStudents(results));
                        }
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnSendStudent.setEnabled(true);
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(MainClientFrame.this, "Lỗi khi gửi sinh viên: " + errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    lblStatusMessage.setText("Gửi sinh viên thất bại: " + errorMsg);
                } else {
                    lblStatusMessage.setText("Đã lưu sinh viên [" + id + "] thành công vào SQL Server (Mã hóa DES)!");
                    // Xóa form nhập
                    txtStudentId.setText("");
                    txtFullName.setText("");
                    txtMathScore.setText("");
                    txtLiteratureScore.setText("");
                    txtEnglishScore.setText("");
                    txtStudentId.requestFocus();
                }
            }
        }.execute();
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Chọn File Mẫu Danh Sách Sinh Viên (.csv, .txt)");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    private void uploadBatchFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một file dữ liệu hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnUploadFile.setEnabled(false);
        lblStatusMessage.setText("Đang đọc và phân tích file: " + selectedFile.getName() + "...");

        new SwingWorker<String, Void>() {
            private String errorMsg = null;

            @Override
            protected String doInBackground() throws Exception {
                try {
                    List<StudentDTO> students = FileParserUtil.parseStudentFile(selectedFile);
                    if (students.isEmpty()) {
                        throw new Exception("File không chứa dòng dữ liệu sinh viên hợp lệ nào!");
                    }

                    BatchStudentDTO batch = new BatchStudentDTO(selectedFile.getName(), students);
                    String jsonPayload = JsonUtil.toJson(batch);

                    synchronized (ClientSocketManager.getInstance()) {
                        ClientSocketManager.getInstance().sendPacket(CommandCode.CMD_UPLOAD_FILE_REQ, jsonPayload);

                        // Đọc phản hồi nhập batch
                        ProtocolPacket respPacket = ClientSocketManager.getInstance().readPacket();
                        ServerResponseDTO resp = JsonUtil.parseServerResponse(respPacket.getPayload());
                        if (!resp.isSuccess()) {
                            errorMsg = resp.getMessage();
                            return null;
                        }

                        // Đọc tiếp kết quả danh sách sinh viên đã tính ĐTB
                        ProtocolPacket resultsPacket = ClientSocketManager.getInstance().readPacket();
                        if (resultsPacket.getCommandCode() == CommandCode.CMD_CALC_RESULTS_RESP) {
                            List<StudentResultDTO> results = JsonUtil.parseStudentResults(resultsPacket.getPayload());
                            SwingUtilities.invokeLater(() -> tableModel.setStudents(results));
                        }
                        return resp.getMessage();
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnUploadFile.setEnabled(true);
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(MainClientFrame.this, "Lỗi upload file: " + errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    lblStatusMessage.setText("Lỗi xử lý file: " + errorMsg);
                } else {
                    JOptionPane.showMessageDialog(MainClientFrame.this, "Gửi file và lưu dữ liệu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    lblStatusMessage.setText("Đã xử lý file thành công!");
                }
            }
        }.execute();
    }

    private void requestCalculateResults() {
        btnRefreshResults.setEnabled(false);
        lblStatusMessage.setText("Đang yêu cầu Server giải mã DES và tính Điểm Trung Bình...");

        new SwingWorker<List<StudentResultDTO>, Void>() {
            private String errorMsg = null;

            @Override
            protected List<StudentResultDTO> doInBackground() throws Exception {
                try {
                    synchronized (ClientSocketManager.getInstance()) {
                        ClientSocketManager.getInstance().sendPacket(CommandCode.CMD_CALC_RESULTS_REQ, "{}");
                        ProtocolPacket packet = ClientSocketManager.getInstance().readPacket();
                        if (packet.getCommandCode() == CommandCode.CMD_CALC_RESULTS_RESP) {
                            return JsonUtil.parseStudentResults(packet.getPayload());
                        } else if (packet.getCommandCode() == CommandCode.CMD_ERROR) {
                            ServerResponseDTO err = JsonUtil.parseServerResponse(packet.getPayload());
                            errorMsg = err.getMessage();
                        }
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnRefreshResults.setEnabled(true);
                if (errorMsg != null) {
                    lblStatusMessage.setText("Không thể lấy kết quả: " + errorMsg);
                } else {
                    try {
                        List<StudentResultDTO> results = get();
                        if (results != null) {
                            tableModel.setStudents(results);
                            lblStatusMessage.setText("Đã nhận kết quả bảng điểm của " + results.size() + " sinh viên từ Server.");
                        }
                    } catch (Exception ignored) {}
                }
            }
        }.execute();
    }

    private void downloadSampleTemplate() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File("sample_students.csv"));
        chooser.setDialogTitle("Lưu File Mẫu CSV");
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(target)) {
                writer.write("MSSV,HoTen,DiemToan,DiemVan,DiemAnh\n");
                writer.write("N24DTCN022,Lu Nhựt Đình,9.0,8.5,9.5\n");
                writer.write("N24DTCN048,Phan Trung Kiên,8.5,8.0,8.5\n");
                writer.write("N24DTCN074,Trần Võ Hữu Tài,8.0,7.5,8.5\n");
                writer.write("N24DTCN086,Đào Trọng Trí,7.5,8.0,7.5\n");
                writer.write("N24DTCN039,Lâm Gia Huy,8.0,8.5,8.0\n");
                writer.write("N24DTCN049,Huỳnh La Tiến Lộc,9.0,8.0,9.0\n");
                JOptionPane.showMessageDialog(this, "Đã lưu file mẫu tại:\n" + target.getAbsolutePath(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu file mẫu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
