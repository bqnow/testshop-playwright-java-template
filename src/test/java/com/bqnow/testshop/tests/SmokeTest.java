package com.bqnow.testshop.tests;

import com.bqnow.testshop.base.BaseTest;
import com.microsoft.playwright.Page;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * REQ-001: Authentifizierungs-Smoke-Test.
 * Verifiziert die Login- und Logout-Funktionalität.
 */
@Epic("TestShop E2E")
@Feature("Authentifizierung")
public class SmokeTest extends BaseTest {

    @BeforeMethod
    public void login() {
        performLogin();
    }

    @Test
    @Story("REQ-001: Benutzer Authentifizierung")
    @Description("Benutzer kann sich erfolgreich ein- und ausloggen")
    @Severity(SeverityLevel.CRITICAL)
    public void userCanLoginAndLogout() {
        // VALIDIERUNG: Nach Login sollte man auf der Startseite sein
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*/"));

        // VALIDIERUNG: Logout-Button ist sichtbar
        var logoutBtn = page.getByTestId("nav-logout");
        assertThat(logoutBtn).isVisible();

        // VALIDIERUNG: Session-Token existiert im LocalStorage
        String token = (String) page.evaluate("() => localStorage.getItem('token')");
        assert token != null && !token.isEmpty() : "Token sollte existieren";
        assert token.contains("mock-jwt-token") : "Token sollte 'mock-jwt-token' enthalten";

        // VALIDIERUNG: Benutzername wird angezeigt
        assertThat(page.getByTestId("nav-user-menu")).containsText("consultant");

        // LOGOUT DURCHFÜHREN
        logoutBtn.click();

        // VALIDIERUNG: Weiterleitung zur Startseite (nicht /login)
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*/"));

        // VALIDIERUNG: Login-Link ist wieder sichtbar (statt Logout)
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("login",
                        java.util.regex.Pattern.CASE_INSENSITIVE))))
                .isVisible();
    }
}
