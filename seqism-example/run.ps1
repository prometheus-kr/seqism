# Function Declaration Section =================================================
function Convert-PathForDocker($path) {
    # C:\ABC\XYZ -> /c/ABC/XYZ
    $strPath = [string]$path -replace '\\', '/'
    if ($strPath -match '^([A-Za-z]):') {
        $drive = $matches[1].ToLower()
        $dockerPath = $strPath -replace '^([A-Za-z]):', "/$drive"
    } else {
        $dockerPath = $strPath
    }
    return $dockerPath
}

function Build-Module($moduleName) {
    if (-not (Test-Path $moduleName)) {
        throw "Directory not found: $moduleName"
    }
    $dockerPath = Convert-PathForDocker "$((Resolve-Path $moduleName).Path)"
    $m2Path = Convert-PathForDocker "$env:USERPROFILE/.m2"
    docker run --rm -v ${m2Path}:/root/.m2 -v ${dockerPath}:/app -w /app maven:3.9.6-eclipse-temurin-17 mvn clean install
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed in $moduleName"
    }
}
# ==============================================================================


# 1️⃣ 전체 빌드
Write-Host "Building all Maven modules using Docker..."
Build-Module "ex-gateway"
Build-Module "ex-processor"

# 2️⃣ 기존 Docker 컨테이너 종료
Write-Host "Stopping existing containers..."
docker compose down

# 3️⃣ Docker 컨테이너 다시 시작
Write-Host "Starting containers..."
docker compose up --build -d

# 4️⃣ RabbitMQ가 정상 기동될 때까지 대기
Write-Host "Waiting for RabbitMQ to be ready..."
do {
    Start-Sleep -Seconds 1
    $status = docker exec seqism-mq rabbitmqctl status 2>&1
    if ($status -match "Error") {
        Write-Host "RabbitMQ is not ready yet. Checking again in 1 second..."
    }
} until ($status -match "Status of node")
Write-Host "RabbitMQ is ready!"

# 5️⃣ Gateway가 준비될 때까지 대기
Write-Host "Waiting for Gateway to be ready..."
do {
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method "Get" -TimeoutSec 2
        $ready = $response.status -eq "UP"
    } catch {
        $ready = $false
    }
} until ($ready)
Write-Host "Gateway is ready!"

# 6️⃣ test.ps1 실행
.\test.ps1