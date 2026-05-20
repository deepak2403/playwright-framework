package base;

import com.microsoft.playwright.*;
import core.SessionManager;
import listeners.TestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;
import pages.HomePage;

import java.util.Arrays;

@Listeners(TestListener.class)
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    // ── Per-method isolation (used by smoke / api / independent tests) ────────
    private static final ThreadLocal<Page>           tlPage = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlCtx  = new ThreadLocal<>();

    // ── Per-class shared page (used by chained e2e tests) ────────────────────
    // One context + page lives for the entire test class, so navigation state
    // is preserved across dependsOnMethods chains (searchAndApplyFilters →
    // selectProductAndAddToCart → proceedToCheckout → ...).
    private static final ThreadLocal<Page>           tlClassPage = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlClassCtx  = new ThreadLocal<>();

    protected static Playwright playwright;
    protected static Browser    browser;

    // ── Suite lifecycle ───────────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void globalSetUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(1000).setChannel("chrome")
                        .setArgs(Arrays.asList("--start-maximized","--disable-blink-features=AutomationControlled"))
        );
        if (!SessionManager.sessionExists()) {
            log.info("No session — performing one-time UI login");
            performSessionLogin();
        } else {
            log.info("Session found — skipping login");
        }
    }

    private void performSessionLogin() {
        BrowserContext tempCtx = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(null)
        );
        Page tempPage = tempCtx.newPage();
        try {
            new HomePage(tempPage).open();
            new HomePage(tempPage).login();
            SessionManager.saveSession(tempCtx);
        } finally {
            tempPage.close();
            tempCtx.close();
        }
    }

    // ── Class lifecycle — one shared page for the whole test class ───────────

    @BeforeClass(alwaysRun = true)
    public void classSetUp() {
        Browser.NewContextOptions opts = new Browser.NewContextOptions()
                .setViewportSize(null).setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
        if (SessionManager.sessionExists()) {
            opts.setStorageStatePath(SessionManager.getSessionPath());
        }
        BrowserContext ctx = browser.newContext(opts);
        ctx.setDefaultTimeout(30000);
        ctx.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        Page page = ctx.newPage();
        tlClassCtx.set(ctx);
        tlClassPage.set(page);
        log.info("Class-scoped page created for {}", this.getClass().getSimpleName());
    }

    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        BrowserContext ctx = tlClassCtx.get();
        if (ctx != null) {
            ctx.close();
            tlClassCtx.remove();
            tlClassPage.remove();
            log.info("Class-scoped context closed for {}", this.getClass().getSimpleName());
        }
    }

    // ── Method lifecycle — fresh page per test (non-chained tests only) ───────

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Only create a per-method page if the subclass explicitly needs one.
        // Chained e2e tests use getPage() which returns the CLASS-scoped page —
        // they must NOT call setUpMethodPage() or they'll get a blank page.
        // Non-chained tests (smoke, api) can call setUpMethodPage() in their
        // own @BeforeMethod if they need method-level isolation.
    }

    /**
     * Creates a fresh per-method page. Call this from a subclass @BeforeMethod
     * only for non-chained tests that need method-level browser isolation.
     *
     * CheckoutTest and EcommerceTest must NOT call this — they rely on the
     * class-scoped page to preserve navigation state across chained methods.
     */
    protected void setUpMethodPage() {
        Browser.NewContextOptions opts = new Browser.NewContextOptions()
                .setViewportSize(null);
        if (SessionManager.sessionExists()) {
            opts.setStorageStatePath(SessionManager.getSessionPath());
        }
        BrowserContext ctx = browser.newContext(opts);
        ctx.setDefaultTimeout(30000);
        Page page = ctx.newPage();
        tlCtx.set(ctx);
        tlPage.set(page);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        // Only close if a per-method page was actually created
        BrowserContext ctx = tlCtx.get();
        if (ctx != null) {
            ctx.close();
            tlCtx.remove();
            tlPage.remove();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        if (browser != null)    browser.close();
        if (playwright != null) playwright.close();
    }

    // ── Page accessors ────────────────────────────────────────────────────────

    /**
     * Returns the CLASS-scoped page — shared across all methods in the class.
     * Use this in all chained e2e tests (CheckoutTest, EcommerceTest).
     * Navigation state is preserved between test methods.
     */
    protected Page getPage() {
        return tlClassPage.get();
    }

    /**
     * Returns the METHOD-scoped page — fresh blank page per test method.
     * Use this only in isolated, non-chained tests.
     */
    protected Page getMethodPage() {
        return tlPage.get();
    }

    public static Page getPageStatic() {
        return tlClassPage.get();
    }
}