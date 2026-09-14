@echo off
REM Starts the real-time dashboard server (polls every 1s) and opens it in the browser.
REM Leave this window open - closing it stops the live server. Ctrl+C to stop manually.
start "Live Dashboard Server" powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0live-server.ps1"
timeout /t 2 /nobreak >nul
start "" "http://localhost:5055/"
echo Live dashboard started at http://localhost:5055/
echo Close the "Live Dashboard Server" window to stop it.
