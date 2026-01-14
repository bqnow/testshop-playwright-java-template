# Playwright E2E Framework Template (Java/TestNG) 🎭

Willkommen im offiziellen **Java/TestNG** Test-Framework für die TestShop Applikation. Dieses Repository bietet eine professionelle, entkoppelte Test-Umgebung mit **Playwright Java** und **TestNG**.

### 🚀 Highlights & Features
*   **Parallel Execution:** Maximale Geschwindigkeit durch TestNG Parallel-Modus
*   **Isoliert & Sicher:** Komplette Testausführung in Docker (identisch zur CI)
*   **Multi-Stage Support:** Nahtloses Testen gegen Lokal, QA, Staging und Produktion
*   **Page Object Model (POM):** Hochgradig wartbarer Code durch strikte Trennung
*   **Smart Data Fixtures:** JavaFaker für realistische Zufallsdaten
*   **Deep Reporting:** Allure Report Integration mit Historie und Screenshots
*   **CI/CD Ready:** Integrierte GitHub Actions mit Maven und Docker

---

## 📊 Requirements Coverage

Dieses Template deckt 5 definierte Requirements ab. Details findest du in [REQUIREMENTS.md](REQUIREMENTS.md).

| **Anforderung (ID)** | **Test-Klasse** | **Kategorie** |
| :--- | :--- | :--- |
| **REQ-001: Authentifizierung** | `SmokeTest.java` | Funktional (Smoke) |
| **REQ-002: Happy Path** | `HappyPathTest.java` | Funktional (E2E) |
| **REQ-003: Formular Validierung** | `CheckoutValidationTest.java` | Funktional (Negativ) |
| **REQ-004: Fehlerbehandlung** | `EdgeCaseTest.java` | Funktional (Edge) |
| **REQ-006: State Persistence** | `ApiOptimizationTest.java` | Nicht-Funktional |

---

## 🛠️ Schritt-für-Schritt Einrichtung

### 1. Grundvoraussetzungen installieren
Bevor der erste Test laufen kann, müssen diese Werkzeuge auf dem Computer vorhanden sein:

*   **Java (JDK 17+):** [Hier herunterladen](https://adoptium.net/)
*   **Maven:** [Hier herunterladen](https://maven.apache.org/download.cgi) oder via Package Manager
*   **Git:** [Hier herunterladen](https://git-scm.com/)
*   **Docker Desktop:** [Hier herunterladen](https://www.docker.com/products/docker-desktop/)

**Prüfe, ob alles korrekt installiert ist:**
```bash
java -version   # Sollte Java 17+ zeigen
mvn -version    # Sollte Maven 3.9+ zeigen
git --version   # Sollte git version 2.x zeigen
docker --version # Sollte Docker version 20+ zeigen
```

### 2. Projekt klonen
```bash
git clone https://github.com/bqnow/testshop-playwright-java-template.git
cd testshop-playwright-java-template
```

### 3. Dependencies installieren
```bash
mvn clean install
```

---

## 🚀 Quick Start - Dein erster Test

Jetzt ist alles bereit! So führst du deinen ersten erfolgreichen Test aus:

```bash
# 1. Starte den Webshop im Hintergrund (Docker-Container)
docker compose up -d app

# 2. Warte ~10 Sekunden, bis der Container hochgefahren ist

# 3. Führe die Tests aus
mvn test

# 4. Optional: Generiere und öffne den Allure Report
mvn allure:serve
```

**Beenden:** Mit `docker compose down` kannst du den Container später wieder stoppen.

---

## 🏗️ Framework Architektur

**Page Object Model (POM):**
Jeder Bereich der Webseite hat eine eigene Klasse im Package `com.bqnow.testshop.pages` (z.B. `LoginPage.java`, `CartPage.java`). Selektoren und Interaktions-Logik sind dort zentral definiert.

**BaseTest:**
Alle Tests erben von `BaseTest.java`, welches Playwright-Lifecycle, Browser-Management und gemeinsame Fixtures (z.B. Login) bereitstellt.

**Dynamic Test Data:**
Mit **JavaFaker** werden bei jedem Testlauf realistische Zufallsdaten (Namen, Adressen, E-Mails) generiert.

**TestNG Configuration:**
Die Datei `testng.xml` definiert die Test-Suite und Parallelisierungs-Einstellungen.

---

## ⚙️ Environment & Konfiguration

**Zentrale Konfiguration:**
Alle Zugangsdaten werden aus den `.env`-Dateien im `config/` Ordner geladen:
*   `config/.env` (Lokal)
*   `config/.env.prod` (Produktion)
*   `config/.env.local` (Persönlich, git-ignoriert)

**Variablen:** `BASE_URL`, `TEST_USER_NAME`, `TEST_USER_PASSWORD`

**Priorität:** System/GitHub-Secrets > `.env.local` > `.env.{STAGE}` > `.env`

---

## 🐳 Docker Support

Docker ist der Schlüssel zur **Konsistenz und Isolation**:

*   **Identische Umgebung:** Dein lokaler Test läuft  in exakt derselben Umgebung wie in der CI-Pipeline
*   **Saubere Isolation:** Die Test-App läuft in einem eigenen Container

**Docker-Befehle:**
```bash
# App starten
docker compose up -d app

# Komplette Test-Ausführung (App + Tests in Docker)
docker compose up --build --exit-code-from maven

# Aufräumen
docker compose down
```

---

## 🏃 Test-Workflows

### 1. Lokale Entwicklung (Maven)
```bash
# App im Hintergrund starten
docker compose up -d app

# Tests ausführen
mvn test

# Allure Report generieren
mvn allure:serve
```

### 2. Full Docker (CI-Simulation)
```bash
# Alles in Docker (wie in GitHub Actions)
docker compose up --build --exit-code-from maven
```

### 3. Gegen Produktion testen
```bash
TEST_ENV=prod mvn test
```

---

## 📊 Ergebnisse analysieren & Berichte erstellen

### Allure Report (Grafisches Dashboard)
Das Framework generiert vollautomatisch detaillierte Testberichte.

**Intelligentes Reporting-Features:**
*   📸 **Failure Screenshots:** Bei jedem fehlgeschlagenen Test wird automatisch ein Screenshot angehängt.
*   🎞️ **Video Recording:** Ein Video des gesamten Testlaufs wird gespeichert (nur bei Fehler, um Platz zu sparen - "Retain on Failure").
*   🕵️‍♂️ **Playwright Traces:** Ein vollständiger Trace (Time-Travel Debugging) wird als ZIP angehängt und kann im [Playwright Trace Viewer](https://trace.playwright.dev) geöffnet werden.

**Befehle:**
```bash
# Report generieren und öffnen
mvn allure:serve

# Report nur generieren (statische Dateien in target/site/allure-maven-plugin)
mvn allure:report
```

### Historie & Trends (CI/CD)
Einer der stärksten Vorteile von Allure ist die **Trend-Analyse**.
*   **Lokal:** Historie geht bei `mvn clean` verloren (da `target/` gelöscht wird). Lokal liegt der Fokus auf dem aktuellen Run.
*   **CI/CD (GitHub Actions):** In der Pipeline wird die Historie automatisch bewahrt. Das `allure-action` Plugin kopiert den `history`-Ordner aus vorherigen Runs in den neuen Report. So siehst du über Wochen hinweg Trends zu Stabilität und Ausführungszeit.

### Surefire Report (Standard Maven)
Nach jedem `mvn test` findest du die Berichte unter:
*   `target/surefire-reports/` (XML/TXT)
*   `target/allure-results/` (Allure Rohdaten)

---

## 🤖 CI/CD Integration

Dieses Framework ist für die automatisierte Ausführung vorbereitet:
*   **GitHub Actions:** Bei jedem `push` auf `main` wird automatisch der Docker-Workflow ausgeführt
*   **Artifacts:** Test-Reports werden automatisch hochgeladen

📘 **Architektur-Details: [WORKFLOW_STRATEGY.md](WORKFLOW_STRATEGY.md)**

---

## 🎯 Takeaways

Dieses Template demonstriert:

1.  **Smoke Testing** (REQ-001) - Schneller Check der Basis-Funktionalität
2.  **E2E Testing** (REQ-002) - Kompletter User Journey
3.  **Data-Driven Testing** (REQ-003) - TestNG DataProvider für Validierungsszenarien
4.  **Edge Case Testing** (REQ-004) - Negative Scenarios & Resilience
5.  **State Injection & Performance** (REQ-006) - Tests massiv beschleunigen (~60%)

---

## ❓ Troubleshooting

*   **"Connection Refused"**: Prüfe, ob der Webshop läuft: `docker compose ps`
*   **Maven Build Fehler**: Prüfe Java-Version: `java -version` (muss 17+ sein)
*   **Docker Fehler**: Sauberer Neustart: `docker compose down && docker compose up --build`
*   **Report öffnet sich nicht**: Stelle sicher, dass Browser installiert ist
*   **Hohe "Skipped"-Zahl im Docker**: Das ist normal. Wenn `SKIP_WEBKIT=true` gesetzt ist (Standard in Docker), werden WebKit-Tests übersprungen. TestNG zählt dabei auch die Konfigurationsmethoden (`@BeforeClass`, `@AfterClass`) als "skipped", was die Zahl künstlich erhöht. Die wichtigen Metriken sind **Failures: 0** und **Errors: 0**.

---

## 🛠️ Systemanforderungen
*   **Java JDK 17+**
*   **Maven 3.9+**
*   **Git**
*   **Docker Desktop** (optional, aber empfohlen)

---

**Version:** 1.0 (Java/TestNG)  
**Status:** ✅ Ready for Training  
**Last Update:** 2026-01-13
