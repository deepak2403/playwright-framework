package tests;

import api.clients.AuthApiClient;
import api.clients.ProductApiClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

@Epic("API Validation")
@Feature("automationexercise.com REST API")
public class ApiTest {

    private ProductApiClient productApi;
    private AuthApiClient    authApi;

    @BeforeClass
    public void setUpApiClients() {
        productApi = new ProductApiClient();
        authApi    = new AuthApiClient();
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    @Test(groups = {"api", "regression"},
            dataProvider = "invalidCredentials",
            description = "Invalid credentials return 404 in response body")
    @Story("Auth API - Negative")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /api/verifyLogin — negative cases with bad credentials")
    public void verifyLoginWithInvalidCredentials(String email, String password, String scenario) {
        Response response = authApi.loginRaw(email, password);

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(response.getStatusCode(), 200,
                "HTTP status should be 200 — scenario: " + scenario);
        soft.assertEquals(response.jsonPath().getInt("responseCode"), 404,
                "Body responseCode should be 404 — scenario: " + scenario);
        soft.assertAll();
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][] {
                {"wrong@email.com", "wrongpass",  "Wrong email and password"},
                {"",                "Test@1234",  "Empty email"},
                {"test@example.com","",           "Empty password"},
        };
    }

    // ── PRODUCTS ──────────────────────────────────────────────────────────────

    @Test(groups = {"api", "smoke"},
            description = "GET /api/productsList returns 200")
    @Story("Product API")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify products list endpoint is reachable and returns 200")
    public void verifyProductsListStatusCode() {
        Response response = productApi.getAllProductsRaw();
        Assert.assertEquals(response.getStatusCode(), 200,
                "Products list should return HTTP 200");
    }

    @Test(groups = {"api", "regression"},
            description = "GET on POST-only searchProduct returns 405 in body")
    @Story("Product API - Negative")
    @Severity(SeverityLevel.MINOR)
    @Description("Wrong HTTP method on /api/searchProduct should return 405 in body")
    public void verifySearchProductWrongMethod() {
        Response response = productApi.searchWithWrongMethod();
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(response.getStatusCode(), 200, "HTTP status should be 200");
        soft.assertEquals(response.jsonPath().getInt("responseCode"), 405,
                "Body responseCode should be 405 for wrong HTTP method");
        soft.assertAll();
    }

    @Test(groups = {"api", "regression"},
            description = "Search with empty keyword returns 200")
    @Story("Product API - Negative")
    @Severity(SeverityLevel.MINOR)
    @Description("POST /api/searchProduct with empty string — should handle gracefully")
    public void verifySearchWithEmptyKeyword() {
        Response response = productApi.searchWithNoKeyword();
        Assert.assertEquals(response.getStatusCode(), 200,
                "Empty search should return HTTP 200");
    }

    // ── PERFORMANCE ───────────────────────────────────────────────────────────

    @Test(groups = {"api", "regression"},
            description = "Products list API responds within 3 seconds")
    @Story("API Performance")
    @Severity(SeverityLevel.MINOR)
    @Description("Response time SLA: GET /api/productsList must respond under 3000ms")
    public void verifyProductsApiResponseTime() {
        Response response = productApi.getAllProductsRaw();
        long responseTime = response.getTime();
        Assert.assertTrue(responseTime < 3000,
                "Response time should be < 3000ms, actual: " + responseTime + "ms");
    }
}