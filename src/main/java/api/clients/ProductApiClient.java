package api.clients;

import api.core.ApiHelper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProductApiClient extends ApiHelper {

    private static final Logger log = LogManager.getLogger(ProductApiClient.class);
    private static final String BASE_URL = "https://automationexercise.com";

    public ProductApiClient() {
        super(BASE_URL);
    }

    /** GET /api/productsList — raw response for status/time assertions */
    public Response getAllProductsRaw() {
        return get("/api/productsList");
    }

    /** POST /api/searchProduct — raw response */
    public Response searchProduct(String keyword) {
        log.info("Searching: {}", keyword);
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .formParam("search_product", keyword)
                .post("/api/searchProduct");
    }

    /** Negative: GET on a POST-only endpoint → expect 405 in body */
    public Response searchWithWrongMethod() {
        log.info("Testing wrong HTTP method on /api/searchProduct");
        return get("/api/searchProduct");
    }

    /** Negative: search with empty keyword */
    public Response searchWithNoKeyword() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .formParam("search_product", "")
                .post("/api/searchProduct");
    }
}