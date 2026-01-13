package com.bqnow.testshop.tests;

import com.bqnow.testshop.base.BaseTest;
import com.bqnow.testshop.utils.TestDataGenerator;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * REQ-002: Happy Path E2E Checkout.
 * Komplette Customer Journey von Produktauswahl bis Bestellbestätigung.
 */
@Epic("TestShop E2E")
@Feature("Checkout Prozess")
public class HappyPathTest extends BaseTest {

    // Testdaten
    private static final String PRODUCT_ID = "1";
    private static final String PRODUCT_CATEGORY = "Electronics";
    private static final String PRODUCT_PRICE = "299.99";

    @BeforeMethod
    public void login() {
        performLogin();
    }

    @Test
    @Story("REQ-002: Komplette Checkout Journey")
    @Description("Standard Customer Journey von Produktsuche bis Bestellbestätigung")
    @Severity(SeverityLevel.CRITICAL)
    public void standardCustomerJourney() {
        // Schritt 1: Suchen und Auswählen eines Produkts
        Allure.step("Produkt suchen und auswählen", () -> {
            shopPage.filterByCategory(PRODUCT_CATEGORY);
            shopPage.openProductDetails(PRODUCT_ID);
        });

        // Schritt 2: Zum Warenkorb hinzufügen
        Allure.step("Produkt in den Warenkorb legen", () -> {
            productDetailPage.addToCart(PRODUCT_ID);
        });

        // Schritt 3: Checkout mit dynamischen Daten
        Allure.step("Checkout mit dynamischen Benutzerdaten", () -> {
            cartPage.navigateTo();
            cartPage.increaseQuantity(PRODUCT_ID);
            cartPage.checkTotal(PRODUCT_ID, PRODUCT_PRICE);

            cartPage.proceedToCheckout();

            // Generiere realistische Fake-Daten für jeden Testlauf
            TestDataGenerator.CustomerData customer = TestDataGenerator.generateCustomer();

            Allure.addAttachment("Benutzerdaten", "text/plain", customer.toString());

            cartPage.fillShippingDetails(
                    customer.firstName + " " + customer.lastName,
                    customer.address,
                    customer.city,
                    customer.zipCode,
                    customer.email);

            cartPage.submitOrder();
            cartPage.verifyOrderSuccess();
        });
    }
}
