package com.example.giga67.service;

import com.example.giga67.model.Category;
import com.example.giga67.model.Part;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PartsService {
    private final SupabaseClient client;
    private final Gson gson;
    private final ObservableList<Category> categories;
    private final ObservableList<Part> parts;

    public PartsService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
        this.categories = FXCollections.observableArrayList();
        this.parts = FXCollections.observableArrayList();
        loadDataFromSupabase();
    }

    private void loadDataFromSupabase() {
        try {

            // Загрузка категорий
            var categoriesResponse = client.get("/rest/v1/categories?select=*");
            if (categoriesResponse.statusCode() == 200) {
                JsonArray categoriesJson = gson.fromJson(categoriesResponse.body(), JsonArray.class);

                for (var element : categoriesJson) {
                    JsonObject catJson = element.getAsJsonObject();
                    Category category = new Category(
                            catJson.get("id").getAsInt(),
                            catJson.get("name").getAsString(),
                            catJson.get("icon").getAsString()
                    );
                    categories.add(category);
                }
            }

            // Загрузка товаров
            var partsResponse = client.get("/rest/v1/parts?select=*");
            if (partsResponse.statusCode() == 200) {
                JsonArray partsJson = gson.fromJson(partsResponse.body(), JsonArray.class);

                for (var element : partsJson) {
                    JsonObject partJson = element.getAsJsonObject();

                    // 🔥 ПОЛУЧАЕМ old_price (может быть null)
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

                    parts.add(part);
                }
                }

        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки из Supabase:");
            e.printStackTrace();
            System.err.println("⚠️ Используются пустые данные");
        }
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
        return parts.stream()
                .filter(part -> part.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public ObservableList<Part> getParts() {
        return parts;
    }
}
