# Using Eclipse Temurin JRE (recommended for production)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar file into the container
COPY target/ims-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]