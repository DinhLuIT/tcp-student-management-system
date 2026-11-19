package com.ptit.studentmgmt.common.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class PacketFramingUtil {
    private static final int MAX_PAYLOAD_SIZE = 15 * 1024 * 1024; // 15 Megabytes

    private PacketFramingUtil() {}

    /**
     * Gửi gói tin theo chuẩn Framing:
     * [2 bytes Magic] + [2 bytes CommandCode] + [4 bytes Length] + [N bytes Payload UTF-8]
     */
    public static synchronized void sendPacket(DataOutputStream dos, short commandCode, String payload) throws IOException {
        if (payload == null) {
            payload = "";
        }
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        // Header: 8 bytes
        dos.writeShort(CommandCode.MAGIC);
        dos.writeShort(commandCode);
        dos.writeInt(payloadBytes.length);

        // Body: N bytes
        if (payloadBytes.length > 0) {
            dos.write(payloadBytes);
        }
        dos.flush();
    }

    /**
     * Đọc gói tin từ DataInputStream đảm bảo chống hiện tượng Dính gói và Cắt gói
     */
    public static ProtocolPacket readPacket(DataInputStream dis) throws IOException {
        // 1. Đọc Magic 2 bytes
        short magic = dis.readShort();
        if (magic != CommandCode.MAGIC) {
            throw new IOException("Lỗi Framing: Magic header không khớp! Nhận: 0x" + Integer.toHexString(magic & 0xFFFF));
        }

        // 2. Đọc Command Code 2 bytes
        short cmdCode = dis.readShort();

        // 3. Đọc Payload Length 4 bytes
        int payloadLength = dis.readInt();
        if (payloadLength < 0 || payloadLength > MAX_PAYLOAD_SIZE) {
            throw new IOException("Lỗi Framing: Kích thước Payload vượt quá giới hạn cho phép (" + payloadLength + " bytes)");
        }

        // 4. Đọc CHÍNH XÁC payloadLength bytes bằng readFully (Blocking đến khi gom đủ N bytes)
        byte[] payloadBytes = new byte[payloadLength];
        if (payloadLength > 0) {
            dis.readFully(payloadBytes);
        }

        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        return new ProtocolPacket(cmdCode, payload);
    }
}
