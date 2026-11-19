#!/bin/bash
# ==============================================================================
# Script khởi chạy ứng dụng Server
# ==============================================================================

# Biên dịch nếu chưa có target/classes
if [ ! -d "target/classes" ]; then
    bash scripts/compile.sh
fi

CP="target/classes"
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

echo ">>> Khởi chạy Server Dashboard..."
java -cp "$CP" com.ptit.studentmgmt.server.ServerApp
