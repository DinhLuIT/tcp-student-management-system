#!/bin/bash
# ==============================================================================
# Script biên dịch source code đồ án Lập trình mạng
# ==============================================================================

set -e

echo ">>> Đang biên dịch mã nguồn Java..."
mkdir -p target/classes

# Tìm tất cả các file .java trong src/main/java
find src/main/java -name "*.java" > target/sources.txt

CP=""
if [ -d "$HOME/.m2/repository" ]; then
    for jar in \
        "$HOME/.m2/repository/com/microsoft/sqlserver/mssql-jdbc/12.4.2.jre11/mssql-jdbc-12.4.2.jre11.jar" \
        "$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar" \
        "$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar" \
        "$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar" \
        "$HOME/.m2/repository/com/formdev/flatlaf/3.2.1/flatlaf-3.2.1.jar"; do
        if [ -f "$jar" ]; then
            CP="$CP:$jar"
        fi
    done
fi

if [ -n "$CP" ]; then
    javac -encoding UTF-8 -cp "${CP#:}" -d target/classes @target/sources.txt
else
    javac -encoding UTF-8 -d target/classes @target/sources.txt
fi
rm -f target/sources.txt

echo ">>> Biên dịch thành công! Các file .class đã được lưu tại target/classes"
