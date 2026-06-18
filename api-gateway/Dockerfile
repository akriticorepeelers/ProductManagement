# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built jar from the target folder
COPY target/api-gateway-1.0.0.jar api-gateway.jar

# Expose default Gateway Port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
