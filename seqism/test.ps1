function Check-SeqismResponse {
    param(
        $resp,
        [string[]]$expectedStatus = @("IN_PROGRESS", "SUCCESS") # 기본값: 정상 처리
    )
    if ($null -eq $resp) {
        Write-Host "응답이 없습니다." -ForegroundColor Red
        return
    }
    $status = $resp.header.status
    if ($expectedStatus -notcontains $status) {
        Write-Host "❌ 예상과 다른 상태! status: $status, message: $($resp.body)" -ForegroundColor Red
    } else {
        Write-Host "✅ 예상된 정상 상태: $status"
    }
}


Write-Host "Sending test message to Gateway..."

for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{"header":{"bizCode":"Sample001"}, "body":"Hello, Seqism!"}'
    Check-SeqismResponse $response @("IN_PROGRESS")
    Write-Host ($response | ConvertTo-Json -Depth 5)

    $r1 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    Check-SeqismResponse $r1 @("IN_PROGRESS")
    Write-Host ($r1 | ConvertTo-Json -Depth 5)

    $r2 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    Check-SeqismResponse $r2 @("IN_PROGRESS")
    Write-Host ($r2 | ConvertTo-Json -Depth 5)

    $r3 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    Check-SeqismResponse $r3 @("SUCCESS")
    Write-Host ($r3 | ConvertTo-Json -Depth 5)
}

for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{"header":{"bizCode":"Sample002"}, "body":{"log":"what??", "step":11}}'
    Check-SeqismResponse $response @("IN_PROGRESS")
    Write-Host ($response | ConvertTo-Json -Depth 5)

    $r1 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample002";tranId=$response.header.tranId;}; body=@{log="what??"; step=12}} | ConvertTo-Json)
    Check-SeqismResponse $r1 @("IN_PROGRESS")
    Write-Host ($r1 | ConvertTo-Json -Depth 5)

    $r2 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample002";tranId=$response.header.tranId;}; body=@{log="what??"; step=13}} | ConvertTo-Json)
    Check-SeqismResponse $r2 @("IN_PROGRESS")
    Write-Host ($r2 | ConvertTo-Json -Depth 5)

    $r3 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample002";tranId=$response.header.tranId;}; body=@{log="what??"; step=14}} | ConvertTo-Json)
    Check-SeqismResponse $r3 @("SUCCESS")
    Write-Host ($r3 | ConvertTo-Json -Depth 5)
}

for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{
        "header":{"bizCode":"Sample003"},
        "body":{
            "userId":"userA",
            "transactions":[],
            "meta":{"requestIp":"127.0.0.1","deviceType":"PC"}
        }
    }'
    Check-SeqismResponse $response @("IN_PROGRESS")
    Write-Host ($response | ConvertTo-Json -Depth 5)

    $transactions = @(
        [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
    )
    $r1 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{
        header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
        body = @{
            userId = "userA"
            transactions = $transactions
            meta = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
        }
    } | ConvertTo-Json -Depth 5)
    Check-SeqismResponse $r1 @("IN_PROGRESS")
    Write-Host ($r1 | ConvertTo-Json -Depth 5)

    $transactions = @(
        [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
        [PSCustomObject]@{ txnId = $response.body.transactions[1].txnId; amount = $response.body.transactions[1].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[1].status }
    )
    $r2 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{
        header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
        body = @{
            userId = "userA"
            transactions = $transactions
            meta = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
        }
    } | ConvertTo-Json -Depth 5)
    Check-SeqismResponse $r2 @("IN_PROGRESS")
    Write-Host ($r2 | ConvertTo-Json -Depth 5)

    $transactions = @(
        [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
        [PSCustomObject]@{ txnId = $response.body.transactions[1].txnId; amount = $response.body.transactions[1].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[1].status }
        [PSCustomObject]@{ txnId = $response.body.transactions[2].txnId; amount = $response.body.transactions[2].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[2].status }
    )
    $r3 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{
        header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
        body = @{
            userId = "userA"
            transactions = $transactions
            meta = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
        }
    } | ConvertTo-Json -Depth 5)
    Check-SeqismResponse $r3 @("SUCCESS")
    Write-Host ($r3 | ConvertTo-Json -Depth 5)
}

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{"header":{"bizCode":"XXX"}, "body":"error will occur!"}'
Check-SeqismResponse $response @("FAILURE")
Write-Host ($response | ConvertTo-Json -Depth 5)

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample001";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)
Check-SeqismResponse $response @("FAILURE")
Write-Host ($response | ConvertTo-Json -Depth 5)

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample002";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)
Check-SeqismResponse $response @("FAILURE")
Write-Host ($response | ConvertTo-Json -Depth 5)

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{header=@{bizCode="Sample003";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)
Check-SeqismResponse $response @("FAILURE")
Write-Host ($response | ConvertTo-Json -Depth 5)



$response = Invoke-RestMethod -Uri "http://localhost:8080/api/init" -Method "Post" -ContentType "application/json" -Body '{
    "header":{"bizCode":"Sample003"},
    "body":{
        "xxxxx":"xxxxxxx",
        "transactions":[],
        "meta":{"requestIp":"127.0.0.1","xxxx":"xxxxxx"}
    }
}'
Check-SeqismResponse $response @("IN_PROGRESS")
Write-Host ($response | ConvertTo-Json -Depth 5)

$transactions = @(
    [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
)
$r1 = Invoke-RestMethod -Uri "http://localhost:8080/api/next" -Method "Post" -ContentType "application/json" -Body (@{
    header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
    body = @{
        dummy = "dummy"
        transactions = $transactions
    }
} | ConvertTo-Json -Depth 5)
Check-SeqismResponse $r1 @("IN_PROGRESS")
Write-Host ($r1 | ConvertTo-Json -Depth 5)

Write-Host "Test completed!"