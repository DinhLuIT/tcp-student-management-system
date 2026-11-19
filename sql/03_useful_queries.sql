-- ==================================================================================
-- HỆ THỐNG QUẢN LÝ SINH VIÊN QUA TCP SOCKET - ĐỒ ÁN LẬP TRÌNH MẠNG PTIT
-- Script: Các câu lệnh truy vấn phục vụ kiểm thử và báo cáo đồ án
-- ==================================================================================

USE [StudentManagementDB];
GO

-- 1. Xem toàn bộ dữ liệu đang lưu trong bảng (Minh chứng dữ liệu đã được mã hóa DES)
-- Giảng viên khi chấm sẽ thấy các cột Họ tên và Điểm số đều là chuỗi mã hóa bí mật Base64!
SELECT 
    [StudentID] AS [Mã SV (Khóa chính)],
    [EncryptedFullName] AS [Họ Tên (Đã mã hóa DES)],
    [EncryptedMath] AS [Điểm Toán (Mã hóa DES)],
    [EncryptedLiterature] AS [Điểm Văn (Mã hóa DES)],
    [EncryptedEnglish] AS [Điểm TA (Mã hóa DES)],
    [CreatedAt] AS [Ngày Tạo]
FROM [dbo].[Students];
GO

-- 2. Đếm tổng số lượng sinh viên đã lưu
SELECT COUNT(*) AS [Tổng số sinh viên] FROM [dbo].[Students];
GO

-- 3. Tìm kiếm sinh viên theo Mã sinh viên (Ví dụ: Lu Nhựt Đình)
SELECT * FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN022';
GO

-- 4. Xóa toàn bộ dữ liệu để test lại từ đầu
-- TRUNCATE TABLE [dbo].[Students];
-- GO
