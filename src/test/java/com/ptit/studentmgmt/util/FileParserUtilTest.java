package com.ptit.studentmgmt.util;

import com.ptit.studentmgmt.common.dto.StudentDTO;
import com.ptit.studentmgmt.common.util.FileParserUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileParserUtilTest {
    public static void main(String[] args) throws Exception {
        // Tạo file tạm có ký tự UTF-8 BOM (\uFEFF) mô phỏng xuất từ Microsoft Excel
        File tempFile = File.createTempFile("test_excel_bom_", ".csv");
        tempFile.deleteOnExit();

        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String csvContent = "MSSV,HoTen,DiemToan,DiemVan,DiemAnh\n" +
                "\"N24DTCN022\",\"Lu Nhựt Đình\",9.0,8.5,9.5\n" +
                "N24DTCN048,Phan Trung Kiên,8.5,8.0,8.5\n";

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bom);
            fos.write(csvContent.getBytes(StandardCharsets.UTF_8));
        }

        List<StudentDTO> students = FileParserUtil.parseStudentFile(tempFile);

        assert students.size() == 2 : "Phải đọc được chính xác 2 sinh viên!";
        assert "N24DTCN022".equals(students.get(0).getStudentId()) : "MSSV không được dính ký tự lạ BOM hay dấu ngoặc kép!";
        assert "Lu Nhựt Đình".equals(students.get(0).getFullName()) : "Họ tên phải lọc sạch dấu ngoặc kép!";
        assert students.get(0).getMathScore() == 9.0 : "Điểm Toán phải là 9.0";

        System.out.println(">>> [PASS] Test FileParserUtilTest thành công! Đã lọc sạch BOM UTF-8 và ngoặc kép Excel.");
    }
}
