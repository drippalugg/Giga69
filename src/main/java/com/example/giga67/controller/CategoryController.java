package com.example.giga67.controller;

import com.example.giga67.model.Category;
import com.example.giga67.model.Part;
import com.example.giga67.service.CartManager;
import com.example.giga67.service.PartsService;
import com.example.giga67.util.SceneNavigator;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CategoryController {
    @FXML
    private Label titleLabel;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane productsPane;
    @FXML
    private Label cartBadge;

    private PartsService partsService;
    private CartManager cartManager;
    private Category currentCategory;
    private String searchQuery;

    @FXML
    public void initialize() {
        partsService = new PartsService();
        cartManager = CartManager.getInstance();
        updateCartBadge();
        System.out.println("✅ CategoryController initialized!");
    }

    public void setCategory(Category category) {
        this.currentCategory = category;
        if (titleLabel != null) {
            titleLabel.setText(category.getName());
        }
        loadProducts(partsService.getPartsByCategory(category.getId()));
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
        if (titleLabel != null) {
            titleLabel.setText("Результаты поиска: " + query);
        }
        loadProducts(partsService.searchParts(query));
    }

    private void loadProducts(ObservableList<Part> products) {
        if (productsPane == null) {
            System.err.println("❌ productsPane is null!");
            return;
        }

        productsPane.getChildren().clear();

        if (products.isEmpty()) {
            Label noProducts = new Label("Товары не найдены");
            noProducts.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
            productsPane.getChildren().add(noProducts);
            return;
        }

        for (Part part : products) {
            VBox productCard = createProductCard(part);
            productsPane.getChildren().add(productCard);
        }
    }

    private VBox createProductCard(Part part) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-cursor: hand;");
        card.setPrefSize(250, 320);
        card.setPadding(new Insets(15));

        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 60px;");
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(part.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxHeight(40);

        Label articleLabel = new Label("Арт: " + part.getArticle());
        articleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label brandLabel = new Label(part.getBrand());
        brandLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(String.format("%.0f ₽", part.getPrice()));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        if (part.hasDiscount()) {
            Label oldPriceLabel = new Label(String.format("%.0f ₽", part.getOldPrice()));
            oldPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-strikethrough: true;");
            Label discountLabel = new Label("-" + part.getDiscountPercent() + "%");
            discountLabel.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 12px;");
            priceBox.getChildren().addAll(priceLabel, oldPriceLabel, discountLabel);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button cartButton = new Button("В корзину");
        cartButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 20;");
        cartButton.setOnAction(e -> addToCart(part));

        Button favoriteButton = new Button(cartManager.isFavorite(part) ? "💖" : "🤎");
        favoriteButton.setStyle("-fx-font-size: 18px; -fx-cursor: hand;");
        favoriteButton.setOnAction(e -> toggleFavorite(part, favoriteButton));

        buttonsBox.getChildren().addAll(cartButton, favoriteButton);

        card.getChildren().addAll(iconLabel, nameLabel, articleLabel, brandLabel, priceBox, buttonsBox);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openProduct(part);
            }
        });

        return card;
    }

    private void addToCart(Part part) {
        cartManager.addToCart(part, 1);
        updateCartBadge();
        System.out.println("🛒 Added to cart: " + part.getName());
    }

    private void toggleFavorite(Part part, Button button) {
        if (cartManager.isFavorite(part)) {
            cartManager.removeFromFavorites(part);
            button.setText("🤎");
        } else {
            cartManager.addToFavorites(part);
            button.setText("💖");
        }
    }

    private void updateCartBadge() {
        if (cartBadge != null) {
            int count = cartManager.getTotalItems();
            cartBadge.setText(count > 0 ? String.valueOf(count) : "");
            cartBadge.setVisible(count > 0);
        }
    }

    private void openProduct(Part part) {
        System.out.println("📦 Opening product: " + part.getName());
        SceneNavigator.goToProduct(part);
    }

    @FXML
    private void goBack() {
        System.out.println("← Go back clicked");
        SceneNavigator.goToMain();
    }

    @FXML
    private void openCart() {
        System.out.println("🛒 Cart clicked");
        SceneNavigator.goToCart();
    }
}
