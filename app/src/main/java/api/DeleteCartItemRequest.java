package api;

public class DeleteCartItemRequest {
    private String productId;
    private String userId;

    public DeleteCartItemRequest(String productId, String userId) {
        this.productId = productId;
        this.userId = userId;
    }
}
