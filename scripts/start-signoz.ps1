# PowerShell script to launch SigNoz (Open-Source Datadog alternative)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\.."
$SignozDir = "$ProjectRoot\.signoz"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Starting SigNoz (Open-Source Datadog Alternative)" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Verify Docker is installed and running
try {
    $dockerCheck = docker ps 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Docker is not running or Docker Desktop is closed." -ForegroundColor Red
        Write-Host "👉 Please launch 'Docker Desktop' from your Windows Start menu." -ForegroundColor Yellow
        Write-Host "   Once Docker Desktop shows 'Engine running' (green icon), run this script again." -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "❌ Docker command not found. Please ensure Docker Desktop is installed." -ForegroundColor Red
    exit 1
}

# 2. Download / clone official SigNoz standalone repository if not present
if (-not (Test-Path "$SignozDir\deploy")) {
    Write-Host "Downloading SigNoz standalone deployment configuration..." -ForegroundColor Cyan
    git clone --depth 1 https://github.com/SigNoz/signoz.git $SignozDir
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to clone SigNoz deployment repository. Please check your internet connection." -ForegroundColor Red
        exit 1
    }
}

# 3. Locate the docker-compose file inside SigNoz
$ComposePath = "$SignozDir\deploy\docker\clickhouse-setup\docker-compose.yaml"
if (-not (Test-Path $ComposePath)) {
    # Fallback to alternate compose location
    $ComposePath = "$SignozDir\deploy\docker-compose.yaml"
}

if (-not (Test-Path $ComposePath)) {
    Write-Host "Looking for SigNoz compose file in $SignozDir\deploy..." -ForegroundColor Yellow
    $found = Get-ChildItem -Path "$SignozDir\deploy" -Filter "*docker-compose*.y*ml" -Recurse | Select-Object -First 1
    if ($found) {
        $ComposePath = $found.FullName
    }
}

if (-not (Test-Path $ComposePath)) {
    Write-Host "❌ Could not find SigNoz docker-compose file in $SignozDir\deploy." -ForegroundColor Red
    exit 1
}

$ComposeDir = Split-Path -Parent $ComposePath

Write-Host "Launching SigNoz containers using Docker Compose..." -ForegroundColor Cyan
Set-Location $ComposeDir
docker compose -f $ComposePath up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host " 🎉 SigNoz is running successfully!" -ForegroundColor Green
    Write-Host " 🌐 Dashboard URL:    http://localhost:3301" -ForegroundColor Yellow
    Write-Host " 📡 OTLP Ingestion:   localhost:4317 (gRPC) / localhost:4318 (HTTP)" -ForegroundColor Yellow
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "👉 Next step: Run ./scripts/run-with-signoz.ps1 to start your app" -ForegroundColor Cyan
} else {
    Write-Host "❌ Docker Compose encountered an error while starting SigNoz." -ForegroundColor Red
}
