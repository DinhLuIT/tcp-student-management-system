#!/bin/bash
# ==============================================================================
# Tiện ích quản lý SQL Server Docker cho MacBook / macOS
# ==============================================================================

COMMAND=$1

case "$COMMAND" in
    start)
        echo ">>> Đang khởi động SQL Server Container..."
        docker-compose up -d
        echo ""
        echo "================================================================="
        echo ">>> SQL Server đã khởi động thành công trên cổng 1433!"
        echo "    Host: localhost"
        echo "    Port: 1433"
        echo "    Database: StudentManagementDB"
        echo "    Username: sa"
        echo "    Password: Admin@123456"
        echo "================================================================="
        ;;
    stop)
        echo ">>> Đang dừng SQL Server Container..."
        docker-compose down
        echo ">>> Đã dừng SQL Server."
        ;;
    logs)
        docker-compose logs -f sqlserver
        ;;
    query)
        echo ">>> Đang truy vấn bảng Students từ container..."
        docker run --rm --network ptit_network mcr.microsoft.com/mssql-tools /opt/mssql-tools/bin/sqlcmd \
            -S ptit-sqlserver -U sa -P "Admin@123456" -d StudentManagementDB \
            -Q "SELECT StudentID, EncryptedFullName, EncryptedMath, EncryptedLiterature, EncryptedEnglish FROM Students;"
        ;;
    status)
        docker ps -f "name=ptit-sqlserver"
        ;;
    *)
        echo "Cách sử dụng: ./scripts/docker-sql.sh {start|stop|logs|query|status}"
        echo "  - start  : Khởi động SQL Server và tự động tạo Database, bảng Students"
        echo "  - stop   : Dừng SQL Server Container"
        echo "  - logs   : Xem nhật ký hoạt động của SQL Server"
        echo "  - query  : Xem nhanh dữ liệu trong CSDL qua terminal"
        echo "  - status : Kiểm tra trạng thái hoạt động của container"
        ;;
esac
