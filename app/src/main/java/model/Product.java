package model;

import java.io.Serializable;

public class Product implements Serializable {

    private String id;
    private String imageUrl;
    private String productName;
    private String price;

    public Product(String id, String imageUrl, String productName, String price) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.productName = productName;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
