# 사용할 기본 이미지 (OpenJDK 17)
FROM openjdk:17-jdk-slim

# Maven 빌드 이미지 설정
FROM maven:3.9.3-eclipse-temurin-17 AS builder
WORKDIR /app

# 프로젝트 소스 복사 및 Maven 빌드 실행
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 런타임 이미지 설정
FROM openjdk:17-jdk-slim
WORKDIR /app

# Maven 빌드된 JAR 파일을 복사
COPY --from=builder /app/target/*.jar app.jar

# 컨테이너에서 실행할 명령
CMD ["java", "-jar", "app.jar"]