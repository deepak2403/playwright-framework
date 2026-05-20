package tests;

import base.BaseTest;
import com.microsoft.playwright.Page;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ProductPage;

/**
 * EcommerceTest — search → filter → add to cart flow.
 *
 * Session note:
 *   BaseTest.@BeforeMethod loads storageState into every BrowserContext,
 *   so the page is already authenticated when setUp() completes.
 *   Never call home.login() here — it would attempt a second sign-in
 *   on an already-authenticated page and fail.
 *
 * Page-chaining note:
 *   ProductPage.addToCart() opens the product in a NEW browser tab and
 *   returns that tab's Page object. We store it in productDetailPage so
 *   CheckoutTest (or a future continuation test) can pick up from there.
 */
@Feature("E-commerce Search and Cart")
public class EcommerceTest extends BaseTest {

    /** Shared across tests in this class — the new tab opened by addToCart(). */
    private Page productDetailPage;

    // ── Test 1 ────────────────────────────────────────────────────────────────

    @Test(priority = 0)
    @Description("Navigate to base URL (session already loaded), verify login state, then search for the configured product")
    public void navigateAndSearchProduct() {
        HomePage home = new HomePage(getPage());

        // Navigate to the storefront — session cookie makes us land logged-in.
        home.open();

        // Guard: confirm the session loaded correctly before proceeding.
        // If this assertion fails, check storageState.json — it may have expired.
        Assert.assertTrue(home.isLoggedIn(),
                "Expected to land logged-in via storageState, but 'Hello,' was not visible. "
                        + "Delete storageState.json and re-run to trigger a fresh session login.");

        // Search for the configured product (Brand + device from config.yml).
        home.searchProduct();
    }

    // ── Test 2 ────────────────────────────────────────────────────────────────

    @Test(priority = 1, dependsOnMethods = "navigateAndSearchProduct")
    @Description("Apply brand/RAM filters on search results page")
    public void applyProductFilters() {
        ProductPage product = new ProductPage(getPage());
        product.applyFilters();
    }

    // ── Test 3 ────────────────────────────────────────────────────────────────

    @Test(priority = 2, dependsOnMethods = "applyProductFilters")
    @Description("Find the first eligible laptop (rating ≥ 3.5, price ≤ ₹1,10,000) and click Add to Cart")
    public void addProductToCart() throws InterruptedException {
        ProductPage product = new ProductPage(getPage());

        // addToCart() opens a new tab; we must hold onto that Page reference.
        // Discarding it (as the original code did) means checkout has no page to act on.
        productDetailPage = product.addToCart();

        Assert.assertNotNull(productDetailPage,
                "addToCart() returned null — no eligible product was found.");
        Assert.assertTrue(product.isProductDetailPageLoaded(),
                "Product detail page did not load after Add to Cart click.");
    }

    // ── Accessor for cross-test page chaining ─────────────────────────────────

    /**
     * Returns the product detail tab opened by addToCart().
     * CheckoutTest can extend or delegate to this class and call this method
     * to continue the checkout flow on the correct page.
     *
     * Example usage in CheckoutTest:
     * <pre>
     *   Page detailPage = ecommerceTest.getProductDetailPage();
     *   new CheckoutPage(detailPage).proceedToCheckout();
     * </pre>
     */
    public Page getProductDetailPage() {
        if (productDetailPage == null) {
            throw new IllegalStateException(
                    "productDetailPage is null — addProductToCart() has not run yet "
                            + "or ran on a different instance.");
        }
        return productDetailPage;
    }
}