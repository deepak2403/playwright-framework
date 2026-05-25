package api.clients;

import api.core.ApiHelper;
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