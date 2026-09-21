# Starts all microservices + frontend hidden (no cmd windows), logging to .\run-logs\<service>.log
# Usage:  powershell -ExecutionPolicy Bypass -File start-all-background.ps1
#         powershell -ExecutionPolicy Bypass -File start-all-background.ps1 -Stop

param([switch]$Stop)

$root = $PSScriptRoot
$logDir = Join-Path $root 'run-logs'
$pidFile = Join-Path $logDir 'pids.txt'

if ($Stop) {
    if (Test-Path $pidFile) {
        Get-Content $pidFile | ForEach-Object {
            if ($_ -match '^\d+$') { taskkill /PID $_ /T /F | Out-Null }
        }
        Remove-Item $pidFile
        Write-Host 'All background services stopped.'
    } else { Write-Host 'No pid file found.' }
    return
}

New-Item -ItemType Directory -Force $logDir | Out-Null
Remove-Item $pidFile -ErrorAction SilentlyContinue

$env:MAVEN_OPTS = '-Xms128m -Xmx384m -XX:+UseSerialGC'

$services = 'api-gateway','auth-service','cart-service','cartonization-service','customer-service',
    'delivery-service','audit-service','inventory-service','invoice-service','logging-service',
    'notification-service','order-group-service','order-service','payment-service','picking-service',
    'return-refund-service','scheduler-service','shipment-service'

function Start-Hidden($name, $workDir, $command) {
    $log = Join-Path $logDir "$name.log"
    $p = Start-Process -FilePath 'cmd.exe' -ArgumentList "/c $command > `"$log`" 2>&1" `
        -WorkingDirectory $workDir -WindowStyle Hidden -PassThru
    $p.Id | Add-Content $pidFile
    Write-Host "Started $name (PID $($p.Id))"
}

foreach ($s in $services) {
    Start-Hidden $s (Join-Path $root $s) 'mvn spring-boot:run'
    Start-Sleep -Seconds 2
}
Start-Hidden 'frontend' (Join-Path $root 'frontend\angular-app') 'npm start'

Write-Host "`nAll started in background. Logs: $logDir"
Write-Host 'Stop everything with: start-all-background.ps1 -Stop'
