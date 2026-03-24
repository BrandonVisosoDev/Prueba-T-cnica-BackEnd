# Stage 1: build with Maven + JDK 17
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only what is needed to leverage Docker cache
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster builds; remove -DskipTests to run tests)
RUN mvn -B -DskipTests package

# Stage 2: runtime with smaller JRE image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create non-root user
RUN useradd -m appuser

# Copy the fat jar produced by Maven (assumes target/*.jar)
COPY --from=build /workspace/target/*.jar app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser

# JVM options configurable at runtime
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
