package base;

import com.microsoft.playwright.*;
import core.SessionManager;
import core.UserConfig;
import listeners.TestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;
import pages.HomePage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Listeners(TestListener.class)
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    // ── Per-method isolation (used by smoke / api / independent tests) ────────
    private static final ThreadLocal<Page>           tlPage    = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlCtx     = new ThreadLocal<>();

    // ── Per-class shared page (used by chained e2e tests) ────────────────────
    private static final ThreadLocal<Page>           tlClassPage = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlClassCtx  = new ThreadLocal<>();



    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser>    tlBrowser    = new ThreadLocal<>();
    protected Playwright getPlaywright() { return tlPlaywright.get(); }
    protected Browser    getBrowser()    { return tlBrowser.get(); }

    // ── Suite lifecycle ───────────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void globalSetUp() {
        log.info("Suite started — browsers will be created per thread");
    }

    // ── Class lifecycle ───────────────────────────────────────────────────────

    @BeforeClass(alwaysRun = true)
    public void classSetUp() {
        // Each thread gets its own Playwright + Browser instance
        Playwright pw = Playwright.create();
        boolean headless = Boolean.parseBoolean(
                System.getProperty("playwright.headless",
                        System.getenv("JENKINS_HOME") != null ? "true" : "false")
        );
        Browser br = pw.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(headless ? 0 : 500)
                        .setArgs(Arrays.asList(
                                "--start-maximized",
                                "--disable-blink-features=AutomationControlled",
                                "--no-sandbox",
                                "--disable-dev-shm-usage"
                        ))
        );
        tlPlaywright.set(pw);
        tlBrowser.set(br);

        UserConfig cfg = getUserConfig();
        Path sessionPath = (cfg != null)
                ? SessionManager.getSessionPath(cfg.getEmail())
                : SessionManager.getSessionPath();

        if (!Files.exists(sessionPath)) {
            log.info("No session for {} — performing login",
                    cfg != null ? cfg.getEmail() : "default user");
            performSessionLogin(cfg, sessionPath);
        } else {
            log.info("Session found — skipping login for {}",
                    cfg != null ? cfg.getEmail() : "default user");
        }

        Browser.NewContextOptions opts = new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/136.0.0.0 Safari/537.36")
                .setStorageStatePath(sessionPath);
        BrowserContext ctx = tlBrowser.get().newContext(opts);
        ctx.setDefaultTimeout(30000);
        ctx.addInitScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

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
        }
        Browser br = tlBrowser.get();
        if (br != null) {
            br.close();
            tlBrowser.remove();
        }
        Playwright pw = tlPlaywright.get();
        if (pw != null) {
            pw.close();
            tlPlaywright.remove();
        }
        log.info("Browser closed for thread: {}", Thread.currentThread().getName());
    }

    // ── Method lifecycle ──────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Intentionally empty — chained e2e tests use the class-scoped page.
        // Non-chained tests call setUpMethodPage() in their own @BeforeMethod.
    }

    protected void setUpMethodPage() {
        Path sessionPath = SessionManager.getSessionPath();
        Browser.NewContextOptions opts = new Browser.NewContextOptions()
                .setViewportSize(null);
        if (Files.exists(sessionPath)) {
            opts.setStorageStatePath(sessionPath);
        }
        BrowserContext ctx = tlBrowser.get().newContext(opts);
        ctx.setDefaultTimeout(30000);
        Page page = ctx.newPage();
        tlCtx.set(ctx);
        tlPage.set(page);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        BrowserContext ctx = tlCtx.get();
        if (ctx != null) {
            ctx.close();
            tlCtx.remove();
            tlPage.remove();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        log.info("Suite complete.");
    }

    // ── Page accessors ────────────────────────────────────────────────────────

    protected Page getPage()       { return tlClassPage.get(); }
    protected Page getMethodPage() { return tlPage.get(); }
    public static Page getPageStatic() { return tlClassPage.get(); }

    // ── Factory hook ──────────────────────────────────────────────────────────

    /**
     * Override in subclasses used with @Factory to supply per-instance config.
     * Returns null by default — single-user runs are unaffected.
     */
    protected UserConfig getUserConfig() {
        return null;
    }

    // ── Session login ─────────────────────────────────────────────────────────

    private void performSessionLogin(UserConfig cfg, Path saveTo) {
        BrowserContext tempCtx = tlBrowser.get().newContext(
                new Browser.NewContextOptions().setViewportSize(null)
        );
        Page tempPage = tempCtx.newPage();
        try {
            HomePage home = new HomePage(tempPage);
            home.open();
            if (cfg != null) {
                home.login(cfg.getEmail(), cfg.getPassword());
            } else {
                home.login();
            }
            SessionManager.saveSession(tempCtx, saveTo);
        } finally {
            tempPage.close();
            tempCtx.close();
        }
    }
}