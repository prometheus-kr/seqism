# 1️⃣ 기존 Docker 컨테이너 종료
Write-Host "Stopping existing containers..."
docker compose down

# 2️⃣ Docker 컨테이너 다시 시작
Write-Host "Starting containers..."
docker compose up --build -d

# 3️⃣ RabbitMQ가 정상 기동될 때까지 대기
Write-Host "Waiting for RabbitMQ to be ready..."
do {
    Start-Sleep -Seconds 1
    $status = docker exec seqism-mq rabbitmqctl status 2>&1
    if ($status -match "Error") {
        Write-Host "RabbitMQ is not ready yet. Checking again in 1 second..."
    }
} until ($status -match "Status of node")
Write-Host "RabbitMQ is ready!"

# 4️⃣ Gateway가 준비될 때까지 대기
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

# 5️⃣ Gateway로 테스트 메시지 전송
Write-Host "Sending test message to Gateway..."
Invoke-RestMethod -Uri "http://localhost:8080/api/send" -Method "Post" -ContentType "application/json" -Body '{"message": "Hello, Seqism!"}'

Write-Host "Test completed!"