@echo off
REM Generates/refreshes services-dashboard.html from currently running services.
echo Running generate-dashboard.ps1 ...
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0generate-dashboard.ps1"

if errorlevel 1 (
    echo.
    echo ============================================
    echo Dashboard generation FAILED. See error above.
    echo ============================================
    pause
    exit /b 1
)

if not exist "%~dp0services-dashboard.html" (
    echo.
    echo ============================================
    echo services-dashboard.html was not created. Something went wrong.
    echo ============================================
    pause
    exit /b 1
)

echo.
echo Opening dashboard...
start "" "%~dp0services-dashboard.html"
echo.
echo Done. You can close this window.
pause
