FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache docker-cli

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]