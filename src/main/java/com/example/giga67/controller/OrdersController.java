package com.example.giga67.controller;

import com.example.giga67.model.Order;
import com.example.giga67.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class OrdersController {

    @FXML
    private FlowPane ordersPane;

    private List<Order> orders = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("✅ OrdersController initialized!");
        loadOrders();
    }

    private void loadOrders() {
        if (ordersPane == null) {
            System.err.println("❌ ordersPane is null!");
            return;
        }

        ordersPane.getChildren().clear();

        if (orders.isEmpty()) {
            Label emptyLabel = new Label("У вас пока нет заказов");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666666;");
            ordersPane.getChildren().add(emptyLabel);
        } else {
            for (Order order : orders) {
                VBox orderCard = createOrderCard(order);
                ordersPane.getChildren().add(orderCard);
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-padding: 20;");
        card.setPrefWidth(350);

        Label orderNumberLabel = new Label("Заказ #" + order.getId());
        orderNumberLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label dateLabel = new Label("Дата: " + order.getCreatedAt());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        Label statusLabel = new Label("Статус: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px;");

        Label totalLabel = new Label("Сумма: " + order.getTotalAmount() + " ₽");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        card.getChildren().addAll(orderNumberLabel, dateLabel, statusLabel, totalLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 10; -fx-padding: 20; -fx-cursor: hand;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-padding: 20;");
        });

        return card;
    }

    @FXML
    private void goBack() {
        SceneNavigator.goToMain();
    }
}
