param(
    [switch]$testOnly
)

if (-not $testOnly) {
    # 1️⃣ 루트에서 전체 빌드
    Write-Host "Building all Maven modules (including seqism-common)..."
    mvn clean install

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
}

# 6️⃣ Gateway로 테스트 메시지 전송
Write-Host "Sending test message to Gateway..."
for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{"header":{"bizCode":"Sample001"}, "body":"Hello, Seqism!"}'
    $r1 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    $r2 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    $r3 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    Write-Host ($response | ConvertTo-Json)
    Write-Host ($r1 | ConvertTo-Json)
    Write-Host ($r2 | ConvertTo-Json)
    Write-Host ($r3 | ConvertTo-Json)
}

Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{"header":{"bizCode":"XXX"}, "body":"error will occur!"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)

Write-Host "Test completed!"