# Use the same base JDK image as other services
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the executable JAR file
COPY target/streaming-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the service runs on
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]