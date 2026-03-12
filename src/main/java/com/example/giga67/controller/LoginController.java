package com.example.giga67.controller;

import com.example.giga67.service.CartManager;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.CheckBox;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private Label errorLabel;
    @FXML private VBox registerBox;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button toggleButton;
    @FXML private CheckBox rememberMeCheckBox;

    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private static final String PREF_EMAIL = "remember_email";
    private static final String PREF_PASSWORD = "remember_password";
    private static final String PREF_REMEMBER = "remember_flag";
    private SupabaseAuthService authService;
    private boolean isRegisterMode = false;

    @FXML
    public void initialize() {
        authService = SupabaseAuthService.getInstance();

        // загрузка сохранённых данных
        boolean remember = prefs.getBoolean(PREF_REMEMBER, false);
        if (rememberMeCheckBox != null) {
            rememberMeCheckBox.setSelected(remember);
        }
        if (remember) {
            String savedEmail = prefs.get(PREF_EMAIL, "");
            String savedPassword = prefs.get(PREF_PASSWORD, "");
            if (emailField != null) emailField.setText(savedEmail);
            if (passwordField != null) passwordField.setText(savedPassword);
        }

        if (registerBox != null) {
            registerBox.setVisible(false);
            registerBox.setManaged(false);
        }

        if (errorLabel != null) {
            errorLabel.setText("");
        }
        updateUI();
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
        boolean success = authService.login(email, password);

        if (success) {
            // сохранить или очистить
            if (rememberMeCheckBox != null && rememberMeCheckBox.isSelected()) {
                prefs.putBoolean(PREF_REMEMBER, true);
                prefs.put(PREF_EMAIL, email);
                prefs.put(PREF_PASSWORD, password);
            } else {
                prefs.putBoolean(PREF_REMEMBER, false);
                prefs.remove(PREF_EMAIL);
                prefs.remove(PREF_PASSWORD);
            }



            Platform.runLater(() -> {
                CartManager.getInstance().loadData();
                SceneNavigator.goToMain();
            });
        } else {
            showError("Неверный email или пароль");
        }
    }

    @FXML
    private void handleRegister() {

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String name = nameField != null ? nameField.getText().trim() : "";

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
        boolean success = authService.register(email, password, name);

        if (success) {
            Platform.runLater(() -> {
                SceneNavigator.goToMain();
            });
        } else {
            showError("Ошибка регистрации. Email уже используется");
        }
    }

    @FXML
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        updateUI();
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void updateUI() {
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

        if (toggleButton != null) {
            toggleButton.setText(isRegisterMode ? "Уже есть аккаунт? Войти" : "Нет аккаунта? Зарегистрироваться");
        }
    }

    @FXML
    private void guestContinue() {
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
