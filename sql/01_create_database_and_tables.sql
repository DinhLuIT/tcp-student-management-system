-- ==================================================================================
-- HỆ THỐNG QUẢN LÝ SINH VIÊN QUA TCP SOCKET - ĐỒ ÁN LẬP TRÌNH MẠNG PTIT
-- Script: Khởi tạo Cơ sở Dữ liệu và Bảng lưu trữ mã hóa DES
-- Hệ quản trị CSDL mục tiêu: Microsoft SQL Server (2014, 2017, 2019, 2022 hoặc Express)
-- ==================================================================================

-- 1. Tạo Cơ sở Dữ liệu (Nếu chưa tồn tại)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'StudentManagementDB')
BEGIN
    CREATE DATABASE [StudentManagementDB];
    PRINT N'>>> Cơ sở dữ liệu [StudentManagementDB] đã được tạo thành công!';
END
ELSE
BEGIN
    PRINT N'>>> Cơ sở dữ liệu [StudentManagementDB] đã tồn tại.';
END
GO

USE [StudentManagementDB];
GO

-- 2. Tạo Bảng Students để lưu trữ dữ liệu đã mã hóa DES
-- Chú ý: 
-- - StudentID được lưu ở dạng văn bản thô (Plaintext) làm khóa chính phục vụ định danh & indexing
-- - EncryptedFullName, EncryptedMath, EncryptedLiterature, EncryptedEnglish: Lưu chuỗi Ciphertext đã mã hóa bằng thuật toán DES và encode Base64.
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'Students')
BEGIN
    CREATE TABLE [dbo].[Students] (
        [StudentID]            VARCHAR(50)    NOT NULL,
        [EncryptedFullName]    NVARCHAR(500)  NOT NULL,
        [EncryptedMath]        VARCHAR(200)   NOT NULL,
        [EncryptedLiterature]  VARCHAR(200)   NOT NULL,
        [EncryptedEnglish]     VARCHAR(200)   NOT NULL,
        [CreatedAt]            DATETIME       DEFAULT GETDATE(),
        [UpdatedAt]            DATETIME       DEFAULT GETDATE(),
        
        CONSTRAINT [PK_Students] PRIMARY KEY CLUSTERED ([StudentID] ASC)
    );
    
    PRINT N'>>> Bảng [Students] đã được khởi tạo thành công với cấu trúc mã hóa DES!';
END
ELSE
BEGIN
    PRINT N'>>> Bảng [Students] đã tồn tại sẵn trong CSDL.';
END
GO

-- 3. Tạo chỉ mục (Index) hỗ trợ tăng tốc độ truy vấn
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_Students_CreatedAt' AND object_id = OBJECT_ID(N'dbo.Students'))
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Students_CreatedAt] ON [dbo].[Students] ([CreatedAt] DESC);
    PRINT N'>>> Đã tạo chỉ mục [IX_Students_CreatedAt] thành công!';
END
GO
