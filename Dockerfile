# Stage 1 - Build
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2 - Run
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy any jar dynamically
COPY --from=builder /app/target/*.jar app.jar

# Use Render PORT
ENV PORT=8080
EXPOSE 8080

# Run with dynamic port
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]