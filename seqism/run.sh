#!/bin/bash

# Function Declaration Section =================================================
build_module() {
    local module_name="$1"
    if [ ! -d "$module_name" ]; then
        echo "Directory not found: $module_name"
        exit 1
    fi
    cd "$module_name"
    mvn clean install
    if [ $? -ne 0 ]; then
        echo "Build failed in $module_name"
        exit 1
    fi
    cd ..
}
# ==============================================================================

# 1️⃣ 루트에서 전체 빌드
echo "Building all Maven modules..."

CUR_DIR=$(pwd)

build_module "seqism-common"
build_module "seqism-gateway"
build_module "seqism-processor"
build_module "seqism-gateway-starter"
build_module "seqism-processor-starter"

cd "$CUR_DIR"