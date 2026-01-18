package com.bqnow.testshop.reporting;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Grafana Loki Reporter für TestNG.
 * Sendet Testergebnisse exakt im gleichen Format wie der TypeScript-Reporter.
 */
import com.bqnow.testshop.config.ConfigLoader;
import com.google.gson.Gson;

public class GrafanaReporter implements ITestListener {

    private static final String DEFAULT_APP_NAME = "testshop-java";
    private final List<TestDetail> tests = new ArrayList<>();
    private final long suiteStartTime;

    public GrafanaReporter() {
        this.suiteStartTime = System.currentTimeMillis();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        recordTest(result, "passed", null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable throwable = result.getThrowable();
        String errorMsg = throwable != null ? throwable.getMessage() : "Unknown error";
        recordTest(result, "failed", errorMsg);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordTest(result, "skipped", null);
    }

    private void recordTest(ITestResult result, String status, String error) {
        long duration = result.getEndMillis() - result.getStartMillis();
        // Vollständigen Namen erstellen: ClassName › MethodName (analog zu TS Describe
        // › Test)
        String className = result.getTestClass().getRealClass().getSimpleName();
        String testName = className + " › " + result.getMethod().getMethodName();
        // Beschreibung hinzufügen falls vorhanden
        if (result.getMethod().getDescription() != null && !result.getMethod().getDescription().isEmpty()) {
            testName += " (" + result.getMethod().getDescription() + ")";
        }

        // Error übernehmen
        String cleanError = error;

        // Browser aus TestNG Parametern lesen (definiert in testng.xml)
        String browser = result.getTestContext().getCurrentXmlTest().getParameter("browser");
        if (browser == null) {
            browser = "chromium"; // Fallback
        }

        tests.add(new TestDetail(
                testName,
                browser,
                status,
                duration,
                cleanError,
                System.currentTimeMillis()));
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("\n[GrafanaReporter] 🏁 Testlauf beendet. Ergebnisse werden gesammelt...");

        String lokiUrl = ConfigLoader.get("GRAFANA_LOKI_URL");
        String lokiUser = ConfigLoader.get("GRAFANA_LOKI_USER");
        String lokiKey = ConfigLoader.get("GRAFANA_LOKI_KEY");

        if (lokiUrl == null || lokiUser == null || lokiKey == null) {
            System.out.println("⚠️ Grafana Env-Variablen fehlen. Upload übersprungen.");
            return;
        }

        try {
            String payload = buildLokiPayload();
            sendToLoki(lokiUrl, lokiUser, lokiKey, payload);
        } catch (Exception e) {
            System.err.println("❌ Fehler beim Senden an Grafana: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildLokiPayload() {
        // 1. App-Namen standardisieren
        String appName = ConfigLoader.get("GRAFANA_APP_NAME");
        if (appName == null || appName.isEmpty()) {
            appName = DEFAULT_APP_NAME; // Hardcoded Fallback für Konsistenz
        }

        String environment = ConfigLoader.get("TEST_ENV");
        if (environment == null)
            environment = "local";

        System.out.println("[GrafanaReporter] Building Payload for App: " + appName + " | Env: " + environment);

        Gson gson = new Gson();
        Map<String, Object> lokiPayload = new HashMap<>();
        List<Map<String, Object>> streams = new ArrayList<>();

        // Zeitstempel generieren (Jetzt)
        String timestampNs = String.valueOf(System.currentTimeMillis()) + "000000";

        // 1. Individuelle Testergebnisse
        tests.sort(Comparator.comparingLong(t -> t.timestamp));

        for (TestDetail t : tests) {
            Map<String, String> streamLabels = new HashMap<>();
            streamLabels.put("app", appName);
            streamLabels.put("environment", environment);
            streamLabels.put("kind", "test_result");
            streamLabels.put("test_name", t.title);
            streamLabels.put("browser", t.browser);
            streamLabels.put("status", t.status);

            Map<String, Object> logLine = new HashMap<>();
            logLine.put("event", "test_completed");
            logLine.put("test_name", t.title);
            logLine.put("browser", t.browser); // Added for parity with TS
            logLine.put("status", t.status);
            logLine.put("duration_ms", t.duration);
            logLine.put("error", t.error);
            logLine.put("user", ConfigLoader.get("USER") != null ? ConfigLoader.get("USER") : "ci-runner");

            List<Object> valuesEntry = new ArrayList<>();
            // Wir nutzen für ALLE Events den aktuellen Zeitstempel des Versands,
            // um "Out of Order" Fehler bei Loki sicher zu vermeiden.
            valuesEntry.add(timestampNs);
            valuesEntry.add(gson.toJson(logLine));

            Map<String, Object> streamEntry = new HashMap<>();
            streamEntry.put("stream", streamLabels);
            streamEntry.put("values", Collections.singletonList(valuesEntry));

            streams.add(streamEntry);
        }

        // 2. Zusammenfassung Stream (Der wichtigste für das Dashboard!)
        long totalDuration = System.currentTimeMillis() - suiteStartTime;
        long passed = tests.stream().filter(t -> "passed".equals(t.status)).count();
        long failed = tests.stream().filter(t -> "failed".equals(t.status)).count();
        long skipped = tests.stream().filter(t -> "skipped".equals(t.status)).count();

        Map<String, String> summaryLabels = new HashMap<>();
        summaryLabels.put("app", appName);
        summaryLabels.put("environment", environment);
        summaryLabels.put("kind", "test_summary"); // WICHTIGER LABEL FÜR DASHBOARD QUERY

        Map<String, Object> summaryLog = new HashMap<>();
        summaryLog.put("event", "run_completed");
        summaryLog.put("total", tests.size());
        summaryLog.put("passed", passed);
        summaryLog.put("failed", failed);
        summaryLog.put("skipped", skipped);
        summaryLog.put("duration", totalDuration);

        List<Object> summaryValues = new ArrayList<>();
        summaryValues.add(timestampNs);
        summaryValues.add(gson.toJson(summaryLog));

        Map<String, Object> summaryStream = new HashMap<>();
        summaryStream.put("stream", summaryLabels);
        summaryStream.put("values", Collections.singletonList(summaryValues));

        streams.add(summaryStream);

        lokiPayload.put("streams", streams);

        return gson.toJson(lokiPayload);
    }

    private void sendToLoki(String url, String user, String key, String payload) throws Exception {
        System.out.println("📡 Sende " + tests.size() + " Testergebnisse an Grafana Loki...");

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        String auth = Base64.getEncoder().encodeToString((user + ":" + key).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + auth)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("✅ Metriken erfolgreich an Grafana gesendet!");
        } else {
            System.err.println("❌ Grafana API Fehler (" + response.statusCode() + "): " + response.body());
        }
    }

    // Hilfsklasse
    private static class TestDetail {
        String title, browser, status, error;
        long duration, timestamp;

        TestDetail(String t, String b, String s, long d, String e, long ts) {
            this.title = t;
            this.browser = b;
            this.status = s;
            this.duration = d;
            this.error = e;
            this.timestamp = ts;
        }
    }

}
