package com.example.giga67.controller;

import com.example.giga67.model.Part;
import com.example.giga67.service.CartManager;
import com.example.giga67.util.SceneNavigator;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FavoritesController {
    @FXML
    private FlowPane productsPane;

    private CartManager cartManager;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        loadFavorites();
        System.out.println("✅ FavoritesController initialized!");
    }

    private void loadFavorites() {
        if (productsPane == null) {
            System.err.println("❌ productsPane is null!");
            return;
        }

        productsPane.getChildren().clear();
        ObservableList<Part> favorites = cartManager.getFavorites();

        if (favorites.isEmpty()) {
            Label noFavorites = new Label("Избранное пусто");
            noFavorites.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
            productsPane.getChildren().add(noFavorites);
            return;
        }

        for (Part part : favorites) {
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

        Button removeFavoriteButton = new Button("💖");
        removeFavoriteButton.setStyle("-fx-font-size: 18px; -fx-cursor: hand;");
        removeFavoriteButton.setOnAction(e -> removeFavorite(part));

        buttonsBox.getChildren().addAll(cartButton, removeFavoriteButton);

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
        System.out.println("🛒 Added to cart: " + part.getName());
    }

    private void removeFavorite(Part part) {
        cartManager.removeFromFavorites(part);
        loadFavorites();
        System.out.println("💔 Removed from favorites: " + part.getName());
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
}
