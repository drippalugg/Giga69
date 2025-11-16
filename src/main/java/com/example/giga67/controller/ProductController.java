package com.example.giga67.controller;

import com.example.giga67.model.Part;
import com.example.giga67.service.CartManager;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Map;

public class ProductController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label brandLabel;

    @FXML
    private Label articleLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label oldPriceLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Button favoriteButton;


    private Part currentPart;
    private CartManager cartManager;
    @FXML
    private ImageView productImageView;

    @FXML
    private Label specificationsLabel;
    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1);
        if (quantitySpinner != null) {
            quantitySpinner.setValueFactory(valueFactory);
        }

        System.out.println("✅ ProductController initialized!");
    }

    public void setProduct(Part part) {
        this.currentPart = part;
        displayProduct();
    }

    private void displayProduct() {
        if (currentPart == null) {
            System.err.println("❌ currentPart is null!");
            return;
        }

        if (nameLabel != null) {
            nameLabel.setText(currentPart.getName());
        }

        if (brandLabel != null) {
            brandLabel.setText("Бренд: " + currentPart.getBrand());
        }

        if (articleLabel != null) {
            articleLabel.setText("Артикул: " + currentPart.getArticle());
        }

        if (priceLabel != null) {
            priceLabel.setText(String.format("%.0f ₽", currentPart.getPrice()));
        }

        if (currentPart.hasDiscount()) {
            if (oldPriceLabel != null) {
                oldPriceLabel.setText(String.format("%.0f ₽", currentPart.getOldPrice()));
                oldPriceLabel.setVisible(true);
            }
            if (discountLabel != null) {
                discountLabel.setText("-" + currentPart.getDiscountPercent() + "%");
                discountLabel.setVisible(true);
            }
        } else {
            if (oldPriceLabel != null) {
                oldPriceLabel.setVisible(false);
            }
            if (discountLabel != null) {
                discountLabel.setVisible(false);
            }
        }

        if (descriptionLabel != null) {
            descriptionLabel.setText(currentPart.getDescription() != null ?
                    currentPart.getDescription() : "Описание товара");
        }

        // Загрузка изображения товара
        if (productImageView != null) {
            String imageUrl = currentPart.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    Image image = new Image(imageUrl, true);
                    productImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Ошибка загрузки изображения: " + e.getMessage());
                }
            }
        }

        // Отображение характеристик товара
        if (specificationsLabel != null && currentPart.getSpecifications() != null && !currentPart.getSpecifications().isEmpty()) {
            StringBuilder specs = new StringBuilder();
            for (Map.Entry<String, String> entry : currentPart.getSpecifications().entrySet()) {
                specs.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            specificationsLabel.setText(specs.toString());
            specificationsLabel.setVisible(true);
            specificationsLabel.setManaged(true);
        }updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (favoriteButton != null && currentPart != null) {
            if (cartManager.isFavorite(currentPart)) {
                favoriteButton.setText("❤️ В избранном");
            } else {
                favoriteButton.setText("🤍 В избранное");
            }
        }
    }

    @FXML
    private void addToCart() {
        if (currentPart == null) {
            System.err.println("❌ Cannot add to cart: currentPart is null");
            return;
        }

        int quantity = 1;
        if (quantitySpinner != null) {
            quantity = quantitySpinner.getValue();
        }

        cartManager.addToCart(currentPart, quantity);
        System.out.println("🛒 Добавлено в корзину: " + currentPart.getName() + " x" + quantity);

        SceneNavigator.goToCart();
    }

    @FXML
    private void toggleFavorite() {
        if (currentPart == null) {
            System.err.println("❌ Cannot toggle favorite: currentPart is null");
            return;
        }

        if (cartManager.isFavorite(currentPart)) {
            cartManager.removeFromFavorites(currentPart);
            System.out.println("💔 Удалено из избранного: " + currentPart.getName());
        } else {
            cartManager.addToFavorites(currentPart);
            System.out.println("❤️ Добавлено в избранное: " + currentPart.getName());
        }

        updateFavoriteButton();
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат назад");
        SceneNavigator.goToMain();
    }
}
