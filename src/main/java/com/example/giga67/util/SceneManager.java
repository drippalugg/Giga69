package com.example.giga67.util;

import com.example.giga67.MainApp;
import com.example.giga67.controller.CategoryController;
import com.example.giga67.model.Category;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneManager {

    public static void goToMain() {
        loadScene("/com/example/giga67/view/main.fxml", "MasterParts", 1280, 800);
    }

    public static void goToCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/com/example/giga67/view/category.fxml")
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

    private static void loadScene(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
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
        var cssUrl = SceneManager.class.getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }
}
