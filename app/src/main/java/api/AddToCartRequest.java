package api;

public class AddToCartRequest {
    private String userId;
    private String productId;
    private int qty;

    public AddToCartRequest(String userId, String productId, int qty) {
        this.userId = userId;
        this.productId = productId;
        this.qty = qty;
    }
}
