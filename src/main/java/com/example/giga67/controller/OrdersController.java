package com.example.giga67.controller;

import com.example.giga67.model.Order;
import com.example.giga67.model.CartItem;
import com.example.giga67.service.OrdersService;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class OrdersController {
    @FXML private ListView<Order> ordersListView;
    @FXML private Label emptyLabel;

    private SupabaseAuthService authService;
    private OrdersService ordersService;

    @FXML
    public void initialize() {
        authService = SupabaseAuthService.getInstance();
        ordersService = OrdersService.getInstance();

        setupOrdersListView();
        loadOrders();

        System.out.println("OrdersController initialized!");
    }

    private void setupOrdersListView() {
        ordersListView.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        ordersListView.setCellFactory(listView -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);

                if (empty || order == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(null);
                    setGraphic(createOrderView(order));
                    setStyle("-fx-background-color: transparent; -fx-padding: 8 0;");
                }
            }
        });
    }

    private VBox createOrderView(Order order) {
        VBox container = new VBox(0);

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 14; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        // Заголовок заказа
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label orderIcon = new Label("📦");
        orderIcon.setStyle("-fx-font-size: 32px;");

        VBox orderInfoBox = new VBox(5);
        HBox.setHgrow(orderInfoBox, Priority.ALWAYS);

        Label orderIdLabel = new Label("Заказ #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #000;");

        Label orderDateLabel = new Label(order.getFormattedDate());
        orderDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        orderInfoBox.getChildren().addAll(orderIdLabel, orderDateLabel);

        // Статус заказа
        Label statusLabel = new Label(order.getStatusText());
        statusLabel.setPadding(new Insets(6, 12, 6, 12));
        statusLabel.setStyle(
                "-fx-background-color: " + getStatusColor(order.getStatus()) + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold;"
        );

        headerBox.getChildren().addAll(orderIcon, orderInfoBox, statusLabel);

        // Список товаров
        VBox itemsBox = new VBox(8);
        itemsBox.setStyle(
                "-fx-background-color: #F9F9F9; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 15;"
        );

        Label itemsTitle = new Label("Товары:");
        itemsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666;");
        itemsBox.getChildren().add(itemsTitle);

        for (CartItem item : order.getItems()) {
            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER_LEFT);

            Label itemName = new Label("• " + item.getPart().getName());
            itemName.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            HBox.setHgrow(itemName, Priority.ALWAYS);

            Label itemQty = new Label("x" + item.getQuantity());
            itemQty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

            Label itemPrice = new Label(String.format("%.2f ₽", item.getTotalPrice()));
            itemPrice.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #667EEA;");
            itemPrice.setMinWidth(100);
            itemPrice.setAlignment(Pos.CENTER_RIGHT);

            itemRow.getChildren().addAll(itemName, itemQty, itemPrice);
            itemsBox.getChildren().add(itemRow);
        }

        // Итоговая сумма
        HBox totalBox = new HBox();
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(10, 0, 0, 0));

        Label totalLabel = new Label("Итого: " + String.format("%.2f ₽", order.getTotalPrice()));
        totalLabel.setStyle(
                "-fx-font-size: 22px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #000;"
        );

        totalBox.getChildren().add(totalLabel);

        mainBox.getChildren().addAll(headerBox, itemsBox, totalBox);
        container.getChildren().add(mainBox);

        return container;
    }

    private String getStatusColor(String status) {
        switch (status) {
            case "pending":
                return "#FFA726"; // Оранжевый
            case "processing":
                return "#42A5F5"; // Синий
            case "shipped":
                return "#AB47BC"; // Фиолетовый
            case "delivered":
                return "#66BB6A"; // Зелёный
            case "cancelled":
                return "#EF5350"; // Красный
            default:
                return "#9E9E9E"; // Серый
        }
    }

    private void loadOrders() {
        System.out.println("Загрузка заказов...");

        if (!authService.isLoggedIn()) {
            System.out.println("Пользователь не авторизован");
            if (emptyLabel != null) {
                emptyLabel.setText("Войдите в систему\nдля просмотра заказов");
                emptyLabel.setStyle(
                        "-fx-font-size: 24px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #999999; " +
                                "-fx-padding: 60; " +
                                "-fx-text-alignment: center;"
                );
                emptyLabel.setVisible(true);
            }
            if (ordersListView != null) {
                ordersListView.setVisible(false);
            }
            return;
        }

        List<Order> orders = ordersService.getUserOrders(
                authService.getCurrentUser().getId(),
                authService.getAccessToken()
        );

        if (ordersListView != null) {
            ordersListView.getItems().clear();

            if (orders.isEmpty()) {
                if (emptyLabel != null) {
                    emptyLabel.setText("Заказов нет");
                    emptyLabel.setStyle(
                            "-fx-font-size: 24px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-text-fill: #999999; " +
                                    "-fx-padding: 60;"
                    );
                    emptyLabel.setVisible(true);
                }
                ordersListView.setVisible(false);
            } else {
                if (emptyLabel != null) {
                    emptyLabel.setVisible(false);
                }
                ordersListView.setVisible(true);
                ordersListView.getItems().addAll(orders);
            }
        }

        System.out.println("Загружено заказов: " + orders.size());
    }

    @FXML
    private void goBack() {
        System.out.println("← Возврат на главную");
        SceneNavigator.goToMain();
    }
}
