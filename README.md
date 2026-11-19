# HỆ THỐNG QUẢN LÝ SINH VIÊN QUA TCP SOCKET (JAVA)
## Tích hợp Mã hóa DES, CSDL SQL Server Động và Giao diện Đa luồng Swing

Dự án đồ án môn học **Lập trình mạng** (PTIT). Xây dựng theo mô hình Client – Server hướng kết nối TCP Socket với tính năng bảo mật mật mã học và xử lý luồng đồng thời.

---

## Tính Năng Nổi Bật và Kiến Thức Ứng Dụng

1. **Lập trình Mạng Cốt lõi (Network Programming)**:
   - Mô hình Client - Server Socket TCP với quản lý phiên kết nối liên tục (Persistent TCP Connection).
   - Thiết kế giao thức tầng ứng dụng (**Custom Framing Protocol: Magic + Command + Length + Payload**) giải quyết triệt để vấn đề **Dính gói (Packet Sticking)** và **Cắt gói (Packet Fragmentation)** trong luồng TCP.
   - Server đa luồng sử dụng `ExecutorService` (Thread Pool) tối ưu hóa tài nguyên phần cứng, chịu tải đồng thời nhiều Client.

2. **Mật mã học & Bảo mật (Cryptography & Security)**:
   - Ứng dụng thuật toán đối xứng **DES (Data Encryption Standard)** chế độ `DES/CBC/PKCS5Padding` kết hợp IV (Initialization Vector) 8 bytes.
   - Toàn bộ dữ liệu điểm số và thông tin cá nhân của sinh viên được mã hóa trước khi lưu vào CSDL và chỉ giải mã khi Server thực thi tính toán.
   - Phòng chống tấn công **SQL Injection** với `PreparedStatement`.

3. **Cơ sở Dữ liệu Động (Dynamic Database Connection)**:
   - Server nhận thông số kết nối CSDL (Host, Port, DB Name, User, Password) được Client truyền lên động qua Socket để khởi tạo kết nối JDBC tới Microsoft SQL Server.
   - Cơ chế tự động khởi tạo bảng (`Auto-migration`) nếu bảng `Students` chưa tồn tại.

4. **Kỹ thuật Đồ họa & Đa luồng Client (Swing & Concurrency)**:
   - Áp dụng `SwingWorker` cho 100% tác vụ mạng và đọc tập tin, đảm bảo giao diện không bao giờ bị đơ (Non-blocking UI).
   - Giao diện hiện đại, trực quan với thư viện **FlatLaf**.
   - Hỗ trợ nhập liệu từng dòng và đính kèm file mẫu (`.csv` / `.txt`) gửi hàng loạt.
   - Bảng kết quả tự động hiển thị Họ tên, MSSV, Điểm Toán, Văn, Anh, Điểm trung bình và Xếp loại.

---

## Tài Liệu Thiết Kế Chi Tiết (Docs)

Hệ thống tài liệu đầy đủ đã được soạn thảo chi tiết trong thư mục `docs/`:

1. [01. Đặc tả Yêu cầu Phần mềm (SRS)](docs/01_TONG_QUAN_VA_YEU_CAU_SRS.md)
2. [02. Thiết kế Kiến trúc Hệ thống & Biểu đồ Tuần tự](docs/02_THIET_KE_KIEN_TRUC_HE_THONG.md)
3. [03. Đặc tả Giao thức Mạng Tầng Ứng dụng & Chống Dính Gói](docs/03_THIET_KE_GIAO_THUC_MANG_PROTOCOL.md)
4. [04. Thiết kế Cơ sở Dữ liệu & Mã hóa DES](docs/04_THIET_KE_CO_SO_DU_LIEU_VA_MA_HOA_DES.md)
5. [05. Hướng dẫn Triển khai, Cấu trúc Code & Tiêu chí Điểm Tối đa](docs/05_HUONG_DAN_TRIEN_KHAI_VA_QUY_CHUAN_CODE.md)

---

## Hướng Dẫn Bắt Đầu Nhanh

### 1. Chuẩn bị môi trường & Cơ sở dữ liệu
- **Java Development Kit (JDK)**: Phiên bản 17 trở lên.
- **Cơ sở dữ liệu (Dành cho MacBook / macOS / Linux)**:
  Chạy SQL Server qua Docker Compose với 1 lệnh duy nhất (tự động tạo Database, bảng Students và nạp dữ liệu mẫu):
  ```bash
  ./scripts/docker-sql.sh start
  # Hoặc: docker-compose up -d
  ```
  *(Thông số kết nối: Host `localhost`, Port `1433`, DB `StudentManagementDB`, User `sa`, Pass `Admin@123456`)*
- **Cơ sở dữ liệu (Dành cho Windows)**: Chạy file `sql/01_create_database_and_tables.sql` trong SQL Server Management Studio (SSMS).

### 2. File mẫu dữ liệu
Sử dụng file mẫu [sample_students.csv](sample_students.csv) để thử nghiệm tính năng đính kèm file.

### 3. Quy trình chạy kiểm thử
1. Khởi động **Server App** (`ServerApp.java`).
2. Khởi động **Client App** (`ClientApp.java`).
3. Trên Client:
   - Nhập `IP: 127.0.0.1`, `Port: 8888` -> Bấm **Kết nối**.
   - Nhập thông số CSDL SQL Server -> Gửi lên Server để kiểm tra kết nối.
   - Sau khi kết nối CSDL thành công: Nhập thủ công sinh viên hoặc đính kèm `sample_students.csv`.
   - Trên Server: Bấm nút **"Đọc & Giải mã dữ liệu CSDL"** để Server giải mã DES, tính Điểm Trung Bình và trả danh sách về cho Client hiển thị lên bảng.
