# 사용할 기본 이미지 (OpenJDK 8)
FROM eclipse-temurin:8-jre-alpine-3.23

# 작업 디렉토리 설정
WORKDIR /app

# Maven 빌드된 JAR 파일을 복사
COPY target/*.jar app.jar

# 컨테이너에서 실행할 명령
ENTRYPOINT ["java", "-jar", "app.jar"]