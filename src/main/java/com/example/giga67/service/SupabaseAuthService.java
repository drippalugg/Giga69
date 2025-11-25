package com.example.giga67.service;

import com.example.giga67.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.http.HttpResponse;

public class SupabaseAuthService {
    private final SupabaseClient client;
    private final Gson gson;
    private User currentUser;
    private String accessToken;

    public SupabaseAuthService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
    }

    public boolean login(String email, String password) {
        try {
            JsonObject credentials = new JsonObject();
            credentials.addProperty("email", email);
            credentials.addProperty("password", password);

            HttpResponse<String> response = client.post(
                    "/auth/v1/token?grant_type=password",
                    gson.toJson(credentials)
            );

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                accessToken = jsonResponse.get("access_token").getAsString();

                JsonObject userJson = jsonResponse.getAsJsonObject("user");
                currentUser = new User(
                        userJson.get("id").getAsString(),
                        userJson.get("email").getAsString(),
                        email.split("@")[0]
                );

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register(String email, String password, String name) {
        try {
            JsonObject credentials = new JsonObject();
            credentials.addProperty("email", email);
            credentials.addProperty("password", password);

            JsonObject metadata = new JsonObject();
            metadata.addProperty("name", name);
            credentials.add("data", metadata);

            HttpResponse<String> response = client.post(
                    "/auth/v1/signup",
                    gson.toJson(credentials)
            );

            if (response.statusCode() == 200) {
                return login(email, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void logout() {
        currentUser = null;
        accessToken = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

}
