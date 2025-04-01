package model;

import java.io.Serializable;

public class CartItem {
    private String productId;
    private String imageUrl;
    private String productName;
    private String size;
    private String price;
    private int qty;
    private boolean isAvailable;

    public CartItem(String productId, String imageUrl, String productName, String size, String price, int qty) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.productName = productName;
        this.size = size;
        this.price = price;
        this.qty = qty;
        this.isAvailable = true;
    }

    public CartItem() {
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getProductId() { return productId; }
    public String getImageUrl() { return imageUrl; }
    public String getProductName() { return productName; }
    public String getSize() { return size; }
    public String getPrice() { return price; }
    public int getQty() { return qty; }

    public void setQty(int qty) {
        this.qty = qty;
    }
}

