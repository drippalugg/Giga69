package com.example.giga67.model;

public class StoreInventory {
    private String id;
    private String storeId;
    private String storeName;
    private int partId;
    private double price;
    private int stockQuantity;
    private boolean inStock;
    private String storeAddress = "";
    private String storeCity = "";
    private String storePhone = "";
    private String storeLogoUrl = "";
    private String partName = "";
    private String partArticle = "";
    private double partOriginalPrice = 0;

    public StoreInventory(String id, String storeId, String storeName, int partId, double price, int stockQuantity, boolean inStock) {
        this.id = id;
        this.storeId = storeId;
        this.storeName = storeName;
        this.partId = partId;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.inStock = inStock;
    }

    public String getId() { return id; }
    public String getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public int getPartId() { return partId; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isInStock() { return inStock; }
    public String getStoreAddress() { return storeAddress; }
    public String getStoreCity() { return storeCity; }
    public String getStorePhone() { return storePhone; }

    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public void setStoreCity(String storeCity) { this.storeCity = storeCity; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }
    public String getStoreLogoUrl() { return storeLogoUrl; }
    public void setStoreLogoUrl(String storeLogoUrl) { this.storeLogoUrl = storeLogoUrl; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getPartArticle() { return partArticle; }
    public void setPartArticle(String partArticle) { this.partArticle = partArticle; }
    public double getPartOriginalPrice() { return partOriginalPrice; }
    public void setPartOriginalPrice(double partOriginalPrice) { this.partOriginalPrice = partOriginalPrice; }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (storeCity != null && !storeCity.isEmpty()) sb.append(storeCity);
        if (storeAddress != null && !storeAddress.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(storeAddress);
        }
        return sb.isEmpty() ? "" : sb.toString();
    }

    public String getStockText() {
        if (inStock && stockQuantity > 0) {
            return "В наличии: " + stockQuantity + " шт.";
        } else if (stockQuantity == 0) {
            return "Нет в наличии";
        } else {
            return "Под заказ";
        }
    }

    @Override
    public String toString() {
        return "StoreInventory{store='" + storeName + "', price=" + price + ", stock=" + stockQuantity + "}";
    }
}
