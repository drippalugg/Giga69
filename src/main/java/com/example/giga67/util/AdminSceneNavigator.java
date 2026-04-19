package com.example.giga67.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
                adminStage.setTitle("MasterParts — Администратор");
                URL iconUrl = AdminSceneNavigator.class.getResource("/com/example/giga67/logo/MasterPartsADMIN.png");
                if (iconUrl != null) {
                    adminStage.getIcons().add(new Image(iconUrl.toExternalForm()));
                }
            }
            adminStage.setScene(scene);
            adminStage.show();
            adminStage.toFront();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
