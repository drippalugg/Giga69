package com.example.giga67.util;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageCache {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ImageCache() {}

    public static Image get(String url) {
        if (url == null || url.isEmpty()) return null;
        return CACHE.computeIfAbsent(url, ImageCache::loadTracked);
    }

    public static Image get(String url, double requestedWidth, double requestedHeight,
                            boolean preserveRatio, boolean smooth) {
        if (url == null || url.isEmpty()) return null;
        String key = url + "|" + requestedWidth + "x" + requestedHeight
                + "|" + preserveRatio + "|" + smooth;
        return CACHE.computeIfAbsent(key, k -> loadTracked(url, requestedWidth, requestedHeight,
                preserveRatio, smooth));
    }

    private static Image loadTracked(String url) {
        Image img = new Image(url, true);
        attachErrorHandler(img, url);
        return img;
    }

    private static Image loadTracked(String url, double w, double h, boolean preserveRatio, boolean smooth) {
        Image img = new Image(url, w, h, preserveRatio, smooth, true);
        attachErrorHandler(img, url);
        return img;
    }

    private static void attachErrorHandler(Image img, String url) {
        img.errorProperty().addListener((obs, was, isErr) -> {
            if (Boolean.TRUE.equals(isErr)) {
                Throwable ex = img.getException();
                System.err.println("[ImageCache] Failed to load " + url
                        + (ex != null ? " — " + ex.getClass().getSimpleName() + ": " + ex.getMessage() : ""));
                CACHE.values().remove(img);
            }
        });
    }
}
