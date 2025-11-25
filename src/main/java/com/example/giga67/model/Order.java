package com.example.giga67.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Order {
    private String id;  // 🔥 Изменено с int на String
    private String userId;
    private LocalDateTime createdAt;
    private String status;
    private double totalPrice;  // 🔥 Переименовано с totalAmount
    private List<CartItem> items;  // 🔥 Добавлена типизация

    // Пустой конструктор
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
    }

    // 🔥 НОВЫЙ конструктор с параметрами
    public Order(String id, String userId, List<CartItem> items, double totalPrice, LocalDateTime createdAt, String status) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = status;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    // 🔥 Дополнительные методы
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
            case "delivered":
                return "Доставлен";
            case "cancelled":
                return "Отменён";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Заказ #" + id + " | " + getFormattedDate() + " | " + totalPrice + "₽ | " + getStatusText();
    }
}
