package com.ptit.studentmgmt.common.protocol;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class ProtocolPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    private short magic;
    private short commandCode;
    private int payloadLength;
    private String payload;

    public ProtocolPacket() {
        this.magic = CommandCode.MAGIC;
        this.payload = "";
        this.payloadLength = 0;
    }

    public ProtocolPacket(short commandCode, String payload) {
        this.magic = CommandCode.MAGIC;
        this.commandCode = commandCode;
        this.payload = payload != null ? payload : "";
        this.payloadLength = this.payload.getBytes(StandardCharsets.UTF_8).length;
    }

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public short getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(short commandCode) {
        this.commandCode = commandCode;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload != null ? payload : "";
        this.payloadLength = this.payload.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public String toString() {
        return "ProtocolPacket{" +
                "cmd=" + CommandCode.getCommandName(commandCode) +
                ", length=" + payloadLength +
                ", payload='" + (payload.length() > 100 ? payload.substring(0, 100) + "..." : payload) + '\'' +
                '}';
    }
}
