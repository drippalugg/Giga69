package com.example.giga67.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final HttpClient httpClient;

    private SupabaseClient() {
        this.supabaseUrl = "https://uarcxsotrpdnwabpgjhp.supabase.co";
        this.supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVhcmN4c290cnBkbndhYnBnamhwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI4NjU5OTMsImV4cCI6MjA3ODQ0MTk5M30.nR2JZDVWD3wtdVYehE6ps6x35NClNBw1niNEA42qKGc";
        this.httpClient = HttpClient.newHttpClient();
        System.out.println("🔗 Supabase Client initialized");
        System.out.println("  URL: " + supabaseUrl);
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public HttpResponse<String> get(String endpoint) throws IOException, InterruptedException {
        return get(endpoint, null);
    }

    public HttpResponse<String> get(String endpoint, String userToken) throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + endpoint))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        System.out.println("🔍 GET " + endpoint);
        System.out.println("🔑 Using token: " + (userToken != null ? "USER" : "ANON"));

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // 🔥 POST с optional user token
    public HttpResponse<String> post(String endpoint, String jsonBody) throws IOException, InterruptedException {
        return post(endpoint, jsonBody, null);
    }

    public HttpResponse<String> post(String endpoint, String jsonBody, String userToken) throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + endpoint))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("📤 POST " + endpoint);
        System.out.println("🔑 Using token: " + (userToken != null ? "USER" : "ANON"));

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // 🔥 PATCH с optional user token
    public HttpResponse<String> patch(String endpoint, String jsonBody) throws IOException, InterruptedException {
        return patch(endpoint, jsonBody, null);
    }

    public HttpResponse<String> patch(String endpoint, String jsonBody, String userToken) throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + endpoint))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("🔄 PATCH " + endpoint);
        System.out.println("🔑 Using token: " + (userToken != null ? "USER" : "ANON"));

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // 🔥 DELETE с optional user token
    public HttpResponse<String> delete(String endpoint) throws IOException, InterruptedException {
        return delete(endpoint, null);
    }

    public HttpResponse<String> delete(String endpoint, String userToken) throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + endpoint))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        System.out.println("🗑️ DELETE " + endpoint);
        System.out.println("🔑 Using token: " + (userToken != null ? "USER" : "ANON"));

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
