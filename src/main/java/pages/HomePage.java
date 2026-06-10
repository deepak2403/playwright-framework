package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;

import utils.ConfigReader;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.Map;
import java.util.regex.Pattern;
import utils.WaitHelper;

public class HomePage {

    private static final Logger log = LogManager.getLogger(HomePage.class);
    private Page page;
    private ConfigReader configReader;
    private final Locator searchBox;
    private final Locator searchButton;


    public HomePage(Page page) {
        this.page = page;
        this.configReader = new ConfigReader();
        this.searchBox = page.getByRole(AriaRole.SEARCHBOX, new Page.GetByRoleOptions().setName("Search Amazon.in").setExact(false));
        this.searchButton = page.locator("//input[@id='nav-search-submit-button']");
    }


    // Replace open() with:
    @Step("Open Amazon homepage")
    public void open() {
        log.info("Opening: {}", configReader.getString("applicationUrl"));
        page.navigate(configReader.getString("applicationUrl"));
    }

    public void login() {
        Map<String,String> credentials = configReader.getDeviceConfig("creds");

        Locator accountLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("sign in Account")));
        Locator signInLink  = page.locator("//span[text()='Sign in']/parent::a");
        WaitHelper.hoverAndWaitForVisible(accountLink, signInLink);
        signInLink.click();

        page.waitForURL("**/ap/signin**");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(Pattern.compile("Enter mobile number"))).fill(credentials.get("username"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Continue"))).click();
        page.waitForURL("**/ax/claim**");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(credentials.get("password"));
        //page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Sign in"))).click();
        page.locator("#signInSubmit").click();

        // Amazon may show OTP screen — wait up to 60s for manual completion
        page.waitForURL(url ->
                        url.contains("amazon.in") &&
                                !url.contains("/ap/signin") &&
                                !url.contains("/ax/claim"),
                new Page.WaitForURLOptions().setTimeout(60000)
        );

        Locator element = page.locator("//span[contains(text(),'Hello,')]");
        assertThat(element).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));
        //assertThat(element).not().containsText("sign in");
    }

    @Step("Login as {email}")
    public void login(String email, String password) {
        Locator accountLink = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("sign in Account")));
        Locator signInLink  = page.locator("//span[text()='Sign in']/parent::a");
        WaitHelper.hoverAndWaitForVisible(accountLink, signInLink);
        signInLink.click();

        page.waitForURL("**/ap/signin**");
        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName(Pattern.compile("Enter mobile number")))
                .fill(email);
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(Pattern.compile("Continue")))
                .click();
        page.waitForURL("**/ax/claim**");
        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName("Password"))
                .fill(password);
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(Pattern.compile("Sign in")))
                .click();


        Locator element = page.locator("//span[contains(text(),'Hello,')]");
        assertThat(element).not().containsText("sign in");
    }

    @Step("Search for configured product")
    public void searchProduct() {
        searchFor(configReader.getString("laptopBrand") + " " + configReader.getString("device"));
        page.waitForURL("**/s?k=MSI+Laptops**");
    }
    @Step("Search for: {searchTerm}")
    public void searchProduct(String searchTerm) {
        searchFor(searchTerm);
        // Wait for results page — URL will contain the encoded search term
        page.waitForURL("**/s?k=**");
    }
    @Step("Search for: {keyword}")
    public void searchFor(String keyword) {
        log.info("Searching for: {}", keyword);
        searchBox.fill(keyword);
        searchButton.click();
    }

    @Step("Check if user is logged in")
    public boolean isLoggedIn() {
        Locator el = page.locator("//span[contains(text(),'Hello,')]");
        return el.isVisible() && !el.innerText().toLowerCase().contains("sign in");
    }

}
