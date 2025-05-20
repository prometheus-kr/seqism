#!/bin/bash

if [ "$1" != "--testOnly" ]; then
  # 1️⃣ 루트에서 전체 빌드
  echo "Building all Maven modules (including seqism-common)..."
  mvn clean install

  # 2️⃣ 기존 Docker 컨테이너 종료
  echo "Stopping existing containers..."
  docker compose down

  # 3️⃣ Docker 컨테이너 다시 시작
  echo "Starting containers..."
  docker compose up --build -d

  # 4️⃣ RabbitMQ가 정상 기동될 때까지 대기
  echo "Waiting for RabbitMQ to be ready..."
  while ! docker exec seqism-mq rabbitmqctl status > /dev/null 2>&1; do
    echo "RabbitMQ is not ready yet. Checking again in 1 second..."
    sleep 1
  done
  echo "RabbitMQ is ready!"

  # 5️⃣ Gateway가 준비될 때까지 대기
  echo "Waiting for Gateway to be ready..."
  until curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; do
    echo "Gateway is not ready yet. Checking again in 1 second..."
    sleep 1
  done
  echo "Gateway is ready!"
fi

# 6️⃣ Gateway로 테스트 메시지 전송
echo "Sending test message to Gateway..."
for i in {1..2}
do
  init_response=$(curl -s -X POST http://localhost:8080/api/init -H "Content-Type: application/json" -d '{"bizCode":"BankIC", "message": "Hello, Seqism!"}')
  tranId=$(echo "$init_response" | grep -oP '"tranId"\s*:\s*"\K[^"]+')
  r1=$(curl -s -X POST http://localhost:8080/api/next -H "Content-Type: application/json" -d "{\"tranId\":\"$tranId\", \"bizCode\":\"BankIC\", \"message\":\"Hello, Seqism!\"}")
  r2=$(curl -s -X POST http://localhost:8080/api/next -H "Content-Type: application/json" -d "{\"tranId\":\"$tranId\", \"bizCode\":\"BankIC\", \"message\":\"Hello, Seqism!\"}")
  echo "$init_response"
  echo "$r1"
  echo "$r2"
done

echo "Test completed!"