package com.example.giga67.model;

public class User {
    private String id;
    private String email;
    private String name;

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    // 🔥 Добавьте этот метод
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', email='" + email + "', name='" + name + "'}";
    }
}
