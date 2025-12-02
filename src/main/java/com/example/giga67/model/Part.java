package com.example.giga67.model;

public class Part {
    private int id;
    private String name;
    private String article;
    private String brand;
    private double price;
    private double oldPrice;
    private int categoryId;
    private String description;
    private String imageUrl;
    private String specifications;

    // Конструктор с 7 параметрами
    public Part(int id, String name, String article, String brand, double price, double oldPrice, int categoryId) {
        this.id = id;
        this.name = name;
        this.article = article;
        this.brand = brand;
        this.price = price;
        this.oldPrice = oldPrice;
        this.categoryId = categoryId;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArticle() {
        return article;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public boolean hasDiscount() {
        return oldPrice > 0 && oldPrice > price;
    }

    public int getDiscountPercent() {
        if (!hasDiscount()) {
            return 0;
        }
        return (int) Math.round(((oldPrice - price) / oldPrice) * 100);
    }

    @Override
    public String toString() {
        return "Part{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", article='" + article + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", oldPrice=" + oldPrice +
                ", categoryId=" + categoryId +
                ", hasDiscount=" + hasDiscount() +
                ", imageUrl='" + imageUrl + '\'' +
                ", specifications='" + specifications + '\'' +
                '}';
    }
}
