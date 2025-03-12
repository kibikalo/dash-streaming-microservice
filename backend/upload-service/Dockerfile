# Use a base image with Java 21 (Spring Boot 3.x requires Java 17+)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR file (ensure it's built before running Docker)
COPY target/upload-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the service port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
