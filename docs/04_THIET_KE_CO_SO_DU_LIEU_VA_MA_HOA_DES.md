# THIẾT KẾ CƠ SỞ DỮ LIỆU VÀ THUẬT TOÁN MÃ HÓA DES

---

### 1. Kiến trúc Bảo mật & Mã hóa DES (Data Encryption Standard)

#### 1.1. Tổng quan về Thuật toán DES
- **DES (Data Encryption Standard)** là thuật toán mã hóa khóa đối xứng (Symmetric-key block cipher) do IBM phát triển và được NIST công nhận.
- **Kích thước khối (Block size)**: 64 bits (8 bytes).
- **Độ dài khóa (Key length)**: 64 bits (trong đó 56 bits thực tế dùng cho mã hóa, 8 bits dùng kiểm tra chẵn lẻ Parity).
- **Chế độ hoạt động (Cipher Modes)**:
  - **ECB (Electronic Codebook)**: Mã hóa từng khối độc lập. Dễ triển khai, tuy nhiên nếu 2 khối plaintext giống nhau sẽ sinh ra 2 khối ciphertext giống nhau.
  - **CBC (Cipher Block Chaining - Khuyên dùng)**: Mỗi khối trước khi mã hóa được XOR với khối ciphertext trước đó kết hợp cùng **Vector khởi tạo IV (Initialization Vector - 8 bytes)**. Đảm bảo dữ liệu trùng lặp không sinh ra ciphertext trùng lặp.
- **Cơ chế đệm (Padding)**: `PKCS5Padding` để bù các khối dữ liệu chưa đủ 8 bytes.

#### 1.2. Sơ đồ Mã hóa và Lưu trữ vào Cơ sở dữ liệu

```
[Dữ liệu Sinh viên từ Client]
 (Họ tên, Điểm Toán, Văn, Anh)
              |
              v
     +-----------------+
     |  DES Encryptor  | <--- [Secret Key 56-bit] + [IV 8-byte]
     +-----------------+
              |
              v
       [Cipher Bytes]
              |
              v
     +-----------------+
     | Base64 Encoding |
     +-----------------+
              |
              v (Chuỗi văn bản an toàn)
 [INSERT INTO Students...] via PreparedStatement
              |
              v
    +-------------------+
    |    SQL Server     |
    | (Encrypted Table) |
    +-------------------+
              |
     (Khi có lệnh đọc dữ liệu)
              v
 [SELECT * FROM Students...]
              |
              v
     +-----------------+
     | Base64 Decoding |
     +-----------------+
              |
              v
     +-----------------+
     |  DES Decryptor  | <--- [Secret Key 56-bit] + [IV 8-byte]
     +-----------------+
              |
              v
[Dữ liệu gốc: Plaintext]
              |
              v
 [Tính ĐTB = (T + V + A)/3]
              |
              v
    [Gửi về cho Client]
```

---

### 2. Thiết kế Lược đồ Cơ sở dữ liệu (Database Schema)

#### 2.1. Script Tự động Tạo Bảng SQL Server
Bảng `Students` được thiết kế để lưu trữ `StudentID` dạng chuỗi rõ ràng (Plaintext) phục vụ cho việc lập chỉ mục (Indexing) và tìm kiếm, trong khi các trường thông tin cá nhân và điểm số nhạy cảm được lưu dưới dạng chuỗi đã mã hóa DES (Base64 String):

```sql
-- Tạo Database nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'StudentManagementDB')
BEGIN
    CREATE DATABASE StudentManagementDB;
END
GO

USE StudentManagementDB;
GO

-- Tạo bảng Students
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Students')
BEGIN
    CREATE TABLE Students (
        StudentID VARCHAR(50) NOT NULL PRIMARY KEY,            -- Mã sinh viên (Khóa chính)
        EncryptedFullName NVARCHAR(255) NOT NULL,             -- Họ tên (Ciphertext Base64)
        EncryptedMath NVARCHAR(100) NOT NULL,                 -- Điểm Toán (Ciphertext Base64)
        EncryptedLiterature NVARCHAR(100) NOT NULL,           -- Điểm Văn (Ciphertext Base64)
        EncryptedEnglish NVARCHAR(100) NOT NULL,              -- Điểm Tiếng Anh (Ciphertext Base64)
        CreatedAt DATETIME DEFAULT GETDATE(),                 -- Thời điểm ghi nhận
        UpdatedAt DATETIME DEFAULT GETDATE()
    );
END
GO
```

> [!NOTE]
> **Giải pháp thay thế (Encrypted Payload)**: Ta cũng có thể lưu toàn bộ đối tượng JSON sinh viên `{ "name": "...", "math": 8.5, ... }` thành một cột duy nhất `EncryptedPayload NVARCHAR(MAX)`. Cách tiếp cận theo từng cột ở trên thể hiện tính tỉ mỉ và cấu trúc chuẩn quan hệ hơn.

---

### 3. Cài đặt Chi tiết Module Mã hóa DES (`DESCryptoService.java`)

Dưới đây là mã nguồn chuẩn hóa sử dụng thư viện mật mã `javax.crypto` có sẵn của Java:

```java
package com.ptit.studentmgmt.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class DESCryptoService {
    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";
    
    // Khóa bí mật 8 bytes (64-bit) và IV cố định trong hệ thống Server
    // Trong thực tế, khóa này được load từ file cấu hình bảo mật hoặc biến môi trường
    private static final byte[] FIXED_KEY_BYTES = "P@ssw0rd".getBytes(StandardCharsets.UTF_8); // Đúng 8 bytes
    private static final byte[] FIXED_IV_BYTES  = "12345678".getBytes(StandardCharsets.UTF_8); // Đúng 8 bytes
    
    private final SecretKey secretKey;
    private final IvParameterSpec ivSpec;

    public DESCryptoService() {
        this.secretKey = new SecretKeySpec(FIXED_KEY_BYTES, ALGORITHM);
        this.ivSpec = new IvParameterSpec(FIXED_IV_BYTES);
    }

    /**
     * Mã hóa chuỗi Plaintext thành Ciphertext định dạng Base64
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null) return null;
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Giải mã chuỗi Base64 Ciphertext về chuỗi Plaintext ban đầu
     */
    public String decrypt(String base64CipherText) throws Exception {
        if (base64CipherText == null) return null;
        byte[] cipherBytes = Base64.getDecoder().decode(base64CipherText);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
```

---

### 4. Cài đặt Tầng Truy cập Dữ liệu (`StudentDAO.java`)

#### 4.1. Kết nối Động với SQL Server qua JDBC
```java
package com.ptit.studentmgmt.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionManager {
    private static Connection currentConnection = null;

    public static synchronized Connection connect(String host, int port, String dbName, String user, String pass) throws SQLException, ClassNotFoundException {
        // Nạp Driver SQL Server
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        // Tạo URL kết nối JDBC (Bật trustServerCertificate=true để tránh lỗi SSL Self-Signed Certificate)
        String jdbcUrl = String.format(
            "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
            host, port, dbName
        );
        
        // Đóng connection cũ nếu có
        if (currentConnection != null && !currentConnection.isClosed()) {
            currentConnection.close();
        }
        
        currentConnection = DriverManager.getConnection(jdbcUrl, user, pass);
        return currentConnection;
    }

    public static synchronized Connection getConnection() {
        return currentConnection;
    }
}
```

#### 4.2. Thao tác Lưu và Đọc Dữ liệu Mã hóa
```java
public class StudentDAO {
    private final DESCryptoService cryptoService;

    public StudentDAO(DESCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    // Lưu sinh viên với dữ liệu mã hóa DES
    public void saveStudent(StudentDTO student) throws Exception {
        String sql = "INSERT INTO Students (StudentID, EncryptedFullName, EncryptedMath, EncryptedLiterature, EncryptedEnglish) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, student.getStudentId());
            ps.setString(2, cryptoService.encrypt(student.getFullName()));
            ps.setString(3, cryptoService.encrypt(String.valueOf(student.getMathScore())));
            ps.setString(4, cryptoService.encrypt(String.valueOf(student.getLiteratureScore())));
            ps.setString(5, cryptoService.encrypt(String.valueOf(student.getEnglishScore())));
            
            ps.executeUpdate();
        }
    }

    // Đọc tất cả sinh viên, giải mã DES và trả về danh sách đối tượng Plaintext
    public List<StudentResultDTO> getAllStudentsDecrypted() throws Exception {
        List<StudentResultDTO> list = new ArrayList<>();
        String sql = "SELECT StudentID, EncryptedFullName, EncryptedMath, EncryptedLiterature, EncryptedEnglish FROM Students";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String id = rs.getString("StudentID");
                String name = cryptoService.decrypt(rs.getString("EncryptedFullName"));
                double math = Double.parseDouble(cryptoService.decrypt(rs.getString("EncryptedMath")));
                double lit = Double.parseDouble(cryptoService.decrypt(rs.getString("EncryptedLiterature")));
                double eng = Double.parseDouble(cryptoService.decrypt(rs.getString("EncryptedEnglish")));
                
                double avg = Math.round(((math + lit + eng) / 3.0) * 100.0) / 100.0;
                String rank = calculateRank(avg);
                
                list.add(new StudentResultDTO(id, name, math, lit, eng, avg, rank));
            }
        }
        return list;
    }

    private String calculateRank(double avg) {
        if (avg >= 8.5) return "Xuất sắc";
        if (avg >= 8.0) return "Giỏi";
        if (avg >= 6.5) return "Khá";
        if (avg >= 5.0) return "Trung bình";
        return "Yếu";
    }
}
```
