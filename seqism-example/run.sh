#!/bin/bash

# Function Declaration Section =================================================
# Convert Windows path to Docker-compatible path (for Git Bash/MSYS)
convert_path_for_docker() {
    local path="$1"
    path="${path//\\//}"
    if [[ "$path" =~ ^([A-Za-z]): ]]; then
        local drive=$(echo "${BASH_REMATCH[1]}" | tr 'A-Z' 'a-z')
        path="/$drive${path:2}"
    fi
    echo "$path"
}

build_module() {
    local module_name="$1"
    if [ ! -d "$module_name" ]; then
        echo "Directory not found: $module_name"
        exit 1
    fi
    abs_path="$(pwd)/$module_name"

    unameOut="$(uname -s)"
    case "${unameOut}" in
        MINGW*|MSYS*)
            docker_path="$(convert_path_for_docker "$abs_path")"
            m2_path="$HOME/.m2"
            MSYS_NO_PATHCONV=1 docker run --rm -v "$m2_path":/root/.m2 -v "$docker_path":/app -w /app maven:3.9.6-eclipse-temurin-17 mvn clean install
            ;;
        *)
            docker_path="$abs_path"
            m2_path="$HOME/.m2"
            docker run --rm -v "$m2_path":/root/.m2 -v "$docker_path":/app -w /app maven:3.9.6-eclipse-temurin-17 mvn clean install
            ;;
    esac

    if [ $? -ne 0 ]; then
        echo "Build failed in $module_name"
        exit 1
    fi
}
# ==============================================================================

# 1️⃣ 전체 빌드
echo "Building all Maven modules using Docker..."
build_module "ex-gateway"
build_module "ex-processor"

# 2️⃣ 기존 Docker 컨테이너 종료
echo "Stopping existing containers..."
docker compose down

# 3️⃣ Docker 컨테이너 다시 시작
echo "Starting containers..."
docker compose up --build -d

# 4️⃣ RabbitMQ가 정상 기동될 때까지 대기
echo "Waiting for RabbitMQ to be ready..."
while true; do
    sleep 1
    status=$(docker exec seqism-mq rabbitmqctl status 2>&1)
    if [[ "$status" == *"Status of node"* ]]; then
        echo "RabbitMQ is ready!"
        break
    else
        echo "RabbitMQ is not ready yet. Checking again in 1 second..."
    fi
done

# 5️⃣ Gateway가 준비될 때까지 대기
echo "Waiting for Gateway to be ready..."
while true; do
    sleep 1
    response=$(curl -s --max-time 2 http://localhost:8080/actuator/health)
    if [[ "$response" == *'"status":"UP"'* ]]; then
        echo "Gateway is ready!"
        break
    fi
done

# 6️⃣ test.ps1 실행
./test.sh