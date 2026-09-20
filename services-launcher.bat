@echo off
REM Advanced Microservices Launcher
REM This script provides options to start services with different configurations

setlocal enabledelayedexpansion

REM Cap each service's JVM heap/virtual memory so many concurrent JVMs don't exceed
REM the Windows commit limit (RAM + pagefile) - without this, G1GC's default heap
REM reservation per JVM can exhaust available virtual memory and crash with
REM "The paging file is too small for this operation to complete".
set MAVEN_OPTS=-Xms128m -Xmx384m -XX:+UseSerialGC

:menu
cls
echo.
echo ============================================
echo    E-Shop Microservices Launcher
echo ============================================
echo.
echo Choose an option:
echo 1. Start all microservices
echo 2. Start microservices with frontend
echo 3. Start specific services
echo 4. Stop all services
echo 5. Rebuild and start all services
echo 6. Show running services
echo 0. Exit
echo.
set /p choice="Enter your choice (0-6): "

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto start_with_frontend
if "%choice%"=="3" goto start_specific
if "%choice%"=="4" goto stop_all
if "%choice%"=="5" goto rebuild_all
if "%choice%"=="6" goto show_running
if "%choice%"=="0" goto exit
goto menu

:start_all
cls
echo.
echo Starting all microservices...
echo.
call :start_services
pause
goto menu

:start_with_frontend
cls
echo.
echo Starting microservices and frontend...
echo.
call :start_services
echo.
echo Starting frontend (Angular app)...
echo.
start "Frontend" cmd /k "cd frontend\angular-app && npm install && npm start"
pause
goto menu

:start_specific
cls
echo.
echo Available services:
echo 1. api-gateway
echo 2. auth-service
echo 3. cart-service
echo 4. customer-service
echo 5. delivery-service
echo 6. order-service
echo 7. payment-service
echo 8. inventory-service
echo 9. notification-service
echo 10. All services
echo.
set /p service_choice="Enter service number or name: "

if "%service_choice%"=="1" set service=api-gateway
if "%service_choice%"=="2" set service=auth-service
if "%service_choice%"=="3" set service=cart-service
if "%service_choice%"=="4" set service=customer-service
if "%service_choice%"=="5" set service=delivery-service
if "%service_choice%"=="6" set service=order-service
if "%service_choice%"=="7" set service=payment-service
if "%service_choice%"=="8" set service=inventory-service
if "%service_choice%"=="9" set service=notification-service
if "%service_choice%"=="10" (
    call :start_services
    pause
    goto menu
)

if not "!service!"=="" (
    echo Starting !service!...
    start "!service!" cmd /k "cd !service! && mvn clean spring-boot:run"
)
pause
goto menu

:stop_all
cls
echo.
echo Stopping all microservices...
echo.
for /f "tokens=2" %%A in ('tasklist ^| find /i "javaw"') do (
    taskkill /pid %%A /f 2>nul
)
for /f "tokens=2" %%A in ('tasklist ^| find /i "java.exe"') do (
    taskkill /pid %%A /f 2>nul
)
echo All services stopped.
echo.
pause
goto menu

:rebuild_all
cls
echo.
echo Rebuilding all services... This may take a few minutes.
echo.
set services=api-gateway auth-service cart-service cartonization-service customer-service delivery-service audit-service inventory-service invoice-service logging-service notification-service order-group-service order-service payment-service picking-service return-refund-service scheduler-service shipment-service

for %%S in (%services%) do (
    echo Building %%S...
    cd %%S
    mvn clean install -DskipTests
    cd ..
)

echo.
echo All services built. Starting services...
echo.
call :start_services
pause
goto menu

:show_running
cls
echo.
echo Running Java processes:
echo.
tasklist /fo table /v | find "java"
if %errorlevel% neq 0 (
    echo No Java processes running.
)
echo.
pause
goto menu

:start_services
set services=api-gateway auth-service cart-service cartonization-service customer-service delivery-service audit-service inventory-service invoice-service logging-service notification-service order-group-service order-service payment-service picking-service return-refund-service scheduler-service shipment-service

for %%S in (%services%) do (
    if exist "%%S\pom.xml" (
        echo Starting %%S...
        start "%%S" cmd /k "cd %%S && mvn spring-boot:run"
        timeout /t 2 /nobreak
    )
)
exit /b

:exit
echo.
echo Exiting launcher...
echo.
exit /b
