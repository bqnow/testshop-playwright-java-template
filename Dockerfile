# Multi-stage Dockerfile for Playwright Java tests

# Stage 1: Build dependencies
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM and download dependencies (layer caching)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY testng.xml ./
COPY config ./config

# Stage 2: Runtime with Playwright browsers
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy from build stage
COPY --from=build /app ./
COPY --from=build /root/.m2 /root/.m2

# Environment variables for CI
ENV CI=true
ENV SKIP_WEBKIT=true
ENV HEADLESS=true

# Run tests
CMD ["mvn", "test"]
