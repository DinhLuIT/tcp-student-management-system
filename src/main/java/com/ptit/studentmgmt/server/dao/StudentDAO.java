package com.ptit.studentmgmt.server.dao;

import com.ptit.studentmgmt.common.dto.StudentDTO;
import com.ptit.studentmgmt.common.dto.StudentResultDTO;
import com.ptit.studentmgmt.server.crypto.DESCryptoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private static final Object DB_LOCK = new Object();
    private final DESCryptoService cryptoService;

    public StudentDAO(DESCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * Thêm mới hoặc cập nhật một sinh viên với dữ liệu mã hóa DES
     */
    public void saveStudent(StudentDTO student) throws Exception {
        synchronized (DB_LOCK) {
            Connection conn = DBConnectionManager.getConnection();

            // Kiểm tra xem MSSV đã tồn tại chưa
            String checkSql = "SELECT COUNT(*) FROM [dbo].[Students] WHERE [StudentID] = ?";
            boolean exists = false;
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, student.getStudentId());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }

            // Mã hóa các trường dữ liệu bằng DES
            String encName = cryptoService.encrypt(student.getFullName());
            String encMath = cryptoService.encrypt(String.valueOf(student.getMathScore()));
            String encLit  = cryptoService.encrypt(String.valueOf(student.getLiteratureScore()));
            String encEng  = cryptoService.encrypt(String.valueOf(student.getEnglishScore()));

            if (exists) {
                String updateSql = "UPDATE [dbo].[Students] SET [EncryptedFullName] = ?, [EncryptedMath] = ?, " +
                        "[EncryptedLiterature] = ?, [EncryptedEnglish] = ?, [UpdatedAt] = GETDATE() WHERE [StudentID] = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, encName);
                    ps.setString(2, encMath);
                    ps.setString(3, encLit);
                    ps.setString(4, encEng);
                    ps.setString(5, student.getStudentId());
                    ps.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO [dbo].[Students] ([StudentID], [EncryptedFullName], [EncryptedMath], " +
                        "[EncryptedLiterature], [EncryptedEnglish]) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, student.getStudentId());
                    ps.setString(2, encName);
                    ps.setString(3, encMath);
                    ps.setString(4, encLit);
                    ps.setString(5, encEng);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Lưu danh sách sinh viên theo lô với Transaction an toàn
     */
    public int saveBatchStudents(List<StudentDTO> students) throws Exception {
        if (students == null || students.isEmpty()) return 0;
        synchronized (DB_LOCK) {
            Connection conn = DBConnectionManager.getConnection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            int count = 0;
            try {
                for (StudentDTO s : students) {
                    saveStudent(s);
                    count++;
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
            return count;
        }
    }

    /**
     * Đọc toàn bộ dữ liệu từ CSDL, giải mã DES, tính điểm trung bình và xếp loại
     */
    public List<StudentResultDTO> getAllStudentsWithCalculatedAverage() throws Exception {
        synchronized (DB_LOCK) {
            Connection conn = DBConnectionManager.getConnection();
            List<StudentResultDTO> results = new ArrayList<>();

            String sql = "SELECT [StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish] " +
                         "FROM [dbo].[Students] ORDER BY [StudentID] ASC";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("StudentID");
                    String encName = rs.getString("EncryptedFullName");
                    String encMath = rs.getString("EncryptedMath");
                    String encLit  = rs.getString("EncryptedLiterature");
                    String encEng  = rs.getString("EncryptedEnglish");

                    // Giải mã dữ liệu qua DES
                    String fullName = cryptoService.decrypt(encName);
                    double math = 0.0, lit = 0.0, eng = 0.0;

                    try {
                        math = Double.parseDouble(cryptoService.decrypt(encMath));
                    } catch (Exception ignored) {}

                    try {
                        lit = Double.parseDouble(cryptoService.decrypt(encLit));
                    } catch (Exception ignored) {}

                    try {
                        eng = Double.parseDouble(cryptoService.decrypt(encEng));
                    } catch (Exception ignored) {}

                    // Tính điểm trung bình 3 môn
                    double rawAvg = (math + lit + eng) / 3.0;
                    double avg = Math.round(rawAvg * 100.0) / 100.0;
                    String rank = calculateRank(avg);

                    results.add(new StudentResultDTO(id, fullName, math, lit, eng, avg, rank));
                }
            }
            return results;
        }
    }

    private String calculateRank(double avg) {
        if (avg >= 8.5) return "Xuất sắc";
        if (avg >= 8.0) return "Giỏi";
        if (avg >= 6.5) return "Khá";
        if (avg >= 5.0) return "Trung bình";
        return "Yếu";
    }
}
