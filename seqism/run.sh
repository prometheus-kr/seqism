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

echo "Building all Maven modules using Docker..."

build_module "seqism-common"
build_module "seqism-gateway"
build_module "seqism-processor"
build_module "seqism-gateway-starter"
build_module "seqism-processor-starter"