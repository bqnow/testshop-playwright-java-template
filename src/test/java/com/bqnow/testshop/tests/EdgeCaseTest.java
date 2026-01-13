package com.bqnow.testshop.tests;

import com.bqnow.testshop.base.BaseTest;
import com.bqnow.testshop.utils.TestDataGenerator;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * REQ-004: Randfälle und Fehlerbehandlung.
 * Testet die Widerstandsfähigkeit der Anwendung bei fehlerhaften Produkten.
 */
@Epic("TestShop E2E")
@Feature("Fehlerbehandlung")
public class EdgeCaseTest extends BaseTest {

    private static final String BUGGY_PRODUCT_ID = "999";
    private static final String BUGGY_PRODUCT_NAME = "Glitchy Gadget";

    @BeforeMethod
    public void login() {
        performLogin();
    }

    @Test
    @Story("REQ-004: Fehlerbehandlung")
    @Description("Checkout schlägt kontrolliert fehl beim Kauf eines fehlerhaften Produkts (ID 999)")
    @Severity(SeverityLevel.NORMAL)
    public void checkoutFailsWithBuggyProduct() {
        // Schritt 1: Fehlerhaftes Produkt zum Warenkorb hinzufügen
        Allure.step("Fehlerhaftes Produkt zum Warenkorb hinzufügen", () -> {
            shopPage.searchProduct(BUGGY_PRODUCT_NAME);
            shopPage.addProductDirectlyToCart(BUGGY_PRODUCT_ID);
        });

        // Schritt 2: Checkout versuchen
        Allure.step("Checkout mit gültigen Daten versuchen", () -> {
            cartPage.navigateTo();
            cartPage.proceedToCheckout();

            TestDataGenerator.CustomerData customer = TestDataGenerator.generateCustomer();
            Allure.addAttachment("Kundendaten", "text/plain", customer.toString());

            cartPage.fillShippingDetails(
                    customer.firstName + " " + customer.lastName,
                    customer.address,
                    customer.city,
                    customer.zipCode,
                    customer.email);

            // Erwarte Serverfehler von actions.ts - tatsächliche Nachricht ist:
            // "Checkout failed: Internal Server Error: processing failed for item 999."
            cartPage.submitOrderExpectingError("Checkout failed");
        });

        // Schritt 3: Überprüfe App-Stabilität nach Fehler
        Allure.step("Überprüfe App-Stabilität nach Fehler", () -> {
            assertThat(page.getByTestId("submit-order-btn")).isVisible();

            shopPage.navigateTo();
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*/"));
        });
    }
}
