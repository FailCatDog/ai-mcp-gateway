param(
    [string]$BaseUrl = "http://localhost:8091/api-gateway",
    [string]$GatewayId = "gateway_001",
    [string]$ApiKey = "RS590LKPOD8877DDLMFKS4",
    [string]$ToolName = "JavaSDKMCPClient_getCompanyEmployee",
    [string]$ToolArgumentsJson = '{"xxxRequest01":{"city":"beijing","company":{"name":"jd","type":"internet"}},"xxxRequest02":{"employeeCount":"10"}}',
    [int]$SessionWaitSeconds = 20,
    [switch]$SkipToolCall,
    [switch]$KeepSse
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$streamFile = Join-Path $scriptRoot "mcp-sse-stream.log"
$httpStatusCodes = @()

if (Test-Path $streamFile) {
    Remove-Item $streamFile -Force
}

New-Item -Path $streamFile -ItemType File -Force | Out-Null

$sseUrl = "{0}/{1}/mcp/sse?apiKey={2}" -f $BaseUrl, $GatewayId, $ApiKey
Write-Host "[1/5] Open SSE: $sseUrl"

$curlProcess = Start-Process -FilePath "curl.exe" -ArgumentList @("-N", $sseUrl) -NoNewWindow -RedirectStandardOutput $streamFile -PassThru

function Get-SessionIdFromFile {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return $null }
    $content = Get-Content -Path $Path -Raw
    if ([string]::IsNullOrWhiteSpace($content)) { return $null }
    $match = [regex]::Match($content, 'sessionId=([^&\s"\r\n]+)')
    if ($match.Success) {
        return $match.Groups[1].Value
    }
    return $null
}

$sessionId = $null
$deadline = (Get-Date).AddSeconds($SessionWaitSeconds)
while ((Get-Date) -lt $deadline -and [string]::IsNullOrWhiteSpace($sessionId)) {
    Start-Sleep -Milliseconds 400
    $sessionId = Get-SessionIdFromFile -Path $streamFile
}

if ([string]::IsNullOrWhiteSpace($sessionId)) {
    if (-not $KeepSse -and $curlProcess -and -not $curlProcess.HasExited) {
        Stop-Process -Id $curlProcess.Id -Force
    }
    throw "No sessionId found within ${SessionWaitSeconds}s. Check gatewayId/apiKey/service logs."
}

Write-Host "[2/5] sessionId: $sessionId"

$postUrl = "{0}/{1}/mcp/sse?sessionId={2}&apiKey={3}" -f $BaseUrl, $GatewayId, $sessionId, $ApiKey

function Send-JsonRpc {
    param(
        [string]$Id,
        [string]$Method,
        [object]$Params
    )

    $payload = @{
        jsonrpc = "2.0"
        id = $Id
        method = $Method
        params = $Params
    }

    $body = $payload | ConvertTo-Json -Depth 30 -Compress
    $resp = Invoke-WebRequest -Uri $postUrl -Method Post -ContentType "application/json" -Body $body
    Write-Host "POST $Method -> HTTP $($resp.StatusCode)"
    return [int]$resp.StatusCode
}

Write-Host "[3/5] initialize"
$initParams = @{
    protocolVersion = "2024-11-05"
    capabilities = @{}
    clientInfo = @{
        name = "manual-powershell-client"
        version = "1.0.0"
    }
}
$httpStatusCodes += Send-JsonRpc -Id "1" -Method "initialize" -Params $initParams
Start-Sleep -Milliseconds 500

Write-Host "[4/5] tools/list"
$httpStatusCodes += Send-JsonRpc -Id "2" -Method "tools/list" -Params @{}
Start-Sleep -Milliseconds 500

if (-not $SkipToolCall) {
    Write-Host "[5/5] tools/call"
    $toolArgs = $ToolArgumentsJson | ConvertFrom-Json
    $toolParams = @{
        name = $ToolName
        arguments = $toolArgs
    }
    $httpStatusCodes += Send-JsonRpc -Id "3" -Method "tools/call" -Params $toolParams
} else {
    Write-Host "[5/5] skip tools/call"
}

Start-Sleep -Seconds 2
Write-Host ""
Write-Host "SSE output (last 200 lines):"
Get-Content -Path $streamFile -Tail 200

$sseContent = Get-Content -Path $streamFile -Raw
$allHttpAccepted = ($httpStatusCodes.Count -gt 0) -and ((@($httpStatusCodes | Where-Object { $_ -ne 202 })).Count -eq 0)
$hasError = ($sseContent -match 'event:\s*error') -or ($sseContent -match '"error"\s*:')
$hasResult = $sseContent -match '"result"\s*:'
$isPass = $allHttpAccepted -and $hasResult -and (-not $hasError)

Write-Host ""
if ($isPass) {
    Write-Host "FINAL RESULT: PASS"
} else {
    Write-Host "FINAL RESULT: FAIL"
    Write-Host "- all HTTP 202: $allHttpAccepted"
    Write-Host "- has JSON-RPC result: $hasResult"
    Write-Host "- has error event/object: $hasError"
}

if (-not $KeepSse -and $curlProcess -and -not $curlProcess.HasExited) {
    Stop-Process -Id $curlProcess.Id -Force
    Write-Host ""
    Write-Host "SSE closed."
} else {
    Write-Host ""
    Write-Host "SSE kept alive, processId: $($curlProcess.Id)"
}

if (-not $isPass) {
    exit 1
}
