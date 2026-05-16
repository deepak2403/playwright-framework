package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ProductPage;

public class EcommerceTest extends BaseTest {

    @Test(priority=0)
    public void navigateToBaseUrlandSearchProduct() {
        HomePage home = new HomePage(getPage());
        home.open();
        home.login();
        home.searchProduct();
    }

//    @Test(priority=1)
//    public void AddProductToCart() {
//        ProductPage product = new ProductPage(getPage());
//        product.addToCart();
//    }
}
