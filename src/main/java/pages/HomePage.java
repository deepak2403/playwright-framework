package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import utils.ConfigReader;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.Map;
import java.util.regex.Pattern;

public class HomePage {

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

    public void open() {
        page.navigate(configReader.getString("applicationUrl"));
    }

    public void login() {
        Map<String,String> credentials = configReader.getDeviceConfig("creds");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("sign in Account"))).hover();
        page.locator("//span[text()='Sign in']/parent::a").click();
        page.waitForURL("**/ap/signin**");

        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(Pattern.compile("Enter mobile number"))).fill(credentials.get("username"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Continue"))).click();
        page.waitForURL("**/ax/claim**");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(credentials.get("password"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Sign in"))).click();
        Locator element = page.locator("//span[contains(text(),'Hello,')]");
        assertThat(element).not().containsText("sign in");



    }

    public void searchProduct() {

        searchBox.fill(configReader.getString("Brand").toString()+" "+configReader.getString("device").toString());
        searchButton.click();

    }


//    public void selectProduct() {
//        page.locator().click();
//    }
}
