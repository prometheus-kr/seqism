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
    $modulePath = Convert-PathForDocker "$((Resolve-Path $moduleName).Path)"
    $m2Path = Convert-PathForDocker "$env:USERPROFILE/.m2"
    $dockerCmd = "docker run --rm -v ${m2Path}:/root/.m2 -v ${modulePath}:/app -w /app maven:3.9.6-eclipse-temurin-17 mvn clean install"
    Write-Host "👉 Executing: $dockerCmd"
    Invoke-Expression $dockerCmd
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed in $moduleName"
    }
}
# ==============================================================================


# ==============================================================================
# 1️⃣ 전체 빌드
Write-Host "================================================================"
Write-Host "👉 Building all Maven modules using Docker..."

Build-Module "seqism-common"
Build-Module "seqism-gateway"
Build-Module "seqism-processor"
Build-Module "seqism-gateway-starter"
Build-Module "seqism-processor-starter"

Write-Host "================================================================"
Write-Host "👌 All Maven modules built successfully."
# ==============================================================================
