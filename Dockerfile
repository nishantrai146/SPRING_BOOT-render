# Use JRE instead of JDK if you don't need compilation tools
FROM openjdk:17-jre-slim

WORKDIR /app

# Better to copy with consistent name
COPY target/*.jar app.jar

# Add JVM options if needed (memory, GC, etc)
ENTRYPOINT ["java", "-jar", "app.jar"]

# Healthcheck (optional but recommended)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1