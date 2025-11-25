package com.example.giga67.model;

import java.util.Map;

public class Part {
    private int id;
    private String name;
    private int categoryId;
    private double price;
    private Double oldPrice;
    private String article;
    private String brand;
    private String description;
    private String image;
    private String imageUrl;
    private Map<String, String> specifications;

    public Part(int id, String name, int categoryId, double price, String article, String brand) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.article = article;
        this.brand = brand;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public Object getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Double getOldPrice() { return oldPrice; }
    public void setOldPrice(Double oldPrice) { this.oldPrice = oldPrice; }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }

    public boolean hasDiscount() {
        return oldPrice != null && oldPrice > price;
    }

    public int getDiscountPercent() {
        if (!hasDiscount()) return 0;
        return (int) Math.round(((oldPrice - price) / oldPrice) * 100);
    }
}
