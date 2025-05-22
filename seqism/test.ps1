function Invoke-SeqismRestMethod {
    param(
        [string]$Uri,
        [string]$Method = "Post",
        [string]$ContentType = "application/json",
        $Body
    )
    $resp = Invoke-WebRequest -Uri $Uri -Method $Method -ContentType $ContentType -Body $Body -SkipHttpErrorCheck
    try {
        return $resp.Content | ConvertFrom-Json -Depth 20
    } catch {
        return $resp.Content
    }
}

function Remove-FieldsRecursive {
    param($obj, [string[]]$fieldsToRemove)
    if ($null -eq $obj) { return }
    if ($obj -is [System.Collections.IEnumerable] -and -not ($obj -is [string])) {
        foreach ($item in $obj) {
            Remove-FieldsRecursive $item $fieldsToRemove
        }
    } elseif ($obj -is [psobject]) {
        foreach ($field in $fieldsToRemove) {
            if ($obj.PSObject.Properties[$field]) {
                $obj.PSObject.Properties.Remove($field)
            }
        }
        foreach ($prop in $obj.PSObject.Properties) {
            Remove-FieldsRecursive $prop.Value $fieldsToRemove
        }
    }
}

function Test-SeqismResponse {
    param(
        $actualResp,
        $expectedResp,
        [string[]]$ignoreFields = @("tranId", "timestamp")
    )
    if ($null -eq $actualResp) {
        Write-Host "응답이 없습니다." -ForegroundColor Red
        return
    }

    # 깊은 복사 후 무시할 필드 제거
    $actualCopy = $actualResp | ConvertTo-Json -Depth 20 | ConvertFrom-Json
    $expectedCopy = $expectedResp | ConvertFrom-Json
    Remove-FieldsRecursive $actualCopy $ignoreFields
    Remove-FieldsRecursive $expectedCopy $ignoreFields

    $ok = ($actualCopy | ConvertTo-Json -Depth 20 -Compress) -eq ($expectedCopy | ConvertTo-Json -Depth 20 -Compress)

    if ($ok) {
        Write-Host "✅ 예상과 일치"
    } else {
        Write-Host "❌ 전체 응답 불일치!`n예상: $($expectedCopy | ConvertTo-Json -Depth 20)`n실제: $($actualCopy | ConvertTo-Json -Depth 20)" -ForegroundColor Red
        exit
    }
}

# 6️⃣ Gateway로 테스트 메시지 전송
Write-Host "Sending test message to Gateway..."

for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"Sample001"}, "body":"Hello, Seqism!"}'
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "da46e17d-8a57-4c85-996a-bf64f39126b3",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": "=====> Command_1111"
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "bc486dc0-89b8-4bdc-8fd0-0e86c4240e4e",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_2222"
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "1a019975-6ebd-4b8a-9a56-ca652dff1037",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_3333"
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample001";tranId=$response.header.tranId;}; body="Hello, Seqism!"} | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "85d5ce35-372c-4f04-8e2a-502657a0a7da",
            "status": "SUCCESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_4444"
    }'
    Test-SeqismResponse $response $expected
}
for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"Sample002"}, "body":{"log":"what??", "step":11}}'
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "395c79eb-a68f-4972-bca8-474c8cb3eed4",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "log": "[Sample002] Step1",
            "step": 1
        }
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample002";tranId=$response.header.tranId;}; body=@{log="what??"; step=12}} | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "8f4499e5-7fb7-43ea-ac01-a71929834131",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "log": "what?? -> [Sample002] Step2",
            "step": 2
        }
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample002";tranId=$response.header.tranId;}; body=@{log="what??"; step=13}} | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "61577df6-2632-4dad-b711-17486e3acd75",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "log": "what?? -> [Sample002] Step3",
            "step": 3
        }
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample002";tranId=$response.header.tranId;}; body=@{log="what??"; step=14}} | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "1cccdaed-bbbe-4f3e-b784-b5384b61886c",
            "status": "SUCCESS",
            "error": null
        },
        "body": {
            "log": "what?? -> [Sample002] Done",
            "step": 4
        }
    }'
    Test-SeqismResponse $response $expected
}

for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{
        "header":{"bizCode":"Sample003"},
        "body":{
            "userId":"userA",
            "transactions":[],
            "meta":{"requestIp":"127.0.0.1","deviceType":"PC"}
        }
    }'
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "9960ff65-a22e-4310-9ab4-d268fa0aac1e",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "userId": "userA",
            "transactions": [
            {
                "txnId": "TXN-001",
                "amount": 1000,
                "timestamp": "2025-05-22T06:18:25",
                "status": "INIT"
            }
            ],
            "meta": {
            "requestIp": "127.0.0.1",
            "deviceType": "PC"
            }
        }
    }'
    Test-SeqismResponse $response $expected

    $transactions = @(
        [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
    )
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
        header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
        body = @{
            userId = "userA"
            transactions = $transactions
            meta = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
        }
    } | ConvertTo-Json -Depth 20)
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "00cce1d6-3c66-4ff2-8166-88d7d7babe69",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "userId": "userA",
            "transactions": [
            {
                "txnId": "TXN-001",
                "amount": 1000,
                "timestamp": "2025-05-20T23:16:15",
                "status": "STEP2"
            }
            ],
            "meta": {
            "requestIp": "127.0.0.1",
            "deviceType": "PC"
            }
        }
    }'
    Test-SeqismResponse $response $expected

    $transactions = @(
        [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
        [PSCustomObject]@{ txnId = $response.body.transactions[1].txnId; amount = $response.body.transactions[1].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[1].status }
    )
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
        header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
        body = @{
            userId = "userA"
            transactions = $transactions
            meta = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
        }
    } | ConvertTo-Json -Depth 20)
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "5704e00e-08b6-409a-97cb-171f68868244",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "userId": "userA",
            "transactions": [
            {
                "txnId": "TXN-001",
                "amount": 1000,
                "timestamp": "2025-05-20T23:16:15",
                "status": "STEP2"
            },
            {
                "txnId": null,
                "amount": 0,
                "timestamp": "2025-05-20T23:16:15",
                "status": null
            },
            {
                "txnId": "TXN-002",
                "amount": 2000,
                "timestamp": "2025-05-22T06:20:36",
                "status": "STEP3"
            }
            ],
            "meta": {
            "requestIp": "127.0.0.1",
            "deviceType": "PC"
            }
        }
    }'
    Test-SeqismResponse $response $expected

    $transactions = @(
        [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
        [PSCustomObject]@{ txnId = $response.body.transactions[1].txnId; amount = $response.body.transactions[1].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[1].status }
        [PSCustomObject]@{ txnId = $response.body.transactions[2].txnId; amount = $response.body.transactions[2].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[2].status }
    )
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
        header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
        body = @{
            userId = "userA"
            transactions = $transactions
            meta = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
        }
    } | ConvertTo-Json -Depth 20)
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "04cf62e1-e977-498a-a1d8-45e48467986d",
            "status": "SUCCESS",
            "error": null
        },
        "body": {
            "userId": "userA",
            "transactions": [
            {
                "txnId": "TXN-001",
                "amount": 1000,
                "timestamp": "2025-05-20T23:16:15",
                "status": "DONE"
            },
            {
                "txnId": null,
                "amount": 0,
                "timestamp": "2025-05-20T23:16:15",
                "status": "DONE"
            },
            {
                "txnId": "TXN-002",
                "amount": 2000,
                "timestamp": "2025-05-20T23:16:15",
                "status": "DONE"
            }
            ],
            "meta": {
            "requestIp": "127.0.0.1",
            "deviceType": "PC"
            }
        }
    }'
    Test-SeqismResponse $response $expected
}

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"XXX"}, "body":"error will occur!"}'
$expected = '{
  "header": {
    "bizCode": "XXX",
    "tranId": "5f384fd9-d91a-489f-a057-f67f0661d6e4",
    "status": "FAILURE",
    "error": {
      "errorCode": "00020001",
      "errorMessage": "BP Error : No processor found for bizCode : XXX"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample001";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample001",
    "tranId": "xxx",
    "status": "FAILURE",
    "error": {
      "errorCode": "00010003",
      "errorMessage": "GW Error : Queue does not exist"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample002";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample002",
    "tranId": "xxx",
    "status": "FAILURE",
    "error": {
      "errorCode": "00010003",
      "errorMessage": "GW Error : Queue does not exist"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header=@{bizCode="Sample003";tranId="xxx";}; body="error will occur!"} | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample003",
    "tranId": "xxx",
    "status": "FAILURE",
    "error": {
      "errorCode": "00010003",
      "errorMessage": "GW Error : Queue does not exist"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected



$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{
    "header":{"bizCode":"Sample003"},
    "body":{
        "xxxxx":"xxxxxxx",
        "transactions":[],
        "meta":{"requestIp":"127.0.0.1","xxxx":"xxxxxx"}
    }
}'
$expected = '{
  "header": {
    "bizCode": "Sample003",
    "tranId": "bb3bafa0-d534-4afa-b0d3-cb75d6e9e3a7",
    "status": "IN_PROGRESS",
    "error": null
  },
  "body": {
    "userId": null,
    "transactions": [
      {
        "txnId": "TXN-001",
        "amount": 1000,
        "timestamp": "2025-05-22T05:25:00",
        "status": "INIT"
      }
    ],
    "meta": {
      "requestIp": "127.0.0.1",
      "deviceType": null
    }
  }
}'
Test-SeqismResponse $response $expected

$transactions = @(
    [PSCustomObject]@{ txnId = $response.body.transactions[0].txnId; amount = $response.body.transactions[0].amount; timestamp = "2025-05-20T23:16:15"; status = $response.body.transactions[0].status }
)
$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
    header = @{ bizCode = "Sample003"; tranId = $response.header.tranId }
    body = @{
        dummy = "dummy"
        transactions = $transactions
    }
} | ConvertTo-Json -Depth 5)
 $expected = '{
  "header": {
    "bizCode": "Sample003",
    "tranId": "fe020ca6-c13d-4844-b243-13e02574f8be",
    "status": "IN_PROGRESS",
    "error": null
  },
  "body": {
    "userId": null,
    "transactions": [
      {
        "txnId": "TXN-001",
        "amount": 1000,
        "timestamp": "2025-05-20T23:16:15",
        "status": "STEP2"
      }
    ],
    "meta": null
  }
}'
Test-SeqismResponse $response $expected

Write-Host "Test completed!"