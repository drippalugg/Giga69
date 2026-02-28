package com.example.giga67.service;

import com.example.giga67.model.CartItem;
import com.example.giga67.model.Part;
import com.example.giga67.model.User;
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
    private SupabaseAuthService authService;
    private PartsService partsService;
    private Gson gson;

    private CartManager() {
        this.client = SupabaseClient.getInstance();
        this.authService = SupabaseAuthService.getInstance();
        this.partsService = new PartsService();
        this.gson = new Gson();
        this.cartItems = FXCollections.observableArrayList();
        this.favorites = FXCollections.observableArrayList();

        // 🔥 НЕ ЗАГРУЖАЕМ ЗДЕСЬ! Загрузим после входа
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // ==================== ЗАГРУЗКА ИЗ SUPABASE ====================

    public void loadData() {
        User user = authService.getCurrentUser();

        if (user == null) {
            System.out.println("⚠️ Пользователь не авторизован, очищаем данные");
            cartItems.clear();
            favorites.clear();
            return;
        }

        System.out.println("📡 Загрузка данных для пользователя: " + user.getEmail());
        loadCartFromSupabase(user.getId());
        loadFavoritesFromSupabase(user.getId());
    }

    private void loadCartFromSupabase(String userId) {
        try {
            String endpoint = "/rest/v1/cart?user_id=eq." + userId + "&select=*";
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.get(endpoint, token);

            if (response.statusCode() == 200) {
                JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);
                cartItems.clear();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject item = jsonArray.get(i).getAsJsonObject();
                    int partId = item.get("part_id").getAsInt();
                    int quantity = item.get("quantity").getAsInt();

                    Part part = partsService.getPartById(partId);
                    if (part != null) {
                        cartItems.add(new CartItem(part, quantity));
                    }
                }

                System.out.println("✅ Корзина загружена из Supabase (" + cartItems.size() + " товаров)");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки корзины: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFavoritesFromSupabase(String userId) {
        try {
            String endpoint = "/rest/v1/favorites?user_id=eq." + userId + "&select=*";
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.get(endpoint, token);

            if (response.statusCode() == 200) {
                JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);
                favorites.clear();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject item = jsonArray.get(i).getAsJsonObject();
                    int partId = item.get("part_id").getAsInt();

                    Part part = partsService.getPartById(partId);
                    if (part != null) {
                        favorites.add(part);
                    }
                }

                System.out.println("✅ Избранное загружено из Supabase (" + favorites.size() + " товаров)");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки избранного: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== КОРЗИНА ====================

    public void addToCart(Part part, int quantity) {
        User user = authService.getCurrentUser();

        if (user == null) {
            System.out.println("⚠️ Войдите, чтобы добавить в корзину");
            return;
        }

        // Обновляем локально
        for (CartItem item : cartItems) {
            if (item.getPart().getId() == part.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                updateCartInSupabase(user.getId(), part.getId(), item.getQuantity());
                System.out.println("🛒 Обновлено количество: " + part.getName() + " → " + item.getQuantity());
                return;
            }
        }

        // Добавляем новый товар
        cartItems.add(new CartItem(part, quantity));
        addCartToSupabase(user.getId(), part.getId(), quantity);
        System.out.println("🛒 Товар добавлен в корзину: " + part.getName() + " x" + quantity);
    }

    public void removeItem(int partId) {
        User user = authService.getCurrentUser();
        if (user == null) return;

        cartItems.removeIf(item -> item.getPart().getId() == partId);
        deleteCartFromSupabase(user.getId(), partId);
        System.out.println("🗑️ Товар удалён из корзины (ID: " + partId + ")");
    }

    public void updateQuantity(Part part, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(part.getId());
            return;
        }

        User user = authService.getCurrentUser();
        if (user == null) return;

        for (CartItem item : cartItems) {
            if (item.getPart().getId() == part.getId()) {
                item.setQuantity(newQuantity);
                updateCartInSupabase(user.getId(), part.getId(), newQuantity);
                System.out.println("🔄 Количество обновлено: " + part.getName() + " → " + newQuantity);
                return;
            }
        }
    }

    public void clear() {
        User user = authService.getCurrentUser();
        if (user == null) return;

        cartItems.clear();
        clearCartInSupabase(user.getId());
        System.out.println("🗑️ Корзина очищена");
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(cartItems);
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotal() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getPart().getPrice() * item.getQuantity();
        }
        return total;
    }

    public int getTotalItems() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    // ==================== ИЗБРАННОЕ ====================

    public void addToFavorites(Part part) {
        User user = authService.getCurrentUser();

        if (user == null) {
            System.out.println("⚠️ Войдите, чтобы добавить в избранное");
            return;
        }

        if (!isFavorite(part)) {
            favorites.add(part);
            addFavoriteToSupabase(user.getId(), part.getId());
            System.out.println("💖 Товар добавлен в избранное: " + part.getName());
        }
    }

    public void removeFromFavorites(Part part) {
        User user = authService.getCurrentUser();
        if (user == null) return;

        favorites.removeIf(p -> p.getId() == part.getId());
        deleteFavoriteFromSupabase(user.getId(), part.getId());
        System.out.println("💔 Товар удалён из избранного: " + part.getName());
    }

    public boolean isFavorite(Part part) {
        return favorites.stream().anyMatch(p -> p.getId() == part.getId());
    }

    public ObservableList<Part> getFavorites() {
        return favorites;
    }

// ==================== SUPABASE ОПЕРАЦИИ ====================

    private void addCartToSupabase(String userId, int partId, int quantity) {
        try {
            String json = String.format(
                    "{\"user_id\":\"%s\",\"part_id\":%d,\"quantity\":%d}",
                    userId, partId, quantity
            );
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.post("/rest/v1/cart", json, token);
            System.out.println("💾 Товар добавлен в корзину (Supabase) - статус: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления в корзину: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCartInSupabase(String userId, int partId, int quantity) {
        try {
            String json = String.format("{\"quantity\":%d}", quantity);
            String endpoint = String.format(
                    "/rest/v1/cart?user_id=eq.%s&part_id=eq.%d",
                    userId, partId
            );
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.patch(endpoint, json, token);
            System.out.println("💾 Количество обновлено (Supabase) - статус: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления корзины: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteCartFromSupabase(String userId, int partId) {
        try {
            String endpoint = String.format(
                    "/rest/v1/cart?user_id=eq.%s&part_id=eq.%d",
                    userId, partId
            );
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.delete(endpoint, token);
            System.out.println("🗑️ Товар удалён из корзины (Supabase) - статус: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("❌ Ошибка удаления из корзины: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearCartInSupabase(String userId) {
        try {
            String endpoint = "/rest/v1/cart?user_id=eq." + userId;
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.delete(endpoint, token);
            System.out.println("🗑️ Корзина очищена (Supabase) - статус: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("❌ Ошибка очистки корзины: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addFavoriteToSupabase(String userId, int partId) {
        try {
            String json = String.format(
                    "{\"user_id\":\"%s\",\"part_id\":%d}",
                    userId, partId
            );
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.post("/rest/v1/favorites", json, token);
            System.out.println("💖 Товар добавлен в избранное (Supabase) - статус: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления в избранное: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteFavoriteFromSupabase(String userId, int partId) {
        try {
            String endpoint = String.format(
                    "/rest/v1/favorites?user_id=eq.%s&part_id=eq.%d",
                    userId, partId
            );
            String token = authService.getAccessToken();
            HttpResponse<String> response = client.delete(endpoint, token);
            System.out.println("💔 Товар удалён из избранного (Supabase) - статус: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("❌ Ошибка удаления из избранного: " + e.getMessage());
            e.printStackTrace();
        }
    }
}