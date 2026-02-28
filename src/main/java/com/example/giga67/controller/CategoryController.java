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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CategoryController {
    @FXML private Label titleLabel;
    @FXML private FlowPane productsPane;

    private PartsService partsService;
    private CartManager cartManager;
    private Category currentCategory;

    @FXML
    public void initialize() {
        partsService = new PartsService();
        cartManager = CartManager.getInstance();
        System.out.println("CategoryController initialized!");
    }

    public void setCategory(Category category) {
        this.currentCategory = category;
        if (titleLabel != null) {
            titleLabel.setText(category.getName());
        }
        loadProducts(partsService.getPartsByCategory(category.getId()));
    }

    public void setSearchQuery(String query) {
        if (titleLabel != null) {
            titleLabel.setText("Результаты поиска: " + query);
        }
        loadProducts(partsService.searchParts(query));
    }

    private void loadProducts(ObservableList<Part> products) {
        if (productsPane == null) {
            System.err.println("productsPane is null!");
            return;
        }

        productsPane.getChildren().clear();

        if (products.isEmpty()) {
            Label noProducts = new Label("Товары не найдены");
            noProducts.getStyleClass().add("empty-message");
            productsPane.getChildren().add(noProducts);
            return;
        }

        for (Part part : products) {
            VBox productCard = createProductCard(part);
            productsPane.getChildren().add(productCard);
        }

        System.out.println("Загружено товаров: " + products.size());
    }

    private VBox createProductCard(Part part) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(280, 400);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(12));

        // Картинка товара
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        String imageUrl = part.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image img = new Image(imageUrl, 180, 180, true, true);
                imageView.setImage(img);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки изображения списка: " + e.getMessage());
            }
        }

        // Название
        Label nameLabel = new Label(part.getName());
        nameLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #000000;"
        );
        nameLabel.setWrapText(true);

        // Артикул
        Label articleLabel = new Label("Артикул: " + part.getArticle());
        articleLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #666666;"
        );

        // Бренд
        Label brandLabel = new Label("Бренд: " + part.getBrand());
        brandLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #666666;"
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Цена и скидка
        HBox priceBox = new HBox(8);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        if (part.hasDiscount()) {
            Label oldPriceLabel = new Label(String.format("%.0f ₽", part.getOldPrice()));
            oldPriceLabel.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-text-fill: #888888; " +
                            "-fx-strikethrough: true;"
            );

            Label priceLabel = new Label(String.format("%.0f ₽", part.getPrice()));
            priceLabel.setStyle(
                    "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #000000;"
            );

            Label discountLabel = new Label("-" + part.getDiscountPercent() + "%");
            discountLabel.setStyle(
                    "-fx-background-color: #FF4757; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 4 10;"
            );

            priceBox.getChildren().addAll(oldPriceLabel, priceLabel, discountLabel);
        } else {
            Label priceLabel = new Label(String.format("%.0f ₽", part.getPrice()));
            priceLabel.setStyle(
                    "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #000000;"
            );
            priceBox.getChildren().add(priceLabel);
        }

        // Кнопки
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button cartButton = new Button("🛒 В корзину");
        cartButton.getStyleClass().add("primary-button");
        cartButton.setMaxWidth(Double.MAX_VALUE);
        cartButton.setOnAction(e -> addToCart(part));
        HBox.setHgrow(cartButton, Priority.ALWAYS);

        Button favoriteButton = new Button(cartManager.isFavorite(part) ? "💖" : "❤");
        favoriteButton.setStyle(
                "-fx-font-size: 20px; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-color: transparent; " +
                        "-fx-padding: 8 12;"
        );
        favoriteButton.setOnAction(e -> toggleFavorite(part, favoriteButton));

        buttonsBox.getChildren().addAll(cartButton, favoriteButton);

        card.getChildren().addAll(
                imageView,
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


    private void addToCart(Part part) {
        cartManager.addToCart(part, 1);
        System.out.println("Добавлено в корзину: " + part.getName());
    }

    private void toggleFavorite(Part part, Button button) {
        if (cartManager.isFavorite(part)) {
            cartManager.removeFromFavorites(part);
            button.setText("❤");
        } else {
            cartManager.addToFavorites(part);
            button.setText("💖");
        }
    }

    private void openProduct(Part part) {
        System.out.println("Открытие товара: " + part.getName());
        SceneNavigator.goToProduct(part);
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат на главную");
        SceneNavigator.goToMain();
    }

    @FXML
    private void openCart() {
        System.out.println("Переход в корзину");
        SceneNavigator.goToCart();
    }
}