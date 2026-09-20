@echo off
REM Stop All Microservices Batch File
REM This script stops all running microservices

echo.
echo ============================================
echo Stopping All Microservices...
echo ============================================
echo.

REM Kill all Java processes (Spring Boot services)
for /f "tokens=2" %%A in ('tasklist ^| find /i "javaw"') do (
    taskkill /pid %%A /f 2>nul
)

for /f "tokens=2" %%A in ('tasklist ^| find /i "java.exe"') do (
    taskkill /pid %%A /f 2>nul
)

REM Close the Frontend cmd window (and its child processes, e.g. npm/node)
taskkill /FI "WINDOWTITLE eq Frontend*" /T /F 2>nul

REM Kill any leftover node.exe running the Angular dev server
for /f "tokens=2 delims=," %%A in ('wmic process where "name='node.exe' and commandline like '%%angular-app%%'" get processid /format:csv 2^>nul ^| find ","') do (
    taskkill /pid %%A /f 2>nul
)

echo.
echo All services stopped.
echo.
pause
