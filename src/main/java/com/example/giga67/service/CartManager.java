package com.example.giga67.service;

import com.example.giga67.model.CartItem;
import com.example.giga67.model.Part;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private ObservableList<CartItem> cartItems;
    private ObservableList<Part> favorites;
    private SupabaseClient client;
    private Gson gson;

    private CartManager() {
        this.cartItems = FXCollections.observableArrayList();
        this.favorites = FXCollections.observableArrayList();
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
        System.out.println("🛒 CartManager initialized");
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // 🔥 НОВЫЙ МЕТОД - загрузка корзины из Supabase
    public void loadCartFromServer(String userId, String accessToken) {
        try {
            System.out.println("📥 Загрузка корзины из Supabase...");

            HttpResponse<String> response = client.get(
                    "/rest/v1/cart?user_id=eq." + userId,
                    accessToken
            );

            if (response.statusCode() == 200) {
                cartItems.clear();
                JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);

                // Здесь нужно загрузить полные данные о товарах из таблицы parts
                // Пока просто логируем
                System.out.println("✅ Загружено элементов корзины: " + jsonArray.size());
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки корзины: " + e.getMessage());
        }
    }

    // 🔥 НОВЫЙ МЕТОД - загрузка избранного из Supabase
    public void loadFavoritesFromServer(String userId, String accessToken) {
        try {
            System.out.println("📥 Загрузка избранного из Supabase...");

            HttpResponse<String> response = client.get(
                    "/rest/v1/favorites?user_id=eq." + userId,
                    accessToken
            );

            if (response.statusCode() == 200) {
                favorites.clear();
                JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);

                System.out.println("✅ Загружено избранных товаров: " + jsonArray.size());
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки избранного: " + e.getMessage());
        }
    }

    // ============ МЕТОДЫ КОРЗИНЫ ============

    public void addToCart(Part part, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getPart().getId() == part.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                System.out.println("🛒 Обновлено количество: " + part.getName() + " x" + item.getQuantity());

                // 🔥 Синхронизация с Supabase
                syncCartToServer();
                return;
            }
        }
        cartItems.add(new CartItem(part, quantity));
        System.out.println("🛒 Добавлено в корзину: " + part.getName() + " x" + quantity);

        // 🔥 Синхронизация с Supabase
        syncCartToServer();
    }

    public void removeFromCart(Part part) {
        for (int i = cartItems.size() - 1; i >= 0; i--) {
            CartItem item = cartItems.get(i);
            if (item.getPart().getId() == part.getId()) {
                cartItems.remove(i);
                System.out.println("🗑️ Удалено из корзины: " + part.getName());

                // 🔥 Синхронизация с Supabase
                syncCartToServer();
                break;
            }
        }
    }

    public void removeItem(int partId) {
        for (int i = cartItems.size() - 1; i >= 0; i--) {
            CartItem item = cartItems.get(i);
            if (item.getPart().getId() == partId) {
                cartItems.remove(i);
                System.out.println("🗑️ Удалено из корзины товар с ID: " + partId);

                // 🔥 Синхронизация с Supabase
                syncCartToServer();
                break;
            }
        }
    }

    public void updateQuantity(Part part, int quantity) {
        if (quantity <= 0) {
            removeFromCart(part);
            return;
        }

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getPart().getId() == part.getId()) {
                item.setQuantity(quantity);
                System.out.println("🔄 Обновлено количество: " + part.getName() + " -> " + quantity);

                //  Синхронизация с Supabase
                syncCartToServer();
                return;
            }
        }
    }

    public void clear() {
        clearCart();
    }

    public void clearCart() {
        cartItems.clear();
        System.out.println("🗑️ Корзина очищена");

        //  Синхронизация с Supabase
        syncCartToServer();
    }

    //  НОВЫЙ МЕТОД - синхронизация корзины с Supabase
    private void syncCartToServer() {
        SupabaseAuthService authService = SupabaseAuthService.getInstance();
        if (!authService.isLoggedIn()) {
            System.out.println("⚠️ Пользователь не залогинен, пропускаем синхронизацию корзины");
            return;
        }

        try {
            String userId = authService.getCurrentUser().getId();
            String accessToken = authService.getAccessToken();

            // Удаляем всю корзину пользователя
            client.delete("/rest/v1/cart?user_id=eq." + userId, accessToken);

            // Добавляем текущие товары
            for (CartItem item : cartItems) {
                JsonObject cartData = new JsonObject();
                cartData.addProperty("user_id", userId);
                cartData.addProperty("part_id", item.getPart().getId());
                cartData.addProperty("quantity", item.getQuantity());

                client.post("/rest/v1/cart", gson.toJson(cartData), accessToken);
            }


        } catch (Exception e) {
            System.err.println("❌ Ошибка синхронизации корзины: " + e.getMessage());
        }
    }

    public double getTotal() {
        return getTotalPrice();
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            Part part = item.getPart();
            double price = (double) part.getPrice();
            int qty = item.getQuantity();
            total += (price * qty);
        }
        return total;
    }

    public int getTotalItems() {
        int total = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            total += item.getQuantity();
        }
        return total;
    }

    public List<CartItem> getItems() {
        List<CartItem> list = new ArrayList<>();
        for (int i = 0; i < cartItems.size(); i++) {
            list.add(cartItems.get(i));
        }
        return list;
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    // ============ МЕТОДЫ ИЗБРАННОГО ============

    public void addToFavorites(Part part) {
        if (!favorites.contains(part)) {
            favorites.add(part);
            System.out.println("💖 Добавлено в избранное: " + part.getName());

            // Синхронизация с Supabase
            syncFavoritesToServer();
        }
    }

    public void removeFromFavorites(Part part) {
        favorites.remove(part);
        System.out.println("💔 Удалено из избранного: " + part.getName());

        // 🔥 Синхронизация с Supabase
        syncFavoritesToServer();
    }

    // 🔥 НОВЫЙ МЕТОД - синхронизация избранного с Supabase
    private void syncFavoritesToServer() {
        SupabaseAuthService authService = SupabaseAuthService.getInstance();
        if (!authService.isLoggedIn()) {
            System.out.println("⚠️ Пользователь не залогинен, пропускаем синхронизацию избранного");
            return;
        }

        try {
            String userId = authService.getCurrentUser().getId();
            String accessToken = authService.getAccessToken();

            // Удаляем всё избранное пользователя
            client.delete("/rest/v1/favorites?user_id=eq." + userId, accessToken);

            // Добавляем текущие товары
            for (Part part : favorites) {
                JsonObject favData = new JsonObject();
                favData.addProperty("user_id", userId);
                favData.addProperty("part_id", part.getId());

                client.post("/rest/v1/favorites", gson.toJson(favData), accessToken);
            }

            System.out.println("✅ Избранное синхронизировано с сервером");
        } catch (Exception e) {
            System.err.println("❌ Ошибка синхронизации избранного: " + e.getMessage());
        }
    }

    public boolean isFavorite(Part part) {
        return favorites.contains(part);
    }

    public ObservableList<Part> getFavorites() {
        return favorites;
    }
}
