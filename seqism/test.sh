#!/bin/bash

# jq로 특정 필드(tranId, timestamp 등) 재귀적으로 제거
Remove-FieldsRecursive() {
  local json="$1"
  shift
  local jq_filter="."
  for field in "$@"; do
    jq_filter+=" | del(.. | .${field}?)"
  done
  echo "$json" | jq "$jq_filter"
}

# 실제/예상 응답 비교 함수
Test-SeqismResponse() {
  local actual_json="$1"
  local expected_json="$2"
  local ignore_fields=("tranId" "timestamp")

  # 필드 제거
  for field in "${ignore_fields[@]}"; do
    actual_json=$(Remove-FieldsRecursive "$actual_json" "$field")
    expected_json=$(Remove-FieldsRecursive "$expected_json" "$field")
  done

  # 비교
  if diff <(echo "$actual_json" | jq -S .) <(echo "$expected_json" | jq -S .) >/dev/null; then
    echo "✅ 예상과 일치"
  else
    echo "❌ 전체 응답 불일치!"
    echo "예상: $expected_json"
    echo "실제: $actual_json"
  fi
}

# 6️⃣ Gateway로 테스트 메시지 전송
echo "Sending test message to Gateway..."

echo "Test completed!"