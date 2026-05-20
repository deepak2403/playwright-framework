package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * CheckoutPage — Amazon checkout flow from address selection to payment page.
 *
 * FLOW:
 *   selectDeliveryAddress()
 *   → selectDeliveryOption()
 *   → verifyOrderSummary via getters
 *   → isPaymentPageVisible()
 *   → isPlaceOrderButtonVisible()   ← STOP HERE (no real purchase)
 */
public class CheckoutPage {

    private static final Logger log = LogManager.getLogger(CheckoutPage.class);

    private final Page page;

    // Address
    private final Locator addressSection;
    private final Locator firstAddressRadio;
    private final Locator continueBtn;

    // Order summary
    private final Locator orderTotal;
    private final Locator orderSubtotal;
    private final Locator productNameInSummary;

    // Payment
    private final Locator paymentHeading;
    private final Locator codOption;
    private final Locator upiOption;
    private final Locator placeOrderBtn;

    // Confirmation
    private final Locator confirmationHeading;

    public CheckoutPage(Page page) {
        this.page = page;

        this.addressSection      = page.locator("#address-book-entry-0");
        this.firstAddressRadio   = page.locator("input[name='address-ui-widgets-shipToThisAddress']").first();
        this.continueBtn         = page.locator("input[name='continue-top']").first();

        this.orderTotal          = page.locator("//td[contains(text(),'Order Total')]/following-sibling::td/span");
        this.orderSubtotal       = page.locator("//span[contains(text(),'Items')]/following-sibling::span");
        this.productNameInSummary= page.locator(".shipment-top-row span.a-text-bold").first();

        this.paymentHeading      = page.locator("h1:has-text('Select a payment')")
                .or(page.locator("h1:has-text('Choose a payment')"));
        this.codOption           = page.locator("//label[contains(.,'Cash on Delivery')]//input");
        this.upiOption           = page.locator("//label[contains(.,'UPI')]//input").first();
        this.placeOrderBtn       = page.locator("input[name='placeYourOrder1']");

        this.confirmationHeading = page.locator("h4:has-text('Order placed')")
                .or(page.locator("h1:has-text('Thank you')")).first();
    }

    // ── Step 1: Delivery Address ──────────────────

    @Step("Verify address section is visible")
    public boolean isAddressSectionVisible() {
        page.pause();
        return page.locator("h1:has-text('Choose a delivery address')")
                .or(page.locator("h2:has-text('Choose a shipping address')"))
                .isVisible();
    }
    @Step("Select delivery address")
    public void selectDeliveryAddress() {
        log.info("Selecting delivery address");
        page.getByLabel("Change delivery address").click();
        page.getByLabel("Delivery addresses (4)").click();
        page.locator("#select-destination-on-sasp-desktop-panel-id-4X5GXNDSFAALBYAMWCTAG12KBBM69Z0ALA2ALA0Z96MBBK2PXTQ2EQA2OXIGSXZV i").click();
        page.getByTestId("bottom-continue-button").click();
        log.info("Delivery address selected");
    }

    @Step("Select delivery speed")
    public void selectDeliveryOption() {
        log.info("Selecting delivery option - Tomorrow");
        page.getByLabel("Tomorrow, 21 May").check();
    }

    @Step("Verify Place Order button is visible")
    public void verifyPlaceOrderVisible() {
        log.info("Asserting Place Order button is visible");
        assertThat(page.locator("#submitOrderButtonId")
                .getByTestId("SPC_selectPlaceOrder"))
                .isVisible();
    }

    @Step("Click Place Order")
    public void placeOrder() {
        log.info("Clicking Place Order");
        page.locator("#submitOrderButtonId")
                .getByTestId("SPC_selectPlaceOrder")
                .click();
    }
    @Step("Select payment method - Net Banking ICICI")
    public void selectPaymentMethod() {
        log.info("Selecting Net Banking - ICICI");
        page.getByLabel("Net Banking").check();
        page.locator("span")
                .filter(new Locator.FilterOptions().setHasText("Choose an Option"))
                .nth(3)
                .click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("ICICI Bank")).click();
        page.getByTestId("bottom-continue-button").click();
        log.info("Payment method selected");
    }


}