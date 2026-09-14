<#
Generates services-dashboard.html - a static status page for all 18 microservices
plus their database connections.
Reads each service's port, resolves the PID bound to it (netstat), pulls process
start time / memory from that PID, checks /actuator/health for service health, and
probes each service's configured database host:port for reachability.
Run this any time after start-all-services.bat to refresh the dashboard, then open
services-dashboard.html in a browser (no server needed - data is embedded inline).
#>

$ErrorActionPreference = 'SilentlyContinue'
. "$PSScriptRoot\services-data.ps1"
$services = $Services

Write-Host "Scanning listening ports (netstat)..."
$netstatLines = netstat -ano | Select-String -Pattern "LISTENING"

function Get-PidOnPort($port) {
    $line = $netstatLines | Select-String -Pattern (":{0}\s" -f $port)
    if ($line) {
        $tokens = ($line.Line -split '\s+') | Where-Object { $_ -ne '' }
        return $tokens[-1]
    }
    return $null
}

function Test-Port($host_, $port) {
    if (-not $host_ -or -not $port) { return $false }
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect($host_, $port, $null, $null)
        $ok = $iar.AsyncWaitHandle.WaitOne(800, $false)
        if ($ok -and $client.Connected) { $client.Close(); return $true }
        $client.Close()
        return $false
    } catch { return $false }
}

$results = @()

foreach ($svc in ($services | Sort-Object { $_.Name })) {
    Write-Host "Checking $($svc.Name)..."
    $procId = Get-PidOnPort $svc.Port
    $running = $false
    $started = ''
    $memGb = 0
    $status = 'Yet To Start'

    if ($procId) {
        $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
        if ($proc) {
            $running = $true
            $started = $proc.StartTime.ToString('yyyy-MM-dd HH:mm:ss')
            $memGb = [math]::Round($proc.WorkingSet64 / 1GB, 2)
        }
    }

    if ($running) {
        if ($svc.IsFrontend) {
            try {
                $resp = Invoke-WebRequest -Uri "http://localhost:$($svc.Port)/" -TimeoutSec 3 -UseBasicParsing
                if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 400) { $status = 'Healthy' } else { $status = 'Issue' }
            } catch {
                $status = 'Issue'
            }
        } else {
            try {
                $resp = Invoke-RestMethod -Uri "http://localhost:$($svc.Port)/actuator/health" -TimeoutSec 2
                if ($resp.status -eq 'UP') { $status = 'Healthy' } else { $status = 'Issue' }
            } catch {
                $status = 'Issue'
            }
        }
    }

    $dbReachable = $false
    if ($svc.DbType -eq 'None') {
        $dbStatus = 'N/A'
    } elseif ($svc.DbType -like 'H2*') {
        $dbStatus = if ($running) { 'In-Memory (Up)' } else { 'In-Memory (Down)' }
    } else {
        $dbReachable = Test-Port $svc.DbHost $svc.DbPort
        $dbStatus = if ($dbReachable) { 'Reachable' } else { 'Unreachable' }
    }

    $results += [PSCustomObject]@{
        Name       = $svc.Name
        Url        = "http://localhost:$($svc.Port)"
        Status     = $status
        Running    = $running
        Started    = $started
        Memory     = $memGb
        Pid        = $procId
        DbType     = $svc.DbType
        DbHost     = $svc.DbHost
        DbPort     = $svc.DbPort
        DbName     = $svc.DbName
        DbUser     = $svc.DbUser
        DbPass     = $svc.DbPass
        DbStatus   = $dbStatus
    }
}

$instanceRows = ($results | ForEach-Object {
    $statusClass = switch ($_.Status) { 'Healthy' { 'ok' } 'Issue' { 'bad' } default { 'bad' } }
    $liveClass   = if ($_.Running) { 'ok' } else { 'bad' }
    $liveText    = if ($_.Running) { 'Running' } else { 'Not Running' }
    $started     = if ($_.Started) { $_.Started } else { 'N/A' }
    $memory      = if ($_.Running) { "$($_.Memory) GB" } else { 'N/A' }
    $pidText     = if ($_.Pid) { $_.Pid } else { 'N/A' }
    @"
    <tr>
      <td class="svc">$($_.Name)</td>
      <td><a href="$($_.Url)" target="_blank">$($_.Url)</a></td>
      <td class="$statusClass">$($_.Status)</td>
      <td class="$liveClass">$liveText</td>
      <td>$started</td>
      <td>$memory</td>
      <td>$pidText</td>
    </tr>
"@
}) -join "`n"

$dbRows = ($results | ForEach-Object {
    $dbClass = switch ($_.DbStatus) {
        'Reachable'         { 'ok' }
        'In-Memory (Up)'     { 'ok' }
        'N/A'                { '' }
        default              { 'bad' }
    }
    $dbTarget = if ($_.DbHost) { "$($_.DbHost):$($_.DbPort)" } else { 'N/A' }
    @"
    <tr>
      <td class="svc">$($_.Name)</td>
      <td>$($_.DbType)</td>
      <td>$dbTarget</td>
      <td>$($_.DbName)</td>
      <td>$($_.DbUser)</td>
      <td class="pw-cell" data-pw="$($_.DbPass)">********</td>
      <td class="$dbClass">$($_.DbStatus)</td>
    </tr>
"@
}) -join "`n"

$generatedAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')

$html = @"
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Microservices Dashboard</title>
<style>
  body { font-family: Segoe UI, Arial, sans-serif; background: #1e1e2e; color: #e4e4e4; margin: 0; padding: 32px; }
  h1 { margin: 0 0 4px; }
  .meta { color: #9a9ab0; margin-bottom: 20px; font-size: 13px; }
  .toolbar { display: flex; align-items: center; gap: 24px; margin-bottom: 20px; flex-wrap: wrap; }
  .tabs { display: flex; gap: 8px; }
  .tab-btn { background: #27273a; color: #e4e4e4; border: 1px solid #3a3a52; padding: 8px 18px; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600; }
  .tab-btn.active { background: #7aa2ff; color: #14141f; border-color: #7aa2ff; }
  .refresh-btn { background: #34d058; color: #0b1f0f; border: none; padding: 8px 18px; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 700; }
  .refresh-btn:hover { background: #2fc350; }
  .auto-refresh { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #c8c8dc; }
  .countdown { font-variant-numeric: tabular-nums; font-weight: 700; color: #7aa2ff; min-width: 20px; display: inline-block; }
  .switch { position: relative; display: inline-block; width: 42px; height: 22px; }
  .switch input { opacity: 0; width: 0; height: 0; }
  .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #4a4a63; transition: .2s; border-radius: 22px; }
  .slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: .2s; border-radius: 50%; }
  input:checked + .slider { background-color: #34d058; }
  input:checked + .slider:before { transform: translateX(20px); }
  table { border-collapse: collapse; width: 100%; background: #27273a; box-shadow: 0 2px 8px rgba(0,0,0,0.3); display: none; }
  table.visible { display: table; }
  th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #3a3a52; font-size: 14px; }
  th { background: #32324a; color: #ffffff; font-weight: 600; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; cursor: pointer; user-select: none; white-space: nowrap; }
  th:hover { background: #3d3d58; }
  th .arrow { font-size: 10px; opacity: 0.7; margin-left: 4px; }
  tr:hover { background: #32324a; }
  td.svc { font-weight: 600; }
  a { color: #7aa2ff; text-decoration: none; }
  a:hover { text-decoration: underline; }
  .ok { color: #34d058; font-weight: 700; }
  .bad { color: #ff5c5c; font-weight: 700; }
</style>
</head>
<body>
  <h1>Microservices Dashboard</h1>
  <div class="meta">Generated at $generatedAt -- run generate-dashboard.ps1 (or generate-dashboard.bat) to refresh data</div>

  <div class="toolbar">
    <div class="tabs">
      <button class="tab-btn active" id="tab-instances" onclick="showTab('instances')">Instances</button>
      <button class="tab-btn" id="tab-databases" onclick="showTab('databases')">Databases</button>
    </div>
    <button class="refresh-btn" onclick="location.reload()">Refresh</button>
    <button class="refresh-btn" id="pwToggleBtn" style="background:#7aa2ff;color:#14141f;" onclick="togglePasswords()">Show Passwords</button>
    <div class="auto-refresh">
      <label class="switch">
        <input type="checkbox" id="autoRefreshToggle" onchange="toggleAutoRefresh()">
        <span class="slider"></span>
      </label>
      <span>Auto-refresh every 5s</span>
      <span id="countdown" class="countdown"></span>
    </div>
  </div>

  <table id="instances-table" class="visible">
    <thead>
      <tr>
        <th onclick="sortTable('instances-table',0,'text')">Service<span class="arrow"></span></th>
        <th onclick="sortTable('instances-table',1,'text')">URL<span class="arrow"></span></th>
        <th onclick="sortTable('instances-table',2,'text')">Status<span class="arrow"></span></th>
        <th onclick="sortTable('instances-table',3,'text')">Live<span class="arrow"></span></th>
        <th onclick="sortTable('instances-table',4,'text')">Started<span class="arrow"></span></th>
        <th onclick="sortTable('instances-table',5,'num')">Storage (Memory)<span class="arrow"></span></th>
        <th onclick="sortTable('instances-table',6,'num')">PID<span class="arrow"></span></th>
      </tr>
    </thead>
    <tbody>
$instanceRows
    </tbody>
  </table>

  <table id="databases-table">
    <thead>
      <tr>
        <th onclick="sortTable('databases-table',0,'text')">Service<span class="arrow"></span></th>
        <th onclick="sortTable('databases-table',1,'text')">DB Type<span class="arrow"></span></th>
        <th onclick="sortTable('databases-table',2,'text')">Host:Port<span class="arrow"></span></th>
        <th onclick="sortTable('databases-table',3,'text')">Database Name<span class="arrow"></span></th>
        <th onclick="sortTable('databases-table',4,'text')">Username<span class="arrow"></span></th>
        <th>Password</th>
        <th onclick="sortTable('databases-table',6,'text')">DB Status<span class="arrow"></span></th>
      </tr>
    </thead>
    <tbody>
$dbRows
    </tbody>
  </table>

<script>
  function showTab(name) {
    document.getElementById('instances-table').classList.toggle('visible', name === 'instances');
    document.getElementById('databases-table').classList.toggle('visible', name === 'databases');
    document.getElementById('tab-instances').classList.toggle('active', name === 'instances');
    document.getElementById('tab-databases').classList.toggle('active', name === 'databases');
    localStorage.setItem('dashboardTab', name);
  }

  var sortState = {};
  function sortTable(tableId, colIndex, type) {
    var table = document.getElementById(tableId);
    var tbody = table.querySelector('tbody');
    var rows = Array.prototype.slice.call(tbody.querySelectorAll('tr'));
    var key = tableId + '-' + colIndex;
    var asc = !(sortState[key] === 'asc');
    sortState[key] = asc ? 'asc' : 'desc';

    rows.sort(function (a, b) {
      var av = a.children[colIndex].innerText.trim();
      var bv = b.children[colIndex].innerText.trim();
      if (type === 'num') {
        av = parseFloat(av) || 0;
        bv = parseFloat(bv) || 0;
        return asc ? av - bv : bv - av;
      }
      return asc ? av.localeCompare(bv) : bv.localeCompare(av);
    });

    rows.forEach(function (r) { tbody.appendChild(r); });

    table.querySelectorAll('th .arrow').forEach(function (el) { el.textContent = ''; });
    table.querySelectorAll('th')[colIndex].querySelector('.arrow').textContent = asc ? ' (asc)' : ' (desc)';
  }

  var passwordsVisible = false;
  function togglePasswords() {
    passwordsVisible = !passwordsVisible;
    document.querySelectorAll('.pw-cell').forEach(function (el) {
      el.textContent = passwordsVisible ? el.getAttribute('data-pw') : '********';
    });
    document.getElementById('pwToggleBtn').textContent = passwordsVisible ? 'Hide Passwords' : 'Show Passwords';
  }

  var REFRESH_SECONDS = 5;
  var autoRefreshTimer = null;
  var countdownTimer = null;
  var secondsLeft = REFRESH_SECONDS;

  function updateCountdownDisplay() {
    document.getElementById('countdown').textContent = '(' + secondsLeft + 's)';
  }

  function toggleAutoRefresh() {
    var on = document.getElementById('autoRefreshToggle').checked;
    localStorage.setItem('autoRefresh', on ? '1' : '0');
    if (on) {
      secondsLeft = REFRESH_SECONDS;
      updateCountdownDisplay();
      autoRefreshTimer = setInterval(function () { location.reload(); }, REFRESH_SECONDS * 1000);
      countdownTimer = setInterval(function () {
        secondsLeft = secondsLeft > 0 ? secondsLeft - 1 : 0;
        updateCountdownDisplay();
      }, 1000);
    } else {
      if (autoRefreshTimer) { clearInterval(autoRefreshTimer); autoRefreshTimer = null; }
      if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null; }
      document.getElementById('countdown').textContent = '';
    }
  }

  (function init() {
    var savedTab = localStorage.getItem('dashboardTab') || 'instances';
    showTab(savedTab);
    if (localStorage.getItem('autoRefresh') === '1') {
      document.getElementById('autoRefreshToggle').checked = true;
      toggleAutoRefresh();
    }
  })();
</script>
</body>
</html>
"@

$outPath = Join-Path $PSScriptRoot 'services-dashboard.html'
Set-Content -Path $outPath -Value $html -Encoding UTF8
Write-Host "Dashboard generated: $outPath"
