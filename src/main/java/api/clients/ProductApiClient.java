package api.clients;

import api.core.ApiHelper;
import api.models.ProductResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class ProductApiClient extends ApiHelper {

    private static final Logger log = LogManager.getLogger(ProductApiClient.class);
    private static final String BASE_URL = "https://automationexercise.com";

    public ProductApiClient() {
        super(BASE_URL);
    }

    /** GET /api/productsList — full typed response */
    public ProductResponse getAllProducts() {
        log.info("Fetching all products");
        Response response = get("/api/productsList");
        assertStatusCode(response, 200);
        return response.as(ProductResponse.class);
    }

    /** GET /api/productsList — raw response for status-only assertions */
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

    /** POST /api/searchProduct — typed product list */
    public List<ProductResponse.Product> searchProducts(String keyword) {
        Response response = searchProduct(keyword);
        assertStatusCode(response, 200);
        ProductResponse result = response.as(ProductResponse.class);
        log.info("Found {} products for '{}'", result.getProducts().size(), keyword);
        return result.getProducts();
    }

    /** Filter products by brand from the full list */
    public List<ProductResponse.Product> getProductsByBrand(String brand) {
        return getAllProducts().getProducts().stream()
                .filter(p -> brand.equalsIgnoreCase(p.getBrand()))
                .collect(Collectors.toList());
    }

    /** Check if a product name exists via API */
    public boolean productExists(String productName) {
        return searchProducts(productName).stream()
                .anyMatch(p -> p.getName().toLowerCase().contains(productName.toLowerCase()));
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