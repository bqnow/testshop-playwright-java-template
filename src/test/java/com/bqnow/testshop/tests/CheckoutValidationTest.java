package com.bqnow.testshop.tests;

import com.bqnow.testshop.base.BaseTest;
import com.bqnow.testshop.utils.TestDataGenerator;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * REQ-003: Formular-Validierungs-Tests.
 * Datengesteuertes Testen für die Validierung des Checkout-Formulars.
 */
@Epic("TestShop E2E")
@Feature("Formular-Validierung")
public class CheckoutValidationTest extends BaseTest {

    private static final String PRODUCT_ID = "1";
    private static final String PRODUCT_CATEGORY = "Electronics";

    @BeforeMethod
    public void login() {
        performLogin();
    }

    @DataProvider(name = "validationScenarios")
    public Object[][] validationScenarios() {
        return new Object[][] {
                {
                        "Ungültige PLZ (Muster)",
                        TestDataGenerator.generateCustomer().firstName + " "
                                + TestDataGenerator.generateCustomer().lastName,
                        TestDataGenerator.generateCustomer().address,
                        TestDataGenerator.generateCustomer().city,
                        "123", // Zu kurz
                        TestDataGenerator.generateCustomer().email,
                        "checkout-zip"
                },
                {
                        "Ungültiges E-Mail Format",
                        TestDataGenerator.generateCustomer().firstName + " "
                                + TestDataGenerator.generateCustomer().lastName,
                        TestDataGenerator.generateCustomer().address,
                        TestDataGenerator.generateCustomer().city,
                        "12345",
                        "keine-email", // Fehlendes @
                        "checkout-email"
                }
        };
    }

    @Test(dataProvider = "validationScenarios")
    @Story("REQ-003: Formular Validierung")
    @Description("Browser blockiert Absenden: {0}")
    @Severity(SeverityLevel.NORMAL)
    public void browserBlocksInvalidFormData(String testName, String fullName, String address,
            String city, String zip, String email, String fieldToValidate) {
        Allure.parameter("Test Szenario", testName);

        // Schritt 1: Warenkorb vorbereiten
        Allure.step("Produkt zum Warenkorb hinzufügen", () -> {
            shopPage.filterByCategory(PRODUCT_CATEGORY);
            shopPage.addProductDirectlyToCart(PRODUCT_ID);
        });

        // Schritt 2: Zum Checkout navigieren
        Allure.step("Zum Checkout navigieren", () -> {
            cartPage.navigateTo();
            cartPage.proceedToCheckout();
        });

        // Schritt 3: Formular mit ungültigen Daten füllen
        Allure.step("Formular mit ungültigen Daten füllen: " + testName, () -> {
            cartPage.fillShippingDetails(fullName, address, city, zip, email);
        });

        // Schritt 4: Absenden versuchen
        Allure.step("Bestellung versuchen abzusenden", () -> {
            cartPage.submitOrder();
        });

        // Schritt 5: Validieren, dass spezifisches Feld ungültig ist (HTML5
        // Validierung)
        Allure.step("Feld-Validierung verifizieren: " + fieldToValidate, () -> {
            cartPage.expectFieldToBeInvalid(fieldToValidate);
        });
    }
}
