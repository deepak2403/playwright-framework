package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import utils.ConfigReader;

import java.util.Map;

public class ProductPage {

    private Page page;
    private ConfigReader configReader;

    public ProductPage(Page page) {
        this.page = page;
        this.configReader = new ConfigReader();;
    }



    public void addToCart() {

        Map<String,String> deviceSearchConfig = configReader.getDeviceConfig("deviceConfig");
        String brandFilter = "//span[text()='"+configReader.getString("laptopBrand").toString()+"']/parent::a/div/label/input[@type='checkbox']/..";
        String ramSizeFilter = "//span[text()='RAM Size']/../..//ul/span//span/li/span/a//span[text()='"+deviceSearchConfig.get("ramSize").toString()+" GB']/../div/label";
        String ramTechnologyFilter = "//span[text()='RAM Technology']/../..//ul/span//span/li/span/a//span[text()='"+deviceSearchConfig.get("ramType").toString()+"']/../div/label";
        page.locator(brandFilter).check();
        page.locator(ramSizeFilter).check();
        page.locator(ramTechnologyFilter).check();


        Locator laptops = page.locator("//div[@class='puisg-row']")
                .filter(new Locator.FilterOptions().setHasText("MSI"))
                .filter(new Locator.FilterOptions().setHasText("RTX 5050"));
        for(int i=0;i<laptops.count();i++)
        {
            Locator laptop = laptops.nth(i);


            double rating = Double.parseDouble(laptop
                    .locator("[data-cy='reviews-block']")
                    .locator("span")
                    .first()
                    .innerText());

            int pricing = Integer.parseInt(laptop.locator("[data-cy='price-recipe'] div div a span span").
                    first().innerText().
                    replace("₹","").
                    replace(",",""));

            if (rating >= 3.5 && pricing <= 110000)
            {
                Page newPage = page.context().waitForPage(() -> {
                    laptop.locator("[data-cy='title-recipe'] a").click();
                });
                newPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Add to cart").setExact(true)).click();
                break;
            }
        }

//        page.locator("button:has-text('Add to cart')").click();
//        page.locator("u:has-text('View Cart')").click();
    }
}
