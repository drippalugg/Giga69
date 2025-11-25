package com.example.giga67;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            primaryStage.setTitle("MasterParts - Энгельс");
            primaryStage.setWidth(1280);
            primaryStage.setHeight(920);

            // Загружаем главное окно (НЕ login!)
            String fxmlPath = "/com/example/giga67/view/main.fxml";
            System.out.println("🔍 Загрузка main.fxml: " + fxmlPath);

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("❌ FXML файл НЕ НАЙДЕН: " + fxmlPath);
                throw new Exception("FXML файл не найден: " + fxmlPath);
            }

            System.out.println("✅ FXML найден: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Путь к CSS - ИСПРАВЛЕН
            String cssPath = "/com/example/giga67/css/styles.css";
            System.out.println("🔍 Поиск CSS: " + cssPath);

            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                System.out.println("✅ CSS найден: " + cssUrl);
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("⚠️ CSS не найден (необязательно): " + cssPath);
            }

            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Приложение запущено!");

        } catch (Exception e) {
            System.err.println("❌ ОШИБКА при запуске:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }


}
