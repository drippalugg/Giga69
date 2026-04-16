package com.example.giga67.service;

import com.example.giga67.model.Review;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {
    private static ReviewService instance;
    private final SupabaseClient client;
    private final Gson gson;

    private ReviewService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
    }

    public static synchronized ReviewService getInstance() {
        if (instance == null) {
            instance = new ReviewService();
        }
        return instance;
    }

    public List<Review> getReviewsForPart(int partId) {
        List<Review> reviews = new ArrayList<>();
        try {
            HttpResponse<String> response = client.get(
                "/rest/v1/reviews?select=*,profiles(name)&part_id=eq." + partId + "&order=created_at.desc"
            );
            if (response.statusCode() == 200) {
                JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                for (var element : arr) {
                    reviews.add(parseReview(element.getAsJsonObject()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public boolean addReview(int partId, String userId, int rating, String comment, String token) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("part_id", partId);
            body.addProperty("user_id", userId);
            body.addProperty("rating", rating);
            if (comment != null && !comment.isBlank()) {
                body.addProperty("comment", comment);
            }
            HttpResponse<String> response = client.post("/rest/v1/reviews", gson.toJson(body), token);
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateReview(String reviewId, int rating, String comment, String token) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("rating", rating);
            body.addProperty("comment", comment != null ? comment : "");
            body.addProperty("is_edited", true);
            HttpResponse<String> response = client.patch(
                "/rest/v1/reviews?id=eq." + reviewId, gson.toJson(body), token
            );
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteReview(String reviewId, String token) {
        try {
            HttpResponse<String> response = client.delete(
                "/rest/v1/reviews?id=eq." + reviewId, token
            );
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Review parseReview(JsonObject obj) {
        String id = obj.get("id").getAsString();
        int partId = obj.get("part_id").getAsInt();
        String userId = obj.get("user_id").getAsString();
        int rating = obj.get("rating").getAsInt();
        String comment = obj.has("comment") && !obj.get("comment").isJsonNull()
                ? obj.get("comment").getAsString() : "";
        boolean isEdited = obj.has("is_edited") && !obj.get("is_edited").isJsonNull()
                && obj.get("is_edited").getAsBoolean();

        ZonedDateTime createdAt = null;
        if (obj.has("created_at") && !obj.get("created_at").isJsonNull()) {
            try { createdAt = ZonedDateTime.parse(obj.get("created_at").getAsString()); }
            catch (Exception ignored) {}
        }
        ZonedDateTime updatedAt = null;
        if (obj.has("updated_at") && !obj.get("updated_at").isJsonNull()) {
            try { updatedAt = ZonedDateTime.parse(obj.get("updated_at").getAsString()); }
            catch (Exception ignored) {}
        }

        String userName = "Пользователь";
        if (obj.has("profiles") && !obj.get("profiles").isJsonNull()) {
            JsonObject profileObj = obj.getAsJsonObject("profiles");
            if (profileObj.has("name") && !profileObj.get("name").isJsonNull()) {
                userName = profileObj.get("name").getAsString();
            }
        }

        return new Review(id, partId, userId, userName, rating, comment, createdAt, updatedAt, isEdited);
    }
}
