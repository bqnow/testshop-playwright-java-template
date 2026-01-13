package com.bqnow.testshop.pages;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Warenkorb und Checkout Page Object.
 */
public class CartPage extends BasePage {
    private static final String CART_URL = "/cart";
    private static final String CHECKOUT_INIT_BUTTON = "[data-testid='checkout-init-btn']";
    private static final String SUBMIT_ORDER_BUTTON = "[data-testid='submit-order-btn']";
    private static final String CART_TOTAL = "[data-testid='cart-total']";

    public CartPage(Page page) {
        super(page);
    }

    public void navigateTo() {
        navigate(CART_URL);
        waitForLoadState();
    }

    public void increaseQuantity(String productId) {
        String increaseButton = String.format("[data-testid='increase-qty-%s']", productId);
        page.click(increaseButton);
    }

    public void checkTotal(String productId, String unitPrice) {
        String quantitySelector = String.format("[data-testid='quantity-%s']", productId);
        String quantityText = page.locator(quantitySelector).textContent();
        int quantity = Integer.parseInt(quantityText.trim());

        double expectedTotal = Double.parseDouble(unitPrice) * quantity;
        String expectedTotalStr = String.format(java.util.Locale.US, "$%.2f", expectedTotal);

        assertThat(page.locator(CART_TOTAL)).containsText(expectedTotalStr);
    }

    public void proceedToCheckout() {
        page.click(CHECKOUT_INIT_BUTTON);
    }

    public void fillShippingDetails(String fullName, String address, String city, String zip, String email) {
        page.fill("[data-testid='checkout-name']", fullName);
        page.fill("[data-testid='checkout-address']", address);
        page.fill("[data-testid='checkout-city']", city);
        page.fill("[data-testid='checkout-zip']", zip);
        page.fill("[data-testid='checkout-email']", email);
    }

    public void submitOrder() {
        page.click(SUBMIT_ORDER_BUTTON);
    }

    public void submitOrderExpectingError(String expectedMessageSubstring) {
        // Nutze ein Array mit einem Element, um die Dialog-Nachricht zu erfassen
        // (effektiv eine mutable final Variable)
        final String[] capturedMessage = { null };

        // Setzt den Dialog-Listener auf
        page.onDialog(dialog -> {
            capturedMessage[0] = dialog.message();
            dialog.accept();
        });

        // Klick auf Absenden
        page.click(SUBMIT_ORDER_BUTTON);

        // Warten stellt sicher, dass die App Zeit hat, den Alert auszulösen.
        // Ein besserer Ansatz wäre das Warten auf eine Bedingung, aber da ein Alert
        // transient ist,
        // verlassen wir uns darauf, dass die Implementierung blockiert oder das Event
        // schnell feuert.
        // Da wir in einer synchronen Methode schwer auf ein Event warten können ohne
        // Latch,
        // nutzen wir hier ein einfaches Timeout, um die Event-Verarbeitung zuzulassen.
        // Eigentlich wäre page.waitForDialog besser, erfordert aber das Ausführen der
        // Aktion innerhalb des Waits.

        // Warten auf das Erscheinen des Dialogs
        try {
            // Gib ihm einen Moment. In einem echten Szenario wäre eine sicherere
            // Synchronisation besser.
            // Aber onDialog + click ist robust, wenn der Dialog erscheint.
            // Wir nutzen waitForTimeout der Einfachheit halber, da der Event-Loop läuft.
            page.waitForTimeout(2000);
        } catch (Exception e) {
            // ignorieren
        }

        // Überprüfe, ob wir es erfasst haben
        assert capturedMessage[0] != null : "Erwarteter Alert-Dialog erschien nicht";
        assert capturedMessage[0].contains(expectedMessageSubstring)
                : "Erwartet '" + expectedMessageSubstring + "' aber erhalten: '" + capturedMessage[0] + "'";
    }

    public void expectFieldToBeInvalid(String testId) {
        String selector = String.format("[data-testid='%s']", testId);
        boolean isValid = (boolean) page.locator(selector).evaluate("node => node.checkValidity()");
        assert !isValid : "Feld sollte ungültig sein, ist aber gültig";
    }

    public void verifyOrderSuccess() {
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Order Confirmed!"))).isVisible();

        // Verifiziere Bestell-ID Format: ORDER-{13-stelliger Zeitstempel}
        assertThat(page.locator("text=/ORDER-\\d{13}/")).isVisible();
    }
}
