package tests;

import base.BaseTest;
import com.microsoft.playwright.BrowserContext;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CheckoutPage;
import pages.HomePage;
import pages.ProductPage;

import com.microsoft.playwright.Page;

/**
 * CheckoutTest — Full Amazon purchase flow: Search → Cart → Payment gateway.
 *
 * Tests are chained via dependsOnMethods.
 * Run with e2e.xml (parallel="none") to preserve order.
 *
 * STOPS before placing a real order — Test 6 only asserts
 * that the Place Order button is visible.
 */
@Epic("Amazon E-Commerce")
@Feature("End-to-End Purchase Flow")
public class CheckoutTest extends BaseTest {

    // Shared across chained tests — safe because dependsOnMethods = sequential
    private static Page productDetailPage;

    private static Page searchPage;
    private static BrowserContext sharedContext;


    // ── Test 1 ────────────────────────────────────

    @Test(priority = 0,
            groups = {"e2e", "regression"},
            description = "Open Amazon and search with stored session — no UI login")
    @Story("Search")
    @Severity(SeverityLevel.BLOCKER)
    public void searchAndApplyFilters()
    {
        HomePage home = new HomePage(getPage());
        home.open();

        Assert.assertTrue(home.isLoggedIn(), "User should be logged in via stored session");

        home.searchProduct();
        Assert.assertTrue(getPage().url().contains("Laptops"), "Should land on search results page");

        ProductPage product = new ProductPage(getPage());
        product.applyFilters();
        getPage().waitForTimeout(2000);
        Assert.assertTrue(
                getPage().locator("//div[@class='puisg-row']").count() > 0,
                "At least one product should be visible after filters");

        searchPage = getPage();
    }

    // ── Test 2 ────────────────────────────────────

    @Test(priority = 1,
            groups = {"e2e", "regression"},
            dependsOnMethods = {"searchAndApplyFilters"},
            description = "Pick eligible laptop and add to cart")
    @Story("Add to Cart")
    @Severity(SeverityLevel.BLOCKER)
    public void selectProductAndAddToCart() throws InterruptedException {
        ProductPage productPage = new ProductPage(searchPage);
        productDetailPage = productPage.addToCart();

        Assert.assertNotNull(productDetailPage,
                "Product detail page should open in new tab");
        Assert.assertTrue(productPage.isProductDetailPageLoaded(),
                "Product detail page should be fully loaded");
    }

    // ── Test 3 ────────────────────────────────────

    @Test(priority = 2,
            groups = {"e2e", "regression"},
            dependsOnMethods = {"selectProductAndAddToCart"},
            description = "Proceed to checkout from cart sidebar")
    @Story("Proceed to Checkout")
    @Severity(SeverityLevel.BLOCKER)
    public void proceedToCheckout() {

        new ProductPage(productDetailPage).goToCartViaIcon();
        Assert.assertTrue(
                productDetailPage.url().contains("cart"),
                "Should be on cart page"
        );
    }

//    @Test(priority = 3,
//            groups = {"e2e", "regression"},
//            dependsOnMethods = {"proceedToCheckout"},
//            description = "Verify checkout flow till payment gateway")
//    @Story("Checkout")
//    @Severity(SeverityLevel.CRITICAL)
//    public void selectAddressAndPaymentMethod() {
//        CheckoutPage checkout = new CheckoutPage(productDetailPage);
//
//        // Address
//        Assert.assertTrue(checkout.isAddressSectionVisible(), "Address section should be visible");
//        checkout.selectDeliveryAddress();
//        productDetailPage.waitForTimeout(2000);
//
//    }
}