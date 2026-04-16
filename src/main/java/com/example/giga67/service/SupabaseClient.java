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
        this.supabaseUrl = "https://mgklafqwfppkmcawjwuc.supabase.co";
        this.supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1na2xhZnF3ZnBwa21jYXdqd3VjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM0OTY4ODMsImV4cCI6MjA3OTA3Mjg4M30.ScQU-AIQvmmNZ77tExaWbfLyHYJV-dvvy3fZzbK_Bao";
        this.httpClient = HttpClient.newHttpClient();
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    // -------------------- Rest API --------------------

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

    // -------------------- Хранилище API --------------------
    public HttpResponse<String> uploadFile(String bucketName, String filePath, Path localFile)
            throws IOException, InterruptedException {
        return uploadFile(bucketName, filePath, localFile, null);
    }

    public HttpResponse<String> uploadFile(String bucketName, String filePath, Path localFile, String userToken)
            throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        String contentType = Files.probeContentType(localFile);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        byte[] fileBytes = Files.readAllBytes(localFile);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> updateFile(String bucketName, String filePath, Path localFile)
            throws IOException, InterruptedException {
        return updateFile(bucketName, filePath, localFile, null);
    }

    public HttpResponse<String> updateFile(String bucketName, String filePath, Path localFile, String userToken)
            throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        String contentType = Files.probeContentType(localFile);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        byte[] fileBytes = Files.readAllBytes(localFile);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> deleteFile(String bucketName, String filePath)
            throws IOException, InterruptedException {
        return deleteFile(bucketName, filePath, null);
    }

    public HttpResponse<String> deleteFile(String bucketName, String filePath, String userToken)
            throws IOException, InterruptedException {
        String authToken = (userToken != null && !userToken.isEmpty()) ? userToken : supabaseKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    //Возвращает публичный URL файла из хранилища БД
    public String getPublicUrl(String bucketName, String filePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
    }
}