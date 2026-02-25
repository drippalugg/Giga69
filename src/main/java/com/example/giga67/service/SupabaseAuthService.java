package com.example.giga67.service;

import com.example.giga67.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.http.HttpResponse;

public class SupabaseAuthService {
    private static SupabaseAuthService instance;

    private final SupabaseClient client;
    private final Gson gson;
    private User currentUser;
    private String accessToken;

    private SupabaseAuthService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
    }

    public static synchronized SupabaseAuthService getInstance() {
        if (instance == null) {
            instance = new SupabaseAuthService();
        }
        return instance;
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

            System.out.println("🔐 Login response status: " + response.statusCode());
            System.out.println("📩 Response body: " + response.body());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                accessToken = jsonResponse.get("access_token").getAsString();
                JsonObject userJson = jsonResponse.getAsJsonObject("user");

                String userId = userJson.get("id").getAsString();
                String userEmail = userJson.get("email").getAsString();

                // тянем профиль (name + role) из таблицы profiles
                User userWithProfile = fetchUserProfile(userId, userEmail);
                currentUser = userWithProfile;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private User fetchUserProfile(String userId, String fallbackEmail) {
        try {
            // /rest/v1/profiles?id=eq.<uuid>&select=*
            HttpResponse<String> response = client.get(
                    "/rest/v1/profiles?id=eq." + userId + "&select=*",
                    accessToken
            );

            if (response.statusCode() == 200) {
                JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                if (arr.size() > 0) {
                    JsonObject obj = arr.get(0).getAsJsonObject();
                    String email = obj.has("email") && !obj.get("email").isJsonNull()
                            ? obj.get("email").getAsString()
                            : fallbackEmail;
                    String name = obj.has("name") && !obj.get("name").isJsonNull()
                            ? obj.get("name").getAsString()
                            : email.split("@")[0];
                    String role = obj.has("role") && !obj.get("role").isJsonNull()
                            ? obj.get("role").getAsString()
                            : "user";

                    return new User(userId, email, name, role);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ fetchUserProfile error: " + e.getMessage());
        }

        // fallback: без профиля, роль user
        return new User(userId, fallbackEmail, fallbackEmail.split("@")[0], "user");
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

            System.out.println("📝 Register response status: " + response.statusCode());
            System.out.println("📩 Response body: " + response.body());

            if (response.statusCode() == 200) {
                // после регистрации Supabase сам создаёт запись в profiles (если настроен триггер)
                return login(email, password);
            }
        } catch (Exception e) {
            System.err.println("❌ Register error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    private void createProfileIfNotExists(String userId, String email, String name) {
        try {
            // проверяем, есть ли запись в profiles
            HttpResponse<String> check = client.get(
                    "/rest/v1/profiles?id=eq." + userId + "&select=id",
                    accessToken
            );
            if (check.statusCode() == 200 && !gson.fromJson(check.body(), JsonArray.class).isEmpty()) {
                return; // профиль уже есть
            }

            JsonObject profile = new JsonObject();
            profile.addProperty("id", userId);
            profile.addProperty("email", email);
            profile.addProperty("name", name);
            profile.addProperty("role", "user");

            HttpResponse<String> insert = client.post(
                    "/rest/v1/profiles",
                    gson.toJson(profile),
                    accessToken
            );
            System.out.println("📄 createProfile status: " + insert.statusCode());
        } catch (Exception e) {
            System.err.println("⚠️ createProfile error: " + e.getMessage());
        }
    }


    public void logout() {
        currentUser = null;
        accessToken = null;
        System.out.println("👋 Пользователь вышел");
    }

    public boolean isLoggedIn() {
        boolean loggedIn = currentUser != null;
        System.out.println("🔍 isLoggedIn проверка: " + loggedIn);
        return loggedIn;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
