# HƯỚNG DẪN CÀI ĐẶT VÀ CẤU HÌNH CƠ SỞ DỮ LIỆU SQL SERVER

Tài liệu này hướng dẫn chi tiết cách chuẩn bị Cơ sở dữ liệu Microsoft SQL Server để nộp kèm mã nguồn cho Giảng viên và chạy nghiệm thu đồ án.

---

### 1. Chuẩn bị CSDL SQL Server

#### Cách 1: Sử dụng Docker Compose (Khuyên dùng cho MacBook / macOS & Linux)
Dự án đã tích hợp sẵn [docker-compose.yml](../docker-compose.yml) sử dụng `azure-sql-edge` (Native ARM64 cho chip Apple Silicon M1/M2/M3/M4 và Intel). 

Chỉ cần 1 lệnh duy nhất tại thư mục dự án:
```bash
./scripts/docker-sql.sh start
# Hoặc: docker-compose up -d
```

> **Lưu ý**: Container `sql-init` đi kèm sẽ tự động chạy script tạo Database `StudentManagementDB`, tạo bảng `Students` và nạp dữ liệu mẫu mã hóa DES. Bạn không cần phải mở công cụ ngoài để gõ bất kỳ lệnh SQL nào.

- Xem log SQL Server: `./scripts/docker-sql.sh logs`
- Truy vấn kiểm tra dữ liệu từ terminal: `./scripts/docker-sql.sh query`
- Dừng SQL Server khi dùng xong: `./scripts/docker-sql.sh stop`

---

#### Cách 2: Sử dụng SQL Server đã cài trên máy tính (Khuyên dùng trên Windows)
1. Mở SQL Server Management Studio (SSMS).
2. Kết nối vào Server của bạn (ví dụ: `.` hoặc `localhost` hoặc `SQLEXPRESS`).
3. Mở file [01_create_database_and_tables.sql](01_create_database_and_tables.sql) và bấm **Execute (F5)**.
4. Mở tiếp file [02_sample_data.sql](02_sample_data.sql) và bấm **Execute (F5)**.

---

### 2. Các Thiết Lập Quan Trọng Trên Windows (Nếu dùng SSMS)

1. **Bật chế độ SQL Server Authentication**:
   - Trong SSMS: Chuột phải vào tên Server -> **Properties** -> **Security** -> Chọn **SQL Server and Windows Authentication mode**.
2. **Kích hoạt tài khoản `sa` và đặt mật khẩu**:
   - SSMS -> **Security** -> **Logins** -> Chuột phải vào tài khoản `sa` -> **Properties**.
   - Đặt Password là `Admin@123456`.
   - Vào thẻ **Status** -> Chọn **Permission: Grant** và **Login: Enabled**.
3. **Kích hoạt giao thức TCP/IP trên cổng 1433**:
   - Mở **SQL Server Configuration Manager** -> **Protocols for SQLEXPRESS**.
   - Bật **TCP/IP = Enabled** và cấu hình cổng `1433`.

---

### 3. Thông số Nhập Trên Giao Diện Client

Khi ứng dụng Client mở hộp thoại cấu hình CSDL gửi lên Server, nhập các thông số:
- **Database Host**: `localhost` (hoặc `127.0.0.1`)
- **Port**: `1433`
- **Database Name**: `StudentManagementDB`
- **Username**: `sa`
- **Password**: `Admin@123456`
