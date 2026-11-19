package com.ptit.studentmgmt.server.network;

import com.ptit.studentmgmt.server.crypto.DESCryptoService;
import com.ptit.studentmgmt.server.dao.StudentDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TcpServer {
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final List<ClientHandler> activeClients;
    private final DESCryptoService cryptoService;
    private final StudentDAO studentDAO;
    private boolean isRunning = false;
    private Consumer<String> logListener;

    public TcpServer() {
        this.threadPool = Executors.newFixedThreadPool(50);
        this.activeClients = new CopyOnWriteArrayList<>();
        this.cryptoService = new DESCryptoService();
        this.studentDAO = new StudentDAO(this.cryptoService);
    }

    public void setLogListener(Consumer<String> listener) {
        this.logListener = listener;
    }

    public synchronized void start(int port) throws IOException {
        if (isRunning) return;

        serverSocket = new ServerSocket(port);
        isRunning = true;
        log("=================================================");
        log(">>> TCP SERVER ĐÃ KHỞI ĐỘNG THÀNH CÔNG TRÊN CỔNG: " + port);
        log(">>> Luồng bảo mật DES và ThreadPool 50 workers đã sẵn sàng!");
        log("=================================================");

        // Chạy vòng lặp lắng nghe kết nối trên Thread riêng
        new Thread(() -> {
            while (isRunning && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this, studentDAO);
                    activeClients.add(handler);
                    threadPool.submit(handler);
                } catch (IOException e) {
                    if (isRunning) {
                        log("Lỗi tiếp nhận kết nối ServerSocket: " + e.getMessage());
                    }
                }
            }
        }, "TcpServer-Acceptor").start();
    }

    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;

        log("Đang dừng Server và đóng các kết nối Client...");
        for (ClientHandler handler : activeClients) {
            handler.close();
        }
        activeClients.clear();

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }

        log(">>> TCP Server đã dừng hoàn toàn!");
    }

    public void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
    }

    public int getActiveClientCount() {
        return activeClients.size();
    }

    public StudentDAO getStudentDAO() {
        return studentDAO;
    }

    public DESCryptoService getCryptoService() {
        return cryptoService;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Kích hoạt từ giao diện Server: Đọc dữ liệu CSDL, giải mã DES, tính Điểm TB và phát cho tất cả Client
     */
    public void triggerDecryptionAndBroadcast() {
        log(">>> [SERVER ACTION] Kích hoạt giải mã dữ liệu CSDL và gửi bảng điểm cho toàn bộ Client...");
        for (ClientHandler handler : activeClients) {
            try {
                handler.sendUpdatedResults();
            } catch (Exception e) {
                log("Lỗi gửi kết quả cho Client: " + e.getMessage());
            }
        }
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String formatted = String.format("[%s] %s", timestamp, message);
        System.out.println(formatted);
        if (logListener != null) {
            logListener.accept(formatted);
        }
    }
}
