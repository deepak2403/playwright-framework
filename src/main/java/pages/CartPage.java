package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CartPage {

    private static final Logger log = LogManager.getLogger(CartPage.class);
    private final Page page;

    private final Locator cartItems;
    private final Locator proceedToCheckoutBtn;
    private final Locator orderTotal;

    public CartPage(Page page) {
        this.page = page;
        this.cartItems            = page.locator("div[data-name='Active Items'] .sc-list-item");
        this.proceedToCheckoutBtn = page.locator("input[name='proceedToRetailCheckout']");
        this.orderTotal           = page.locator("#sc-subtotal-amount-activecart");
    }

    @Step("Check cart is not empty")
    public boolean isCartNotEmpty() {
        return cartItems.count() > 0;
    }

    @Step("Check product is in cart")
    public boolean hasProductInCart() {
        return cartItems.count() > 0;
    }

    @Step("Check Proceed to Checkout is visible")
    public boolean hasProceedToCheckout() {
        return proceedToCheckoutBtn.isVisible();
    }

    @Step("Get cart item count")
    public int getCartItemCount() {
        return cartItems.count();
    }

    @Step("Get subtotal text")
    public String getSubtotalText() {
        return orderTotal.isVisible() ? orderTotal.innerText().trim() : "";
    }

    @Step("Proceed to checkout")
    public void proceedToCheckout() {
        proceedToCheckoutBtn.click();
        page.waitForURL("**/checkout/**");
    }
}