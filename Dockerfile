# Mehrstufiges Dockerfile für Playwright Java Tests

# Stufe 1: Abhängigkeiten bauen
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# POM kopieren und Abhängigkeiten herunterladen (Layer-Caching)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Quellcode kopieren
COPY src ./src
COPY testng.xml ./
COPY config ./config

# Stufe 2: Laufzeitumgebung mit Playwright Browsern
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy

WORKDIR /app

# Maven installieren
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Aus der Build-Stufe kopieren
COPY --from=build /app ./
COPY --from=build /root/.m2 /root/.m2

# Umgebungsvariablen für CI
ENV CI=true
ENV SKIP_WEBKIT=true
ENV HEADLESS=true

# Tests ausführen
CMD ["mvn", "test"]
