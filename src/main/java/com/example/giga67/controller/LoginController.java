package com.example.giga67.controller;

import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nameField;

    @FXML
    private Label errorLabel;

    @FXML
    private VBox registerBox;

    private SupabaseAuthService authService;
    private boolean isRegisterMode = false;

    @FXML
    public void initialize() {
        authService = new SupabaseAuthService();

        if (registerBox != null) {
            registerBox.setVisible(false);
            registerBox.setManaged(false);
        }

        if (errorLabel != null) {
            errorLabel.setText("");
        }

        System.out.println("✅ LoginController initialized!");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Неверный формат email");
            return;
        }

        System.out.println("🔐 Попытка входа: " + email);

        boolean success = authService.login(email, password);

        if (success) {
            System.out.println("✅ Вход успешен!");
            SceneNavigator.goToMain();
        } else {
            showError("Неверный email или пароль");
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String name = nameField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Неверный формат email");
            return;
        }

        if (password.length() < 6) {
            showError("Пароль должен содержать минимум 6 символов");
            return;
        }

        System.out.println("📝 Попытка регистрации: " + email);

        boolean success = authService.register(email, password, name);

        if (success) {
            System.out.println("✅ Регистрация успешна!");
            SceneNavigator.goToMain();
        } else {
            showError("Ошибка регистрации. Email уже используется");
        }
    }

    @FXML
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;

        if (registerBox != null) {
            registerBox.setVisible(isRegisterMode);
            registerBox.setManaged(isRegisterMode);
        }

        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML
    private void guestContinue() {
        System.out.println("👤 Продолжить как гость");
        SceneNavigator.goToMain();
    }

    @FXML
    private void goBack() {
        System.out.println("← Go back clicked");
        SceneNavigator.goToMain();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
