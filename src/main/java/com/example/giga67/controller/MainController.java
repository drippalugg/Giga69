package com.example.giga67.controller;

import com.example.giga67.model.Category;
import com.example.giga67.service.PartsService;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import com.example.giga67.util.AdminSceneNavigator;
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
            locationLabel.setText("г.Энгельс");
        }

        loadCategories();
        updateLoginButton();
        if (authService.isLoggedIn()
                && authService.getCurrentUser() != null
                && authService.getCurrentUser().isAdmin()) {
            addAdminButton();
        }

        System.out.println("Loaded " + partsService.getCategories().size() + " categories");
        System.out.println("Loaded " + partsService.getParts().size() + " products");
    }

    private void addAdminButton() {
        try {
            // защита от дублей: если уже есть кнопка с таким текстом в родителе loginButton – выходим
            if (loginButton != null && loginButton.getParent() != null) {
                var parent = loginButton.getParent();

                if (parent instanceof VBox vbox) {
                    boolean exists = vbox.getChildren().stream()
                            .filter(n -> n instanceof Button)
                            .anyMatch(n -> "Администратор".equals(((Button) n).getText()));
                    if (exists) return;
                } else if (parent instanceof javafx.scene.layout.HBox hbox) {
                    boolean exists = hbox.getChildren().stream()
                            .filter(n -> n instanceof Button)
                            .anyMatch(n -> "Администратор".equals(((Button) n).getText()));
                    if (exists) return;
                }
            }

            Button adminBtn = new Button("Администратор");
            adminBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 15; " +
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-cursor: hand;");
            adminBtn.setOnAction(e -> handleAdminPanel());

            // способ 1: если loginButton есть – добавляем рядом с ним
            if (loginButton != null && loginButton.getParent() != null) {
                var parent = loginButton.getParent();

                if (parent instanceof VBox vbox) {
                    vbox.getChildren().add(adminBtn);
                    return;
                } else if (parent instanceof javafx.scene.layout.HBox hbox) {
                    hbox.getChildren().add(adminBtn);
                    return;
                }
            }

            // способ 2: если есть categoriesPane – вставляем над ним
            if (categoriesPane != null && categoriesPane.getParent() != null) {
                var parent = categoriesPane.getParent();
                if (parent instanceof VBox vbox) {
                    int index = vbox.getChildren().indexOf(categoriesPane);
                    if (index >= 0) {
                        vbox.getChildren().add(index, adminBtn);
                    } else {
                        vbox.getChildren().add(adminBtn);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminPanel() {
        if (authService.isLoggedIn()
                && authService.getCurrentUser() != null
                && authService.getCurrentUser().isAdmin()) {
            AdminSceneNavigator.goToAdminPanel();
        } else {
            System.out.println("Нет прав администратора");
        }
    }

    private void updateLoginButton() {
        if (loginButton != null) {
            if (authService.isLoggedIn()) {
                loginButton.setText(authService.getCurrentUser().getName());
            } else {
                loginButton.setText("Войти");
            }
        }
    }

    private void loadCategories() {
        if (categoriesPane == null) {
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
            SceneNavigator.goToCategory(category);
        });

        return card;
    }

    @FXML
    private void handleSearch() {
        if (searchField == null) {
            return;
        }

        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            SceneNavigator.goToSearch(query);
        }
    }

    @FXML
    private void handleOrders() {
        SceneNavigator.goToOrders();
    }

    @FXML
    private void handleFavorites() {
        SceneNavigator.goToFavorites();
    }

    @FXML
    private void handleCart() {
        SceneNavigator.goToCart();
    }

    @FXML
    private void handleLogin() {

        if (authService.isLoggedIn()) {
            // Если пользователь залогинен - идём в профиль
            SceneNavigator.goToProfile();
        } else {
            // Если не залогинен - идём на экран входа
            SceneNavigator.goToLogin();
        }
    }
}