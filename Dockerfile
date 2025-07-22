# -------- Build stage --------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY . .


# -------- Runtime stage --------
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/ims-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
