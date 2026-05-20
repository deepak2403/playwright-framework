package utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public class WaitHelper {
    private static final Logger log = LogManager.getLogger(WaitHelper.class);
    private static final int DEFAULT_TIMEOUT = 10000;

    // ── Locator state waits ───────────────────────

    public static void waitForVisible(Locator locator) {
        waitForVisible(locator, DEFAULT_TIMEOUT);
    }

    public static void waitForVisible(Locator locator, int timeoutMs) {
        log.info("Waiting for element to be visible (timeout: {}ms)", timeoutMs);
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }

    /**
     * Dismisses Amazon's "Not added" / sidebar popups if they appear.
     * Call this after any Add to Cart or link interaction.
     */
    public static void dismissPopupIfPresent(Page page) {
        Locator closeBtn = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("Exit this panel and return to the product page")).setExact(true));
        if (closeBtn.isVisible()) {
            closeBtn.click();
            page.waitForTimeout(500);
        }
    }

    public static void waitForHidden(Locator locator) {
        log.info("Waiting for element to be hidden");
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(DEFAULT_TIMEOUT));
    }

    public static void waitForAttached(Locator locator) {
        log.info("Waiting for element to be attached to DOM");
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(DEFAULT_TIMEOUT));
    }

    // ── Hover then wait ───────────────────────────

    /**
     * Hover over a locator, then wait for a target locator to become visible.
     * Use case: navbar hover reveals a dropdown — wait for dropdown link before clicking.
     */
    public static void hoverAndWaitForVisible(Locator hoverTarget, Locator waitTarget) {
        hoverAndWaitForVisible(hoverTarget, waitTarget, DEFAULT_TIMEOUT);
    }

    public static void hoverAndWaitForVisible(Locator hoverTarget, Locator waitTarget, int timeoutMs) {
        log.info("Hovering element and waiting for target to become visible");
        hoverTarget.hover();
        waitForVisible(waitTarget, timeoutMs);
    }

    // ── Page level waits ──────────────────────────

    public static void waitForUrl(Page page, String urlPattern) {
        log.info("Waiting for URL to match: {}", urlPattern);
        page.waitForURL("**" + urlPattern + "**");
    }

    public static void waitForPageLoad(Page page) {
        log.info("Waiting for page load state");
        page.waitForLoadState();
    }

    public static void hardWait(Page page, int millis) {
        log.info("Hard wait: {}ms", millis);
        page.waitForTimeout(millis);
    }
}
