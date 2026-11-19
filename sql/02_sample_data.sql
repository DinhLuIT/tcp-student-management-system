-- ==================================================================================
-- HỆ THỐNG QUẢN LÝ SINH VIÊN QUA TCP SOCKET - ĐỒ ÁN LẬP TRÌNH MẠNG PTIT
-- Script: Chèn Dữ liệu Mẫu Đã Mã hóa DES (Chuẩn thuật toán DES/CBC/PKCS5Padding)
-- Lớp: D24TXCN01-N - Học viện Công nghệ Bưu chính Viễn thông (PTIT HCM)
-- Khóa đối xứng: 'P@ssw0rd' (8 bytes), IV: '12345678' (8 bytes)
-- ==================================================================================

USE [StudentManagementDB];
GO

-- 1. Sinh viên: Lu Nhựt Đình (N24DTCN022) - Điểm: Toán 9.0, Văn 8.5, Anh 9.5 -> ĐTB: 9.00 (Xuất sắc)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN022')
BEGIN
    INSERT INTO [dbo].[Students] 
        ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish])
    VALUES 
        ('N24DTCN022', N'KzFDpNJ/qSII/dqsoqps7QDCIncJ78je', '/ZIvw2Z+YMQ=', '9kppVywjqiM=', '6C5JX7wrb1o=');
    PRINT N'>>> Đã chèn dữ liệu mẫu cho sinh viên N24DTCN022 - Lu Nhựt Đình';
END
GO

-- 2. Sinh viên: Phan Trung Kiên (N24DTCN048) - Điểm: Toán 8.5, Văn 8.0, Anh 8.5 -> ĐTB: 8.33 (Giỏi)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN048')
BEGIN
    INSERT INTO [dbo].[Students] 
        ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish])
    VALUES 
        ('N24DTCN048', N'Q3lZygk/NvbtFfEzXLgHXxJczohHZz/Y', '9kppVywjqiM=', 'pVHRpyXg8pI=', '9kppVywjqiM=');
    PRINT N'>>> Đã chèn dữ liệu mẫu cho sinh viên N24DTCN048 - Phan Trung Kiên';
END
GO

-- 3. Sinh viên: Trần Võ Hữu Tài (N24DTCN074) - Điểm: Toán 8.0, Văn 7.5, Anh 8.5 -> ĐTB: 8.00 (Giỏi)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN074')
BEGIN
    INSERT INTO [dbo].[Students] 
        ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish])
    VALUES 
        ('N24DTCN074', N'9Y6l/aN6NrBZ0t3+ZKxffnO9IRa7Oaq0', 'pVHRpyXg8pI=', 'Jqw1OoPeWks=', '9kppVywjqiM=');
    PRINT N'>>> Đã chèn dữ liệu mẫu cho sinh viên N24DTCN074 - Trần Võ Hữu Tài';
END
GO

-- 4. Sinh viên: Đào Trọng Trí (N24DTCN086) - Điểm: Toán 7.5, Văn 8.0, Anh 7.5 -> ĐTB: 7.67 (Khá)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN086')
BEGIN
    INSERT INTO [dbo].[Students] 
        ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish])
    VALUES 
        ('N24DTCN086', N'XWqatUqCTbvdQdKgpeqyzbkSSYBp10Cc', 'Jqw1OoPeWks=', 'pVHRpyXg8pI=', 'Jqw1OoPeWks=');
    PRINT N'>>> Đã chèn dữ liệu mẫu cho sinh viên N24DTCN086 - Đào Trọng Trí';
END
GO

-- 5. Sinh viên: Lâm Gia Huy (N24DTCN039) - Điểm: Toán 8.0, Văn 8.5, Anh 8.0 -> ĐTB: 8.17 (Giỏi)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN039')
BEGIN
    INSERT INTO [dbo].[Students] 
        ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish])
    VALUES 
        ('N24DTCN039', N'7vh4DlqsBguovqFG4JLcAQ==', 'pVHRpyXg8pI=', '9kppVywjqiM=', 'pVHRpyXg8pI=');
    PRINT N'>>> Đã chèn dữ liệu mẫu cho sinh viên N24DTCN039 - Lâm Gia Huy';
END
GO

-- 6. Sinh viên: Huỳnh La Tiến Lộc (N24DTCN049) - Điểm: Toán 9.0, Văn 8.0, Anh 9.0 -> ĐTB: 8.67 (Xuất sắc)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Students] WHERE [StudentID] = 'N24DTCN049')
BEGIN
    INSERT INTO [dbo].[Students] 
        ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish])
    VALUES 
        ('N24DTCN049', N'nlEHfOQLyyD/b16krM4mjsAqWUpWGs9/', '/ZIvw2Z+YMQ=', 'pVHRpyXg8pI=', '/ZIvw2Z+YMQ=');
    PRINT N'>>> Đã chèn dữ liệu mẫu cho sinh viên N24DTCN049 - Huỳnh La Tiến Lộc';
END
GO
