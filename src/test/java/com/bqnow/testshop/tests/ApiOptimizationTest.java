package com.bqnow.testshop.tests;

import com.bqnow.testshop.base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * REQ-006: Zustandspersistenz und API-Optimierung Showcase.
 * Demonstriert Testoptimierung durch Umgehung der UI für das Setup.
 */
@Epic("TestShop E2E")
@Feature("Performance Optimierung")
public class ApiOptimizationTest extends BaseTest {

    private static final String PRODUCT_ID = "1";
    private static final String PRODUCT_NAME = "Premium Wireless Headphones";
    private static final double PRODUCT_PRICE = 299.99;

    @BeforeMethod
    public void login() {
        performLogin();
    }

    @Test
    @Story("REQ-006: Zustandsinjektion")
    @Description("Checkout-Setup via Zustandsinjektion (Umgehung der UI, ~60% schneller)")
    @Severity(SeverityLevel.NORMAL)
    public void checkoutSetupViaStateInjection() {
        // Schritt 1: Warenkorb-Zustand direkt injizieren (Umgehung der UI)
        Allure.step("Setup: Warenkorb via Zustand injizieren (Keine UI-Interaktion)", () -> {
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", PRODUCT_ID);
            productData.put("name", PRODUCT_NAME);
            productData.put("price", PRODUCT_PRICE);

            page.evaluate("(productData) => {" +
                    "const cartState = [{" +
                    "  id: productData.id," +
                    "  name: productData.name," +
                    "  price: productData.price," +
                    "  quantity: 1" +
                    "}];" +
                    "localStorage.setItem('cart', JSON.stringify(cartState));" +
                    "}", productData);
        });

        // Schritt 2: Navigiere direkt zum Warenkorb und messe Performance
        Allure.step("Aktion: Navigiere & messe Performance", () -> {
            long startTime = System.currentTimeMillis();

            page.navigate("/cart");
            assertThat(page.getByTestId("cart-total")).isVisible();

            long duration = System.currentTimeMillis() - startTime;
            Allure.addAttachment("Warenkorb Renderzeit", "text/plain", duration + "ms");

            // Performance Assertion: sollte sehr schnell sein (< 1000ms inklusive
            // Netzwerk-Overhead)
            assert duration < 1000 : "Warenkorb Renderzeit sollte unter 1000ms sein, war aber: " + duration + "ms";
        });

        // Schritt 3: Verifiziere, dass das injizierte Produkt im Warenkorb ist
        Allure.step("Verifizierung: Produkt ist im Warenkorb", () -> {
            assertThat(page.getByTestId("quantity-" + PRODUCT_ID)).isVisible();
            assertThat(page.getByTestId("cart-total")).containsText(String.valueOf(PRODUCT_PRICE));
        });
    }
}
