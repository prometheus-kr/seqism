# Seqism

**SEQ**uential **I**nteractive **S**erver **M**ethod

[![Maven Central](https://img.shields.io/maven-central/v/io.github.prometheus-kr/seqism-common.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.prometheus-kr/seqism-common)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview

Seqism은 **순차적 대화형 서버 메서드**를 구현하기 위한 Java 라이브러리입니다.  
복잡한 비즈니스 프로세스를 단계별로 나누어 처리하고, 각 단계마다 클라이언트와 상호작용할 수 있는 구조를 제공합니다.

### Pronunciation

sequential(/sɪˈkwenʃl/) + ism

=> **시퀴즘** /sɪˈkwɪzəm/ or **시키즘** /sɪˈkɪzəm/

## Features

- **순차적 프로세스 처리**: 복잡한 업무를 단계별로 분할하여 처리
- **대화형 인터페이스**: 각 단계마다 클라이언트와 상호작용
- **모듈화**: Common, Gateway, Processor로 구성된 확장 가능한 아키텍처
- **보안**: 트랜잭션별 독립 큐를 통한 메시지 격리
- **확장성**: 여러 프로세서 인스턴스를 통한 로드 밸런싱

## Examples

프로젝트에는 다양한 예제가 포함되어 있습니다:

- **Sample001**: 기본적인 요청-응답 처리
- **Sample002**: 여러 단계를 거치는 프로세스
- **Sample003**: 조건부 분기 처리
- **Sample004**: 복잡한 상태 관리

자세한 예제는 [seqism-example](seqism-example/) 모듈을 참고하세요.

docker-compose 로 구성된 예제를 실행하기 위해 seqism-example/run.ps1 을 실행하세요.

## Message Queue

현재 RabbitMQ를 지원하며, 추후 다른 메시지 브로커도 지원 예정입니다.

## License

이 프로젝트는 Apache License 2.0 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## Support

- 이슈 리포트: [GitHub Issues](https://github.com/prometheus-kr/seqism/issues)
- 메일: prometheus@kakao.com
