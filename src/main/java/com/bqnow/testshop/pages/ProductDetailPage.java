package com.bqnow.testshop.pages;

import com.microsoft.playwright.Page;

/**
 * Produktdetail-Page-Object.
 */
public class ProductDetailPage extends BasePage {

    public ProductDetailPage(Page page) {
        super(page);
    }

    public void addToCart(String productId) {
        String addButton = "[data-testid='add-to-cart-large']";
        // Standard-Klick verwenden, damit Playwright wartet, bis der Button actionable
        // ist
        // (verhindert Klicks vor Abschluss der React-Hydration)
        page.click(addButton);

        // Warte explizit darauf, dass der LocalStorage aktualisiert wird,
        // um Race Conditions (besonders in WebKit/Docker) zu vermeiden.
        page.waitForFunction("() => {" +
                "  const cart = localStorage.getItem('cart');" +
                "  return cart && JSON.parse(cart).length > 0;" +
                "}");
    }
}
