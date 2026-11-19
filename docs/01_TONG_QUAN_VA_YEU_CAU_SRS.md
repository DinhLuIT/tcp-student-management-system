# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)
## Đề tài: Hệ thống Quản lý Sinh viên Client - Server qua TCP Socket tích hợp Mã hóa DES và SQL Server

---

### 1. Giới thiệu tổng quan
Tài liệu này xác định các yêu cầu chức năng, phi chức năng và kiến trúc kỹ thuật cho đồ án môn **Lập trình mạng (Java Socket TCP)**. Mục tiêu của dự án là xây dựng một ứng dụng phân tán hoàn chỉnh theo mô hình **Client - Server**, áp dụng các kỹ thuật cốt lõi:
- Lập trình mạng hướng kết nối tin cậy (TCP Socket, ServerSocket).
- Kiến trúc đa luồng (Multi-threading, Thread Pool Concurrency).
- Thiết kế giao thức tầng ứng dụng (Application-level Protocol) giải quyết vấn đề dính gói/cắt gói (Packet Sticking & Fragmentation).
- Cơ chế mã hóa đối xứng **DES (Data Encryption Standard)** bảo vệ dữ liệu nhạy cảm trước khi lưu trữ vào CSDL.
- Kết nối CSDL quan hệ động (Dynamic JDBC Connection) với SQL Server.
- Xử lý truyền file dữ liệu mẫu (CSV/Excel) qua Socket.
- Xây dựng giao diện người dùng trực quan (Java Swing / FlatLaf) mượt mà, không giật lag nhờ tách biệt luồng UI (EDT) và luồng Network (Worker Thread).

---

### 2. Các tác nhân trong hệ thống (Actors)
1. **Người dùng Client (Sinh viên / Giảng viên / Quản trị viên)**:
   - Thao tác trên giao diện Client.
   - Nhập thông số cấu hình Server (IP, Port).
   - Nhập thông số kết nối CSDL (Host, Port, DB Name, User, Password).
   - Nhập thông tin từng sinh viên hoặc tải lên file dữ liệu mẫu.
   - Xem kết quả điểm trung bình và trạng thái xử lý do Server gửi về.
2. **Quản trị viên Server (Server Operator)**:
   - Khởi động, dừng Server Socket.
   - Theo dõi log kết nối của các Client theo thời gian thực (IP, Port, thời điểm kết nối, lịch sử gửi nhận).
   - Cung cấp giao diện để Server đọc, giải mã dữ liệu DES trong CSDL, kích hoạt tính toán điểm trung bình.

---

### 3. Yêu cầu chức năng chi tiết (Functional Requirements)

#### 3.1. Phân hệ Client
* **REQ-CLI-01: Kết nối Socket Server**
  - Cho phép người dùng nhập Địa chỉ IP (hoặc Domain) và Port của Server.
  - Có cơ chế kiểm tra tính hợp lệ dữ liệu đầu vào (Validation: Port từ 1024 - 65535, IP format).
  - Thiết lập thời gian timeout kết nối (Connection Timeout: 5000ms).
  - Nếu kết nối thất bại: Hiển thị hộp thoại lỗi chi tiết và cho phép người dùng nhập lại.
  - Nếu thành công: Chuyển sang màn hình xác thực thông số CSDL, duy trì kết nối TCP Socket liên tục (Persistent TCP Connection).

* **REQ-CLI-02: Cấu hình và Kiểm tra Kết nối CSDL qua Server**
  - Giao diện nhập thông số: Database Host, Port, Database Name, Username, Password.
  - Đóng gói thông tin gửi lên Server qua TCP Socket.
  - Nhận phản hồi trạng thái từ Server:
    - Nếu Server kết nối CSDL thành công: Mở khóa các tính năng nhập liệu sinh viên.
    - Nếu thất bại: Hiển thị mã lỗi cụ thể từ Server (sai tài khoản, DB không tồn tại, v.v.).

* **REQ-CLI-03: Nhập liệu Sinh viên từng dòng (Single Entry)**
  - Form nhập liệu gồm: Mã sinh viên (MSSV), Họ tên, Điểm Toán (0-10), Điểm Văn (0-10), Điểm Tiếng Anh (0-10).
  - Client-side validation: Kiểm tra điểm số hợp lệ, họ tên không được trống, MSSV đúng định dạng.
  - Gửi dữ liệu dòng lên Server qua giao thức đã định nghĩa.

* **REQ-CLI-04: Đính kèm và Gửi file dữ liệu mẫu (Batch File Upload)**
  - Hỗ trợ định dạng file mẫu chuẩn: File `.csv` hoặc `.txt` (hoặc `.xlsx` mở rộng).
  - Cho phép chọn file qua `JFileChooser`.
  - Có nút tải về file mẫu (Download Template File) cho người dùng.
  - Đọc và stream file lên Server (hoặc phân tích cú pháp gửi theo batch packet).

* **REQ-CLI-05: Hiển thị kết quả điểm trung bình**
  - Bảng hiển thị danh sách (`JTable`) trực quan gồm: STT, Mã sinh viên, Họ và tên, Điểm Toán, Điểm Văn, Điểm Tiếng Anh, Điểm Trung Bình (ĐTB = (Toán + Văn + Anh)/3, làm tròn 2 chữ số thập phân), Xếp loại học lực.
  - Tự động cập nhật ngay khi Server gửi dữ liệu kết quả về.

---

#### 3.2. Phân hệ Server
* **REQ-SRV-01: Quản lý Lắng nghe và Đa kết nối (Multi-threading)**
  - Khởi tạo `ServerSocket` lắng nghe trên cổng được cấu hình (mặc định e.g., `8888`).
  - Sử dụng mô hình `ExecutorService` (Thread Pool) để tiếp nhận và phục vụ đồng thời nhiều Client mà không gây nghẽn kết nối.
  - Quản lý vòng đời kết nối (Session Management), tự động giải phóng tài nguyên khi Client ngắt kết nối đột ngột hoặc ngắt hợp lệ.

* **REQ-SRV-02: Kết nối CSDL Động (Dynamic Database Connection)**
  - Nhận gói tin cấu hình CSDL từ Client.
  - Khởi tạo `DriverManager` kết nối đến SQL Server dựa trên các tham số động.
  - Phản hồi mã trạng thái kết quả (Thành công: 200, Thất bại: 500 kèm thông điệp lỗi) về cho Client.
  - Tự động tạo bảng (Auto-create table) `STUDENTS` nếu bảng chưa tồn tại trong CSDL.

* **REQ-SRV-03: Mã hóa dữ liệu bằng thuật toán DES và Lưu trữ**
  - Nhận dữ liệu sinh viên từ Client (đơn lẻ hoặc theo lô).
  - Sử dụng thuật toán đối xứng **DES** (chuẩn `DES/CBC/PKCS5Padding` hoặc `DES/ECB/PKCS5Padding`) với SecretKey bí mật quản lý trên Server.
  - Mã hóa các trường dữ liệu nhạy cảm (Họ tên, điểm số hoặc toàn bộ bản ghi JSON) thành chuỗi Ciphertext Base64.
  - Thực thi câu lệnh `PreparedStatement` để lưu dữ liệu đã mã hóa vào SQL Server nhằm bảo mật dữ liệu và chống tấn công SQL Injection.

* **REQ-SRV-04: Giao diện Server & Giải mã, Tính toán Điểm trung bình**
  - Giao diện Server có nút điều khiển hoặc chức năng: "Đọc & Giải mã dữ liệu từ CSDL".
  - Đọc Ciphertext từ SQL Server, dùng SecretKey của DES để giải mã ngược về Plaintext.
  - Tính điểm trung bình của từng sinh viên:
    $$\text{ĐTB} = \frac{\text{Toán} + \text{Văn} + \text{Anh}}{3}$$
  - Đóng gói danh sách kết quả đã giải mã kèm Điểm trung bình và gửi về cho Client qua Socket.

---

### 4. Yêu cầu phi chức năng (Non-Functional Requirements)

1. **Tính tin cậy & Toàn vẹn (Reliability & Integrity)**:
   - Toàn bộ liên lạc sử dụng giao thức truyền vận TCP đảm bảo không mất gói tin, đúng thứ tự.
   - Triển khai cơ chế đóng gói Message Framing với Header chứa Payload Length cố định để ngăn ngừa hoàn toàn hiện tượng dính gói (Packet Sticking) khi mạng gửi liên tiếp.
2. **Tính an toàn & Bảo mật (Security)**:
   - Dữ liệu điểm số và thông tin sinh viên được lưu trữ trong CSDL dưới dạng mã hóa DES. Ngay cả khi bị truy cập trái phép vào DB, dữ liệu vẫn không thể bị đọc trực tiếp.
   - Cơ chế SQL Injection Prevention bằng `PreparedStatement`.
3. **Hiệu năng & Đồng thời (Performance & Concurrency)**:
   - Server đáp ứng tối thiểu 50-100 kết nối đồng thời với Thread Pool mà không tăng vọt mức tiêu thụ CPU/RAM.
   - Thời gian xử lý gói tin mã hóa/giải mã và phản hồi dưới 100ms trong môi trường mạng nội bộ (LAN/Localhost).
4. **Trải nghiệm người dùng (Usability & Responsiveness)**:
   - Giao diện Client sử dụng `SwingWorker` cho mọi tác vụ I/O mạng và đọc file. Tuyệt đối không để ứng dụng bị treo hoặc hiển thị trạng thái "Not Responding".
   - Báo lỗi rõ ràng bằng tiếng Việt có dấu, thân thiện với người dùng.
5. **Khả năng mở rộng & Bảo trì (Maintainability)**:
   - Phân tách theo kiến trúc 3 lớp rõ ràng (Presentation - Business/Network - Data Access).
   - Mã nguồn tuân theo chuẩn đặt tên Java (CamelCase, PascalCase), Clean Code, có JavaDoc đầy đủ.
