package api;

import java.util.List;
import java.util.Map;

public class OrderRequest {
    private String orderId;
    private String userId;
    private Map<String, Object> orderData;
    private List<Map<String, Object>> orderItems;

    public OrderRequest(String orderId, String userId, Map<String, Object> orderData, List<Map<String, Object>> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderData = orderData;
        this.orderItems = orderItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Object> getOrderData() {
        return orderData;
    }

    public List<Map<String, Object>> getOrderItems() {
        return orderItems;
    }
}
