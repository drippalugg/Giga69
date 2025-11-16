package com.example.giga67.controller;

import com.example.giga67.model.User;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    private SupabaseAuthService authService;

    @FXML
    public void initialize() {
        authService = new SupabaseAuthService();
        loadUserData();

        System.out.println("✅ ProfileController initialized!");
    }

    private void loadUserData() {
        User user = authService.getCurrentUser();
        if (user != null) {
            if (nameLabel != null) {
                nameLabel.setText(user.getName());
            }
            if (emailLabel != null) {
                emailLabel.setText(user.getEmail());
            }
        } else {
            if (nameLabel != null) {
                nameLabel.setText("Гость");
            }
            if (emailLabel != null) {
                emailLabel.setText("Не авторизован");
            }
        }
    }

    @FXML
    private void logout() {
        System.out.println("👋 Выход из системы");
        authService.logout();
        SceneNavigator.goToLogin();
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат на главную");
        SceneNavigator.goToMain();
    }
}
