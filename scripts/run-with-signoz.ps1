param (
    [string]$OtelVersion = "2.12.0"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\.."
$AgentJar = "$ProjectRoot\opentelemetry-javaagent.jar"

# 1. Download OpenTelemetry Java Agent if not present
if (-not (Test-Path $AgentJar)) {
    Write-Host "Downloading OpenTelemetry Java Agent v$OtelVersion..." -ForegroundColor Cyan
    $Url = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v$OtelVersion/opentelemetry-javaagent.jar"
    Invoke-WebRequest -Uri $Url -OutFile $AgentJar
    Write-Host "Downloaded opentelemetry-javaagent.jar successfully." -ForegroundColor Green
}

# 2. Configure OpenTelemetry Environment Variables
$env:OTEL_SERVICE_NAME = "flash-card-generator"
$env:OTEL_EXPORTER_OTLP_ENDPOINT = "http://localhost:4317"
$env:OTEL_EXPORTER_OTLP_PROTOCOL = "grpc"
$env:OTEL_METRICS_EXPORTER = "otlp"
$env:OTEL_LOGS_EXPORTER = "otlp"

# 3. Locate Java & Maven
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Users\pavan\.jdks\openjdk-22.0.2") {
        $env:JAVA_HOME = "C:\Users\pavan\.jdks\openjdk-22.0.2"
    }
}

$MavenCmd = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.1.2\plugins\maven\lib\maven3\bin\mvn.cmd"
if (-not (Test-Path $MavenCmd)) {
    $MavenCmd = "mvn"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Starting Flashcard Generator with OpenTelemetry Agent" -ForegroundColor Green
Write-Host " Telemetry Endpoint: $env:OTEL_EXPORTER_OTLP_ENDPOINT" -ForegroundColor Yellow
Write-Host " SigNoz Dashboard:   http://localhost:3301" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

Set-Location $ProjectRoot
& $MavenCmd spring-boot:run "-Dspring-boot.run.jvmArguments=-javaagent:$AgentJar"
