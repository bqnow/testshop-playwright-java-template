package com.bqnow.testshop.pages;

import com.microsoft.playwright.Page;

/**
 * Shop (Home) Page Object mit Produktsuche und -auswahl.
 */
public class ShopPage extends BasePage {
    private static final String SEARCH_INPUT = "[data-testid='search-input']";
    private static final String SEARCH_BUTTON = "[data-testid='search-submit']";

    public ShopPage(Page page) {
        super(page);
    }

    public void navigateTo() {
        navigate("/");
        waitForLoadState();
    }

    public void searchProduct(String searchTerm) {
        page.fill(SEARCH_INPUT, searchTerm);
        page.click(SEARCH_BUTTON);
        page.waitForURL("**/*query=*");
    }

    public void filterByCategory(String category) {
        String categorySelector = String.format("[data-testid='category-%s']", category.toLowerCase());
        page.click(categorySelector);
    }

    public void openProductDetails(String productId) {
        String productCard = String.format("[data-testid='product-card-%s']", productId);
        page.locator(productCard).getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("View")).click();
    }

    public void addProductDirectlyToCart(String productId) {
        String addToCartButton = String.format("[data-testid='add-to-cart-%s']", productId);
        page.click(addToCartButton);
    }
}
