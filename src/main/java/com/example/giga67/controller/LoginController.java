package com.example.giga67.controller;

import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.application.Platform;
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
    @FXML private Button toggleButton;

    private SupabaseAuthService authService;
    private boolean isRegisterMode = false;

    @FXML
    public void initialize() {
        System.out.println("LoginController.initialize() вызван");

        authService = SupabaseAuthService.getInstance();
        System.out.println("SupabaseAuthService получен: " + (authService != null));

        if (registerBox != null) {
            registerBox.setVisible(false);
            registerBox.setManaged(false);
            System.out.println("registerBox скрыт");
        }

        if (errorLabel != null) {
            errorLabel.setText("");
        }

        // Выводим информацию о кнопках
        System.out.println("Кнопки:");
        System.out.println("  loginButton: " + (loginButton != null ? "OK" : "NULL"));
        System.out.println("  registerButton: " + (registerButton != null ? "OK" : "NULL"));
        System.out.println("  toggleButton: " + (toggleButton != null ? "OK" : "NULL"));

        updateUI();
        System.out.println("LoginController initialized!");
    }

    @FXML
    private void handleLogin() {
        System.out.println("handleLogin() вызван");

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        System.out.println("Email: " + email);
        System.out.println("Password length: " + password.length());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Неверный формат email");
            return;
        }

        System.out.println("Вызываем authService.login()...");
        boolean success = authService.login(email, password);
        System.out.println("Результат login: " + success);

        if (success) {
            System.out.println("Вход успешен! Проверяем isLoggedIn: " + authService.isLoggedIn());
            System.out.println("Текущий пользователь: " + authService.getCurrentUser());

            Platform.runLater(() -> {
                System.out.println("🚀 Переход на главный экран...");
                SceneNavigator.goToMain();
            });
        } else {
            System.out.println("Вход не удался");
            showError("Неверный email или пароль");
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("handleRegister() вызван");

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String name = nameField != null ? nameField.getText().trim() : "";

        System.out.println("Email: " + email);
        System.out.println("Password length: " + password.length());
        System.out.println("Name: " + name);

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

        System.out.println("Вызываем authService.register()...");
        boolean success = authService.register(email, password, name);
        System.out.println("Результат register: " + success);

        if (success) {
            System.out.println("Регистрация успешна! Проверяем isLoggedIn: " + authService.isLoggedIn());

            Platform.runLater(() -> {
                System.out.println("🚀 Переход на главный экран...");
                SceneNavigator.goToMain();
            });
        } else {
            System.out.println("❌ Регистрация не удалась");
            showError("Ошибка регистрации. Email уже используется");
        }
    }

    @FXML
    private void toggleMode() {
        System.out.println("toggleMode() вызван. Текущий режим: " + (isRegisterMode ? "REGISTER" : "LOGIN"));

        isRegisterMode = !isRegisterMode;

        System.out.println("Новый режим: " + (isRegisterMode ? "REGISTER" : "LOGIN"));

        updateUI();

        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void updateUI() {
        System.out.println("updateUI() вызван. Режим: " + (isRegisterMode ? "REGISTER" : "LOGIN"));

        if (registerBox != null) {
            registerBox.setVisible(isRegisterMode);
            registerBox.setManaged(isRegisterMode);
            System.out.println("  registerBox visible: " + isRegisterMode);
        }

        if (loginButton != null) {
            loginButton.setVisible(!isRegisterMode);
            loginButton.setManaged(!isRegisterMode);
            System.out.println("  loginButton visible: " + !isRegisterMode);
        }

        if (registerButton != null) {
            registerButton.setVisible(isRegisterMode);
            registerButton.setManaged(isRegisterMode);
            System.out.println("  registerButton visible: " + isRegisterMode);
        }

        if (toggleButton != null) {
            toggleButton.setText(isRegisterMode ? "Уже есть аккаунт? Войти" : "Нет аккаунта? Зарегистрироваться");
            System.out.println("  toggleButton text: " + toggleButton.getText());
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
        System.out.println("Ошибка: " + message);
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
        }
    }

    private boolean isValidEmail(String email) {
        boolean valid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        System.out.println("📧 Email validation (" + email + "): " + valid);
        return valid;
    }
}
