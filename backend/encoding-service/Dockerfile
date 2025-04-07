FROM eclipse-temurin:21-jdk

# Install FFmpeg
# Update package lists, install ffmpeg, clean up apt cache
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* \

WORKDIR /app

COPY target/encoding-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]