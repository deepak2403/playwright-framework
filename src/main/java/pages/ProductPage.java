package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ConfigReader;
import utils.WaitHelper;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * ProductPage — search results filters + add to cart.
 *
 * Key change from original:
 *   addToCart() now returns the new tab Page so CheckoutTest
 *   can continue the flow (proceed to checkout) on that page.
 */
public class ProductPage {

    private static final Logger log = LogManager.getLogger(ProductPage.class);

    private final Page page;
    private final ConfigReader configReader;

    // Captured after clicking a search result — product opens in new tab
    private Page productDetailPage;

    public ProductPage(Page page) {
        this.page = page;
        this.configReader = new ConfigReader();
    }

    // ── Filters ───────────────────────────────────

    @Step("Apply brand, RAM size and RAM type filters")
    public void applyFilters() {
        Map<String, String> cfg = configReader.getDeviceConfig("deviceConfig");

        String brandFilter = "//span[text()='" + configReader.getString("laptopBrand")
                + "']/parent::a/div/label/input[@type='checkbox']/..";
        String ramSizeFilter = "//span[text()='RAM Size']/../..//ul/span//span/li/span/a"
                + "//span[text()='" + cfg.get("ramSize") + " GB']/../div/label";
        String ramTypeFilter = "//span[text()='RAM Technology']/../..//ul/span//span/li/span/a"
                + "//span[text()='" + cfg.get("ramType") + "']/../div/label";
        System.out.println(ramSizeFilter);

        log.info("Applying filters — brand: {}, RAM: {}GB {}", configReader.getString("laptopBrand"),
                cfg.get("ramSize"), cfg.get("ramType"));
        WaitHelper.waitForVisible(page.locator(brandFilter),10000);

        page.locator(brandFilter).check();
        WaitHelper.waitForVisible(page.locator(ramSizeFilter),10000);
        page.locator(ramSizeFilter).check();
        WaitHelper.waitForVisible(page.locator(ramTypeFilter),10000);
        page.locator(ramTypeFilter).check();

}

    @Step("Apply filters — brand: {brand}, RAM: {ramSize}GB {ramType}")
    public void applyFilters(String brand, String ramSize, String ramType) {
        if (brand == null || ramSize == null || ramType == null) {
            throw new IllegalArgumentException(
                    "applyFilters: brand, ramSize and ramType must all be non-null. "
                            + "Got — brand: " + brand + ", ramSize: " + ramSize + ", ramType: " + ramType);
        }

        String brandFilter   = "//span[normalize-space(text())='" + brand
                + "']/parent::a/div/label/input[@type='checkbox']/..";
        String ramSizeFilter = "//span[text()='RAM Size']/../..//ul/span//span/li/span/a"
                + "//span[normalize-space(text())='" + ramSize
                + " GB']/../div/label";
        String ramTypeFilter = "//span[text()='RAM Technology']/../..//ul/span//span/li/span/a"
                + "//span[normalize-space(text())='" + ramType
                + "']/../div/label";

        log.info("Applying filters — brand: {}, RAM: {}GB {}", brand, ramSize, ramType);

        WaitHelper.waitForVisible(page.locator(brandFilter), 10000);
        page.locator(brandFilter).check();
        WaitHelper.waitForVisible(page.locator(ramSizeFilter), 10000);
        page.locator(ramSizeFilter).check();
        WaitHelper.waitForVisible(page.locator(ramTypeFilter), 10000);
        page.locator(ramTypeFilter).check();
    }

    // ── Product selection ─────────────────────────

    /**
     * Finds the first laptop matching rating ≥ 3.5 and price ≤ ₹1,10,000,
     * opens its detail page in a new tab, clicks Add to Cart,
     * and returns the new tab's Page for checkout chaining.
     */
    @Step("Find eligible product and add to cart")
    public Page addToCart() throws InterruptedException {
        Locator allCards = page.locator("[data-component-type='s-search-result']");
        int totalCards = allCards.count();
        log.info("Total product cards on page: {}", totalCards);

        if (totalCards == 0) {
            throw new RuntimeException("No product cards found. URL: " + page.url());
        }

        // Step 2: Filter to only MSI + 5050 cards
        Locator matchingCards = allCards
                .filter(new Locator.FilterOptions().setHasText("MSI"))
                .filter(new Locator.FilterOptions().setHasText("5050"));

        int matchCount = matchingCards.count();
        log.info("Cards matching MSI + 5050: {}", matchCount);

        if (matchCount == 0) {
            throw new RuntimeException("No cards found containing both 'MSI' and '5050'");
        }

        // Step 3: From matching cards, find first with rating >= 3.5 and price <= 110000
        for (int i = 0; i < matchCount; i++) {
            Locator card = matchingCards.nth(i);

            // ── Rating ────────────────────────────────────────────
            Locator ratingLocator = card.locator("span.a-icon-alt").first();
            if (!ratingLocator.isVisible()) {
                log.debug("Card {}: no rating — skipping", i);
                continue;
            }
            String ratingText = ratingLocator.innerText().trim();
            double rating;
            try {
                rating = Double.parseDouble(ratingText.split(" ")[0]);
            } catch (NumberFormatException e) {
                log.debug("Card {}: unparseable rating '{}' — skipping", i, ratingText);
                continue;
            }

            // ── Price ─────────────────────────────────────────────
            Locator priceLocator = card.locator("span.a-price-whole").first();
            if (!priceLocator.isVisible()) {
                log.debug("Card {}: no price — skipping", i);
                continue;
            }
            String priceText = priceLocator.innerText()
                    .replace(",", "")
                    .replace(".", "")
                    .trim();
            int price;
            try {
                price = Integer.parseInt(priceText);
            } catch (NumberFormatException e) {
                log.debug("Card {}: unparseable price '{}' — skipping", i, priceText);
                continue;
            }

            log.info("MSI+5050 Card {}: rating={} price=₹{}", i, rating, price);

            // ── Eligibility ───────────────────────────────────────
            if (rating >= 3.5 && price <= 110000) {
                log.info("Eligible card {} found. Opening product page...", i);
                final int cardIndex = i;
                Page newPage = page.context().waitForPage(() ->
                        matchingCards.nth(cardIndex).locator("[data-cy='title-recipe'] a").click()
                );

                newPage.waitForLoadState();
                productDetailPage = newPage;
                log.info("Product page loaded: {}", newPage.url());

                newPage.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Add to cart").setExact(true)
                ).click();
                WaitHelper.dismissPopupIfPresent(productDetailPage);
                log.info("Add to Cart clicked: {}", newPage.url());
                return productDetailPage;
            }

        }

        throw new RuntimeException(
                "No eligible MSI+5050 laptop found (rating ≥ 3.5, price ≤ ₹1,10,000) across "
                        + matchCount + " matching cards."
        );
    }



    @Step("Find eligible product — brand: {brand}, keyword: {secondaryKeyword}")
    public Page addToCart(String brand, String secondaryKeyword) throws InterruptedException {
        if (brand == null || secondaryKeyword == null) {
            throw new IllegalArgumentException(
                    "addToCart: brand and secondaryKeyword must be non-null. "
                            + "Got — brand: " + brand + ", secondaryKeyword: " + secondaryKeyword);
        }

        Locator allCards = page.locator("[data-component-type='s-search-result']");
        int totalCards = allCards.count();
        log.info("Total product cards on page: {}", totalCards);

        if (totalCards == 0) {
            throw new RuntimeException(
                    "No product cards found on page. Current URL: " + page.url());
        }
//        Locator matchingCards = allCards
//                .filter(new Locator.FilterOptions().setHasText(brand));
//        if (secondaryKeyword != null && !secondaryKeyword.isEmpty()) {
//            matchingCards = matchingCards
//                    .filter(new Locator.FilterOptions().setHasText(secondaryKeyword));
//        }

        Locator matchingCards = allCards
                .filter(new Locator.FilterOptions().setHasText(brand))
                .filter(new Locator.FilterOptions().setHasText(secondaryKeyword));

        int matchCount = matchingCards.count();
        log.info("Cards matching '{}' + '{}': {}", brand, secondaryKeyword, matchCount);

        if (matchCount == 0) {
            throw new RuntimeException(
                    "No cards found containing both '" + brand + "' and '"
                            + secondaryKeyword + "'. URL: " + page.url());
        }

        for (int i = 0; i < matchCount; i++) {
            Locator card = matchingCards.nth(i);

            // ── Rating ────────────────────────────────────────────────────────
            Locator ratingLocator = card.locator("span.a-icon-alt").first();
            if (!ratingLocator.isVisible()) {
                log.debug("Card {}: no rating visible — skipping", i);
                continue;
            }
            double rating;
            try {
                rating = Double.parseDouble(
                        ratingLocator.innerText().trim().split(" ")[0]);
            } catch (NumberFormatException e) {
                log.debug("Card {}: unparseable rating '{}' — skipping",
                        i, ratingLocator.innerText());
                continue;
            }

            // ── Price ─────────────────────────────────────────────────────────
            Locator priceLocator = card.locator("span.a-price-whole").first();
            if (!priceLocator.isVisible()) {
                log.debug("Card {}: no price visible — skipping", i);
                continue;
            }
            int price;
            try {
                price = Integer.parseInt(
                        priceLocator.innerText()
                                .replace(",", "").replace(".", "").trim());
            } catch (NumberFormatException e) {
                log.debug("Card {}: unparseable price '{}' — skipping",
                        i, priceLocator.innerText());
                continue;
            }

            log.info("Card {}: rating={} price=₹{}", i, rating, price);

            // ── Eligibility ───────────────────────────────────────────────────
            if (rating >= 3.5 && price <= 110000) {
                log.info("Eligible card {} found — opening product page", i);
                final int idx = i;
                Page newPage = page.context().waitForPage(() ->
                        matchingCards.nth(idx)
                                .locator("[data-cy='title-recipe'] a").click()
                );
                newPage.waitForLoadState();
                productDetailPage = newPage;
                log.info("Product detail page loaded: {}", newPage.url());

                newPage.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Add to cart").setExact(true)
                ).click();
                WaitHelper.dismissPopupIfPresent(productDetailPage);
                log.info("Add to Cart clicked on: {}", newPage.url());
                return productDetailPage;
            }
        }

        throw new RuntimeException(
                "No eligible " + brand + " / " + secondaryKeyword
                        + " laptop found (rating ≥ 3.5, price ≤ ₹1,10,000) across "
                        + matchCount + " matching cards. URL: " + page.url());
    }



    /** Returns the product detail page opened by addToCart(). */
    public Page getProductDetailPage() {
        if (productDetailPage == null)
            throw new IllegalStateException("Call addToCart() first.");
        return productDetailPage;
    }

    // ── Cart sidebar (after Add to Cart) ─────────

    @Step("Navigate to cart via cart icon")
    public void goToCartViaIcon() {

        Locator cartButton = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Go to Cart").setExact(true));
        WaitHelper.waitForVisible(cartButton,1000);
        cartButton.click();

        page.waitForURL(Pattern.compile("cart"));
        Locator proceedToBuyButton = page.getByLabel("Proceed to Buy Buy Amazon");
        WaitHelper.waitForVisible(proceedToBuyButton,500);
        proceedToBuyButton.click();
    }

    // ── Product detail assertions ─────────────────

    @Step("Check product detail page is loaded")
    public boolean isProductDetailPageLoaded() {
        return productDetailPage != null
                && (productDetailPage.url().contains("/dp/")
                || productDetailPage.locator("#productTitle").isVisible());
    }


}