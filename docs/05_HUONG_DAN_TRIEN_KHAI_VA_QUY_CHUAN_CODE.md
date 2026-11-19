# HƯỚNG DẪN TRIỂN KHAI, CẤU TRÚC DỰ ÁN VÀ QUY CHUẨN CODE

---

### 1. Cấu trúc Thư mục Dự án Chuẩn (Maven Standard Directory Layout)

Để áp dụng các kiến thức chuẩn công nghiệp, dự án nên được tổ chức theo chuẩn **Maven** với cấu trúc package module hóa rõ ràng:

```
project-cuoi-ky/
│
├── pom.xml                                  # Cấu hình dependency Maven
├── sample_students.csv                      # File dữ liệu mẫu đính kèm
├── docs/                                    # Toàn bộ tài liệu thiết kế & kỹ thuật
│   ├── 01_TONG_QUAN_VA_YEU_CAU_SRS.md
│   ├── 02_THIET_KE_KIEN_TRUC_HE_THONG.md
│   ├── 03_THIET_KE_GIAO_THUC_MANG_PROTOCOL.md
│   ├── 04_THIET_KE_CO_SO_DU_LIEU_VA_MA_HOA_DES.md
│   └── 05_HUONG_DAN_TRIEN_KHAI_VA_QUY_CHUAN_CODE.md
│
└── src/
    ├── main/
    │   ├── java/com/ptit/studentmgmt/
    │   │   ├── common/                      # Dùng chung cho cả Client và Server
    │   │   │   ├── dto/                     # Data Transfer Objects
    │   │   │   │   ├── StudentDTO.java
    │   │   │   │   ├── StudentResultDTO.java
    │   │   │   │   └── DBConfigDTO.java
    │   │   │   ├── protocol/                # Giao thức mạng tùy biến
    │   │   │   │   ├── CommandCode.java
    │   │   │   │   ├── ProtocolPacket.java
    │   │   │   │   └── PacketFramingUtil.java
    │   │   │   └── util/
    │   │   │       ├── JsonUtil.java
    │   │   │       └── FileParserUtil.java
    │   │   │
    │   │   ├── client/                      # Phân hệ Client
    │   │   │   ├── ClientApp.java           # Entry point khởi chạy Client
    │   │   │   ├── network/
    │   │   │   │   ├── ClientSocketManager.java
    │   │   │   │   └── NetworkWorker.java
    │   │   │   └── ui/
    │   │   │       ├── ConnectServerDialog.java
    │   │   │       ├── ConnectDatabaseDialog.java
    │   │   │       ├── StudentTableModel.java
    │   │   │       └── MainClientFrame.java
    │   │   │
    │   │   └── server/                      # Phân hệ Server
    │   │       ├── ServerApp.java           # Entry point khởi chạy Server
    │   │       ├── network/
    │   │       │   ├── TcpServer.java
    │   │       │   └── ClientHandler.java
    │   │       ├── crypto/
    │   │       │   └── DESCryptoService.java
    │   │       ├── dao/
    │   │       │   ├── DBConnectionManager.java
    │   │       │   └── StudentDAO.java
    │   │       └── ui/
    │   │           └── ServerDashboardFrame.java
    │   │
    │   └── resources/                       # Tài nguyên cấu hình, icons, mẫu
    │       └── icons/
    └── test/
        └── java/com/ptit/studentmgmt/       # Unit Tests (JUnit 5)
            ├── crypto/DESCryptoServiceTest.java
            └── protocol/PacketFramingUtilTest.java
```

---

### 2. Cấu hình Thư viện Phụ thuộc (`pom.xml`)

Sử dụng các thư viện chuẩn, hiện đại để nâng tầm đồ án:
- `mssql-jdbc`: Driver kết nối SQL Server chính thức từ Microsoft.
- `jackson-databind`: Xử lý phân tích JSON hiệu năng cao cho giao thức mạng.
- `flatlaf`: Thư viện giao diện Swing hiện đại (FlatLaf Light/Dark) giúp UI đẹp mắt như ứng dụng macOS/Windows 11.
- `junit-jupiter`: Kiểm thử đơn vị thuật toán DES và giao thức mạng.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ptit.studentmgmt</groupId>
    <artifactId>student-management-socket</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Microsoft SQL Server JDBC Driver -->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <version>12.4.2.jre11</version>
        </dependency>

        <!-- Jackson Databind for JSON Protocol Serialization -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>

        <!-- FlatLaf - Modern Look and Feel for Swing UI -->
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.2.1</version>
        </dependency>

        <!-- JUnit 5 for Unit Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

### 3. Phân chia Nhiệm vụ Nhóm (Team Work Breakdown Structure)

| Thành viên | Vai trò chính | Nhiệm vụ cụ thể | Tiêu chuẩn bàn giao (Deliverables) |
| :--- | :--- | :--- | :--- |
| **Thành viên 1** | **Backend / Network Lead** | - Xây dựng `TcpServer` đa luồng với `ThreadPoolExecutor`.<br>- Thiết kế `ClientHandler` quản lý session.<br>- Hiện thực thuật toán chống dính/cắt gói `PacketFramingUtil`. | Server Socket lắng nghe ổn định, xử lý song song nhiều kết nối không bị treo, test được bằng script. |
| **Thành viên 2** | **Security & Database Specialist** | - Cài đặt module `DESCryptoService` (mã hóa/giải mã DES Base64).<br>- Viết `DBConnectionManager` kết nối động SQL Server.<br>- Viết `StudentDAO` dùng `PreparedStatement`. | Unit test DES mã hóa/giải mã chính xác 100%; Lưu và đọc được dữ liệu bảo mật từ SQL Server. |
| **Thành viên 3** | **Frontend Client & I/O Engineer** | - Thiết kế giao diện Swing với `FlatLaf` (đẹp, hiện đại).<br>- Tạo form nhập liệu, bảng kết quả `JTable`.<br>- Xây dựng bộ đọc/kiểm tra file `.csv` mẫu.<br>- Triển khai `SwingWorker` gọi mạng bất đồng bộ. | Giao diện Client thao tác mượt mà, không bao giờ bị đơ; Upload file và hiển thị kết quả chính xác. |
| **Thành viên 4** *(Nếu nhóm 4 người)* | **Dashboard, QA & Document Lead** | - Xây dựng `ServerDashboardFrame` hiển thị real-time logs.<br>- Viết kịch bản kiểm thử tải, kiểm thử ngoại lệ (nhập sai IP, sai mật khẩu DB).<br>- Hoàn thiện Slide thuyết trình và Báo cáo đồ án. | Dashboard Server trực quan; Bộ tài liệu báo cáo đầy đủ sơ đồ và video demo hoạt động. |

---

### 4. Định dạng Mẫu File Dữ liệu Đính kèm (`sample_students.csv`)

Chuẩn bị sẵn file dữ liệu mẫu cho người dùng tải về hoặc thử nghiệm đính kèm:

```csv
MSSV,HoTen,DiemToan,DiemVan,DiemAnh
N24DTCN022,Lu Nhựt Đình,9.0,8.5,9.5
N24DTCN048,Phan Trung Kiên,8.5,8.0,8.5
N24DTCN074,Trần Võ Hữu Tài,8.0,7.5,8.5
N24DTCN086,Đào Trọng Trí,7.5,8.0,7.5
N24DTCN039,Lâm Gia Huy,8.0,8.5,8.0
N24DTCN049,Huỳnh La Tiến Lộc,9.0,8.0,9.0
```

---

### 5. Checklist Đạt Điểm Tuyệt Đối Khi Bảo Vệ Đồ Án

- [x] **Không bao giờ đơ UI**: Mọi kết nối mạng và I/O file trên Client đều chạy qua `SwingWorker` (Giảng viên thường thử ngắt mạng hoặc nhập IP không tồn tại để xem app có bị treo "Not Responding" không).
- [x] **Xử lý dính gói / cắt gói chuẩn**: Sử dụng cơ chế Length-Prefixed Framing thay vì `split(",")` đơn giản.
- [x] **Bảo mật dữ liệu thật sự**: Mở trực tiếp SQL Server Management Studio (SSMS) cho Giảng viên thấy bảng `Students` lưu dữ liệu toàn chuỗi mã hóa DES (Base64), không lộ thông tin gốc.
- [x] **Chống SQL Injection**: 100% các câu lệnh truy vấn đều sử dụng `PreparedStatement`.
- [x] **Đa kết nối đồng thời**: Mở cùng lúc 2 - 3 cửa sổ Client, gửi dữ liệu cùng một lúc, Server vẫn tiếp nhận và xử lý song song nhờ Thread Pool.
- [x] **Giao diện hiện đại**: Áp dụng FlatLaf giúp giao diện có giao diện bo tròn, font chữ rõ nét, màu sắc hài hòa thay vì giao diện Swing Java mặc định từ thập niên 90.
