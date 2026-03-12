package com.example.giga67.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private LocalDateTime createdAt;
    private String status;
    private double totalPrice;
    private List<CartItem> items;

    public Order(String id, String userId, List<CartItem> items, double totalPrice, LocalDateTime createdAt, String status) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = status;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public List<CartItem> getItems() {
        return items;
    }
    public String getFormattedDate() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return createdAt.format(formatter);
        }
        return "";
    }

    public String getStatusText() {
        switch (status) {
            case "pending":
                return "Ожидает обработки";
            case "processing":
                return "В обработке";
            case "shipped":
                return "Отправлен";
            case "done":
                return "Доставлен";
            case "canceled":
                return "Отменён";
            case "new":
                return "Новый";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Заказ #" + id + " | " + getFormattedDate() + " | " + totalPrice + "₽ | " + getStatusText();
    }
}
