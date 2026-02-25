package com.example.giga67.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class AdminSceneNavigator {
    private static Stage adminStage;

    public static void goToAdminPanel() {
        try {
            String fxmlResource = "/com/example/giga67/view/admin-panel.fxml";

            URL resource = AdminSceneNavigator.class.getResource(fxmlResource);

            if (resource == null) {
                resource = Thread.currentThread().getContextClassLoader()
                        .getResource("com/example/giga67/view/admin-panel.fxml");

                if (resource == null) {
                    return;
                }
            }


            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1400, 950);

            if (adminStage == null) {
                adminStage = new Stage();
                adminStage.setTitle("MasterParts - Панель администратора");
            }

            adminStage.setScene(scene);
            adminStage.show();
            adminStage.toFront();


        } catch (IOException e) {
            System.out.println("❌ IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void closeAdminPanel() {
        if (adminStage != null) {
            adminStage.close();
        }
    }
}
