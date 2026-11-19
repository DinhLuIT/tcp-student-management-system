package com.ptit.studentmgmt;

import com.ptit.studentmgmt.client.network.ClientSocketManager;
import com.ptit.studentmgmt.common.protocol.CommandCode;
import com.ptit.studentmgmt.common.protocol.ProtocolPacket;
import com.ptit.studentmgmt.server.network.TcpServer;

public class IntegrationTest {
    public static void main(String[] args) throws Exception {
        int testPort = 18888;
        TcpServer server = new TcpServer();
        server.start(testPort);
        System.out.println("1. Server đã khởi động trên port " + testPort);

        Thread.sleep(500);

        ClientSocketManager client = ClientSocketManager.getInstance();
        client.connect("127.0.0.1", testPort, 3000);
        System.out.println("2. Client đã kết nối TCP thành công!");

        // Gửi lệnh PING
        client.sendPacket(CommandCode.CMD_PING, "PING_TEST");
        ProtocolPacket pong = client.readPacket();
        assert pong.getCommandCode() == CommandCode.CMD_PING : "Phải nhận được CMD_PING";
        System.out.println("3. Heartbeat PING-PONG phản hồi thành công: " + pong.getPayload());

        client.disconnect();
        server.stop();
        System.out.println(">>> [PASS] Integration Test kết nối TCP Socket Client - Server hoạt động hoàn hảo!");
    }
}
