package com.ptit.studentmgmt.protocol;

import com.ptit.studentmgmt.common.protocol.CommandCode;
import com.ptit.studentmgmt.common.protocol.PacketFramingUtil;
import com.ptit.studentmgmt.common.protocol.ProtocolPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PacketFramingUtilTest {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        short expectedCmd = CommandCode.CMD_ADD_STUDENT_REQ;
        String expectedPayload = "{\"studentId\":\"B22DCCN001\",\"fullName\":\"Trần Văn Bảo\"}";

        // Gửi gói tin
        PacketFramingUtil.sendPacket(dos, expectedCmd, expectedPayload);

        // Đọc lại từ byte array mô phỏng mạng TCP
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);

        ProtocolPacket received = PacketFramingUtil.readPacket(dis);

        assert received.getMagic() == CommandCode.MAGIC : "Magic Header phải là 0xCAFE";
        assert received.getCommandCode() == expectedCmd : "Command Code phải khớp";
        assert expectedPayload.equals(received.getPayload()) : "Payload phải toàn vẹn 100%";

        System.out.println(">>> [PASS] Test PacketFramingUtil thành công! Giao thức đóng gói hoạt động chuẩn xác.");
    }
}
