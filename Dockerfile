# ============================================================================
# Stage 1: Build the packaged JAR using Maven and JDK 21
# ============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy source tree and Maven build configurations
COPY pom.xml .
COPY src ./src

# Compile and package application (skip tests for faster production builds)
RUN mvn clean package -DskipTests

# ============================================================================
# Stage 2: Packaging lightweight JRE runtime
# ============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Set production environment variables defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    DB_HOST=mysql-db \
    DB_PORT=3306 \
    DB_NAME=student_db \
    DB_USER=sms_user \
    DB_PASS=sms_pass

# Copy compiled JAR from builder stage
COPY --from=builder /app/target/student-management-*.jar app.jar

# Expose standard container port
EXPOSE 8080

# Run JVM bootstrap entry command
ENTRYPOINT ["java", "-jar", "app.jar"]
