package com.example.giga67.model;

public class User {
    private String id;
    private String email;
    private String name;
    private String role;

    public User(String id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
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

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', email='" + email +
                "', name='" + name + "', role='" + role + "'}";
    }
}
