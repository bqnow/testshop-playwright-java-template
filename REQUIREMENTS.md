# TestShop - Functional Requirements (Java/TestNG)
## E-Commerce Training Platform

**Version:** 1.0 (Java)  
**Datum:** 2026-01-13  
**Zweck:** Playwright Java TestNG Template für Test-Automatisierung  

---

**Was die App KANN:**
- Benutzer-Login
- Produkte durchsuchen und kaufen
- Warenkorb-Verwaltung
- Checkout-Prozess
- Fehlerbehandlung (Buggy Product)

---

# FUNCTIONAL REQUIREMENTS

Die folgenden Requirements beschreiben **WAS** die Applikation tut - die Business-Funktionen und User-Workflows.

---

## REQ-001: User Authentication 🔐

**Type:** Functional

**User Story:**
```
Als Testnutzer
möchte ich mich mit vordefinierten Zugangsdaten anmelden
damit ich auf den geschützten Shop zugreifen kann
```

**Akzeptanzkriterien:**
- Login-Formular ist auf `/login` erreichbar
- Test-Credentials: `consultant` / `pwd`
- Nach erfolgreichem Login: Redirect zu `/`
- Session bleibt erhalten (LocalStorage-Token)
- Logout-Button ist sichtbar und funktioniert

**Testabdeckung:**
```
✅ SmokeTest.java
   → Login-Flow komplett
   → Session-Validierung
   → Redirect-Prüfung
```

---

## REQ-002: Complete Checkout Flow (Happy Path) 🛒

**Type:** End-to-End

**User Story:**
```
Als Kunde
möchte ich ein Produkt suchen, in den Warenkorb legen und bestellen
damit ich den Artikel erhalten kann
```

**Akzeptanzkriterien:**
- Login erfolgreich
- Produkt nach Kategorie filtern
- Produkt zum Warenkorb hinzufügen
- Menge erhöhen, Gesamtpreis-Berechnung korrekt
- Checkout-Formular mit dynamischen Daten (JavaFaker)
- Order-ID im Format `ORDER-{timestamp}`

**Testabdeckung:**
```
✅ HappyPathTest.java
   → Kompletter User Journey
   → Login → Filter → Produkt → Warenkorb → Checkout
   → Dynamische Testdaten (JavaFaker)
```

---

## REQ-003: Form Validation ✅

**Type:** Input Validation

**User Story:**
```
Als System
möchte ich ungültige Eingaben verhindern
damit nur korrekte Daten gespeichert werden
```

**Akzeptanzkriterien:**
- **PLZ:** Genau 5 Ziffern (HTML Pattern)
- **E-Mail:** Muss `@` enthalten
- Browser zeigt native Fehlermeldung

**Testabdeckung:**
```
✅ CheckoutValidationTest.java
   → Data-Driven Test mit TestNG DataProvider
   → 2 Szenarien: Ungültige PLZ, Ungültiges E-Mail-Format
```

---

## REQ-004: Error Handling (Buggy Product) ⚠️

**Type:** Edge Case / Resilience

**User Story:**
```
Als System
möchte ich mit fehlerhaften Produkten korrekt umgehen
damit keine korrupten Bestellungen entstehen
```

**Akzeptanzkriterien:**
- Produkt ID 999 führt zu Fehler
- Error-Meldung: \"Internal Server Error\"
- App bleibt benutzbar

**Testabdeckung:**
```
✅ EdgeCaseTest.java
   → Buggy Product Checkout
   → Fehler erwarten
   → App-Stabilität validieren
```

---

# NON-FUNCTIONAL REQUIREMENTS

---

## REQ-006: State Persistence & Performance ⚡️

**Type:** Non-Functional

**User Story:**
```
Als Tester
möchte ich State Injection nutzen
damit Tests schneller laufen (~60% Zeitersparnis)
```

**Akzeptanzkriterien:**
- Warenkorb via LocalStorage manipulierbar
- App erkennt State sofort (< 200ms)
- Ermöglicht Test-Optimierung

**Testabdeckung:**
```
✅ ApiOptimizationTest.java
   → Injiziert Warenkorb direkt
   → Validiert Performance
```

---

## 📊 Test-Strategie Übersicht

### Requirements → Tests Mapping

| REQ | Requirement | Test-Klasse | Test-Typ |
|-----|-------------|------------|----------|
| 001 | Login | `SmokeTest.java` | Smoke |
| 002 | Checkout Flow | `HappyPathTest.java` | E2E |
| 003 | Form Validation | `CheckoutValidationTest.java` | Data-Driven |
| 004 | Error Handling | `EdgeCaseTest.java` | Edge Case |
| 006 | State Persistence | `ApiOptimizationTest.java` | Performance |

---

**Version:** 1.0 (Java/TestNG)  
**Status:** ✅ Implemented  
**Last Update:** 2026-01-13
