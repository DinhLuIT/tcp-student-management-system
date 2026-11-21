package com.ptit.studentmgmt.server.dao;

import com.ptit.studentmgmt.common.dto.DBConfigDTO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBConnectionManager {
    private static Connection connection = null;
    private static DBConfigDTO lastConfig = null;

    private DBConnectionManager() {}

    /**
     * Khởi tạo kết nối JDBC động tới Microsoft SQL Server
     */
    public static synchronized Connection connect(DBConfigDTO config) throws SQLException, ClassNotFoundException {
        // Nạp Driver SQL Server
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // Đóng kết nối cũ nếu đang mở
        close();

        String host = config.getHost() != null && !config.getHost().isEmpty() ? config.getHost() : "localhost";
        int port = config.getPort() > 0 ? config.getPort() : 1433;
        String dbName = config.getDatabaseName() != null && !config.getDatabaseName().isEmpty() ? config.getDatabaseName() : "StudentManagementDB";
        String user = config.getUsername() != null ? config.getUsername() : "sa";
        String pass = config.getPassword() != null ? config.getPassword() : "";

        // Kiểm tra tính hợp lệ của dbName để chống SQL Injection
        if (!dbName.matches("^[a-zA-Z0-9_]+$")) {
            throw new SQLException("Tên Database không hợp lệ! Chỉ cho phép ký tự chữ, số và dấu gạch dưới.");
        }

        // URL chuẩn hỗ trợ SSL Self-signed và timeout 5 giây
        String jdbcUrl = String.format(
                "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
                host, port, dbName
        );

        DriverManager.setLoginTimeout(5);
        try {
            connection = DriverManager.getConnection(jdbcUrl, user, pass);
        } catch (SQLException e) {
            // Nếu lỗi do Database chưa tồn tại (Error code 4060: Cannot open database requested by the login)
            if (e.getErrorCode() == 4060 || (e.getMessage() != null && e.getMessage().contains("Cannot open database"))) {
                String masterUrl = String.format(
                        "jdbc:sqlserver://%s:%d;databaseName=master;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
                        host, port
                );
                try (Connection masterConn = DriverManager.getConnection(masterUrl, user, pass);
                     Statement stmt = masterConn.createStatement()) {
                    stmt.executeUpdate(
                            "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'" + dbName + "') " +
                            "BEGIN CREATE DATABASE [" + dbName + "]; END"
                    );
                } catch (Exception ex) {
                    System.err.println("Không thể tự động tạo Database qua master: " + ex.getMessage());
                }
                // Thử kết nối lại vào database vừa được khởi tạo
                connection = DriverManager.getConnection(jdbcUrl, user, pass);
            } else {
                throw e;
            }
        }
        lastConfig = config;

        // Tự động kiểm tra và tạo bảng Students nếu chưa có
        autoMigrateTable();

        return connection;
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (lastConfig != null) {
                try {
                    return connect(lastConfig);
                } catch (Exception e) {
                    throw new SQLException("Mất kết nối CSDL và không thể tự động kết nối lại: " + e.getMessage(), e);
                }
            }
            throw new SQLException("Chưa có kết nối CSDL nào được thiết lập!");
        }
        return connection;
    }

    public static synchronized boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public static synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Tự động khởi tạo bảng Students nếu chưa tồn tại trong CSDL
     */
    private static void autoMigrateTable() {
        if (connection == null) return;
        String createTableSql =
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'Students') " +
                "BEGIN " +
                "    CREATE TABLE [dbo].[Students] ( " +
                "        [StudentID] VARCHAR(50) NOT NULL PRIMARY KEY, " +
                "        [EncryptedFullName] NVARCHAR(500) NOT NULL, " +
                "        [EncryptedMath] VARCHAR(200) NOT NULL, " +
                "        [EncryptedLiterature] VARCHAR(200) NOT NULL, " +
                "        [EncryptedEnglish] VARCHAR(200) NOT NULL, " +
                "        [CreatedAt] DATETIME DEFAULT GETDATE(), " +
                "        [UpdatedAt] DATETIME DEFAULT GETDATE() " +
                "    ); " +
                "END";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);

            // Tự động nạp dữ liệu mẫu ban đầu nếu bảng đang trống
            String seedSampleDataSql =
                    "IF NOT EXISTS (SELECT 1 FROM [dbo].[Students]) " +
                    "BEGIN " +
                    "    INSERT INTO [dbo].[Students] ([StudentID], [EncryptedFullName], [EncryptedMath], [EncryptedLiterature], [EncryptedEnglish]) " +
                    "    VALUES " +
                    "        ('N24DTCN022', N'KzFDpNJ/qSII/dqsoqps7QDCIncJ78je', '/ZIvw2Z+YMQ=', '9kppVywjqiM=', '6C5JX7wrb1o='), " +
                    "        ('N24DTCN048', N'Q3lZygk/NvbtFfEzXLgHXxJczohHZz/Y', '9kppVywjqiM=', 'pVHRpyXg8pI=', '9kppVywjqiM='), " +
                    "        ('N24DTCN074', N'9Y6l/aN6NrBZ0t3+ZKxffnO9IRa7Oaq0', 'pVHRpyXg8pI=', 'Jqw1OoPeWks=', '9kppVywjqiM='), " +
                    "        ('N24DTCN086', N'XWqatUqCTbvdQdKgpeqyzbkSSYBp10Cc', 'Jqw1OoPeWks=', 'pVHRpyXg8pI=', 'Jqw1OoPeWks='), " +
                    "        ('N24DTCN039', N'7vh4DlqsBguovqFG4JLcAQ==', 'pVHRpyXg8pI=', '9kppVywjqiM=', 'pVHRpyXg8pI='), " +
                    "        ('N24DTCN049', N'nlEHfOQLyyD/b16krM4mjsAqWUpWGs9/', '/ZIvw2Z+YMQ=', 'pVHRpyXg8pI=', '/ZIvw2Z+YMQ='); " +
                    "END";
            stmt.executeUpdate(seedSampleDataSql);
        } catch (SQLException e) {
            System.err.println("Cảnh báo autoMigrateTable: " + e.getMessage());
        }
    }
}
