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
    }
    catch {
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
    }
    elseif ($obj -is [psobject]) {
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
    }
    else {
        Write-Host "❌ 전체 응답 불일치!`n예상: $($expectedCopy | ConvertTo-Json -Depth 20)`n실제: $($actualCopy | ConvertTo-Json -Depth 20)" -ForegroundColor Red
        exit
    }
}

# 1️⃣ Gateway로 테스트 메시지 전송
Write-Host "Sending test message to Gateway..."



$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"XXX"}, "body":"error will occur!"}'
$expected = '{
  "header": {
    "bizCode": "XXX",
    "tranId": "IGNORE",
    "status": "FAILURE",
    "error": {
      "errorCode": "00020001",
      "errorMessage": "BP Error : No processor found for bizCode : XXX"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample001"; tranId = "xxx"; }; body = "error will occur!" } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample001",
    "tranId": "IGNORE",
    "status": "FAILURE",
    "error": {
      "errorCode": "00010003",
      "errorMessage": "GW Error : Queue does not exist"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample002"; tranId = "xxx"; }; body = "error will occur!" } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample002",
    "tranId": "IGNORE",
    "status": "FAILURE",
    "error": {
      "errorCode": "00010003",
      "errorMessage": "GW Error : Queue does not exist"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample003"; tranId = "xxx"; }; body = "error will occur!" } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample003",
    "tranId": "IGNORE",
    "status": "FAILURE",
    "error": {
      "errorCode": "00010003",
      "errorMessage": "GW Error : Queue does not exist"
    }
  },
  "body": null
}'
Test-SeqismResponse $response $expected



for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"Sample001"}, "body":"Hello, Seqism!"}'
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "IGNORE",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_1111"
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample001"; tranId = $response.header.tranId; }; body = "Hello, Seqism!" } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "IGNORE",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_2222"
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample001"; tranId = $response.header.tranId; }; body = "Hello, Seqism!" } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "IGNORE",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_3333"
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample001"; tranId = $response.header.tranId; }; body = "Hello, Seqism!" } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample001",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": "Hello, Seqism!=====> Command_4444"
    }'
    Test-SeqismResponse $response $expected
}
for ($i = 1; $i -le 2; $i++) { 
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"Sample002"}, "body":{"log":"when??", "step":11}}'
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "IGNORE",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "log": "when?? -> [Sample002] Step1",
            "step": 1
        }
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample002"; tranId = $response.header.tranId; }; body = @{log = "what??"; step = 12 } } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "IGNORE",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "log": "what?? -> [Sample002] Step2",
            "step": 2
        }
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample002"; tranId = $response.header.tranId; }; body = @{log = "who??"; step = 13 } } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "IGNORE",
            "status": "IN_PROGRESS",
            "error": null
        },
        "body": {
            "log": "who?? -> [Sample002] Step3",
            "step": 3
        }
    }'
    Test-SeqismResponse $response $expected

    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{header = @{bizCode = "Sample002"; tranId = $response.header.tranId; }; body = @{log = "where??"; step = 14 } } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Sample002",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": {
            "log": "where?? -> [Sample002] Done",
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
            "tranId": "IGNORE",
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
            body   = @{
                userId       = "userA"
                transactions = $transactions
                meta         = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
            }
        } | ConvertTo-Json -Depth 20)
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "IGNORE",
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
            body   = @{
                userId       = "userA"
                transactions = $transactions
                meta         = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
            }
        } | ConvertTo-Json -Depth 20)
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "IGNORE",
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
            body   = @{
                userId       = "userA"
                transactions = $transactions
                meta         = @{ requestIp = "127.0.0.1"; deviceType = "PC" }
            }
        } | ConvertTo-Json -Depth 20)
    $expected = '{
        "header": {
            "bizCode": "Sample003",
            "tranId": "IGNORE",
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
    "tranId": "IGNORE",
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
        body   = @{
            dummy        = "dummy"
            transactions = $transactions
        }
    } | ConvertTo-Json -Depth 5)
$expected = '{
  "header": {
    "bizCode": "Sample003",
    "tranId": "IGNORE",
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



# Sample004 테스트
$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body (@{
        header = @{ bizCode = "Sample004" }
        body   = @{ number = "42" }
    } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample004",
    "tranId": "IGNORE",
    "status": "IN_PROGRESS",
    "error": null
  },
  "body": {
    "result": "A",
    "ssString": "11111111111"
  }
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
        header = @{ bizCode = "Sample004"; tranId = $response.header.tranId }
        body   = @{ number = "42" }
    } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample004",
    "tranId": "IGNORE",
    "status": "IN_PROGRESS",
    "error": null
  },
  "body": {
    "result": "B",
    "ssString": "22222222222"
  }
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
        header = @{ bizCode = "Sample004"; tranId = $response.header.tranId }
        body   = @{ number = "42" }
    } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample004",
    "tranId": "IGNORE",
    "status": "IN_PROGRESS",
    "error": null
  },
  "body": {
    "result": "C",
    "ssString": "33333333333"
  }
}'
Test-SeqismResponse $response $expected

$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/next" -Body (@{
        header = @{ bizCode = "Sample004"; tranId = $response.header.tranId }
        body   = @{ number = "42" }
    } | ConvertTo-Json)
$expected = '{
  "header": {
    "bizCode": "Sample004",
    "tranId": "IGNORE",
    "status": "SUCCESS",
    "error": null
  },
  "body": {
    "result": "D",
    "ssString": "44444444444"
  }
}'
Test-SeqismResponse $response $expected



# Onetime001 테스트
for ($i = 1; $i -le 2; $i++) {
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/onetime" -Body '{"header":{"bizCode":"Onetime001"}, "body":"Hello, Onetime!"}'
    $expected = '{
        "header": {
            "bizCode": "Onetime001",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": "Hello, Onetime! =====> Onetime Done!"
    }'
    Test-SeqismResponse $response $expected
}

# Onetime002 테스트
for ($i = 1; $i -le 2; $i++) {
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/onetime" -Body (@{
            header = @{ bizCode = "Onetime002" }
            body   = @{ name = "TestItem"; value = 50; status = "PENDING" }
        } | ConvertTo-Json)
    $expected = '{
        "header": {
            "bizCode": "Onetime002",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": {
            "name": "TestItem",
            "value": 100,
            "status": "COMPLETED"
        }
    }'
    Test-SeqismResponse $response $expected
}

# Onetime003 테스트
for ($i = 1; $i -le 2; $i++) {
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/onetime" -Body (@{
            header = @{ bizCode = "Onetime003" }
            body   = @{
                orderId     = "ORD-001"
                items       = @(
                    @{ itemName = "Apple"; price = 1000; quantity = 3; subtotal = 0 },
                    @{ itemName = "Banana"; price = 500; quantity = 5; subtotal = 0 }
                )
                totalAmount = 0
                status      = "PENDING"
            }
        } | ConvertTo-Json -Depth 5)
    $expected = '{
        "header": {
            "bizCode": "Onetime003",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": {
            "orderId": "ORD-001",
            "items": [
                {
                    "itemName": "Apple",
                    "price": 1000,
                    "quantity": 3,
                    "subtotal": 3000
                },
                {
                    "itemName": "Banana",
                    "price": 500,
                    "quantity": 5,
                    "subtotal": 2500
                }
            ],
            "totalAmount": 5500,
            "status": "CONFIRMED"
        }
    }'
    Test-SeqismResponse $response $expected
}

# Onetime 존재하지 않는 bizCode 테스트
$response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/onetime" -Body '{"header":{"bizCode":"XXX"}, "body":"error!"}'
$expected = '{
    "header": {
        "bizCode": "XXX",
        "tranId": "IGNORE",
        "status": "FAILURE",
        "error": {
            "errorCode": "00020001",
            "errorMessage": "BP Error : No processor found for bizCode : XXX"
        }
    },
    "body": null
}'
Test-SeqismResponse $response $expected


# ==========================================
# Init vs Onetime 단일 트랜잭션 속도 비교
# ==========================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " Init vs Onetime 단일 트랜잭션 속도 비교" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 워밍업
Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"InitSingle"}, "body":"warmup"}' | Out-Null
Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/onetime" -Body '{"header":{"bizCode":"OnetimeSingle"}, "body":"warmup"}' | Out-Null

$iterations = 30

# InitSingle 테스트
$initTimes = @()
for ($i = 1; $i -le $iterations; $i++) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/init" -Body '{"header":{"bizCode":"InitSingle"}, "body":"SpeedTest"}'
    $sw.Stop()
    $initTimes += $sw.ElapsedMilliseconds

    $expected = '{
        "header": {
            "bizCode": "InitSingle",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": "SpeedTest =====> Init Single Done!"
    }'
    Test-SeqismResponse $response $expected
}

# OnetimeSingle 테스트
$onetimeTimes = @()
for ($i = 1; $i -le $iterations; $i++) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-SeqismRestMethod -Uri "http://localhost:8080/api/onetime" -Body '{"header":{"bizCode":"OnetimeSingle"}, "body":"SpeedTest"}'
    $sw.Stop()
    $onetimeTimes += $sw.ElapsedMilliseconds

    $expected = '{
        "header": {
            "bizCode": "OnetimeSingle",
            "tranId": "IGNORE",
            "status": "SUCCESS",
            "error": null
        },
        "body": "SpeedTest =====> Onetime Single Done!"
    }'
    Test-SeqismResponse $response $expected
}

# 결과 출력
$initAvg = ($initTimes | Measure-Object -Average).Average
$initMin = ($initTimes | Measure-Object -Minimum).Minimum
$initMax = ($initTimes | Measure-Object -Maximum).Maximum

$onetimeAvg = ($onetimeTimes | Measure-Object -Average).Average
$onetimeMin = ($onetimeTimes | Measure-Object -Minimum).Minimum
$onetimeMax = ($onetimeTimes | Measure-Object -Maximum).Maximum

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " 속도 비교 결과 ($iterations 회 반복)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ("[Init Single]    평균: {0:N1}ms | 최소: {1}ms | 최대: {2}ms" -f $initAvg, $initMin, $initMax) -ForegroundColor Yellow
Write-Host ("[Onetime Single] 평균: {0:N1}ms | 최소: {1}ms | 최대: {2}ms" -f $onetimeAvg, $onetimeMin, $onetimeMax) -ForegroundColor Green

$diff = $initAvg - $onetimeAvg
if ($diff -gt 0) {
    Write-Host ("`nOnetime이 평균 {0:N1}ms 빠름 ({1:N1}% 향상)" -f $diff, (($diff / $initAvg) * 100)) -ForegroundColor Green
}
else {
    Write-Host ("`nInit이 평균 {0:N1}ms 빠름" -f (-$diff)) -ForegroundColor Yellow
}
Write-Host "========================================`n" -ForegroundColor Cyan


Write-Host "Test completed!"