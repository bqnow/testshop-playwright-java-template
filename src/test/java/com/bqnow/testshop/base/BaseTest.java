package com.bqnow.testshop.base;

import com.bqnow.testshop.config.ConfigLoader;
import com.bqnow.testshop.pages.*;
import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Basis-Testklasse mit Playwright Lifecycle-Management und gemeinsamen
 * Fixtures.
 */
public abstract class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    // Konfiguration
    protected static ConfigLoader config;

    // Page Objects
    protected LoginPage loginPage;
    protected ShopPage shopPage;
    protected ProductDetailPage productDetailPage;
    protected CartPage cartPage;

    @Parameters("browser")
    @BeforeClass
    public void setupBrowser(@Optional("chromium") String browserName) {
        config = ConfigLoader.getInstance();
        playwright = Playwright.create();

        // Browser mit entsprechenden Einstellungen erstellen
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless());

        switch (browserName.toLowerCase()) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                if (config.skipWebkit()) {
                    System.out.println("Überspringe WebKit gemäß Konfiguration");
                    // browser = playwright.webkit().launch(launchOptions); // Weiterhin über Config
                    // überspringbar
                    throw new SkipException("Überspringe WebKit Tests in dieser Umgebung (CI/Docker)");
                } else {
                    browser = playwright.webkit().launch(launchOptions);
                }
                break;
            case "chromium":
            default:
                browser = playwright.chromium().launch(launchOptions);
                break;
        }
    }

    @BeforeMethod
    public void setupTest() {
        // Neuen Context für Test-Isolation erstellen
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setBaseURL(config.getBaseURL())
                .setIgnoreHTTPSErrors(true)
                // Videoaufnahme aktivieren
                .setRecordVideoDir(Paths.get("target/videos/"));

        context = browser.newContext(contextOptions);

        // Tracing immer aktivieren (geringer Overhead, essenziell für Debugging)
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        page = context.newPage();

        // Page Objects initialisieren
        loginPage = new LoginPage(page);
        shopPage = new ShopPage(page);
        productDetailPage = new ProductDetailPage(page);
        cartPage = new CartPage(page);
    }

    @AfterMethod
    public void teardownTest(org.testng.ITestResult result) {
        java.nio.file.Path videoPath = null;
        if (page != null && page.video() != null) {
            videoPath = page.video().path(); // Video-Pfad vor dem Schließen des Contexts holen
        }

        if (result.getStatus() == org.testng.ITestResult.FAILURE) {
            // Screenshot bei Fehler aufnehmen
            if (page != null) {
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                Allure.addAttachment("Failure Screenshot", "image/png",
                        new java.io.ByteArrayInputStream(screenshot), "png");
            }

            // Trace bei Fehler aufnehmen
            if (context != null) {
                try {
                    String traceName = "trace-" + result.getName() + "-" + System.currentTimeMillis() + ".zip";
                    java.nio.file.Path traceOutputPath = Paths.get("target/allure-results/" + traceName);

                    context.tracing().stop(new Tracing.StopOptions().setPath(traceOutputPath));

                    Allure.addAttachment("Playwright Trace", "application/zip",
                            new java.io.FileInputStream(traceOutputPath.toFile()), "zip");
                } catch (Exception e) {
                    System.err.println("Fehler beim Speichern des Trace: " + e.getMessage());
                }
            }
        }

        // Context schließen, um Video und Traces auf die Festplatte zu schreiben
        if (context != null) {
            context.close();
        }

        // Video-Datei nach dem Schließen des Contexts verarbeiten
        if (videoPath != null) {
            if (result.getStatus() == org.testng.ITestResult.FAILURE) {
                try {
                    String videoFileName = "video-" + result.getName() + "-" + System.currentTimeMillis() + ".webm";
                    java.nio.file.Path targetVideoPath = Paths.get("target/allure-results/" + videoFileName);
                    Files.move(videoPath, targetVideoPath, StandardCopyOption.REPLACE_EXISTING);
                    Allure.addAttachment("Failure Video", "video/webm",
                            new java.io.FileInputStream(targetVideoPath.toFile()), "webm");
                } catch (IOException e) {
                    System.err.println("Fehler beim Verschieben oder Anhängen des Videos: " + e.getMessage());
                }
            } else {
                // Video bei Erfolg löschen
                try {
                    Files.deleteIfExists(videoPath);
                } catch (IOException e) {
                    System.err.println("Fehler beim Löschen der Videodatei: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Hilfsmethode für Tests, die einen authentifizierten Status benötigen.
     * Aufzurufen in @BeforeMethod in Unterklassen, die Login benötigen.
     */
    protected void performLogin() {
        loginPage.navigateTo();
        loginPage.login(config.getTestUserName(), config.getTestUserPassword());
        // Warten bis Navigation abgeschlossen ist
        page.waitForURL("**/");
    }

    @AfterClass
    public void teardownBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
