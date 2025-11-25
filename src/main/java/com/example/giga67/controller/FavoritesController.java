package com.example.giga67.controller;

import com.example.giga67.model.Part;
import com.example.giga67.service.CartManager;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class FavoritesController {
    @FXML private FlowPane productsPane;
    @FXML private Label emptyLabel;
    @FXML private ScrollPane favoritesScrollPane;

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

        if (cartManager.getFavorites().isEmpty()) {
            // Показываем центрированное сообщение
            if (emptyLabel != null) {
                emptyLabel.setText("Избранное пусто");
                emptyLabel.setStyle(
                        "-fx-font-size: 24px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #999999; " +
                                "-fx-padding: 60;"
                );
                emptyLabel.setVisible(true);
            }
            if (favoritesScrollPane != null) {
                favoritesScrollPane.setVisible(false);
            }
            System.out.println("💔 Избранное пусто");
            return;
        }

        // Скрываем сообщение, показываем товары
        if (emptyLabel != null) {
            emptyLabel.setVisible(false);
        }
        if (favoritesScrollPane != null) {
            favoritesScrollPane.setVisible(true);
        }

        for (Part part : cartManager.getFavorites()) {
            VBox productCard = createProductCard(part);
            productsPane.getChildren().add(productCard);
        }

        System.out.println("💖 Загружено избранных товаров: " + cartManager.getFavorites().size());
    }

    private VBox createProductCard(Part part) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(280, 380);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                        "-fx-cursor: hand;"
        );

        // Иконка товара
        Label iconLabel = new Label("🎁");
        iconLabel.setStyle("-fx-font-size: 80px;");

        // Название
        Label nameLabel = new Label(part.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #1A1A1A; " +
                        "-fx-wrap-text: true; " +
                        "-fx-text-alignment: center;"
        );
        nameLabel.setMaxWidth(240);
        nameLabel.setWrapText(true);

        // Артикул
        Label articleLabel = new Label("Арт: " + part.getArticle());
        articleLabel.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #666;"
        );

        // Цена
        Label priceLabel = new Label(String.format("%.2f ₽", part.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        priceLabel.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #000;"
        );

        // Кнопка "В корзину"
        Button addToCartBtn = new Button("🛒 В корзину");
        addToCartBtn.getStyleClass().add("primary-button");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(e -> {
            cartManager.addToCart(part, 1);
            System.out.println("🛒 Добавлено в корзину: " + part.getName());
        });

        // Кнопка "Удалить из избранного"
        Button removeBtn = new Button("💔 Удалить");
        removeBtn.getStyleClass().add("danger-button");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setOnAction(e -> {
            cartManager.removeFromFavorites(part);
            loadFavorites();
            System.out.println("💔 Удалено из избранного: " + part.getName());
        });

        card.setOnMouseClicked(e -> {
            System.out.println("🖱️ Открытие товара: " + part.getName());
            SceneNavigator.goToProduct(part);
        });

        // Hover эффект
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-background-radius: 16; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6); " +
                                "-fx-cursor: hand; " +
                                "-fx-scale-x: 1.03; " +
                                "-fx-scale-y: 1.03;"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-background-radius: 16; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                                "-fx-cursor: hand;"
                )
        );

        card.getChildren().addAll(iconLabel, nameLabel, articleLabel, priceLabel, addToCartBtn, removeBtn);

        return card;
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат на главную");
        SceneNavigator.goToMain();
    }
}
