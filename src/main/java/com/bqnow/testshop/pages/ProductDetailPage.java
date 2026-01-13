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
        String addButton = String.format("[data-testid='add-to-cart-%s']", productId);
        // Klick erzwingen (Force), um potenzielle Overlays/Instabilitäten bei hoher
        // Last (parallele Ausführung) zu umgehen
        page.click(addButton, new Page.ClickOptions().setForce(true));

        // Warte explizit darauf, dass der LocalStorage aktualisiert wird,
        // um Race Conditions (besonders in WebKit/Docker) zu vermeiden.
        page.waitForFunction("() => {" +
                "  const cart = localStorage.getItem('cart');" +
                "  return cart && JSON.parse(cart).length > 0;" +
                "}");
    }
}
