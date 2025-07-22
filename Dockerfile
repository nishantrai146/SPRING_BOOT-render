# Use lightweight base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar file (update name if needed)
COPY target/ims-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Set environment variable for active profile (optional fallback)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the jar with env-aware Spring profile
ENTRYPOINT ["java", "-jar", "app.jar"]
