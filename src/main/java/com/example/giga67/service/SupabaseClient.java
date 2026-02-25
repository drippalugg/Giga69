package com.example.giga67.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final HttpClient httpClient;

    private SupabaseClient() {
        this.supabaseUrl = "https://uarcxsotrpdnwabpgjhp.supabase.co";
        this.supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVhcmN4c290cnBkbndhYnBnamhwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI4NjU5OTMsImV4cCI6MjA3ODQ0MTk5M30.nR2JZDVWD3wtdVYehE6ps6x35NClNBw1niNEA42qKGc";
        this.httpClient = HttpClient.newHttpClient();
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    // ==================== REST API ====================

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

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

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

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

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

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

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

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ==================== STORAGE API ====================

    /**
     * Загружает файл в Supabase Storage.
     * @param bucketName имя бакета (например, "product-images")
     * @param filePath путь к файлу в бакете (например, "parts/123.jpg")
     * @param localFile путь к локальному файлу на диске
     * @return HttpResponse с результатом загрузки
     */
    public HttpResponse<String> uploadFile(String bucketName, String filePath, Path localFile)
            throws IOException, InterruptedException {

        String contentType = Files.probeContentType(localFile);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        byte[] fileBytes = Files.readAllBytes(localFile);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Обновляет (перезаписывает) файл в Supabase Storage.
     */
    public HttpResponse<String> updateFile(String bucketName, String filePath, Path localFile)
            throws IOException, InterruptedException {

        String contentType = Files.probeContentType(localFile);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        byte[] fileBytes = Files.readAllBytes(localFile);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Удаляет файл из Supabase Storage.
     */
    public HttpResponse<String> deleteFile(String bucketName, String filePath)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .DELETE()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Возвращает публичный URL файла из Supabase Storage.
     */
    public String getPublicUrl(String bucketName, String filePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
    }
}