package com.example.giga67.model;

public class Store {
    private String id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String logoUrl;

    public Store(String id, String name, String address, String city, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
    }

    public Store(String id, String name, String address, String city, String phone, String logoUrl) {
        this(id, name, address, city, phone);
        this.logoUrl = logoUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getFullAddress() {
        String result = "";
        if (city != null && !city.isEmpty()) result += city + ", ";
        if (address != null && !address.isEmpty()) result += address;
        return result.isEmpty() ? "Адрес не указан" : result;
    }

    @Override
    public String toString() {
        return "Store{id='" + id + "', name='" + name + "', city='" + city + "'}";
    }
}
