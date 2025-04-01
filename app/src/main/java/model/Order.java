package model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private Date orderedDate;
    private List<OrderItem> orderItemList;
    private String deliveryFee;
    private String totalPrice;
    private String status;
    private int points;

    public Order(String orderId, Date orderedDate, List<OrderItem> orderItemList, String deliveryFee, String totalPrice, String status, int points) {
        this.orderId = orderId;
        this.orderedDate = orderedDate;
        this.orderItemList = orderItemList;
        this.deliveryFee = deliveryFee;
        this.totalPrice = totalPrice;
        this.status = status;
        this.points = points;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(Date orderedDate) {
        this.orderedDate = orderedDate;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
