package com.bqnow.testshop.pages;

import com.microsoft.playwright.Page;

/**
 * Basis-Page-Klasse mit gemeinsamer Funktionalität für alle Page Objects.
 */
public abstract class BasePage {
    protected final Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    protected void navigate(String path) {
        page.navigate(path);
    }

    protected void waitForLoadState() {
        page.waitForLoadState();
    }
}
