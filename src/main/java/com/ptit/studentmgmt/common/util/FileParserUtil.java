package com.ptit.studentmgmt.common.util;

import com.ptit.studentmgmt.common.dto.StudentDTO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FileParserUtil {
    private FileParserUtil() {}

    /**
     * Đọc và phân tích file CSV / TXT danh sách sinh viên.
     * Cấu trúc mỗi dòng mong đợi: MSSV, Họ tên, Điểm Toán, Điểm Văn, Điểm Tiếng Anh
     */
    public static List<StudentDTO> parseStudentFile(File file) throws Exception {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File không tồn tại hoặc đường dẫn không hợp lệ: " + file.getAbsolutePath());
        }

        List<StudentDTO> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Lọc bỏ ký tự BOM UTF-8 (\uFEFF) nếu có ở đầu file (thường xuất hiện khi lưu file CSV từ Excel trên Windows)
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1).trim();
                }

                // Bỏ qua dòng trống hoặc comment
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Nhận diện phân cách bằng dấu phẩy, chấm phẩy hoặc tab
                String[] parts;
                if (line.contains(",")) {
                    parts = line.split(",");
                } else if (line.contains(";")) {
                    parts = line.split(";");
                } else {
                    parts = line.split("\t");
                }

                if (parts.length < 5) {
                    continue; // Bỏ qua dòng không đủ 5 cột
                }

                String mssv = stripQuotes(parts[0].trim());
                String hoTen = stripQuotes(parts[1].trim());

                // Bỏ qua dòng tiêu đề (header)
                if (lineNumber <= 2 && (mssv.equalsIgnoreCase("MSSV") || mssv.equalsIgnoreCase("studentId") 
                        || mssv.equalsIgnoreCase("Mã SV") || mssv.equalsIgnoreCase("MaSinhVien"))) {
                    continue;
                }

                try {
                    double toan = Double.parseDouble(stripQuotes(parts[2].trim()));
                    double van = Double.parseDouble(stripQuotes(parts[3].trim()));
                    double anh = Double.parseDouble(stripQuotes(parts[4].trim()));

                    if (toan < 0 || toan > 10 || van < 0 || van > 10 || anh < 0 || anh > 10) {
                        System.err.println("Cảnh báo dòng " + lineNumber + ": Điểm số nằm ngoài thang 0-10 (" + line + ")");
                        continue;
                    }

                    list.add(new StudentDTO(mssv, hoTen, toan, van, anh));
                } catch (NumberFormatException e) {
                    // Nếu là dòng tiêu đề nhưng không nhận diện được ở trên
                    if (lineNumber <= 2) continue;
                    System.err.println("Lỗi parse điểm số dòng " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return list;
    }

    private static String stripQuotes(String s) {
        if (s != null && s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }
}
