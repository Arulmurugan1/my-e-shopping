@echo off
REM Starts the real-time dashboard server hidden (no console window) and opens it in the browser.
REM To stop it: run stop-live-dashboard.bat
powershell -NoProfile -Command "Start-Process powershell -WindowStyle Hidden -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-File','\"%~dp0live-server.ps1\"'"
timeout /t 2 /nobreak >nul
start "" "http://localhost:5055/"
