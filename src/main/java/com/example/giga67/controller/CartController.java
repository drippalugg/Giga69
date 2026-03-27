package com.example.giga67.controller;

import com.example.giga67.model.CartItem;
import com.example.giga67.model.Order;
import com.example.giga67.model.Part;
import com.example.giga67.service.CartManager;
import com.example.giga67.service.OrdersService;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartController {
    @FXML private ListView<CartItem> cartListView;
    @FXML private Label totalLabel;
    @FXML private Label emptyLabel;
    private CartManager cartManager;
    private SupabaseAuthService authService;
    private OrdersService ordersService;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        authService = SupabaseAuthService.getInstance();
        ordersService = OrdersService.getInstance();
        setupCartListView();
        loadCart();
        updateTotal();
    }

    // ---------------------- Экран корзины (отображение и изменение параметров товара в корзине) ----------------------
    private void setupCartListView() {
        cartListView.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        cartListView.setCellFactory(listView -> new ListCell<CartItem>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(null);
                    setGraphic(createCartItemView(item));
                    setStyle("-fx-background-color: transparent; -fx-padding: 8 0;");
                }
            }
        });
    }

    private VBox createCartItemView(CartItem item) {
        VBox container = new VBox(0);

        HBox mainBox = new HBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setAlignment(Pos.CENTER_LEFT);
        mainBox.getStyleClass().add("cart-item");

        Part part = item.getPart();

        // Картинка товара или заглушка
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        String imageUrl = part.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                imageView.setImage(new Image(imageUrl, 80, 80, true, true));
            } catch (Exception e) {
                System.err.println("Ошибка загрузки изображения в корзине: " + e.getMessage());
                imageView.setImage(null);
            }
        }
        // При неудачной загрузки изображении отображается заглушка
        Label iconLabel = new Label("🎁");
        iconLabel.setStyle("-fx-font-size: 36px;");

        VBox infoBox = new VBox(6);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(part.getName());
        nameLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #000;");

        Label articleLabel = new Label("Артикул: " + part.getArticle());
        articleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label priceLabel = new Label(String.format("%.2f ₽", part.getPrice()));
        priceLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #666;");

        infoBox.getChildren().addAll(nameLabel, articleLabel, priceLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox quantityBox = new HBox(8);
        quantityBox.setAlignment(Pos.CENTER);

        Label qtyLabel = new Label("Количество:");
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Spinner<Integer> quantitySpinner = new Spinner<>();
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, item.getQuantity());
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.setEditable(true);

        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal > 0) {
                cartManager.updateQuantity(part, newVal);
                updateTotal();
            }
        });

        quantityBox.getChildren().addAll(qtyLabel, quantitySpinner);

        VBox totalBox = new VBox(4);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setMinWidth(120);

        Label totalPriceLabel = new Label(String.format("%.2f ₽", item.getTotalPrice()));
        totalPriceLabel.setStyle(
                "-fx-font-size: 22px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #667EEA;"
        );

        totalBox.getChildren().add(totalPriceLabel);

        Button removeBtn = new Button("❌");
        removeBtn.setStyle(
                "-fx-background-color: #FFE5E5; " +
                        "-fx-text-fill: #FF4757; " +
                        "-fx-font-size: 20px; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 12; " +
                        "-fx-min-width: 50; " +
                        "-fx-min-height: 50;"
        );
        removeBtn.setOnMouseEntered(e ->
                removeBtn.setStyle(
                        "-fx-background-color: #FF4757; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 20px; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 10; " +
                                "-fx-padding: 12; " +
                                "-fx-min-width: 50; " +
                                "-fx-min-height: 50;"
                )
        );
        removeBtn.setOnMouseExited(e ->
                removeBtn.setStyle(
                        "-fx-background-color: #FFE5E5; " +
                                "-fx-text-fill: #FF4757; " +
                                "-fx-font-size: 20px; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 10; " +
                                "-fx-padding: 12; " +
                                "-fx-min-width: 50; " +
                                "-fx-min-height: 50;"
                )
        );
        removeBtn.setOnAction(e -> {
            cartManager.removeItem(part.getId());
            loadCart();
            updateTotal();
        });

        // При наличии изображения товара ставится imageView, иначе отображается заглушка
        if (imageView.getImage() != null) {
            mainBox.getChildren().addAll(imageView, infoBox, spacer, quantityBox, totalBox, removeBtn);
        } else {
            mainBox.getChildren().addAll(iconLabel, infoBox, spacer, quantityBox, totalBox, removeBtn);
        }

        container.getChildren().add(mainBox);
        return container;
    }

    private void loadCart() {
        List<CartItem> items = cartManager.getItems();
        cartListView.getItems().clear();

        if (items.isEmpty()) {
            // Отображение центрированного сообщения
            if (emptyLabel != null) {
                emptyLabel.setText("Корзина пуста");
                emptyLabel.setStyle(
                        "-fx-font-size: 24px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #999999; " +
                                "-fx-padding: 60;"
                );
                emptyLabel.setVisible(true);
            }
            if (cartListView != null) {
                cartListView.setVisible(false);
            }
        } else {
            if (emptyLabel != null) {
                emptyLabel.setVisible(false);
            }
            if (cartListView != null) {
                cartListView.setVisible(true);
                cartListView.getItems().addAll(items);
            }
        }
    }

    private void updateTotal() {
        double total = 0.0;
        List<CartItem> items = cartManager.getItems();

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            double price = item.getPart().getPrice();
            int quantity = item.getQuantity();
            total += (price * quantity);
        }

        if (totalLabel != null) {
            totalLabel.setText(String.format("Итого: %.2f ₽", total));
        }
    }

    @FXML
    private void handleCheckout() {
        if (!authService.isLoggedIn()) {
            showAlert("Требуется авторизация",
                    "Пожалуйста, войдите в систему для оформления заказа",
                    Alert.AlertType.WARNING);
            SceneNavigator.goToLogin();
            return;
        }

        if (cartManager.getItems().isEmpty()) {
            showAlert("Пустая корзина",
                    "Добавьте товары в корзину перед оформлением заказа",
                    Alert.AlertType.WARNING);
            return;
        }

        String orderId = "ORDER-" + System.currentTimeMillis();
        Order order = new Order(
                orderId,
                authService.getCurrentUser().getId(),
                new ArrayList<>(cartManager.getItems()),
                cartManager.getTotal(),
                LocalDateTime.now(),
                "pending"
        );

        boolean success = ordersService.createOrder(
                order,
                authService.getCurrentUser().getId(),
                authService.getAccessToken()
        );

        if (success) {
            cartManager.clear();
            loadCart();
            updateTotal();

            showAlert("Заказ оформлен",
                    "Ваш заказ успешно оформлен!\nНомер заказа: " + orderId,
                    Alert.AlertType.INFORMATION);
            SceneNavigator.goToOrders();
        } else {
            showAlert("Ошибка",
                    "Не удалось оформить заказ. Попробуйте позже.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClearCart() {
        if (cartManager.getItems().isEmpty()) {
            return;
        }
        cartManager.clear();
        loadCart();
        updateTotal();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        SceneNavigator.goToMain();
    }
}
