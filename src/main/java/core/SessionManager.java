package core;

import com.microsoft.playwright.BrowserContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SessionManager {

    private static final Logger log = LogManager.getLogger(SessionManager.class);

    private static final String SESSION_DIR  = "src/test/resources/session/";
    private static final String SESSION_FILE = SESSION_DIR + "storageState.json";

    private static String authToken;

    // ── storageState ──────────────────────────────

    public static void saveSession(BrowserContext context) {
        try {
            new File(SESSION_DIR).mkdirs();
            context.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(Paths.get(SESSION_FILE)));
            log.info("Session saved → {}", SESSION_FILE);
        } catch (Exception e) {
            log.error("Failed to save session: {}", e.getMessage());
            throw new RuntimeException("Session save failed", e);
        }
    }

    /**
     * Returns a per-user session path derived from the email address.
     * Used by @Factory runs where each instance needs its own session file.
     * Example: user1@gmail.com → storageState_user1_gmail_com.json
     */
    public static Path getSessionPath(String email) {
        String sanitized = email.replaceAll("[^a-zA-Z0-9]", "_");
        return Paths.get(SESSION_DIR + "storageState_" + sanitized + ".json");
    }

    /**
     * Save session to an explicit path — used by @Factory per-user login.
     */
    public static void saveSession(BrowserContext context, Path saveTo) {
        try {
            new File(SESSION_DIR).mkdirs();
            context.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(saveTo));
            log.info("Session saved → {}", saveTo);
        } catch (Exception e) {
            log.error("Failed to save session: {}", e.getMessage());
            throw new RuntimeException("Session save failed", e);
        }
    }

    public static boolean sessionExists() {
        File f = new File(SESSION_FILE);
        return f.exists() && f.length() > 0;
    }

    public static Path getSessionPath() {
        return Paths.get(SESSION_FILE);
    }

    public static void clearSession() {
        File f = new File(SESSION_FILE);
        if (f.exists()) {
            f.delete();
            log.info("Session cleared.");
        }
    }

    // ── Auth Token ────────────────────────────────

    public static void setAuthToken(String token) {
        authToken = token;
        log.info("Auth token stored.");
    }

    public static String getAuthToken() {
        if (authToken == null) log.warn("Auth token is null — was API login called?");
        return authToken;
    }

    public static boolean hasAuthToken() {
        return authToken != null && !authToken.isEmpty();
    }
}