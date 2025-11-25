package com.example.giga67.model;

public class CartItem {
    private Part part;
    private int quantity;

    public CartItem(Part part, int quantity) {
        this.part = part;
        this.quantity = quantity;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        if (part == null) {
            return 0.0;
        }
        return ((double) part.getPrice()) * quantity;
    }

    public double getTotalOldPrice() {
        if (part == null) {
            return 0.0;
        }
        if (part.hasDiscount()) {
            return ((double) part.getOldPrice()) * quantity;
        }
        return getTotalPrice();
    }
    @Override
    public String toString() {
        if (part == null) {
            return "Неизвестный товар";
        }
        return String.format("%s × %d = %.2f ₽",
                part.getName(),
                quantity,
                getTotalPrice()
        );
    }
}
