# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built jar from the target folder
COPY target/auth-service-1.0.0.jar auth-service.jar

# Expose default port
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
