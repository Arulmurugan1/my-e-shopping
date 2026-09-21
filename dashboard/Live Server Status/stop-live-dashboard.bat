@echo off
REM Stops the hidden live dashboard server (the powershell process running live-server.ps1).
REM Note: HttpListener shows as PID 4 on the port, so we match on the command line instead.
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='powershell.exe'\" | Where-Object { $_.CommandLine -match 'live-server\.ps1' -and $_.ProcessId -ne $PID } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"
echo Live dashboard stopped.
