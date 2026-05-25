package tests;

import base.BaseTest;
import com.microsoft.playwright.Page;
import core.UserConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ProductPage;

/**
 * EcommerceTest — search → filter → add to cart flow.
 *
 * Single-user run:  use no-arg constructor — reads everything from config.yml.
 * Multi-user run:   EcommerceTestFactory creates instances with UserConfig,
 *                   each carrying its own credentials, search term, and brand.
 */
@Feature("E-commerce Search and Cart")
public class EcommerceTest extends BaseTest {

    private Page productDetailPage;
    private final UserConfig userConfig;

    /** Used by @Factory — each instance carries its own user config. */
    public EcommerceTest(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    /** Used by TestNG direct class execution — falls back to config.yml. */
    public EcommerceTest() {
        this.userConfig = null;
    }

    @Override
    protected UserConfig getUserConfig() {
        return userConfig;
    }

    // ── Test 1 ────────────────────────────────────────────────────────────────

    @Test(priority = 0)
    @Description("Open Amazon, verify session is loaded, search for configured product")
    public void navigateAndSearchProduct() {
        HomePage home = new HomePage(getPage());
        home.open();

        Assert.assertTrue(home.isLoggedIn(),
                "Expected to land logged-in via storageState, but 'Hello,' was not visible. "
                        + "Delete storageState.json and re-run to trigger a fresh session login.");

        if (userConfig != null) {
            home.searchProduct(userConfig.getSearchTerm());
        } else {
            home.searchProduct(); // reads laptopBrand + device from config.yml
        }
    }

    // ── Test 2 ────────────────────────────────────────────────────────────────

    @Test(priority = 1, dependsOnMethods = "navigateAndSearchProduct")
    @Description("Apply brand and RAM filters on search results page")
    public void applyProductFilters() {
        ProductPage product = new ProductPage(getPage());

        if (userConfig != null) {
            product.applyFilters(
                    userConfig.getBrand(),
                    userConfig.getRamSize(),
                    userConfig.getRamType()
            );
        } else {
            product.applyFilters();
        }
    }

    // ── Test 3 ────────────────────────────────────────────────────────────────

    @Test(priority = 2, dependsOnMethods = "applyProductFilters")
    @Description("Find first eligible product and add to cart")
    public void addProductToCart() throws InterruptedException {
        ProductPage product = new ProductPage(getPage());

        if (userConfig != null) {
            productDetailPage = product.addToCart(
                    userConfig.getBrand(),
                    userConfig.getSecondaryKeyword()
            );
        } else {
            productDetailPage = product.addToCart();
        }

        Assert.assertNotNull(productDetailPage,
                "addToCart() returned null — no eligible product was found.");
        Assert.assertTrue(product.isProductDetailPageLoaded(),
                "Product detail page did not load after Add to Cart click.");
    }

    // ── Accessor ──────────────────────────────────────────────────────────────

    public Page getProductDetailPage() {
        if (productDetailPage == null) {
            throw new IllegalStateException(
                    "productDetailPage is null — addProductToCart() has not run yet "
                            + "or ran on a different instance.");
        }
        return productDetailPage;
    }
}