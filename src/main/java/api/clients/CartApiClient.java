package api.clients;

import api.core.ApiHelper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CartApiClient extends ApiHelper {

    private static final Logger log = LogManager.getLogger(CartApiClient.class);
    private static final String BASE_URL = "https://automationexercise.com";

    public CartApiClient() {
        super(BASE_URL);
    }

    /** POST /api/addToCart */
    public Response addToCart(int productId, int quantity) {
        log.info("Add to cart — productId: {}, qty: {}", productId, quantity);
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .formParam("product_id", productId)
                .formParam("quantity", quantity)
                .post("/api/addToCart");
    }

    public Response addToCart(int productId) {
        return addToCart(productId, 1);
    }

    public int addToCartStatus(int productId) {
        return addToCart(productId).getStatusCode();
    }

    /** DELETE /api/deleteCartItem */
    public Response removeFromCart(int productId) {
        log.info("Remove from cart — productId: {}", productId);
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .formParam("product_id", productId)
                .delete("/api/deleteCartItem");
    }

    /**
     * Hybrid helper: pre-populate cart via API before a UI test.
     * Call in @BeforeMethod so the UI test only needs to assert rendering.
     */
    public void setupCartForUITest(int productId) {
        log.info("Setting up cart via API for UI test — productId: {}", productId);
        Response response = addToCart(productId, 1);
        int status = response.getStatusCode();
        if (status != 200 && status != 201) {
            throw new RuntimeException("Cart API setup failed. Status: " + status);
        }
        log.info("Cart ready for UI assertion.");
    }
}