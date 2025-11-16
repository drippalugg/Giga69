package com.example.giga67.service;

import com.example.giga67.model.CartItem;
import com.example.giga67.model.Part;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartManager {
    private static CartManager instance;
    private final ObservableList<CartItem> cartItems;
    private final ObservableList<Part> favorites;

    private CartManager() {
        this.cartItems = FXCollections.observableArrayList();
        this.favorites = FXCollections.observableArrayList();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Part part, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getPart().getId() == part.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(part, quantity));
    }

    public void removeFromCart(Part part) {
        cartItems.removeIf(item -> item.getPart().getId() == part.getId());
    }

    public void updateQuantity(Part part, int quantity) {
        if (quantity <= 0) {
            removeFromCart(part);
            return;
        }

        for (CartItem item : cartItems) {
            if (item.getPart().getId() == part.getId()) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    public double getTotalPrice() {
        return cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    public int getTotalItems() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    // Избранное
    public void addToFavorites(Part part) {
        if (!favorites.contains(part)) {
            favorites.add(part);
        }
    }

    public void removeFromFavorites(Part part) {
        favorites.remove(part);
    }

    public boolean isFavorite(Part part) {
        return favorites.contains(part);
    }

    public ObservableList<Part> getFavorites() {
        return favorites;
    }
}
