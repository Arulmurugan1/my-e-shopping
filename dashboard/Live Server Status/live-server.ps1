<#
Real-time microservices dashboard server.
Serves dashboard/live-dashboard.html at / and a fresh JSON status snapshot at /api/status,
computed on every request (PID/health/DB-reachability), so the page can poll every second
and always show current data - no more regenerating a static HTML file via a .bat script.

Usage: run this script, then open http://localhost:5055/
Stop with Ctrl+C.
#>

param(
    [int]$HttpPort = 5055
)

$ErrorActionPreference = 'SilentlyContinue'
Add-Type -AssemblyName System.Net.Http
. "$PSScriptRoot\services-data.ps1"

$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$HttpPort/")
$listener.Start()
Write-Host "Live dashboard server running at http://localhost:$HttpPort/"
Write-Host "Press Ctrl+C to stop."

$shellHtmlPath = Join-Path $PSScriptRoot 'live-dashboard.html'
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

$script:ServicesByName = @{}
foreach ($s in $Services) { $script:ServicesByName[$s.Name] = $s }

function Get-ServiceWorkingDir {
    param($svc)
    if ($svc.IsFrontend) { return Join-Path $RepoRoot 'frontend\angular-app' }
    return Join-Path $RepoRoot $svc.Name
}

function Start-ServiceProcess {
    param($svc)
    $path = Get-ServiceWorkingDir $svc
    if (-not (Test-Path $path)) { return @{ ok = $false; message = "Path not found: $path" } }

    $existing = (Get-ListeningPortToPidMap)[$svc.Port]
    if ($existing) { return @{ ok = $false; message = "$($svc.Name) is already running (PID $($existing.Pid))" } }

    $runCmd = if ($svc.IsFrontend) { 'npm install && npm start' } else { 'set MAVEN_OPTS=-Xms128m -Xmx384m -XX:+UseSerialGC && mvn spring-boot:run' }
    $argLine = "/k title $($svc.Name) && $runCmd"
    try {
        Start-Process -FilePath 'cmd.exe' -ArgumentList $argLine -WorkingDirectory $path -WindowStyle Normal | Out-Null
        return @{ ok = $true; message = "Starting $($svc.Name)..." }
    } catch {
        return @{ ok = $false; message = "Failed to start $($svc.Name): $($_.Exception.Message)" }
    }
}

function Stop-ServiceProcess {
    param($svc)
    $entry = (Get-ListeningPortToPidMap)[$svc.Port]
    if (-not $entry) { return @{ ok = $false; message = "$($svc.Name) is not running (no process on port $($svc.Port))" } }
    $procId = $entry.Pid
    try {
        # Fire-and-forget (no -Wait): the HTTP listener below is single-threaded, so
        # blocking here would freeze every other dashboard request (status polls included)
        # until taskkill finishes.
        Start-Process -FilePath 'taskkill.exe' -ArgumentList "/PID $procId /T /F" -WindowStyle Hidden | Out-Null
        return @{ ok = $true; message = "Stopping $($svc.Name) (PID $procId)..." }
    } catch {
        return @{ ok = $false; message = "Failed to stop $($svc.Name): $($_.Exception.Message)" }
    }
}

function Restart-ServiceProcess {
    param($svc)
    $existing = (Get-ListeningPortToPidMap)[$svc.Port]
    $path = Get-ServiceWorkingDir $svc
    $runCmd = if ($svc.IsFrontend) { 'npm install && npm start' } else { 'set MAVEN_OPTS=-Xms128m -Xmx384m -XX:+UseSerialGC && mvn spring-boot:run' }
    $startArgLine = "/k title $($svc.Name) && $runCmd"
    $killPart = if ($existing) { "taskkill /PID $($existing.Pid) /T /F | Out-Null; Start-Sleep -Seconds 2; " } else { '' }
    # The stop-wait-start sequence runs in a detached background process so the dashboard's
    # own HTTP listener (single-threaded) never blocks on it and keeps serving status polls.
    $deferredCmd = "$killPart Start-Process -FilePath 'cmd.exe' -ArgumentList '$startArgLine' -WorkingDirectory '$path' -WindowStyle Normal"
    Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoProfile', '-WindowStyle', 'Hidden', '-Command', $deferredCmd) -WindowStyle Hidden | Out-Null
    return @{ ok = $true; message = "Restarting $($svc.Name)..." }
}

function Invoke-ServiceAction {
    param([string]$ServiceName, [string]$Action)
    $svc = $script:ServicesByName[$ServiceName]
    if (-not $svc) { return @{ ok = $false; message = "Unknown service: $ServiceName" } }
    switch ($Action) {
        'start'   { return Start-ServiceProcess $svc }
        'stop'    { return Stop-ServiceProcess $svc }
        'restart' { return Restart-ServiceProcess $svc }
        default   { return @{ ok = $false; message = "Unknown action: $Action" } }
    }
}

function Start-AllServices {
    $portToPid = Get-ListeningPortToPidMap
    $parts = @()
    foreach ($s in $Services) {
        if ($portToPid[[int]$s.Port]) { continue }
        $path = Get-ServiceWorkingDir $s
        if (-not (Test-Path $path)) { continue }
        $runCmd = if ($s.IsFrontend) { 'npm install && npm start' } else { 'set MAVEN_OPTS=-Xms128m -Xmx384m -XX:+UseSerialGC && mvn spring-boot:run' }
        $argLine = "/k title $($s.Name) && $runCmd"
        # Staggered ~2s apart, same pacing as start-all-services.bat, so we don't spawn
        # a dozen+ JVMs at once and repeat the native-memory OOM crash seen before.
        $parts += "Start-Process -FilePath 'cmd.exe' -ArgumentList '$argLine' -WorkingDirectory '$path' -WindowStyle Normal; Start-Sleep -Seconds 2"
    }
    if ($parts.Count -eq 0) { return @{ ok = $false; message = 'All services are already running' } }
    $deferredCmd = $parts -join '; '
    Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoProfile', '-WindowStyle', 'Hidden', '-Command', $deferredCmd) -WindowStyle Hidden | Out-Null
    return @{ ok = $true; message = "Starting $($parts.Count) service(s) (staggered, ~2s apart)..." }
}

function Stop-AllServices {
    $portToPid = Get-ListeningPortToPidMap
    $count = 0
    foreach ($s in $Services) {
        $entry = $portToPid[[int]$s.Port]
        if ($entry) {
            try { Start-Process -FilePath 'taskkill.exe' -ArgumentList "/PID $($entry.Pid) /T /F" -WindowStyle Hidden | Out-Null; $count++ } catch { }
        }
    }
    if ($count -eq 0) { return @{ ok = $false; message = 'No services are currently running' } }
    return @{ ok = $true; message = "Stopping $count running service(s)..." }
}

function Invoke-ServiceActionAll {
    param([string]$Action)
    switch ($Action) {
        'start' { return Start-AllServices }
        'stop'  { return Stop-AllServices }
        default { return @{ ok = $false; message = "Unknown action: $Action" } }
    }
}

# --- Docker ---------------------------------------------------------------
# Container names follow docker compose's "<project>-<service>-1" convention;
# the project name is the folder holding the compose file ("docker").
$script:DockerServices = @('postgres', 'redis', 'zookeeper', 'kafka', 'mongodb', 'mysql', 'oracle', 'prometheus', 'grafana')
$script:DockerComposePath = Join-Path $RepoRoot 'infrastructure\docker\docker-compose.yml'

function Get-DockerStatusList {
    $dockerAvailable = $true
    $psOutput = @()
    try {
        $psOutput = & docker.exe ps -a --format "{{.Names}}|{{.Status}}" 2>$null
        if ($LASTEXITCODE -ne 0) { $dockerAvailable = $false }
    } catch {
        $dockerAvailable = $false
    }

    $statusByName = @{}
    foreach ($line in $psOutput) {
        $parts = $line -split '\|', 2
        if ($parts.Count -eq 2) { $statusByName[$parts[0]] = $parts[1] }
    }

    $containers = @()
    foreach ($svc in $script:DockerServices) {
        $containerName = "docker-$svc-1"
        $status = $statusByName[$containerName]
        $running = [bool]($status -and $status -like 'Up*')
        $containers += [PSCustomObject]@{
            name      = $svc
            container = $containerName
            status    = $(if ($status) { $status } else { 'Not Found' })
            running   = $running
        }
    }

    return [PSCustomObject]@{ dockerAvailable = $dockerAvailable; containers = $containers }
}

function Invoke-DockerAction {
    param([string]$Service, [string]$Action)
    if ($script:DockerServices -notcontains $Service) { return @{ ok = $false; message = "Unknown docker service: $Service" } }
    if ($Action -notin @('start', 'stop', 'restart')) { return @{ ok = $false; message = "Unknown action: $Action" } }
    $containerName = "docker-$Service-1"
    try {
        # Fire-and-forget so the dashboard's single-threaded listener stays responsive.
        Start-Process -FilePath 'docker.exe' -ArgumentList "$Action $containerName" -WindowStyle Hidden | Out-Null
        return @{ ok = $true; message = "$($Action.Substring(0,1).ToUpper() + $Action.Substring(1))ing $containerName..." }
    } catch {
        return @{ ok = $false; message = "Failed to $Action ${containerName}: $($_.Exception.Message)" }
    }
}

function Invoke-DockerComposeAction {
    param([string]$Action)
    if ($Action -notin @('up', 'down')) { return @{ ok = $false; message = "Unknown compose action: $Action" } }
    $composeArgs = if ($Action -eq 'up') { "compose -f `"$script:DockerComposePath`" up -d" } else { "compose -f `"$script:DockerComposePath`" down" }
    try {
        Start-Process -FilePath 'docker.exe' -ArgumentList $composeArgs -WindowStyle Hidden | Out-Null
        $msg = if ($Action -eq 'up') { 'Starting all Docker containers...' } else { 'Stopping all Docker containers...' }
        return @{ ok = $true; message = $msg }
    } catch {
        return @{ ok = $false; message = "Failed to run docker compose $($Action): $($_.Exception.Message)" }
    }
}

$script:cpuCoreCount = [Environment]::ProcessorCount
$script:cpuPrevTime = @{}
$script:cpuPrevSampledAt = @{}

# DB engine type -> Windows process name(s) used to attribute host CPU/RAM to a "database" row.
$script:DbEngineProcessNames = @{
    'PostgreSQL' = @('postgres')
    'MySQL'      = @('mysqld')
    'MongoDB'    = @('mongod')
    'Oracle'     = @('oracle', 'ora_pmon_XE', 'OracleServiceXE')
}

function Get-CpuPercentForPids {
    param([int[]]$ProcIds, [hashtable]$ProcsById)

    $now = Get-Date
    $totalPercent = 0.0
    $found = $false
    foreach ($procId in $ProcIds) {
        if (-not $ProcsById.ContainsKey($procId)) { continue }
        $proc = $ProcsById[$procId]
        $curTime = $proc.TotalProcessorTime
        $key = [string]$procId
        if ($script:cpuPrevTime.ContainsKey($key)) {
            $elapsedMs = ($now - $script:cpuPrevSampledAt[$key]).TotalMilliseconds
            $cpuDeltaMs = ($curTime - $script:cpuPrevTime[$key]).TotalMilliseconds
            if ($elapsedMs -gt 0) {
                $totalPercent += ([math]::Max(0, $cpuDeltaMs) / $elapsedMs) * 100.0 / $script:cpuCoreCount
            }
            $found = $true
        }
        $script:cpuPrevTime[$key] = $curTime
        $script:cpuPrevSampledAt[$key] = $now
    }
    if (-not $found) { return $null }
    return [math]::Round($totalPercent, 1)
}

function Get-ListeningPortToPidMap {
    # Get-NetTCPConnection goes through a slow WMI provider (~1.3s per call); netstat.exe
    # returns the same listening-socket info in well under 100ms, which matters a lot
    # since this dashboard polls every second.
    # No "-p TCP" filter: on Windows that restricts netstat to IPv4-only sockets, which
    # silently misses services that listen on IPv6 loopback only (e.g. Angular's dev
    # server binds "[::1]:4200", not "0.0.0.0:4200"). Both families print as "TCP" anyway.
    # Values are @{ Pid = <int>; IsV6Only = <bool> } - IsV6Only lets callers building a
    # health-check URL know to hit "[::1]:port" instead of "localhost:port", since
    # .NET's HttpClient resolves "localhost" to 127.0.0.1 first and a service bound only
    # to "[::1]" (e.g. Angular's dev server) would otherwise time out on every poll.
    $portToPid = @{}
    $lines = netstat.exe -ano 2>$null | Select-String '^\s*TCP\s.*LISTENING'
    foreach ($line in $lines) {
        $parts = -split $line.Line
        if ($parts.Count -ge 5) {
            $localAddr = $parts[1]
            $sep = $localAddr.LastIndexOf(':')
            if ($sep -ge 0) {
                $portNum = 0
                if ([int]::TryParse($localAddr.Substring($sep + 1), [ref]$portNum)) {
                    $procIdNum = 0
                    if ([int]::TryParse($parts[4], [ref]$procIdNum)) {
                        # Only "[::1]" (loopback-only) actually rejects IPv4 connections; a
                        # "[::]" wildcard bind is dual-stack and still answers on 127.0.0.1.
                        $portToPid[$portNum] = @{ Pid = $procIdNum; IsV6Only = $localAddr.StartsWith('[::1]') }
                    }
                }
            }
        }
    }
    return $portToPid
}

function Get-SystemCpuPercent {
    # Avoids Get-CimInstance Win32_Processor (~1.1s per call) by summing per-process CPU
    # time deltas against the process list the caller already fetched, the same technique
    # Get-CpuPercentForPids uses per-service.
    param([hashtable]$ProcsById)
    $now = Get-Date
    $totalMs = 0.0
    foreach ($p in $ProcsById.Values) {
        try { $totalMs += $p.TotalProcessorTime.TotalMilliseconds } catch { }
    }
    $result = 0
    if ($script:sysCpuPrevSampledAt) {
        $elapsedMs = ($now - $script:sysCpuPrevSampledAt).TotalMilliseconds
        $deltaMs = $totalMs - $script:sysCpuPrevTotalMs
        if ($elapsedMs -gt 0) {
            $result = [math]::Round(([math]::Max(0, $deltaMs) / $elapsedMs) * 100.0 / $script:cpuCoreCount, 1)
        }
    }
    $script:sysCpuPrevTotalMs = $totalMs
    $script:sysCpuPrevSampledAt = $now
    return $result
}

function Get-StatusSnapshot {
    $portToPid = Get-ListeningPortToPidMap

    $procsById = @{}
    foreach ($proc in (Get-Process -ErrorAction SilentlyContinue)) {
        $procsById[$proc.Id] = $proc
    }

    $os = Get-CimInstance -ClassName Win32_OperatingSystem -ErrorAction SilentlyContinue
    $totalRamGb = if ($os) { [math]::Round($os.TotalVisibleMemorySize / 1MB, 2) } else { 0 }
    $availRamGb = if ($os) { [math]::Round($os.FreePhysicalMemory / 1MB, 2) } else { 0 }
    $freeRamGb  = $availRamGb
    $usedRamGb  = [math]::Round($totalRamGb - $availRamGb, 2)
    $ramUsedPct = if ($totalRamGb -gt 0) { [math]::Round(($usedRamGb / $totalRamGb) * 100, 1) } else { 0 }

    $totalCpuPercent = Get-SystemCpuPercent -ProcsById $procsById

    $sysDrive = Get-CimInstance -ClassName Win32_LogicalDisk -Filter "DeviceID='$($env:SystemDrive)'" -ErrorAction SilentlyContinue
    $totalStorageGb = if ($sysDrive) { [math]::Round($sysDrive.Size / 1GB, 2) } else { 0 }
    $freeStorageGb  = if ($sysDrive) { [math]::Round($sysDrive.FreeSpace / 1GB, 2) } else { 0 }
    $availStorageGb = $freeStorageGb
    $usedStorageGb  = [math]::Round($totalStorageGb - $freeStorageGb, 2)
    $storageUsedPct = if ($totalStorageGb -gt 0) { [math]::Round(($usedStorageGb / $totalStorageGb) * 100, 1) } else { 0 }

    $httpClient = New-Object System.Net.Http.HttpClient
    $httpClient.Timeout = [TimeSpan]::FromMilliseconds(900)

    $pending = @()
    foreach ($svc in $Services) {
        $entry = $portToPid[[int]$svc.Port]
        $procId = if ($entry) { $entry.Pid } else { $null }
        $running = $false; $started = ''; $memGb = 0
        if ($procId -and $procsById.ContainsKey([int]$procId)) {
            $proc = $procsById[[int]$procId]
            $running = $true
            $started = $proc.StartTime.ToString('yyyy-MM-dd HH:mm:ss')
            $memGb = [math]::Round($proc.WorkingSet64 / 1GB, 2)
        }

        $healthTask = $null
        if ($running) {
            # A loopback-only IPv6 bind (Angular's dev server) never answers on
            # "localhost", which .NET resolves to 127.0.0.1 first.
            $healthHost = if ($entry.IsV6Only) { '[::1]' } else { 'localhost' }
            $healthUrl = if ($svc.IsFrontend) { "http://${healthHost}:$($svc.Port)/" } else { "http://${healthHost}:$($svc.Port)/actuator/health" }
            try { $healthTask = $httpClient.GetAsync($healthUrl) } catch { $healthTask = $null }
        }

        $dbSocket = $null; $dbConnectResult = $null
        if ($svc.DbType -ne 'None' -and $svc.DbType -notlike 'H2*') {
            try {
                $dbSocket = New-Object System.Net.Sockets.TcpClient
                $dbConnectResult = $dbSocket.BeginConnect($svc.DbHost, $svc.DbPort, $null, $null)
            } catch { $dbSocket = $null }
        }

        $pending += [PSCustomObject]@{
            Svc = $svc; ProcId = $procId; Running = $running; Started = $started; Mem = $memGb
            HealthTask = $healthTask; DbSocket = $dbSocket; DbConnectResult = $dbConnectResult
        }
    }

    $healthTasks = $pending.HealthTask | Where-Object { $_ -ne $null }
    if ($healthTasks.Count -gt 0) {
        [System.Threading.Tasks.Task]::WaitAll($healthTasks, 900) | Out-Null
    }

    $instances = @()
    $databases = @()

    foreach ($r in $pending) {
        $status = 'Yet To Start'
        if ($r.Running) {
            $status = 'Issue'
            # Task.IsCompletedSuccessfully doesn't exist on this Windows PowerShell's .NET
            # Framework runtime (always reads as $null/false here), so every health check
            # fell through to "Issue" regardless of the real result - use the fields that
            # do exist instead.
            if ($r.HealthTask -and $r.HealthTask.IsCompleted -and -not $r.HealthTask.IsFaulted -and -not $r.HealthTask.IsCanceled) {
                try {
                    $resp = $r.HealthTask.Result
                    if ($r.Svc.IsFrontend) {
                        if ($resp.IsSuccessStatusCode) { $status = 'Healthy' }
                    } else {
                        $body = $resp.Content.ReadAsStringAsync().Result
                        $parsed = $body | ConvertFrom-Json
                        if ($parsed.status -eq 'UP') { $status = 'Healthy' }
                    }
                } catch { }
            }
        }

        $cpuPercent = $null
        $ramPercent = $null
        if ($r.Running -and $r.ProcId) {
            $cpuPercent = Get-CpuPercentForPids -ProcIds @([int]$r.ProcId) -ProcsById $procsById
            if ($totalRamGb -gt 0) { $ramPercent = [math]::Round(($r.Mem / $totalRamGb) * 100, 1) }
        }

        $instances += [PSCustomObject]@{
            name    = $r.Svc.Name
            url     = "http://localhost:$($r.Svc.Port)"
            status  = $status
            running = $r.Running
            started = $(if ($r.Started) { $r.Started } else { 'N/A' })
            memory  = $(if ($r.Running) { "$($r.Mem) GB" } else { 'N/A' })
            cpuPct  = $(if ($cpuPercent -ne $null) { $cpuPercent } else { 'N/A' })
            ramPct  = $(if ($ramPercent -ne $null) { $ramPercent } else { 'N/A' })
            pid_    = $(if ($r.ProcId) { $r.ProcId } else { 'N/A' })
        }

        $dbStatus = 'N/A'
        if ($r.Svc.DbType -like 'H2*') {
            $dbStatus = if ($r.Running) { 'In-Memory (Up)' } else { 'In-Memory (Down)' }
        } elseif ($r.DbSocket) {
            try {
                $ok = $r.DbConnectResult.IsCompleted -and $r.DbSocket.Connected
                $dbStatus = if ($ok) { 'Reachable' } else { 'Unreachable' }
            } catch { $dbStatus = 'Unreachable' }
            finally { try { $r.DbSocket.Close() } catch { } }
        }

        if ($r.Svc.DbType -ne 'None') {
            $dbCpuPercent = $null
            $dbMemGb = $null
            $dbRamPercent = $null
            $engineNames = $script:DbEngineProcessNames[$r.Svc.DbType]
            if ($engineNames) {
                $enginePids = @()
                $enginePids += ($procsById.Values | Where-Object { $engineNames -contains $_.ProcessName } | Select-Object -ExpandProperty Id)
                if ($enginePids.Count -gt 0) {
                    $dbCpuPercent = Get-CpuPercentForPids -ProcIds $enginePids -ProcsById $procsById
                    $dbMemGb = [math]::Round((($enginePids | ForEach-Object { $procsById[$_].WorkingSet64 } | Measure-Object -Sum).Sum) / 1GB, 2)
                    if ($totalRamGb -gt 0) { $dbRamPercent = [math]::Round(($dbMemGb / $totalRamGb) * 100, 1) }
                }
            }

            $databases += [PSCustomObject]@{
                name     = $r.Svc.Name
                dbType   = $r.Svc.DbType
                dbTarget = $(if ($r.Svc.DbHost) { "$($r.Svc.DbHost):$($r.Svc.DbPort)" } else { 'N/A' })
                dbName   = $r.Svc.DbName
                dbUser   = $r.Svc.DbUser
                dbPass   = $r.Svc.DbPass
                dbStatus = $dbStatus
                dbCpuPct = $(if ($dbCpuPercent -ne $null) { $dbCpuPercent } else { 'N/A' })
                dbMem    = $(if ($dbMemGb -ne $null) { "$dbMemGb GB" } else { 'N/A' })
                dbRamPct = $(if ($dbRamPercent -ne $null) { $dbRamPercent } else { 'N/A' })
            }
        }
    }

    $httpClient.Dispose()

    return [PSCustomObject]@{
        generatedAt     = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
        totalRamGb      = $totalRamGb
        availRamGb      = $availRamGb
        freeRamGb       = $freeRamGb
        usedRamGb       = $usedRamGb
        ramUsedPct      = $ramUsedPct
        cpuCores        = $script:cpuCoreCount
        totalCpuPercent = $totalCpuPercent
        totalStorageGb  = $totalStorageGb
        freeStorageGb   = $freeStorageGb
        availStorageGb  = $availStorageGb
        usedStorageGb   = $usedStorageGb
        storageUsedPct  = $storageUsedPct
        instances       = $instances
        databases       = $databases
    } | ConvertTo-Json -Depth 6
}

function Get-QueryParams {
    param($request)
    $queryParams = @{}
    foreach ($kv in $request.Url.Query.TrimStart('?').Split('&')) {
        if ($kv) {
            $p = $kv.Split('=', 2)
            $queryParams[$p[0]] = [Uri]::UnescapeDataString($p[1])
        }
    }
    return $queryParams
}

function Write-JsonResponse {
    param($response, $obj, [int]$depth = 6)
    $json = $obj | ConvertTo-Json -Depth $depth
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $response.ContentType = 'application/json'
    $response.ContentLength64 = $bytes.Length
    $response.OutputStream.Write($bytes, 0, $bytes.Length)
}

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response
        try {
            switch ($request.Url.AbsolutePath) {
                '/api/status' {
                    $json = Get-StatusSnapshot
                    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
                    $response.ContentType = 'application/json'
                    $response.ContentLength64 = $bytes.Length
                    $response.OutputStream.Write($bytes, 0, $bytes.Length)
                }
                '/api/action' {
                    $q = Get-QueryParams $request
                    $result = Invoke-ServiceAction -ServiceName $q['service'] -Action $q['action']
                    Write-JsonResponse -response $response -obj $result
                }
                '/api/action-all' {
                    $q = Get-QueryParams $request
                    $result = Invoke-ServiceActionAll -Action $q['action']
                    Write-JsonResponse -response $response -obj $result
                }
                '/api/docker-status' {
                    Write-JsonResponse -response $response -obj (Get-DockerStatusList)
                }
                '/api/docker-action' {
                    $q = Get-QueryParams $request
                    $result = Invoke-DockerAction -Service $q['service'] -Action $q['action']
                    Write-JsonResponse -response $response -obj $result
                }
                '/api/docker-compose-action' {
                    $q = Get-QueryParams $request
                    $result = Invoke-DockerComposeAction -Action $q['action']
                    Write-JsonResponse -response $response -obj $result
                }
                default {
                    $html = Get-Content -Path $shellHtmlPath -Raw -Encoding UTF8
                    $bytes = [System.Text.Encoding]::UTF8.GetBytes($html)
                    $response.ContentType = 'text/html; charset=utf-8'
                    $response.ContentLength64 = $bytes.Length
                    $response.OutputStream.Write($bytes, 0, $bytes.Length)
                }
            }
        } catch {
            Write-Host "Request error: $($_.Exception.Message)"
            $response.StatusCode = 500
        } finally {
            $response.OutputStream.Close()
        }
    }
} finally {
    $listener.Stop()
    $listener.Close()
}
