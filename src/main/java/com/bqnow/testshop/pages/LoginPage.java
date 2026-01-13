package com.bqnow.testshop.pages;

import com.microsoft.playwright.Page;

/**
 * Login Page Object für die TestShop-Authentifizierung.
 */
public class LoginPage extends BasePage {
    // Locators unter Verwendung von test-id Selektoren
    private static final String USERNAME_INPUT = "[data-testid='username-input']";
    private static final String PASSWORD_INPUT = "[data-testid='password-input']";
    private static final String LOGIN_BUTTON = "[data-testid='login-btn']";

    public LoginPage(Page page) {
        super(page);
    }

    public void navigateTo() {
        navigate("/login");
        waitForLoadState();
    }

    public void login(String username, String password) {
        page.fill(USERNAME_INPUT, username);
        page.fill(PASSWORD_INPUT, password);
        page.click(LOGIN_BUTTON);
    }
}
