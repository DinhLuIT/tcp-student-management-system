package com.ptit.studentmgmt.client.network;

import com.ptit.studentmgmt.common.protocol.PacketFramingUtil;
import com.ptit.studentmgmt.common.protocol.ProtocolPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketManager {
    private static ClientSocketManager instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String serverHost;
    private int serverPort;

    private ClientSocketManager() {}

    public static synchronized ClientSocketManager getInstance() {
        if (instance == null) {
            instance = new ClientSocketManager();
        }
        return instance;
    }

    /**
     * Kết nối tới Server với Timeout (mặc định 5000ms)
     */
    public synchronized void connect(String host, int port, int timeoutMs) throws IOException {
        disconnect();

        this.serverHost = host;
        this.serverPort = port;
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(host, port), timeoutMs > 0 ? timeoutMs : 5000);

        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public synchronized void sendPacket(short commandCode, String payload) throws IOException {
        if (!isConnected()) {
            throw new IOException("Chưa kết nối đến Server!");
        }
        PacketFramingUtil.sendPacket(dos, commandCode, payload);
    }

    public synchronized ProtocolPacket readPacket() throws IOException {
        if (!isConnected()) {
            throw new IOException("Chưa kết nối đến Server!");
        }
        return PacketFramingUtil.readPacket(dis);
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void disconnect() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {
        } finally {
            socket = null;
            dis = null;
            dos = null;
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }
}
