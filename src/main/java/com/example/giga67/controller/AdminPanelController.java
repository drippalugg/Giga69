package com.example.giga67.controller;

import com.example.giga67.model.Category;
import com.example.giga67.model.Order;
import com.example.giga67.model.Part;
import com.example.giga67.service.OrdersService;
import com.example.giga67.service.PartsService;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.service.SupabaseClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.http.HttpResponse;
import java.util.Optional;

public class AdminPanelController {

    private static final String BUCKET_NAME = "images";

    // Товары
    @FXML
    private Button addProductBtn;
    @FXML
    private Button editProductBtn;
    @FXML
    private Button deleteProductBtn;
    @FXML
    private Button refreshProductsBtn;
    @FXML
    private TableView<Part> productsTable;
    @FXML
    private Label productCountLabel;

    // Категории
    @FXML
    private Button addCategoryBtn;
    @FXML
    private Button editCategoryBtn;
    @FXML
    private Button deleteCategoryBtn;
    @FXML
    private Button refreshCategoriesBtn;
    @FXML
    private TableView<Category> categoriesTable;
    @FXML
    private Label categoryCountLabel;

    // Заказы
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> numberCol;
    @FXML
    private TableColumn<Order, String> userCol;
    @FXML
    private TableColumn<Order, Number> totalCol;
    @FXML
    private TableColumn<Order, String> statusCol;

    private OrdersService ordersService;
    private SupabaseAuthService authService;

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final Gson gson = new Gson();
    private final ObservableList<Part> productsList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    @FXML
    private ComboBox<Category> categoryComboBox;

    private PartsService partsService = new PartsService();
    @FXML
    public void initialize() {

        // Настройка таблиц и кнопок для товаров и категорий
        setupProductsTable();
        setupCategoriesTable();
        setupProductsTab();
        setupCategoriesTab();
        loadData();

        // Настройка таблицы заказов и загрузка заказов
        ordersService = new OrdersService();
        authService = SupabaseAuthService.getInstance();
        setupOrdersTable();
        loadOrders();

        // Заполнение выпадающего списка категорий для выбора при создании/редактировании товара
        if (categoryComboBox != null) {
            categoryComboBox.setItems(partsService.getCategories());
            categoryComboBox.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            categoryComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
        }
    }

    // -------------------- Заказы --------------------

    // Настройка колонок таблицы заказов и выпадающего списка статуса в каждой строчке
    private void setupOrdersTable() {
        if (ordersTable == null) return;

        numberCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getId())
        );
        userCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUserId())
        );
        totalCol.setCellValueFactory(
                data -> new SimpleDoubleProperty(data.getValue().getTotalPrice())
        );
        statusCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus())
        );

        statusCol.setCellFactory(col -> new TableCell<Order, String>() {
            private final ComboBox<String> combo = new ComboBox<>();

            {
                combo.getItems().addAll("new", "processing", "shipped", "done", "canceled");
                combo.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order == null) return;

                    String newStatus = combo.getValue();
                    boolean ok = ordersService.updateOrderStatus(
                            order.getId(),
                            newStatus,
                            authService.getAccessToken()
                    );
                    if (ok) {
                        order.setStatus(newStatus);
                    } else {
                        combo.setValue(order.getStatus());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    combo.setValue(order != null ? order.getStatus() : null);
                    setGraphic(combo);
                }
            }
        });
    }

    // Загрузка всех заказов из сервиса и привязка к таблице
    private void loadOrders() {
        String token = authService.getAccessToken();
        ObservableList<Order> orders = ordersService.getAllOrders(token); // ← вот он, ObservableList<Order>
        ordersTable.setItems(orders);
    }



    // Настройка таблиц
    @SuppressWarnings("unchecked")
    private void setupProductsTable() {
        TableColumn<Part, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Part, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Part, String> articleCol = new TableColumn<>("Артикул");
        articleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArticle()));
        articleCol.setPrefWidth(120);

        TableColumn<Part, String> brandCol = new TableColumn<>("Бренд");
        brandCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand()));
        brandCol.setPrefWidth(120);

        TableColumn<Part, Double> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(100);

        TableColumn<Part, Double> oldPriceCol = new TableColumn<>("Старая цена");
        oldPriceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getOldPrice()).asObject());
        oldPriceCol.setPrefWidth(100);

        TableColumn<Part, Integer> catCol = new TableColumn<>("Категория");
        catCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCategoryId()).asObject());
        catCol.setPrefWidth(80);

        TableColumn<Part, String> imageCol = new TableColumn<>("Изображение");
        imageCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getImageUrl() != null && !c.getValue().getImageUrl().isEmpty() ? "!" : "X"
        ));
        imageCol.setPrefWidth(100);

        productsTable.getColumns().setAll(idCol, nameCol, articleCol, brandCol, priceCol, oldPriceCol, catCol, imageCol);
        productsTable.setItems(productsList);
    }

    // Создает колонки и привязывает ObservableList товаров к таблице
    @SuppressWarnings("unchecked")
    private void setupCategoriesTable() {
        TableColumn<Category, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Category, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<Category, String> iconCol = new TableColumn<>("Иконка");
        iconCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIcon()));
        iconCol.setPrefWidth(80);

        categoriesTable.getColumns().setAll(idCol, nameCol, iconCol);
        categoriesTable.setItems(categoriesList);
    }

    // Вешает обработчики на внопки вкладки товаров
    private void setupProductsTab() {
        addProductBtn.setOnAction(e -> addProduct());
        editProductBtn.setOnAction(e -> editProduct());
        deleteProductBtn.setOnAction(e -> deleteProduct());
        refreshProductsBtn.setOnAction(e -> loadProducts());
    }

    // Обработчик добавления нового товара (+Supabase)
    private void addProduct() {
        Dialog<Part> dialog = createProductDialog("Добавить товар", null);
        Optional<Part> result = dialog.showAndWait();
        result.ifPresent(part -> {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("name", part.getName());
                json.addProperty("article", part.getArticle());
                json.addProperty("brand", part.getBrand());
                json.addProperty("price", part.getPrice());
                if (part.getOldPrice() > 0) {
                    json.addProperty("old_price", part.getOldPrice());
                }
                json.addProperty("category_id", part.getCategoryId());
                if (part.getDescription() != null && !part.getDescription().isEmpty()) {
                    json.addProperty("description", part.getDescription());
                }
                if (part.getImageUrl() != null && !part.getImageUrl().isEmpty()) {
                    json.addProperty("image_url", part.getImageUrl());
                }

                HttpResponse<String> response = client.post(
                        "/rest/v1/parts",
                        gson.toJson(json)
                );

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    showInfo("Успех", "Товар успешно добавлен!");
                    loadProducts();
                } else {
                    showError("Ошибка", "Не удалось добавить товар: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", "Ошибка при добавлении: " + ex.getMessage());
            }
        });
    }
    // Обработчик редактирования выбранного товара
    private void editProduct() {
        Part selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите товар из таблицы для редактирования.");
            return;
        }

        Dialog<Part> dialog = createProductDialog("Редактировать товар", selected);
        Optional<Part> result = dialog.showAndWait();

        result.ifPresent(part -> {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("name", part.getName());
                json.addProperty("article", part.getArticle());
                json.addProperty("brand", part.getBrand());
                json.addProperty("price", part.getPrice());
                json.addProperty("old_price", part.getOldPrice());
                json.addProperty("category_id", part.getCategoryId());
                json.addProperty("description", part.getDescription() != null ? part.getDescription() : "");
                json.addProperty("image_url", part.getImageUrl() != null ? part.getImageUrl() : "");

                HttpResponse<String> response = client.patch(
                        "/rest/v1/parts?id=eq." + selected.getId(),
                        gson.toJson(json)
                );

                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    showInfo("Успех", "Товар успешно обновлён!");
                    loadProducts();
                } else {
                    showError("Ошибка", "Не удалось обновить товар: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", "Ошибка при обновлении: " + ex.getMessage());
            }
        });
    }

    // Обработчик удаления выбранного товара
    private void deleteProduct() {
        Part selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите товар из таблицы для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление товара");
        confirm.setHeaderText("Удалить товар: " + selected.getName() + "?");
        confirm.setContentText("Это действие нельзя отменить.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (selected.getImageUrl() != null && !selected.getImageUrl().isEmpty()) {
                        deleteImageFromStorage(selected.getImageUrl());
                    }
                    HttpResponse<String> response = client.delete(
                            "/rest/v1/parts?id=eq." + selected.getId()
                    );

                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        showInfo("Успех", "Товар удалён!");
                        loadProducts();
                    } else {
                        showError("Ошибка", "Не удалось удалить товар: " + response.body());
                    }
                } catch (Exception ex) {
                    showError("Ошибка", "Ошибка при удалении: " + ex.getMessage());
                }
            }
        });
    }

    // Загрузка товаров
    private void loadProducts() {
        new Thread(() -> {
            try {
                HttpResponse<String> response = client.get("/rest/v1/parts?select=*&order=id.asc");
                if (response.statusCode() == 200) {
                    JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                    ObservableList<Part> loaded = FXCollections.observableArrayList();

                    for (var el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        double oldPrice = 0.0;
                        if (obj.has("old_price") && !obj.get("old_price").isJsonNull()) {
                            oldPrice = obj.get("old_price").getAsDouble();
                        }
                        Part part = new Part(
                                obj.get("id").getAsInt(),
                                obj.get("name").getAsString(),
                                obj.get("article").getAsString(),
                                obj.get("brand").getAsString(),
                                obj.get("price").getAsDouble(),
                                oldPrice,
                                obj.get("category_id").getAsInt()
                        );

                        if (obj.has("description") && !obj.get("description").isJsonNull()) {
                            part.setDescription(obj.get("description").getAsString());
                        }
                        if (obj.has("image_url") && !obj.get("image_url").isJsonNull()) {
                            part.setImageUrl(obj.get("image_url").getAsString());
                        }

                        loaded.add(part);
                    }

                    Platform.runLater(() -> {
                        productsList.setAll(loaded);
                        productCountLabel.setText("Товаров: " + loaded.size());
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка", "Не удалось загрузить товары: " + e.getMessage()));
            }
        }).start();
    }

    // Вешает обработчики на кнопки вкладки категорий
    private void setupCategoriesTab() {
        addCategoryBtn.setOnAction(e -> addCategory());
        editCategoryBtn.setOnAction(e -> editCategory());
        deleteCategoryBtn.setOnAction(e -> deleteCategory());
        refreshCategoriesBtn.setOnAction(e -> loadCategories());
    }

    // Добавление новой категории через диалог и post в Supa
    private void addCategory() {
        Dialog<Category> dialog = createCategoryDialog("Добавить категорию", null);
        Optional<Category> result = dialog.showAndWait();

        result.ifPresent(cat -> {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("name", cat.getName());
                json.addProperty("icon", cat.getIcon());

                HttpResponse<String> response = client.post("/rest/v1/categories", gson.toJson(json));
                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    showInfo("Успех", "Категория добавлена!");
                    loadCategories();
                } else {
                    showError("Ошибка", "Не удалось добавить категорию: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", ex.getMessage());
            }
        });
    }

    // Редактирование выбранной категории
    private void editCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите категорию из таблицы.");
            return;
        }

        Dialog<Category> dialog = createCategoryDialog("Редактировать категорию", selected);
        Optional<Category> result = dialog.showAndWait();

        result.ifPresent(cat -> {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("name", cat.getName());
                json.addProperty("icon", cat.getIcon());

                HttpResponse<String> response = client.patch(
                        "/rest/v1/categories?id=eq." + selected.getId(),
                        gson.toJson(json)
                );

                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    showInfo("Успех", "Категория обновлена!");
                    loadCategories();
                } else {
                    showError("Ошибка", "Не удалось обновить: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", ex.getMessage());
            }
        });
    }

    // Удаление выбранной категории
    private void deleteCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите категорию из таблицы.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление категории");
        confirm.setHeaderText("Удалить категорию: " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    HttpResponse<String> response = client.delete(
                            "/rest/v1/categories?id=eq." + selected.getId()
                    );
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        showInfo("Успех", "Категория удалена!");
                        loadCategories();
                    } else {
                        showError("Ошибка", response.body());
                    }
                } catch (Exception ex) {
                    showError("Ошибка", ex.getMessage());
                }
            }
        });
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                HttpResponse<String> response = client.get("/rest/v1/categories?select=*&order=id.asc");
                if (response.statusCode() == 200) {
                    JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                    ObservableList<Category> loaded = FXCollections.observableArrayList();

                    for (var el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        loaded.add(new Category(
                                obj.get("id").getAsInt(),
                                obj.get("name").getAsString(),
                                obj.get("icon").getAsString()
                        ));
                    }
                    Platform.runLater(() -> {
                        categoriesList.setAll(loaded);
                        categoryCountLabel.setText("Категорий: " + loaded.size());
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка", "Не удалось загрузить категории: " + e.getMessage()));
            }
        }).start();
    }

    private String uploadImageToSupabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение товара");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        Stage stage = (Stage) productsTable.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return null;
        }

        try {
            // Генерация уникального имя файла типа timestamp_originalname.ext
            String originalName = selectedFile.getName();
            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String uniqueName = "part_" + System.currentTimeMillis() + extension;
            String storagePath = "parts/" + uniqueName;

            HttpResponse<String> response = client.uploadFile(
                    BUCKET_NAME,
                    storagePath,
                    selectedFile.toPath()
            );

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String publicUrl = client.getPublicUrl(BUCKET_NAME, storagePath);
                return publicUrl;
            } else {
                showError("Ошибка загрузки", "Статус: " + response.statusCode() + "\n" + response.body());
                return null;
            }
        } catch (Exception e) {
            showError("Ошибка загрузки", "Не удалось загрузить изображение: " + e.getMessage());
            return null;
        }
    }

    private void deleteImageFromStorage(String imageUrl) {
        try {
            // Извлекаем путь файла из публичного URL
            String marker = "/storage/v1/object/public/" + BUCKET_NAME + "/";
            int idx = imageUrl.indexOf(marker);
            if (idx >= 0) {
                String filePath = imageUrl.substring(idx + marker.length());
                client.deleteFile(BUCKET_NAME, filePath);
            }
        } catch (Exception e) {
        }
    }

    // Диалоги
    private Dialog<Part> createProductDialog(String title, Part existing) {
        Dialog<Part> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setResizable(true);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Название товара");
        TextField articleField = new TextField();
        articleField.setPromptText("Артикул");
        TextField brandField = new TextField();
        brandField.setPromptText("Бренд");
        TextField priceField = new TextField();
        priceField.setPromptText("Цена");
        TextField oldPriceField = new TextField();
        oldPriceField.setPromptText("Старая цена (0 если нет)");
        ComboBox<Category> categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Категория");
        categoryComboBox.setItems(partsService.getCategories());
        categoryComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Описание товара");
        descriptionArea.setPrefRowCount(3);

        // Блок изображения
        Label imageUrlLabel = new Label("Нет изображения");
        imageUrlLabel.setWrapText(true);
        imageUrlLabel.setMaxWidth(300);

        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(150);
        imagePreview.setFitHeight(150);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

        // Скрытое поле для хранения URL
        TextField imageUrlHidden = new TextField();
        imageUrlHidden.setVisible(false);
        imageUrlHidden.setManaged(false);

        Button uploadImageBtn = new Button("Загрузить изображение");
        uploadImageBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

        Button removeImageBtn = new Button("Удалить изображение");
        removeImageBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        removeImageBtn.setVisible(false);

        uploadImageBtn.setOnAction(e -> {
            String url = uploadImageToSupabase();
            if (url != null) {
                imageUrlHidden.setText(url);
                imageUrlLabel.setText("Изображение загружено");
                removeImageBtn.setVisible(true);
                try {
                    Image img = new Image(url, 150, 150, true, true);
                    imagePreview.setImage(img);
                } catch (Exception ex) {
                    // Превью недоступно, но URL сохранён
                }
            }
        });

        removeImageBtn.setOnAction(e -> {
            String currentUrl = imageUrlHidden.getText();
            if (currentUrl != null && !currentUrl.isEmpty()) {
                deleteImageFromStorage(currentUrl);
            }
            imageUrlHidden.setText("");
            imageUrlLabel.setText("Нет изображения");
            imagePreview.setImage(null);
            removeImageBtn.setVisible(false);
        });

        // Заполнение поля при редактировании
        if (existing != null) {
            nameField.setText(existing.getName());
            articleField.setText(existing.getArticle());
            brandField.setText(existing.getBrand());
            priceField.setText(String.valueOf(existing.getPrice()));
            oldPriceField.setText(String.valueOf(existing.getOldPrice()));
            Category currentCat = partsService.getCategories().stream()
                    .filter(c -> c.getId() == existing.getCategoryId())
                    .findFirst()
                    .orElse(null);
            categoryComboBox.setValue(currentCat);
            descriptionArea.setText(existing.getDescription() != null ? existing.getDescription() : "");

            if (existing.getImageUrl() != null && !existing.getImageUrl().isEmpty()) {
                imageUrlHidden.setText(existing.getImageUrl());
                imageUrlLabel.setText("Есть изображение");
                removeImageBtn.setVisible(true);
                try {
                    Image img = new Image(existing.getImageUrl(), 150, 150, true, true);
                    imagePreview.setImage(img);
                } catch (Exception ex) {
                    imageUrlLabel.setText("URL: " + existing.getImageUrl());
                }
            }
        }

        // Размещение элементов в сетке
        int row = 0;
        grid.add(new Label("Название:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Артикул:"), 0, row);
        grid.add(articleField, 1, row++);
        grid.add(new Label("Бренд:"), 0, row);
        grid.add(brandField, 1, row++);
        grid.add(new Label("Цена:"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Старая цена:"), 0, row);
        grid.add(oldPriceField, 1, row++);
        grid.add(new Label("Категория:"), 0, row);
        grid.add(categoryComboBox, 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row++);


        // Секция "Изображения"
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("Изображение:"), 0, row);

        VBox imageBox = new VBox(8);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        HBox imageBtns = new HBox(8, uploadImageBtn, removeImageBtn);
        imageBox.getChildren().addAll(imagePreview, imageUrlLabel, imageBtns);
        grid.add(imageBox, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(650);

        // Конвертер результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String article = articleField.getText().trim();
                    String brand = brandField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    double oldPrice = oldPriceField.getText().trim().isEmpty() ? 0 :
                            Double.parseDouble(oldPriceField.getText().trim());
                    Category selected = categoryComboBox.getValue();
                    if (selected == null) {
                        return null;
                    }
                    int categoryId = selected.getId();

                    if (name.isEmpty() || article.isEmpty() || brand.isEmpty()) {
                        showError("Ошибка", "Заполните все обязательные поля.");
                        return null;
                    }

                    Part part = new Part(
                            existing != null ? existing.getId() : 0,
                            name, article, brand, price, oldPrice, categoryId
                    );
                    part.setDescription(descriptionArea.getText().trim());
                    part.setImageUrl(imageUrlHidden.getText().trim());

                    return part;
                } catch (NumberFormatException e) {
                    showError("Ошибка", "Проверьте числовые поля (цена, старая цена, ID категории).");
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    private Dialog<Category> createCategoryDialog(String title, Category existing) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Название категории");
        TextField iconField = new TextField();
        iconField.setPromptText("Иконка (emoji)");

        if (existing != null) {
            nameField.setText(existing.getName());
            iconField.setText(existing.getIcon());
        }

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Иконка:"), 0, 1);
        grid.add(iconField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String icon = iconField.getText().trim();
                if (name.isEmpty()) {
                    showError("Ошибка", "Введите название категории.");
                    return null;
                }
                return new Category(
                        existing != null ? existing.getId() : 0,
                        name,
                        icon.isEmpty() ? "📦" : icon
                );
            }
            return null;
        });

        return dialog;
    }

    // -------------------- Вспомогательные методы --------------------

    // Загрузка товаров и категорий из SB в ObservableList
    private void loadData() {
        loadProducts();
        loadCategories();
    }

    // Универсальный диалог информации
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Универсальный диалог ошибки
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}