package tests;

import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiTest {

    @Test
    public void verifyProductsApi() {
        int statusCode =
                RestAssured.get("https://automationexercise.com/api/productsList")
                        .getStatusCode();

        Assert.assertEquals(statusCode, 200);
    }
}
