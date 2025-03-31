FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=target/storage-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]