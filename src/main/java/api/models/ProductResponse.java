package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

    @JsonProperty("responseCode")
    private int responseCode;

    @JsonProperty("products")
    private List<Product> products;

    public int            getResponseCode()            { return responseCode; }
    public void           setResponseCode(int rc)      { this.responseCode = rc; }
    public List<Product>  getProducts()                { return products; }
    public void           setProducts(List<Product> p) { this.products = p; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {

        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("price")
        private String price;

        @JsonProperty("brand")
        private String brand;

        @JsonProperty("category")
        private Category category;

        public int      getId()                  { return id; }
        public void     setId(int id)            { this.id = id; }
        public String   getName()                { return name; }
        public void     setName(String name)     { this.name = name; }
        public String   getPrice()               { return price; }
        public void     setPrice(String price)   { this.price = price; }
        public String   getBrand()               { return brand; }
        public void     setBrand(String brand)   { this.brand = brand; }
        public Category getCategory()            { return category; }
        public void     setCategory(Category c)  { this.category = c; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {

        @JsonProperty("usertype")
        private UserType usertype;

        @JsonProperty("category")
        private String category;

        public UserType getUsertype()              { return usertype; }
        public void     setUsertype(UserType u)    { this.usertype = u; }
        public String   getCategory()              { return category; }
        public void     setCategory(String cat)    { this.category = cat; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserType {

        @JsonProperty("usertype")
        private String usertype;

        public String getUsertype()              { return usertype; }
        public void   setUsertype(String u)      { this.usertype = u; }
    }
}