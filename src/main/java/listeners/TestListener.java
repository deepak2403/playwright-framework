package listeners;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hooks into TestNG lifecycle to:
 *   1. Auto-attach RetryAnalyzer to every test
 *   2. Take screenshot on failure → saves to target/screenshots + attaches to Allure
 *   3. Log test start / pass / fail / skip
 *
 * Registered via @Listeners(TestListener.class) on BaseTest.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);
    private static final String SCREENSHOT_DIR = "target/screenshots/";

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ STARTED : {}", result.getName());
        // Auto-wire RetryAnalyzer so we don't need it on every @Test annotation
        if (result.getMethod().getRetryAnalyzerClass() == null) {
            result.getMethod().setRetryAnalyzerClass(RetryAnalyzer.class);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ PASSED  : {} ({}ms)", result.getName(), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("❌ FAILED  : {} — {}", result.getName(), result.getThrowable().getMessage());
        captureScreenshot(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⏭ SKIPPED : {}", result.getName());
    }

    // ── Screenshot ────────────────────────────────

    private void captureScreenshot(ITestResult result) {
        try {
            Page page = base.BaseTest.getPageStatic();
            if (page == null) {
                log.warn("No Page available for screenshot.");
                return;
            }

            Files.createDirectories(Paths.get(SCREENSHOT_DIR));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path path = Paths.get(SCREENSHOT_DIR + result.getName() + "_" + timestamp + ".png");

            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions().setPath(path).setFullPage(true)
            );

            log.info("Screenshot → {}", path);

            Allure.addAttachment(
                    "Failure Screenshot: " + result.getName(),
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );

        } catch (IOException e) {
            log.error("Screenshot capture failed: {}", e.getMessage());
        }
    }

    private long duration(ITestResult r) {
        return r.getEndMillis() - r.getStartMillis();
    }
}