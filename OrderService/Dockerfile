# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and dependency definitions
COPY pom.xml .

# Fetch dependencies offline for caching
RUN mvn dependency:go-offline -B

# Copy src and build the artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/order-service-1.0.0.jar order-service.jar

# Expose default port
EXPOSE 8084

# Run the jar file
ENTRYPOINT ["java", "-jar", "order-service.jar"]
