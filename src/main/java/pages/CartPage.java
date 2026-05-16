package pages;

import com.microsoft.playwright.Page;
import org.testng.Assert;

public class CartPage {

    private Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    public void verifyCart() {
        Assert.assertTrue(page.locator(".cart_description").isVisible(),
                "Product not visible in cart");
    }
}
