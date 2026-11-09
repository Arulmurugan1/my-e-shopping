@echo off
REM Start All Microservices Batch File
REM This script starts all microservices in separate terminal windows

setlocal enabledelayedexpansion

REM Cap each service's JVM heap/virtual memory so 18 concurrent JVMs don't exceed
REM the Windows commit limit (RAM + pagefile) - without this, G1GC's default heap
REM reservation per JVM can exhaust available virtual memory and crash with
REM "The paging file is too small for this operation to complete".
REM JAVA_TOOL_OPTIONS (not MAVEN_OPTS) so the cap also reaches the service JVM that
REM spring-boot:run forks - MAVEN_OPTS only applies to Maven itself.
set JAVA_TOOL_OPTIONS=-Xms64m -Xmx256m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxMetaspaceSize=256m -XX:ReservedCodeCacheSize=64m

REM Define services array
set services=api-gateway auth-service cart-service cartonization-service customer-service delivery-service audit-service inventory-service invoice-service logging-service notification-service order-group-service order-service payment-service picking-service return-refund-service scheduler-service shipment-service

echo.
echo ============================================
echo Starting All Microservices...
echo ============================================
echo.

REM Loop through each service and start it
for %%S in (%services%) do (
    echo Starting %%S...
    start "%%S" cmd /k "cd %%S && mvn spring-boot:run"
    timeout /t 2 /nobreak
)

echo.
echo Starting Frontend (Angular App)...
echo.
start "Frontend" cmd /k "cd frontend\angular-app && npm install && npm start"

echo.
echo ============================================
echo All services and frontend are starting...
echo Check individual windows for status
echo ============================================
echo.
echo Backend services: localhost:8080-8096
echo Frontend: localhost:4200
echo.
pause
