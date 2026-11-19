package com.ptit.studentmgmt.server.network;

import com.ptit.studentmgmt.common.dto.BatchStudentDTO;
import com.ptit.studentmgmt.common.dto.DBConfigDTO;
import com.ptit.studentmgmt.common.dto.ServerResponseDTO;
import com.ptit.studentmgmt.common.dto.StudentDTO;
import com.ptit.studentmgmt.common.dto.StudentResultDTO;
import com.ptit.studentmgmt.common.protocol.CommandCode;
import com.ptit.studentmgmt.common.protocol.PacketFramingUtil;
import com.ptit.studentmgmt.common.protocol.ProtocolPacket;
import com.ptit.studentmgmt.common.util.JsonUtil;
import com.ptit.studentmgmt.server.dao.DBConnectionManager;
import com.ptit.studentmgmt.server.dao.StudentDAO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final TcpServer tcpServer;
    private final StudentDAO studentDAO;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isRunning = true;
    private final String clientAddress;

    public ClientHandler(Socket socket, TcpServer server, StudentDAO studentDAO) {
        this.clientSocket = socket;
        this.tcpServer = server;
        this.studentDAO = studentDAO;
        this.clientAddress = socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void run() {
        tcpServer.log(">>> [KẾT NỐI MỚI] Client đã kết nối từ: " + clientAddress);
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());

            while (isRunning && !clientSocket.isClosed()) {
                try {
                    ProtocolPacket packet = PacketFramingUtil.readPacket(dis);
                    handlePacket(packet);
                } catch (EOFException | SocketException e) {
                    // Client ngắt kết nối bình thường
                    break;
                } catch (Exception e) {
                    tcpServer.log("Lỗi xử lý gói tin từ " + clientAddress + ": " + e.getMessage());
                    sendError(500, "Lỗi server khi xử lý gói tin: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            tcpServer.log("Lỗi stream với Client " + clientAddress + ": " + e.getMessage());
        } finally {
            close();
        }
    }

    private void handlePacket(ProtocolPacket packet) throws Exception {
        short cmd = packet.getCommandCode();
        tcpServer.log(String.format("<- [%s] Nhận lệnh: %s (%d bytes)",
                clientAddress, CommandCode.getCommandName(cmd), packet.getPayloadLength()));

        switch (cmd) {
            case CommandCode.CMD_PING:
                PacketFramingUtil.sendPacket(dos, CommandCode.CMD_PING, "PONG");
                break;

            case CommandCode.CMD_DB_CONNECT_REQ:
                handleDBConnect(packet.getPayload());
                break;

            case CommandCode.CMD_ADD_STUDENT_REQ:
                handleAddStudent(packet.getPayload());
                break;

            case CommandCode.CMD_UPLOAD_FILE_REQ:
                handleUploadFile(packet.getPayload());
                break;

            case CommandCode.CMD_CALC_RESULTS_REQ:
                handleCalcResults();
                break;

            default:
                tcpServer.log("Cảnh báo: Nhận mã lệnh chưa hỗ trợ: 0x" + Integer.toHexString(cmd));
                sendError(400, "Mã lệnh không hợp lệ!");
                break;
        }
    }

    private void handleDBConnect(String payload) throws IOException {
        try {
            DBConfigDTO config = JsonUtil.parseDBConfig(payload);
            tcpServer.log(String.format("Đang thử kết nối SQL Server [%s:%d, DB=%s, User=%s]...",
                    config.getHost(), config.getPort(), config.getDatabaseName(), config.getUsername()));

            DBConnectionManager.connect(config);

            tcpServer.log(">>> KẾT NỐI CSDL SQL SERVER THÀNH CÔNG CHO CLIENT: " + clientAddress);
            ServerResponseDTO response = ServerResponseDTO.ok("Kết nối cơ sở dữ liệu SQL Server thành công! Bảng Students đã sẵn sàng.");
            PacketFramingUtil.sendPacket(dos, CommandCode.CMD_DB_CONNECT_RESP, JsonUtil.toJson(response));
        } catch (Exception e) {
            tcpServer.log("Lỗi kết nối CSDL: " + e.getMessage());
            ServerResponseDTO response = ServerResponseDTO.error(500, "Không thể kết nối SQL Server: " + e.getMessage());
            PacketFramingUtil.sendPacket(dos, CommandCode.CMD_DB_CONNECT_RESP, JsonUtil.toJson(response));
        }
    }

    private void handleAddStudent(String payload) throws IOException {
        try {
            if (!DBConnectionManager.isConnected()) {
                sendError(400, "Server chưa kết nối Cơ sở Dữ liệu! Vui lòng cấu hình kết nối CSDL trước.");
                return;
            }

            StudentDTO student = JsonUtil.parseStudent(payload);
            tcpServer.log(String.format("Đang mã hóa DES và lưu sinh viên: %s - %s...", student.getStudentId(), student.getFullName()));

            studentDAO.saveStudent(student);

            tcpServer.log(">>> Đã lưu thành công sinh viên " + student.getStudentId() + " vào CSDL!");
            ServerResponseDTO resp = ServerResponseDTO.ok("Lưu dữ liệu sinh viên thành công!");
            PacketFramingUtil.sendPacket(dos, CommandCode.CMD_ADD_STUDENT_RESP, JsonUtil.toJson(resp));

            // Tự động tính toán và gửi bảng điểm mới nhất về cho Client
            sendUpdatedResults();
        } catch (Exception e) {
            tcpServer.log("Lỗi lưu sinh viên: " + e.getMessage());
            sendError(500, "Lỗi khi lưu sinh viên: " + e.getMessage());
        }
    }

    private void handleUploadFile(String payload) throws IOException {
        try {
            if (!DBConnectionManager.isConnected()) {
                sendError(400, "Server chưa kết nối Cơ sở Dữ liệu! Vui lòng cấu hình kết nối CSDL trước.");
                return;
            }

            BatchStudentDTO batch = JsonUtil.parseBatchStudent(payload);
            tcpServer.log(String.format("Bắt đầu xử lý file [%s] gồm %d sinh viên...", batch.getFileName(), batch.getTotalRecords()));

            int savedCount = studentDAO.saveBatchStudents(batch.getStudents());

            tcpServer.log(String.format(">>> Đã mã hóa DES và lưu thành công %d/%d sinh viên từ file!", savedCount, batch.getTotalRecords()));
            ServerResponseDTO resp = ServerResponseDTO.ok(String.format("Đã nhập thành công %d sinh viên từ file!", savedCount));
            PacketFramingUtil.sendPacket(dos, CommandCode.CMD_UPLOAD_FILE_RESP, JsonUtil.toJson(resp));

            // Tự động tính toán và gửi kết quả về cho Client
            sendUpdatedResults();
        } catch (Exception e) {
            tcpServer.log("Lỗi xử lý file upload: " + e.getMessage());
            sendError(500, "Lỗi nhập dữ liệu từ file: " + e.getMessage());
        }
    }

    public void handleCalcResults() throws IOException {
        try {
            if (!DBConnectionManager.isConnected()) {
                sendError(400, "Server chưa kết nối Cơ sở Dữ liệu!");
                return;
            }

            tcpServer.log("Đang đọc dữ liệu từ SQL Server, giải mã DES và tính Điểm trung bình...");
            sendUpdatedResults();
        } catch (Exception e) {
            tcpServer.log("Lỗi giải mã và tính điểm: " + e.getMessage());
            sendError(500, "Lỗi tính toán: " + e.getMessage());
        }
    }

    public synchronized void sendUpdatedResults() throws Exception {
        List<StudentResultDTO> results = studentDAO.getAllStudentsWithCalculatedAverage();
        String jsonResults = JsonUtil.toJson(results);
        PacketFramingUtil.sendPacket(dos, CommandCode.CMD_CALC_RESULTS_RESP, jsonResults);
        tcpServer.log(String.format("-> [%s] Đã gửi danh sách %d sinh viên kèm Điểm TB về Client.", clientAddress, results.size()));
    }

    private void sendError(int code, String message) {
        try {
            ServerResponseDTO error = ServerResponseDTO.error(code, message);
            PacketFramingUtil.sendPacket(dos, CommandCode.CMD_ERROR, JsonUtil.toJson(error));
        } catch (IOException ignored) {}
    }

    public void close() {
        isRunning = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException ignored) {}
        tcpServer.removeClient(this);
        tcpServer.log("<<< [NGẮT KẾT NỐI] Client đã ngắt kết nối: " + clientAddress);
    }
}
