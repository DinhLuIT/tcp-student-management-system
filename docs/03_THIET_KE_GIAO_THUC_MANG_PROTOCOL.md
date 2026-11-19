# ĐẶC TẢ GIAO THỨC TẦNG ỨNG DỤNG (APPLICATION PROTOCOL SPECIFICATION)

---

### 1. Vấn đề của TCP Stream & Giải pháp Message Framing

Trong lập trình mạng TCP Socket:
- **TCP là giao thức hướng luồng (Stream-oriented)**, không có khái niệm "ranh giới thông điệp" (Message Boundary).
- Khi Client gửi 2 gói tin liên tiếp, Server có thể nhận cả 2 gói tin gộp lại trong 1 lần đọc (`read()`) -> **Hiện tượng Dính gói (Packet Sticking)**.
- Khi dữ liệu gửi đi có dung lượng lớn (như file mẫu danh sách sinh viên), dữ liệu có thể bị chia nhỏ thành nhiều mẩu TCP segment -> **Hiện tượng Cắt gói (Packet Fragmentation)**.

**Giải pháp**: Xây dựng một giao thức đóng gói tùy biến (**Custom Framing Protocol: Length-Prefixed Frame**).

---

### 2. Cấu trúc Khung Gói Tin (Packet Structure)

Mỗi thông điệp truyền qua Socket giữa Client và Server đều tuân theo định dạng nhị phân sau:

```
+---------------+---------------+--------------------+-------------------------+
| MAGIC (2B)    | CMD_CODE (2B) | PAYLOAD_LEN (4B)   | PAYLOAD (UTF-8 JSON)    |
| (0xCAFE)      | (Mã lệnh)     | (Độ dài dữ liệu)   | (Nội dung dữ liệu N B)  |
+---------------+---------------+--------------------+-------------------------+
|<----------------- HEADER (8 Bytes) --------------->|<---- BODY (N Bytes) ---->|
```

#### Chi tiết các trường:
1. **MAGIC (2 Bytes - Short)**: Giá trị cố định `0xCAFE`. Dùng để xác thực tính hợp lệ của giao thức, phát hiện các kết nối rác hoặc stream bị lệch offset.
2. **CMD_CODE (2 Bytes - Short)**: Mã lệnh điều khiển định danh tác vụ nghiệp vụ.
3. **PAYLOAD_LEN (4 Bytes - Int)**: Độ dài tính theo byte của phần thân (Payload). Giúp bên nhận biết chính xác cần đọc bao nhiêu byte nữa để gom đủ 1 gói tin hoàn chỉnh.
4. **PAYLOAD (N Bytes)**: Nội dung dữ liệu được định dạng bằng chuỗi UTF-8 JSON (sử dụng thư viện Jackson hoặc Gson) hoặc dữ liệu nhị phân.

---

### 3. Bảng Danh mục Mã Lệnh (Command Codes)

| Command Name | Mã HEX | Hướng truyền | Mô tả chức năng |
| :--- | :--- | :--- | :--- |
| `CMD_PING` | `0x0001` | Client <-> Server | Kiểm tra tính thông suốt của đường truyền (Heartbeat). |
| `CMD_DB_CONNECT_REQ` | `0x0010` | Client -> Server | Gửi thông số kết nối CSDL (Host, Port, DB, User, Pass). |
| `CMD_DB_CONNECT_RESP` | `0x0011` | Server -> Client | Phản hồi kết quả kết nối CSDL (Thành công / Thất bại). |
| `CMD_ADD_STUDENT_REQ` | `0x0020` | Client -> Server | Gửi thông tin 1 sinh viên (MSSV, Họ tên, Điểm). |
| `CMD_ADD_STUDENT_RESP`| `0x0021` | Server -> Client | Phản hồi kết quả lưu sinh viên (Thành công / Mã lỗi). |
| `CMD_UPLOAD_FILE_REQ` | `0x0030` | Client -> Server | Gửi dữ liệu tập tin danh sách sinh viên mẫu. |
| `CMD_UPLOAD_FILE_RESP`| `0x0031` | Server -> Client | Phản hồi kết quả xử lý nhập file hàng loạt. |
| `CMD_CALC_RESULTS_REQ`| `0x0040` | Client/Server UI | Yêu cầu giải mã dữ liệu và tính điểm trung bình. |
| `CMD_CALC_RESULTS_RESP`|`0x0041` | Server -> Client | Trả về danh sách sinh viên đã tính ĐTB để hiển thị lên bảng. |
| `CMD_ERROR` | `0x0099` | Server -> Client | Báo lỗi hệ thống chung (Malformed Packet, Internal Error). |

---

### 4. Chi tiết Cấu trúc Payload (JSON Schemas)

#### 4.1. Gói tin `CMD_DB_CONNECT_REQ` (0x0010)
```json
{
  "host": "localhost",
  "port": 1433,
  "databaseName": "StudentDB",
  "username": "sa",
  "password": "SecretPassword123"
}
```

#### 4.2. Gói tin `CMD_DB_CONNECT_RESP` (0x0011)
```json
{
  "statusCode": 200,
  "message": "Kết nối cơ sở dữ liệu SQL Server thành công! Bảng Students đã sẵn sàng.",
  "success": true
}
```
*(Nếu thất bại: `statusCode: 500`, `success: false`, `message: "Login failed for user 'sa'"`)*

#### 4.3. Gói tin `CMD_ADD_STUDENT_REQ` (0x0020)
```json
{
  "studentId": "N24DTCN022",
  "fullName": "Lu Nhựt Đình",
  "mathScore": 9.0,
  "literatureScore": 8.5,
  "englishScore": 9.5
}
```

#### 4.4. Gói tin `CMD_UPLOAD_FILE_REQ` (0x0030)
```json
{
  "fileName": "danh_sach_sinh_vien.csv",
  "totalRecords": 3,
  "students": [
    { "studentId": "N24DTCN022", "fullName": "Lu Nhựt Đình", "mathScore": 9.0, "literatureScore": 8.5, "englishScore": 9.5 },
    { "studentId": "N24DTCN048", "fullName": "Phan Trung Kiên", "mathScore": 8.5, "literatureScore": 8.0, "englishScore": 8.5 },
    { "studentId": "N24DTCN074", "fullName": "Trần Võ Hữu Tài", "mathScore": 8.0, "literatureScore": 7.5, "englishScore": 8.5 }
  ]
}
```

#### 4.5. Gói tin `CMD_CALC_RESULTS_RESP` (0x0041)
```json
{
  "statusCode": 200,
  "students": [
    {
      "studentId": "N24DTCN022",
      "fullName": "Lu Nhựt Đình",
      "mathScore": 9.0,
      "literatureScore": 8.5,
      "englishScore": 9.5,
      "averageScore": 9.0,
      "academicRank": "Xuất sắc"
    },
    {
      "studentId": "N24DTCN048",
      "fullName": "Phan Trung Kiên",
      "mathScore": 8.5,
      "literatureScore": 8.0,
      "englishScore": 8.5,
      "averageScore": 8.33,
      "academicRank": "Giỏi"
    }
  ]
}
```

---

### 5. Thuật toán Đóng gói và Giải gói (Framing Algorithm Implementation)

#### 5.1. Thuật toán Gửi gói tin (`sendPacket`)
```java
public synchronized void sendPacket(DataOutputStream dos, short cmdCode, String jsonPayload) throws IOException {
    byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
    
    // Ghi Header 8 bytes
    dos.writeShort(0xCAFE);              // 2 bytes Magic
    dos.writeShort(cmdCode);             // 2 bytes Command Code
    dos.writeInt(payloadBytes.length);   // 4 bytes Payload Length
    
    // Ghi Body
    dos.write(payloadBytes);             // N bytes Payload
    dos.flush();
}
```

#### 5.2. Thuật toán Đọc gói tin chống Dính gói / Cắt gói (`readPacket`)
```java
public ProtocolPacket readPacket(DataInputStream dis) throws IOException {
    // 1. Đọc Magic (2 bytes)
    short magic = dis.readShort();
    if (magic != (short) 0xCAFE) {
        throw new IOException("Sai Magic Header! Giao thức không hợp lệ: " + Integer.toHexString(magic));
    }
    
    // 2. Đọc Command Code (2 bytes)
    short cmdCode = dis.readShort();
    
    // 3. Đọc Payload Length (4 bytes)
    int payloadLength = dis.readInt();
    if (payloadLength < 0 || payloadLength > 10 * 1024 * 1024) { // Giới hạn tối đa 10MB
        throw new IOException("Kích thước Payload không hợp lệ: " + payloadLength);
    }
    
    // 4. Đọc CHÍNH XÁC payloadLength bytes (dùng readFully để chống cắt gói)
    byte[] payloadBytes = new byte[payloadLength];
    dis.readFully(payloadBytes); // readFully sẽ block cho đến khi nhận đủ N bytes!
    
    String jsonPayload = new String(payloadBytes, StandardCharsets.UTF_8);
    return new ProtocolPacket(cmdCode, jsonPayload);
}
```

> [!IMPORTANT]
> **Điểm mấu chốt kỹ thuật**: Phương thức `dis.readFully(payloadBytes)` trong Java `DataInputStream` được thiết kế đặc biệt để giải quyết hiện tượng cắt gói (TCP Fragmentation). Nó đảm bảo chỉ trả về khi đã đọc đủ số byte yêu cầu, thay vì trả về ngay khi chỉ nhận được một phần dữ liệu như hàm `read()` thông thường.
