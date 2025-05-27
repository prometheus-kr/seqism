# 루트에서 seqism 및 seqism-example 하위 모듈의 run.ps1 스크립트를 순차적으로 실행하는 스크립트입니다.
# 각 모듈 디렉토리로 이동하여 run.ps1을 실행한 뒤, 원래 위치로 돌아옵니다.

Push-Location seqism         # seqism 디렉토리로 이동
./run.ps1                    # seqism/run.ps1 실행
Pop-Location                 # 원래 위치로 복귀

Push-Location seqism-example # seqism-example 디렉토리로 이동
./run.ps1                    # seqism-example/run.ps1 실행
Pop-Location                 # 원래 위치로 복귀