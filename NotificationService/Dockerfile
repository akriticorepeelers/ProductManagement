# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built jar from the target folder
COPY target/notification-service-1.0.0.jar notification-service.jar

# Expose default port
EXPOSE 8085

# Run the jar file
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
