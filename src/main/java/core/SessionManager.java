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