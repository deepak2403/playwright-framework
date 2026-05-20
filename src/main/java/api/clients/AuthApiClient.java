package api.clients;

import api.core.ApiHelper;
import api.models.LoginResponse;
import core.SessionManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles API login for automationexercise.com.
 *
 * For Amazon: login is done once via UI in @BeforeSuite,
 * saved as storageState, and reused — see SessionManager + BaseTest.
 */
public class AuthApiClient extends ApiHelper {

    private static final Logger log = LogManager.getLogger(AuthApiClient.class);
    private static final String BASE_URL = "https://automationexercise.com";

    public AuthApiClient() {
        super(BASE_URL);
    }

    /**
     * POST /api/verifyLogin
     * Logs in via API and stores token in SessionManager if returned.
     */
    public LoginResponse login(String email, String password) {
        log.info("API login → {}", email);

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .formParam("email", email)
                .formParam("password", password)
                .post("/api/verifyLogin");

        assertStatusCode(response, 200);

        LoginResponse loginResponse = response.as(LoginResponse.class);

        if (loginResponse.isSuccess()) {
            if (loginResponse.getToken() != null) {
                SessionManager.setAuthToken(loginResponse.getToken());
            }
            log.info("API login successful: {}", loginResponse.getMessage());
        } else {
            throw new RuntimeException("API login failed: " + loginResponse.getMessage());
        }

        return loginResponse;
    }

    /**
     * Quick credential check — returns true if credentials are valid.
     */
    public boolean verifyCredentials(String email, String password) {
        try {
            return login(email, password).isSuccess();
        } catch (Exception e) {
            log.error("Credential check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Raw login response — used for negative test assertions.
     */
    public Response loginRaw(String email, String password) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .formParam("email", email)
                .formParam("password", password)
                .post("/api/verifyLogin");
    }
}