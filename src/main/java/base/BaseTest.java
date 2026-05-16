package base;

import com.microsoft.playwright.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Arrays;

public class BaseTest {

    protected static ThreadLocal<Page> page = new ThreadLocal<>();
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;

    @BeforeMethod
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(0).setArgs(Arrays.asList("--start-maximized")));
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
        page.set(context.newPage());


    }

    protected Page getPage() {
        return page.get();
    }

    @AfterMethod
    public void tearDown() {
        context.close();
        browser.close();
        playwright.close();
    }
}
