@echo off
chcp 65001 > nul
echo ===================================================
echo   KHOI CHAY CLIENT - QUAN LY SINH VIEN TCP SOCKET
echo ===================================================

if not exist target\classes (
    echo Dang bien dich ma nguon...
    mkdir target\classes 2>nul
    dir /s /b src\main\java\*.java > target\sources.txt
    javac -encoding UTF-8 -d target\classes @target\sources.txt
    del target\sources.txt
)

echo Dang mo Client UI...
java -cp target\classes com.ptit.studentmgmt.client.ClientApp
pause
