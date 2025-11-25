package com.example.giga67.controller;

import com.example.giga67.service.CartManager;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private Label errorLabel;
    @FXML private VBox registerBox;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label toggleLabel;

    private SupabaseAuthService authService;
    private boolean isRegisterMode = false;

    @FXML
    public void initialize() {
        authService = SupabaseAuthService.getInstance();

        if (registerBox != null) {
            registerBox.setVisible(false);
            registerBox.setManaged(false);
        }

        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }

        System.out.println("✅ LoginController initialized!");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("⚠️ Заполните все поля");
            return;
        }

        if (!isValidEmail(email)) {
            showError("⚠️ Неверный формат email");
            return;
        }

        System.out.println("🔐 Попытка входа: " + email);

        boolean success = authService.login(email, password);

        if (success) {
            System.out.println("✅ Вход выполнен успешно!");

            // Загружаем корзину и избранное
            CartManager cartManager = CartManager.getInstance();
            cartManager.loadCartFromServer(
                    authService.getCurrentUser().getId(),
                    authService.getAccessToken()
            );
            cartManager.loadFavoritesFromServer(
                    authService.getCurrentUser().getId(),
                    authService.getAccessToken()
            );

            SceneNavigator.goToMain();
        } else {
            showError("❌ Неверный email или пароль");
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String name = nameField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("⚠️ Заполните все поля");
            return;
        }

        if (!isValidEmail(email)) {
            showError("⚠️ Неверный формат email");
            return;
        }

        if (password.length() < 6) {
            showError("⚠️ Пароль должен быть не менее 6 символов");
            return;
        }

        System.out.println("📝 Попытка регистрации: " + email);

        boolean success = authService.register(email, password, name);

        if (success) {
            System.out.println("✅ Регистрация успешна!");
            SceneNavigator.goToMain();
        } else {
            showError("❌ Ошибка регистрации. Email уже используется");
        }
    }

    @FXML
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;

        // Переключаем видимость полей
        if (registerBox != null) {
            registerBox.setVisible(isRegisterMode);
            registerBox.setManaged(isRegisterMode);
        }

        if (loginButton != null) {
            loginButton.setVisible(!isRegisterMode);
            loginButton.setManaged(!isRegisterMode);
        }

        if (registerButton != null) {
            registerButton.setVisible(isRegisterMode);
            registerButton.setManaged(isRegisterMode);
        }

        // Обновляем текст переключателя
        if (toggleLabel != null) {
            toggleLabel.setText(isRegisterMode ? "Уже есть аккаунт?" : "Нет аккаунта?");
        }

        // Очищаем ошибки
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    @FXML
    private void guestContinue() {
        System.out.println("👤 Гостевой вход");
        SceneNavigator.goToMain();
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат на главную");
        SceneNavigator.goToMain();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    }
}
