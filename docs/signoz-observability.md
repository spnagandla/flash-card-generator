# Observability with SigNoz (Open-Source Datadog Alternative)

This project is instrumented with the **Cloud Native Computing Foundation (CNCF) OpenTelemetry Java Agent** to stream distributed traces, application metrics, and correlated logs into **SigNoz**, a full-featured, open-source Datadog alternative.

---

## 1. What SigNoz Provides (Like Datadog)

| Feature | What you see in SigNoz |
| :--- | :--- |
| **Service Map** | A live visual diagram showing `Client -> Flashcard API -> Redis -> Gemini AI -> Firebase`. |
| **Distributed Tracing** | Flamegraphs showing how many milliseconds were spent inside `PdfParser.parse()`, calling Google Gemini, or saving to Firestore. |
| **Unified Logs** | All `log.info` and `log.error` statements automatically linked to the exact HTTP request that caused them. |
| **Performance Metrics** | Real-time RPS (requests per second), 5xx error rate, and P50/P90/P99 latency graphs. |

---

## 2. Quick Start Guide

### Step 1: Start Docker Desktop
Ensure **Docker Desktop** is running on your machine (verify the green engine icon in the Windows taskbar).

### Step 2: Start SigNoz
Run the included PowerShell helper script:
```powershell
./scripts/start-signoz.ps1
```
This script:
1. Verifies Docker Desktop is running.
2. Initializes the standalone SigNoz containers (ClickHouse, OpenTelemetry Collector, Query Service, and Web UI).
3. Exposes the SigNoz Dashboard at **http://localhost:3301**.

### Step 3: Run Flashcard Generator with OpenTelemetry
In a separate terminal, launch the application using:
```powershell
./scripts/run-with-signoz.ps1
```
This script:
1. Downloads the official `opentelemetry-javaagent.jar` if not already present.
2. Injects the agent via the JVM arguments (`-javaagent:...`).
3. Starts the Spring Boot backend, automatically streaming telemetry to `localhost:4317` (OTLP gRPC).

---

## 3. Exploring Your Application in SigNoz

Open **http://localhost:3301** in your browser:

### 1. Services Tab
- Select `flash-card-generator`.
- View incoming request rate, error percentage, and latency percentiles.

### 2. Traces Tab
- Submit a document in the React UI or trigger `POST /api/flashcards/upload`.
- Click on the trace for `/api/flashcards/upload`.
- Inspect the breakdown:
  - Time spent in `JobService.createJob` (Redis)
  - Time spent in `ParserService.parseDocument` (PDF/Word/PPT extraction)
  - Time spent calling Google Gemini (`ChatClient.call`)
  - Time spent in `FirebaseService.saveDeck`

### 3. Logs Tab
- Filter logs by service (`service.name = "flash-card-generator"`) or severity (`level = "ERROR"`).
- Click any log line to jump straight to the trace that generated it!

---

## 4. Stopping SigNoz

When you are done testing, you can stop the SigNoz containers using:
```powershell
cd .signoz/deploy/docker/clickhouse-setup
docker compose down
```
*(Or stop the containers directly from the Docker Desktop GUI).*
