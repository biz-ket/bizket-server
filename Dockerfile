FROM --platform=linux/amd64 eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app
COPY build/libs/*.jar app.jar

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/app.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
RUN mkdir -p /logs

EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "bizket-db:3306", "--", "java", "-jar", "app.jar"]
