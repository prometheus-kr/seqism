
# 1️⃣ 루트에서 전체 빌드
Write-Host "Building all Maven modules (including seqism-common)..."

Set-Location seqism-common
mvn clean install
Set-Location ..

Set-Location seqism-gateway
mvn clean install
Set-Location ..

Set-Location seqism-processor
mvn clean install
Set-Location ..