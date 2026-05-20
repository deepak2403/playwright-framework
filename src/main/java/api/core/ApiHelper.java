package api.core;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiHelper {

    private static final Logger log = LogManager.getLogger(ApiHelper.class);

    protected RequestSpecification requestSpec;

    public ApiHelper(String baseUri) {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.ALL)
                .build();
    }

    // ── HTTP Methods ──────────────────────────────

    protected Response get(String endpoint) {
        log.info("GET → {}", endpoint);
        return RestAssured.given(requestSpec).get(endpoint);
    }

    protected Response get(String endpoint, String token) {
        log.info("GET (auth) → {}", endpoint);
        return RestAssured.given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .get(endpoint);
    }

    protected Response post(String endpoint, Object body) {
        log.info("POST → {}", endpoint);
        return RestAssured.given(requestSpec).body(body).post(endpoint);
    }

    protected Response post(String endpoint, Object body, String token) {
        log.info("POST (auth) → {}", endpoint);
        return RestAssured.given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(body).post(endpoint);
    }

    protected Response delete(String endpoint, String token) {
        log.info("DELETE (auth) → {}", endpoint);
        return RestAssured.given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .delete(endpoint);
    }

    // ── Helpers ───────────────────────────────────

    protected void assertStatusCode(Response response, int expected) {
        int actual = response.getStatusCode();
        if (actual != expected) {
            log.error("Expected {} got {}. Body: {}", expected, actual, response.getBody().asString());
            throw new AssertionError("Status mismatch. Expected: " + expected + " Actual: " + actual);
        }
        log.info("Status verified: {}", actual);
    }

    protected String extractField(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    protected <T> T parseResponse(Response response, Class<T> clazz) {
        return response.then().extract().as(clazz);
    }
}