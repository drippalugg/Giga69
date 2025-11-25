package com.example.giga67.controller;

import com.example.giga67.model.CartItem;
import com.example.giga67.service.CartManager;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CartController {

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label emptyCartLabel;

    @FXML
    private Button checkoutButton;

    private CartManager cartManager;
    private SupabaseAuthService authService;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        authService = new SupabaseAuthService();
        loadCart();

        System.out.println("✅ CartController initialized!");
    }

    private void loadCart() {
        cartItemsContainer.getChildren().clear();

        if (cartManager.getCartItems().isEmpty()) {
            emptyCartLabel.setVisible(true);
            checkoutButton.setDisable(true);
            totalPriceLabel.setText("0 ₽");
            return;
        }

        emptyCartLabel.setVisible(false);
        checkoutButton.setDisable(false);

        for (CartItem item : cartManager.getCartItems()) {
            HBox itemBox = createCartItemBox(item);
            cartItemsContainer.getChildren().add(itemBox);
        }

        updateTotalPrice();
    }

    private HBox createCartItemBox(CartItem item) {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10;");

        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 40px;");

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getPart().getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label articleLabel = new Label("Арт: " + item.getPart().getArticle());
        articleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label priceLabel = new Label(String.format("%.0f ₽", item.getPart().getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px;");

        infoBox.getChildren().addAll(nameLabel, articleLabel, priceLabel);

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER);

        Spinner<Integer> quantitySpinner = new Spinner<>();
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, item.getQuantity());
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setPrefWidth(80);

        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            cartManager.updateQuantity(item.getPart(), newVal);
            updateTotalPrice();
        });

        quantityBox.getChildren().add(quantitySpinner);

        Label itemTotalLabel = new Label(String.format("%.0f ₽", item.getTotalPrice()));
        itemTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        itemTotalLabel.setMinWidth(100);

        Button removeButton = new Button("🗑️");
        removeButton.setStyle("-fx-background-color: #FFE0E0; -fx-cursor: hand; -fx-padding: 8;");
        removeButton.setOnAction(e -> {
            cartManager.removeFromCart(item.getPart());
            loadCart();
        });

        box.getChildren().addAll(iconLabel, infoBox, quantityBox, itemTotalLabel, removeButton);

        return box;
    }

    private void updateTotalPrice() {
        double total = cartManager.getTotalPrice();
        totalPriceLabel.setText(String.format("%.0f ₽", total));
    }

    @FXML
    private void checkout() {
        if (!authService.isLoggedIn()) {
            System.out.println("⚠️ Требуется авторизация");
            SceneNavigator.goToLogin();
            return;
        }

        System.out.println("✅ Оформление заказа");
        cartManager.clearCart();
        loadCart();

        Label successLabel = new Label("✅ Заказ успешно оформлен!");
        successLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: green; -fx-padding: 20px;");
        cartItemsContainer.getChildren().add(0, successLabel);
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат на главную");
        SceneNavigator.goToMain();
    }
}
