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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

public class FavoritesController {
    @FXML private FlowPane productsPane;
    @FXML private Label emptyLabel;
    @FXML private ScrollPane favoritesScrollPane;

    private CartManager cartManager;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        loadFavorites();
    }

    // ------------- Избранное (отображение товаров в избранном/корзине пользователей) ------------- \\

    // Загрузка избранного пользователя
    private void loadFavorites() {
        if (productsPane == null) {
            return;
        }
        productsPane.getChildren().clear();

        if (cartManager.getFavorites().isEmpty()) {

            // Отображение центрированного сообщения
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
    }

    // Создание карточки товара в избранном с его хар-ми
    private VBox createProductCard(Part part) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(280, 380);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(20));

        // Изображение товара
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        String imageUrl = part.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                imageView.setImage(new Image(imageUrl, 120, 120, true, true));
                card.getChildren().add(imageView);           // Изображение товара
            } catch (Exception e) {
                Label iconLabel = new Label("🎁");
                iconLabel.setStyle("-fx-font-size: 80px;");
                card.getChildren().add(iconLabel);           // Заглушка при ошибке загрузке изображения
            }
        } else {
            Label iconLabel = new Label("🎁");
            iconLabel.setStyle("-fx-font-size: 80px;");
            card.getChildren().add(iconLabel);               // Заглушка при отсутствии URL изображения
        }

        // Название
        Label nameLabel = new Label(part.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1A1A1A;" +
                        "-fx-wrap-text: true;" +
                        "-fx-text-alignment: center;"
        );
        nameLabel.setMaxWidth(240);
        nameLabel.setWrapText(true);

        // Артикул
        Label articleLabel = new Label("Арт: " + part.getArticle());
        articleLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #666;"
        );

        // Бренд
        Label brandLabel = new Label("Бренд: " + part.getBrand());
        brandLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #666;"
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Цена (с учётом возможной скидки)
        HBox priceBox = new HBox(8);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        if (part.hasDiscount()) {
            Label oldPriceLabel = new Label(String.format("%.0f ₽", part.getOldPrice()));
            oldPriceLabel.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-text-fill: #888;" +
                            "-fx-strikethrough: true;"
            );

            Label priceLabel = new Label(String.format("%.0f ₽", part.getPrice()));
            priceLabel.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #000;"
            );

            Label discountLabel = new Label("-" + part.getDiscountPercent() + "%");
            discountLabel.setStyle(
                    "-fx-background-color: #FF4757;" +
                            "-fx-text-fill: #FFF;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 4 10;"
            );

            priceBox.getChildren().addAll(oldPriceLabel, priceLabel, discountLabel);
        } else {
            Label priceLabel = new Label(String.format("%.0f ₽", part.getPrice()));
            priceLabel.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #000;"
            );
            priceBox.getChildren().add(priceLabel);
        }

        // Кнопки
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button removeButton = new Button("Убрать из избранного");
        removeButton.getStyleClass().add("secondary-button");
        removeButton.setOnAction(e -> removeFromFavorites(part));

        Button openButton = new Button("Открыть");
        openButton.getStyleClass().add("primary-button");
        openButton.setOnAction(e -> openProduct(part));

        buttonsBox.getChildren().addAll(openButton, removeButton);

        // Наполнение карточки
        card.getChildren().addAll(
                nameLabel,
                articleLabel,
                brandLabel,
                spacer,
                priceBox,
                buttonsBox
        );
        card.setOnMouseClicked(e -> openProduct(part));
        return card;
    }

    private void removeFromFavorites(Part part) {
        cartManager.removeFromFavorites(part);
        loadFavorites();
    }

    private void openProduct(Part part) {
        SceneNavigator.goToProduct(part);
    }

    @FXML
    private void goBack() {
        SceneNavigator.goToMain();
    }
}