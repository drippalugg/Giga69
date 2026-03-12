package com.example.giga67.service;

import com.example.giga67.model.Order;
import com.example.giga67.model.CartItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrdersService {
    private static OrdersService instance;
    private final SupabaseClient client;
    private final Gson gson;

    public OrdersService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
    }

    public static synchronized OrdersService getInstance() {
        if (instance == null) {
            instance = new OrdersService();
        }
        return instance;
    }

    public boolean createOrder(Order order, String userId, String accessToken) {
        try {
            JsonObject orderData = new JsonObject();
            orderData.addProperty("user_id", userId);
            orderData.addProperty("order_number", order.getId());
            orderData.addProperty("total_price", order.getTotalPrice());
            orderData.addProperty("status", order.getStatus());
            JsonArray itemsArray = gson.toJsonTree(order.getItems()).getAsJsonArray();
            orderData.add("items", itemsArray);
            String jsonBody = gson.toJson(orderData);
            HttpResponse<String> response = client.post(
                    "/rest/v1/orders",
                    jsonBody,
                    accessToken
            );

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return true;
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Order> getUserOrders(String userId, String accessToken) {
        List<Order> orders = new ArrayList<>();

        try {
            HttpResponse<String> response = client.get(
                    "/rest/v1/orders?user_id=eq." + userId + "&order=created_at.desc",
                    accessToken
            );

            if (response.statusCode() == 200) {
                JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject orderJson = jsonArray.get(i).getAsJsonObject();

                    String orderId = orderJson.get("order_number").getAsString();
                    double totalPrice = orderJson.get("total_price").getAsDouble();
                    String status = orderJson.get("status").getAsString();

                    // Работающий парсинг с timezone
                    LocalDateTime createdAt = LocalDateTime.now();
                    if (orderJson.has("created_at") && !orderJson.get("created_at").isJsonNull()) {
                        String createdAtStr = orderJson.get("created_at").getAsString();
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(createdAtStr);
                            createdAt = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                        }
                    }

                    JsonArray itemsArray = orderJson.getAsJsonArray("items");
                    List<CartItem> items = gson.fromJson(itemsArray, new TypeToken<List<CartItem>>() {
                    }.getType());

                    Order order = new Order(
                            orderId,
                            userId,
                            items,
                            totalPrice,
                            createdAt,
                            status
                    );

                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public ObservableList<Order> getAllOrders(String accessToken) {
        ObservableList<Order> orders = FXCollections.observableArrayList();

        try {
            String endpoint = "/rest/v1/orders?select=*";
            HttpResponse<String> response = client.get(endpoint, accessToken);

            if (response.statusCode() == 200) {
                JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject o = jsonArray.get(i).getAsJsonObject();

                    String id = o.get("id").getAsString();                    // uuid
                    String userId = o.get("user_id").getAsString();           // uuid

                    double total = o.has("total_price") && !o.get("total_price").isJsonNull()
                            ? o.get("total_price").getAsDouble()
                            : 0.0;

                    String status = o.has("status") && !o.get("status").isJsonNull()
                            ? o.get("status").getAsString()
                            : "pending";

                    LocalDateTime createdAt = LocalDateTime.now();
                    if (o.has("created_at") && !o.get("created_at").isJsonNull()) {
                        String createdStr = o.get("created_at").getAsString();
                        createdStr = createdStr.replace("Z", "");
                        int plusIdx = createdStr.indexOf('+');
                        if (plusIdx > 0) createdStr = createdStr.substring(0, plusIdx);
                        createdAt = LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }

                    Order order = new Order(
                            id,
                            userId,
                            null,
                            total,
                            createdAt,
                            status
                    );

                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public boolean updateOrderStatus(String orderId, String newStatus, String accessToken) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("status", newStatus);        // статус text

            String endpoint = "/rest/v1/orders?id=eq." + orderId; // фильтр по uuid id

            HttpResponse<String> resp = client.patch(
                    endpoint,
                    gson.toJson(body),
                    accessToken
            );

            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
