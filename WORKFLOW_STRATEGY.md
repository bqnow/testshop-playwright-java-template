# Workflow-Strategie: Gated Pipelines & CI 🛠️

Diese Dokumentation beschreibt die orchestrale Zusammenarbeit zwischen der **TestShop Applikation** und dem **Playwright Java TestNG Framework**.

---

## 1. Der "Gated" Deployment Prozess

Die Applikation wird nicht einfach blind deployed. Wir nutzen einen **Gated Workflow** in der App-Repo (`bqnow-testapp`):

1.  **Build Phase**: Das Docker-Image der App wird gebaut und in die GitHub Container Registry (GHCR) gepusht.
2.  **Test Phase (The Gate)**: 
    *   Die Pipeline checkt das *separate* Test-Framework aus.
    *   Die App wird aus dem soeben gebauten Image gestartet.
    *   Die E2E-Tests (Java/TestNG) werden gegen diesen Container ausgeführt.
3.  **Deployment Phase**: Nur wenn die Tests erfolgreich waren, wird der Code zu Vercel gepusht und live geschaltet.

---

## 2. Steuerung über Umgebungsvariablen

Sowohl lokal als auch in der CI stehen Variablen zur Verfügung, um den Ablauf zu steuern:

| Variable | Werte | Beschreibung |
| :--- | :--- | :--- |
| `BASE_URL` | URL | Das Ziel der Tests (Default: `http://localhost:3000`). |
| `SKIP_WEBKIT` | `true` / `false` | Überspringt Webkit-Tests (Sinnvoll unter Linux/Docker). |
| `SHOP_IMAGE_TAG`| Tag | Bestimmt, welches App-Image geladen wird (Default: `latest`). |
| `SKIP_E2E` | `true` | (Nur App-Repo) Überspringt die komplette Test-Gatung. |

---

## 3. Lokale Validierung (Pre-Push)

Bevor du Änderungen pushst, kannst du den kompletten Pipeline-Ablauf lokal simulieren:

```bash
# Testet die lokalen Tests gegen die lokale App (beide in Docker)
docker compose up --build --exit-code-from maven

# Oder mit Maven direkt (benötigt laufende App):
mvn clean test
```

Dies führt `docker compose up --build` aus, was exakt dem Schritt in der CI entspricht.
