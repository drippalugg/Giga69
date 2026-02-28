package com.example.giga67.controller;

import com.example.giga67.model.User;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    private SupabaseAuthService authService;

    @FXML
    public void initialize() {
        authService = SupabaseAuthService.getInstance();
        loadUserData();
        System.out.println("ProfileController initialized!");
    }

    private void loadUserData() {
        User user = authService.getCurrentUser();

        if (user != null) {
            if (nameField != null) {
                nameField.setText(user.getName());
            }
            if (emailField != null) {
                emailField.setText(user.getEmail());
            }
            System.out.println("Загружены данные пользователя: " + user.getName());
        } else {
            if (nameField != null) {
                nameField.setText("Гость");
                nameField.setEditable(false);
            }
            if (emailField != null) {
                emailField.setText("Не авторизован");
            }
        }
    }

    @FXML
    private void handleSave() {
        User user = authService.getCurrentUser();

        if (user == null) {
            showMessage("Вы не авторизованы", false);
            return;
        }

        String newName = nameField.getText().trim();

        if (newName.isEmpty()) {
            showMessage("Имя не может быть пустым", false);
            return;
        }

        boolean ok = authService.updateProfileName(newName);
        if (ok) {
            user.setName(newName);
            showMessage("Изменения сохранены успешно!", true);
        } else {
            showMessage("Ошибка сохранения имени в профиле", false);
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        SceneNavigator.goToLogin();
    }

    @FXML
    private void goBack() {
        SceneNavigator.goToMain();
    }

    private void showMessage(String message, boolean isSuccess) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setVisible(true);

            if (isSuccess) {
                messageLabel.setStyle(
                        "-fx-text-fill: #66BB6A; " +
                                "-fx-font-size: 14px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: rgba(102, 187, 106, 0.1); " +
                                "-fx-background-radius: 8; " +
                                "-fx-padding: 10;"
                );
            } else {
                messageLabel.setStyle(
                        "-fx-text-fill: #FF4757; " +
                                "-fx-font-size: 14px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: rgba(255, 71, 87, 0.1); " +
                                "-fx-background-radius: 8; " +
                                "-fx-padding: 10;"
                );
            }
        }
    }
}
