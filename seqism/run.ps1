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
    $dockerPath = Convert-PathForDocker "$((Resolve-Path $moduleName).Path)"
    $m2Path = Convert-PathForDocker "$env:USERPROFILE/.m2"
    docker run --rm -v ${m2Path}:/root/.m2 -v ${dockerPath}:/app -w /app maven:3.9.6-eclipse-temurin-17 mvn clean install "-Dmaven.source.skip=true" "-Dmaven.javadoc.skip=true" "-Dgpg.skip=true"
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed in $moduleName"
    }
}
# ==============================================================================


Write-Host "Building all Maven modules using Docker..."

Build-Module "seqism-common"
Build-Module "seqism-gateway"
Build-Module "seqism-processor"
Build-Module "seqism-gateway-starter"
Build-Module "seqism-processor-starter"