package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to MAX_RETRY times before marking it failed.
 * Auto-attached to every test via TestListener — no need to add
 * retryAnalyzer = RetryAnalyzer.class on each @Test.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 2;

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("Retrying '{}' — attempt {}/{}", result.getName(), retryCount, MAX_RETRY);
            return true;
        }
        log.error("'{}' failed after {} retries", result.getName(), MAX_RETRY);
        return false;
    }
}