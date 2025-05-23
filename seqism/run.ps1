# Function Declaration Section =================================================
function Build-Module($moduleName) {
    if (-not (Test-Path $moduleName)) {
        throw "Directory not found: $moduleName"
    }
    Set-Location $moduleName
    mvn clean install
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed in $moduleName"
    }
    Set-Location ..
}
# ==============================================================================

# 1️⃣ 루트에서 전체 빌드
Write-Host "Building all Maven modules..."

$currentDir = Get-Location

try {
    Build-Module "seqism-common"
    Build-Module "seqism-gateway"
    Build-Module "seqism-processor"
    Build-Module "seqism-gateway-starter"
    Build-Module "seqism-processor-starter"
}
finally {
    Set-Location $currentDir
}