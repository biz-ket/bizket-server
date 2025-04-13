FROM --platform=linux/amd64 eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app
COPY build/libs/*.jar app.jar
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=builder /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
