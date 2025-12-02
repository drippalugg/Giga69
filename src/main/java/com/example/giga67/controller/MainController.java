package com.example.giga67.controller;

import com.example.giga67.model.Category;
import com.example.giga67.service.PartsService;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML private Label locationLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane categoriesPane;
    @FXML private Button loginButton;

    private PartsService partsService;
    private SupabaseAuthService authService;

    @FXML
    public void initialize() {
        System.out.println("MainController initialized!");

        partsService = new PartsService();
        authService = SupabaseAuthService.getInstance();

        if (locationLabel != null) {
            locationLabel.setText("📍 Энгельс");
        }

        loadCategories();
        updateLoginButton();

        System.out.println("Loaded " + partsService.getCategories().size() + " categories");
        System.out.println("Loaded " + partsService.getParts().size() + " products");
    }

    private void updateLoginButton() {
        if (loginButton != null) {
            if (authService.isLoggedIn()) {
                loginButton.setText("👤 " + authService.getCurrentUser().getName());
                System.out.println("Пользователь залогинен: " + authService.getCurrentUser().getEmail());
            } else {
                loginButton.setText("👤 Войти");
                System.out.println("⚠Пользователь не залогинен");
            }
        }
    }

    private void loadCategories() {
        if (categoriesPane == null) {
            System.err.println("categoriesPane is null!");
            return;
        }

        categoriesPane.getChildren().clear();
        for (Category category : partsService.getCategories()) {
            VBox categoryCard = createCategoryCard(category);
            categoriesPane.getChildren().add(categoryCard);
        }
    }

    private VBox createCategoryCard(Category category) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-cursor: hand;");
        card.setPrefSize(250, 150);
        card.setPadding(new Insets(20));

        Label iconLabel = new Label(category.getIcon());
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label nameLabel = new Label(category.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        card.getChildren().addAll(iconLabel, nameLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 10; -fx-cursor: hand;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-cursor: hand;");
        });

        card.setOnMouseClicked(e -> {
            System.out.println("🖱️ Открытие категории: " + category.getName());
            SceneNavigator.goToCategory(category);
        });

        return card;
    }

    @FXML
    private void handleSearch() {
        if (searchField == null) {
            System.err.println("searchField is null!");
            return;
        }

        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            System.out.println("Поиск: " + query);
            SceneNavigator.goToSearch(query);
        }
    }

    @FXML
    private void handleOrders() {
        System.out.println("Заказы clicked");
        SceneNavigator.goToOrders();
    }

    @FXML
    private void handleFavorites() {
        System.out.println("Избранное clicked");
        SceneNavigator.goToFavorites();
    }

    @FXML
    private void handleCart() {
        System.out.println("Корзина clicked");
        SceneNavigator.goToCart();
    }

    @FXML
    private void handleLogin() {
        System.out.println("Войти clicked");
        System.out.println("Текущий статус: isLoggedIn = " + authService.isLoggedIn());

        if (authService.isLoggedIn()) {
            // Если пользователь залогинен - идём в профиль
            System.out.println("Переход в профиль");
            SceneNavigator.goToProfile();
        } else {
            // Если не залогинен - идём на экран входа
            System.out.println("⚠Переход на экран входа");
            SceneNavigator.goToLogin();
        }
    }
}
