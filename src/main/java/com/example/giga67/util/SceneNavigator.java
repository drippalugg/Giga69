package com.example.giga67.util;

import com.example.giga67.MainApp;
import com.example.giga67.controller.CategoryController;
import com.example.giga67.controller.ProductController;
import com.example.giga67.model.Category;
import com.example.giga67.model.Part;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneNavigator {

    public static void goToMain() {
        loadScene("/com/example/giga67/view/main.fxml", "MasterParts - Энгельс", 1280, 800);
    }

    public static void goToCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/com/example/giga67/view/category.fxml")
            );
            Parent root = loader.load();

            CategoryController controller = loader.getController();
            controller.setCategory(category);

            Scene scene = new Scene(root);
            addCSS(scene);

            MainApp.getPrimaryStage().setScene(scene);
            MainApp.getPrimaryStage().setTitle("MasterParts - " + category.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToSearch(String query) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/com/example/giga67/view/category.fxml")
            );
            Parent root = loader.load();

            CategoryController controller = loader.getController();
            controller.setSearchQuery(query);

            Scene scene = new Scene(root);
            addCSS(scene);

            MainApp.getPrimaryStage().setScene(scene);
            MainApp.getPrimaryStage().setTitle("MasterParts - Поиск: " + query);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToProduct(Part part) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/com/example/giga67/view/product.fxml")
            );
            Parent root = loader.load();

            ProductController controller = loader.getController();
            controller.setProduct(part);

            Scene scene = new Scene(root);
            addCSS(scene);

            MainApp.getPrimaryStage().setScene(scene);
            MainApp.getPrimaryStage().setTitle("MasterParts - " + part.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToCart() {
        loadScene("/com/example/giga67/view/cart.fxml", "MasterParts - Корзина", 1280, 800);
    }

    public static void goToLogin() {
        loadScene("/com/example/giga67/view/login.fxml", "MasterParts - Вход", 600, 700);
    }

    public static void goToProfile() {
        loadScene("/com/example/giga67/view/profile.fxml", "MasterParts - Профиль", 800, 600);
    }

    public static void goToOrders() {
        loadScene("/com/example/giga67/view/orders.fxml", "MasterParts - Заказы", 1280, 800);
    }

    public static void goToFavorites() {
        loadScene("/com/example/giga67/view/favorites.fxml", "MasterParts - Избранное", 1280, 800);
    }

    private static void loadScene(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            addCSS(scene);

            MainApp.getPrimaryStage().setScene(scene);
            MainApp.getPrimaryStage().setTitle(title);
            MainApp.getPrimaryStage().setWidth(width);
            MainApp.getPrimaryStage().setHeight(height);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCSS(Scene scene) {
        var cssUrl = SceneNavigator.class.getResource("/com/example/giga67/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }
}
