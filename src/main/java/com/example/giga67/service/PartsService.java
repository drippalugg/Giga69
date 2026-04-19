package com.example.giga67.service;

import com.example.giga67.model.Category;
import com.example.giga67.model.Part;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PartsService {
    private static volatile PartsService instance;

    private final SupabaseClient client;
    private final Gson gson;
    private final ObservableList<Category> categories;
    private final ObservableList<Part> parts;
    private final Map<Integer, Part> partsById;
    private final CompletableFuture<Void> readyFuture;

    private PartsService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
        this.categories = FXCollections.observableArrayList();
        this.parts = FXCollections.observableArrayList();
        this.partsById = new ConcurrentHashMap<>();
        this.readyFuture = new CompletableFuture<>();
        startBackgroundLoad();
    }

    public static PartsService getInstance() {
        if (instance == null) {
            synchronized (PartsService.class) {
                if (instance == null) {
                    instance = new PartsService();
                }
            }
        }
        return instance;
    }

    private void startBackgroundLoad() {
        Thread loader = new Thread(this::loadDataFromSupabase, "PartsService-Loader");
        loader.setDaemon(true);
        loader.start();
    }

    public void reload() {
        Thread loader = new Thread(this::loadDataFromSupabase, "PartsService-Reloader");
        loader.setDaemon(true);
        loader.start();
    }

    private void loadDataFromSupabase() {
        try {
            List<Category> loadedCategories = new ArrayList<>();
            List<Part> loadedParts = new ArrayList<>();
            Map<Integer, Part> loadedPartsById = new HashMap<>();

            var categoriesResponse = client.get("/rest/v1/categories?select=*");
            if (categoriesResponse.statusCode() == 200) {
                JsonArray categoriesJson = gson.fromJson(categoriesResponse.body(), JsonArray.class);
                for (var element : categoriesJson) {
                    JsonObject catJson = element.getAsJsonObject();
                    loadedCategories.add(new Category(
                            catJson.get("id").getAsInt(),
                            catJson.get("name").getAsString(),
                            catJson.get("icon").getAsString()
                    ));
                }
            }

            var partsResponse = client.get("/rest/v1/parts?select=*");
            if (partsResponse.statusCode() == 200) {
                JsonArray partsJson = gson.fromJson(partsResponse.body(), JsonArray.class);
                for (var element : partsJson) {
                    JsonObject partJson = element.getAsJsonObject();
                    double oldPrice = 0.0;
                    if (partJson.has("old_price") && !partJson.get("old_price").isJsonNull()) {
                        oldPrice = partJson.get("old_price").getAsDouble();
                    }

                    Part part = new Part(
                            partJson.get("id").getAsInt(),
                            partJson.get("name").getAsString(),
                            partJson.get("article").getAsString(),
                            partJson.get("brand").getAsString(),
                            partJson.get("price").getAsDouble(),
                            oldPrice,
                            partJson.get("category_id").getAsInt()
                    );

                    if (partJson.has("description") && !partJson.get("description").isJsonNull()) {
                        part.setDescription(partJson.get("description").getAsString());
                    }
                    if (partJson.has("image_url") && !partJson.get("image_url").isJsonNull()) {
                        part.setImageUrl(partJson.get("image_url").getAsString());
                    }
                    if (partJson.has("rating_avg") && !partJson.get("rating_avg").isJsonNull()) {
                        part.setRatingAvg(partJson.get("rating_avg").getAsDouble());
                    }
                    if (partJson.has("reviews_count") && !partJson.get("reviews_count").isJsonNull()) {
                        part.setReviewsCount(partJson.get("reviews_count").getAsInt());
                    }

                    loadedParts.add(part);
                    loadedPartsById.put(part.getId(), part);
                }
            }

            Platform.runLater(() -> {
                categories.setAll(loadedCategories);
                parts.setAll(loadedParts);
                partsById.clear();
                partsById.putAll(loadedPartsById);
                if (!readyFuture.isDone()) {
                    readyFuture.complete(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (!readyFuture.isDone()) {
                    readyFuture.completeExceptionally(e);
                }
            });
        }
    }

    public boolean isReady() {
        return readyFuture.isDone() && !readyFuture.isCompletedExceptionally();
    }

    public void onReady(Runnable action) {
        if (isReady()) {
            if (Platform.isFxApplicationThread()) {
                action.run();
            } else {
                Platform.runLater(action);
            }
            return;
        }
        readyFuture.whenComplete((v, err) -> {
            if (err == null) Platform.runLater(action);
        });
    }

    public ObservableList<Part> getPartsByCategory(int categoryId) {
        return parts.filtered(part -> part.getCategoryId() == categoryId);
    }

    public ObservableList<Part> searchParts(String query) {
        String lowerQuery = query.toLowerCase();
        return parts.filtered(part ->
                part.getName().toLowerCase().contains(lowerQuery) ||
                        part.getArticle().toLowerCase().contains(lowerQuery) ||
                        part.getBrand().toLowerCase().contains(lowerQuery)
        );
    }

    public Part getPartById(int id) {
        return partsById.get(id);
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }
}
