#!/bin/bash

# 1️⃣ 기존 Docker 컨테이너 종료
echo "Stopping existing containers..."
docker compose down

# 2️⃣ Docker 컨테이너 다시 시작
echo "Starting containers..."
docker compose up --build -d

# 3️⃣ RabbitMQ가 정상 기동될 때까지 대기
echo "Waiting for RabbitMQ to be ready..."
while ! docker exec seqism-mq rabbitmqctl status > /dev/null 2>&1; do
  echo "RabbitMQ is not ready yet. Checking again in 1 second..."
  sleep 1
done
echo "RabbitMQ is ready!"

# 4️⃣ Gateway가 준비될 때까지 대기
echo "Waiting for Gateway to be ready..."
until curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; do
  echo "Gateway is not ready yet. Checking again in 1 second..."
  sleep 1
done
echo "Gateway is ready!"

# 5️⃣ Gateway로 테스트 메시지 전송
echo "Sending test message to Gateway..."
curl -X POST http://localhost:8080/api/send -H "Content-Type: application/json" -d '{"message": "Hello, Seqism!"}'

echo "Test completed!"