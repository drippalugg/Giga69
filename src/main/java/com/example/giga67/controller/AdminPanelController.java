package com.example.giga67.controller;

import com.example.giga67.model.Category;
import com.example.giga67.model.Order;
import com.example.giga67.model.Part;
import com.example.giga67.model.Store;
import com.example.giga67.model.StoreInventory;
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

    private static final String PARTS_BUCKET = "parts";
    private static final String STORES_BUCKET = "stores";

    // Товары
    @FXML private Button addProductBtn;
    @FXML private Button editProductBtn;
    @FXML private Button deleteProductBtn;
    @FXML private Button refreshProductsBtn;
    @FXML private TableView<Part> productsTable;
    @FXML private Label productCountLabel;

    // Категории
    @FXML private Button addCategoryBtn;
    @FXML private Button editCategoryBtn;
    @FXML private Button deleteCategoryBtn;
    @FXML private Button refreshCategoriesBtn;
    @FXML private TableView<Category> categoriesTable;
    @FXML private Label categoryCountLabel;

    // Заказы
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> numberCol;
    @FXML private TableColumn<Order, String> userCol;
    @FXML private TableColumn<Order, Number> totalCol;
    @FXML private TableColumn<Order, String> statusCol;

    // Магазины
    @FXML private Button addStoreBtn;
    @FXML private Button editStoreBtn;
    @FXML private Button deleteStoreBtn;
    @FXML private Button refreshStoresBtn;
    @FXML private TableView<Store> storesTable;
    @FXML private Label storeCountLabel;

    // Склад (цены магазинов)
    @FXML private Label inventoryTitle;
    @FXML private Button addInventoryBtn;
    @FXML private Button editInventoryBtn;
    @FXML private Button deleteInventoryBtn;
    @FXML private Button refreshInventoryBtn;
    @FXML private TableView<StoreInventory> inventoryTable;

    @FXML private ComboBox<Category> categoryComboBox;

    private OrdersService ordersService;
    private SupabaseAuthService authService;

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final Gson gson = new Gson();
    private final ObservableList<Part> productsList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<Store> storesList = FXCollections.observableArrayList();
    private final ObservableList<StoreInventory> inventoryList = FXCollections.observableArrayList();
    private Store selectedStore = null;

    private PartsService partsService = new PartsService();

    @FXML
    public void initialize() {
        ordersService = new OrdersService();
        authService = SupabaseAuthService.getInstance();

        // Товары и категории
        setupProductsTable();
        setupCategoriesTable();
        setupProductsTab();
        setupCategoriesTab();
        loadData();

        // Заказы
        setupOrdersTable();
        loadOrders();

        // Магазины
        setupStoresTable();
        setupInventoryTable();
        setupStoresTab();
        loadStores();

        // Выпадающий список категорий
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

    // ==================== Заказы ====================

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

    private void loadOrders() {
        String token = authService.getAccessToken();
        ObservableList<Order> orders = ordersService.getAllOrders(token);
        ordersTable.setItems(orders);
    }

    // ==================== Товары ====================

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
                c.getValue().getImageUrl() != null && !c.getValue().getImageUrl().isEmpty() ? "+" : "-"
        ));
        imageCol.setPrefWidth(100);

        productsTable.getColumns().setAll(idCol, nameCol, articleCol, brandCol, priceCol, oldPriceCol, catCol, imageCol);
        productsTable.setItems(productsList);
    }

    private void setupProductsTab() {
        addProductBtn.setOnAction(e -> addProduct());
        editProductBtn.setOnAction(e -> editProduct());
        deleteProductBtn.setOnAction(e -> deleteProduct());
        refreshProductsBtn.setOnAction(e -> loadProducts());
    }

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
                    showInfo("Успех", "Товар успешно обновлен!");
                    loadProducts();
                } else {
                    showError("Ошибка", "Не удалось обновить товар: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", "Ошибка при обновлении: " + ex.getMessage());
            }
        });
    }

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
                        showInfo("Успех", "Товар удален!");
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

    // ==================== Категории ====================

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

    private void setupCategoriesTab() {
        addCategoryBtn.setOnAction(e -> addCategory());
        editCategoryBtn.setOnAction(e -> editCategory());
        deleteCategoryBtn.setOnAction(e -> deleteCategory());
        refreshCategoriesBtn.setOnAction(e -> loadCategories());
    }

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

    // ==================== Магазины ====================

    @SuppressWarnings("unchecked")
    private void setupStoresTable() {
        if (storesTable == null) return;

        TableColumn<Store, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Store, String> cityCol = new TableColumn<>("Город");
        cityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCity()));
        cityCol.setPrefWidth(150);

        TableColumn<Store, String> addrCol = new TableColumn<>("Адрес");
        addrCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAddress()));
        addrCol.setPrefWidth(250);

        TableColumn<Store, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        phoneCol.setPrefWidth(150);

        storesTable.getColumns().setAll(nameCol, cityCol, addrCol, phoneCol);
        storesTable.setItems(storesList);

        storesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedStore = newVal;
            if (newVal != null) {
                if (inventoryTitle != null) inventoryTitle.setText("Цены: " + newVal.getName());
                loadInventoryForStore(newVal.getId());
            } else {
                if (inventoryTitle != null) inventoryTitle.setText("Выберите магазин для управления ценами");
                inventoryList.clear();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setupInventoryTable() {
        if (inventoryTable == null) return;

        TableColumn<StoreInventory, String> partNameCol = new TableColumn<>("Товар");
        partNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartName()));
        partNameCol.setPrefWidth(200);

        TableColumn<StoreInventory, String> partArticleCol = new TableColumn<>("Артикул");
        partArticleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartArticle()));
        partArticleCol.setPrefWidth(120);

        TableColumn<StoreInventory, Number> masterPriceCol = new TableColumn<>("Цена MasterParts");
        masterPriceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPartOriginalPrice()));
        masterPriceCol.setPrefWidth(140);

        TableColumn<StoreInventory, Number> storePriceCol = new TableColumn<>("Цена в магазине");
        storePriceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()));
        storePriceCol.setPrefWidth(130);

        TableColumn<StoreInventory, Integer> stockCol = new TableColumn<>("Кол-во");
        stockCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStockQuantity()).asObject());
        stockCol.setPrefWidth(80);

        TableColumn<StoreInventory, String> inStockCol = new TableColumn<>("В наличии");
        inStockCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isInStock() ? "Да" : "Нет"));
        inStockCol.setPrefWidth(80);

        inventoryTable.getColumns().setAll(partNameCol, partArticleCol, masterPriceCol, storePriceCol, stockCol, inStockCol);
        inventoryTable.setItems(inventoryList);
    }

    private void setupStoresTab() {
        if (addStoreBtn != null) addStoreBtn.setOnAction(e -> addStore());
        if (editStoreBtn != null) editStoreBtn.setOnAction(e -> editStore());
        if (deleteStoreBtn != null) deleteStoreBtn.setOnAction(e -> deleteStore());
        if (refreshStoresBtn != null) refreshStoresBtn.setOnAction(e -> loadStores());
        if (addInventoryBtn != null) addInventoryBtn.setOnAction(e -> addInventory());
        if (editInventoryBtn != null) editInventoryBtn.setOnAction(e -> editInventory());
        if (deleteInventoryBtn != null) deleteInventoryBtn.setOnAction(e -> deleteInventory());
        if (refreshInventoryBtn != null) refreshInventoryBtn.setOnAction(e -> {
            if (selectedStore != null) loadInventoryForStore(selectedStore.getId());
        });
    }

    private void loadStores() {
        String token = authService.getAccessToken();
        new Thread(() -> {
            try {
                HttpResponse<String> response = client.get("/rest/v1/stores?select=*&order=name.asc", token);
                if (response.statusCode() == 200) {
                    JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                    ObservableList<Store> loaded = FXCollections.observableArrayList();
                    for (var el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        Store store = new Store(
                            obj.get("id").getAsString(),
                            obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "",
                            obj.has("address") && !obj.get("address").isJsonNull() ? obj.get("address").getAsString() : "",
                            obj.has("city") && !obj.get("city").isJsonNull() ? obj.get("city").getAsString() : "",
                            obj.has("phone") && !obj.get("phone").isJsonNull() ? obj.get("phone").getAsString() : ""
                        );
                        if (obj.has("logo_url") && !obj.get("logo_url").isJsonNull()) {
                            store.setLogoUrl(obj.get("logo_url").getAsString());
                        }
                        loaded.add(store);
                    }
                    Platform.runLater(() -> {
                        storesList.setAll(loaded);
                        if (storeCountLabel != null) storeCountLabel.setText("Магазинов: " + loaded.size());
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка", "Не удалось загрузить магазины: " + e.getMessage()));
            }
        }).start();
    }

    private void addStore() {
        Dialog<Store> dialog = createStoreDialog("Добавить магазин", null);
        Optional<Store> result = dialog.showAndWait();
        result.ifPresent(store -> {
            String token = authService.getAccessToken();
            try {
                JsonObject json = new JsonObject();
                json.addProperty("name", store.getName());
                json.addProperty("address", store.getAddress());
                json.addProperty("city", store.getCity());
                json.addProperty("phone", store.getPhone());
                if (store.getLogoUrl() != null && !store.getLogoUrl().isEmpty()) {
                    json.addProperty("logo_url", store.getLogoUrl());
                }

                HttpResponse<String> response = client.post("/rest/v1/stores", gson.toJson(json), token);
                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    showInfo("Успех", "Магазин добавлен!");
                    loadStores();
                } else {
                    showError("Ошибка", "Не удалось добавить магазин: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", ex.getMessage());
            }
        });
    }

    private void editStore() {
        Store selected = storesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите магазин из таблицы.");
            return;
        }
        Dialog<Store> dialog = createStoreDialog("Редактировать магазин", selected);
        Optional<Store> result = dialog.showAndWait();
        result.ifPresent(store -> {
            String token = authService.getAccessToken();
            try {
                JsonObject json = new JsonObject();
                json.addProperty("name", store.getName());
                json.addProperty("address", store.getAddress());
                json.addProperty("city", store.getCity());
                json.addProperty("phone", store.getPhone());
                json.addProperty("logo_url", store.getLogoUrl() != null ? store.getLogoUrl() : "");

                HttpResponse<String> response = client.patch(
                    "/rest/v1/stores?id=eq." + selected.getId(), gson.toJson(json), token
                );
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    showInfo("Успех", "Магазин обновлен!");
                    loadStores();
                } else {
                    showError("Ошибка", "Не удалось обновить: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", ex.getMessage());
            }
        });
    }

    private void deleteStore() {
        Store selected = storesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите магазин из таблицы.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление магазина");
        confirm.setHeaderText("Удалить магазин: " + selected.getName() + "?");
        confirm.setContentText("Все цены этого магазина также будут удалены.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String token = authService.getAccessToken();
                try {
                    HttpResponse<String> response = client.delete(
                        "/rest/v1/stores?id=eq." + selected.getId(), token
                    );
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        showInfo("Успех", "Магазин удален!");
                        loadStores();
                        inventoryList.clear();
                        if (inventoryTitle != null) inventoryTitle.setText("Выберите магазин для управления ценами");
                    } else {
                        showError("Ошибка", response.body());
                    }
                } catch (Exception ex) {
                    showError("Ошибка", ex.getMessage());
                }
            }
        });
    }

    private void loadInventoryForStore(String storeId) {
        String token = authService.getAccessToken();
        new Thread(() -> {
            try {
                HttpResponse<String> response = client.get(
                    "/rest/v1/store_inventory?select=*,parts(name,article,price)&store_id=eq." + storeId + "&order=part_id.asc",
                    token
                );
                if (response.statusCode() == 200) {
                    JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                    ObservableList<StoreInventory> loaded = FXCollections.observableArrayList();
                    for (var el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        StoreInventory si = new StoreInventory(
                            obj.get("id").getAsString(),
                            obj.get("store_id").getAsString(),
                            "",
                            obj.get("part_id").getAsInt(),
                            obj.get("price").getAsDouble(),
                            obj.get("stock_quantity").getAsInt(),
                            obj.get("in_stock").getAsBoolean()
                        );
                        if (obj.has("parts") && !obj.get("parts").isJsonNull()) {
                            JsonObject partObj = obj.getAsJsonObject("parts");
                            if (partObj.has("name") && !partObj.get("name").isJsonNull())
                                si.setPartName(partObj.get("name").getAsString());
                            if (partObj.has("article") && !partObj.get("article").isJsonNull())
                                si.setPartArticle(partObj.get("article").getAsString());
                            if (partObj.has("price") && !partObj.get("price").isJsonNull())
                                si.setPartOriginalPrice(partObj.get("price").getAsDouble());
                        }
                        loaded.add(si);
                    }
                    Platform.runLater(() -> inventoryList.setAll(loaded));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка", "Не удалось загрузить цены: " + e.getMessage()));
            }
        }).start();
    }

    private void addInventory() {
        if (selectedStore == null) {
            showInfo("Внимание", "Сначала выберите магазин из таблицы сверху.");
            return;
        }
        Dialog<StoreInventory> dialog = createInventoryDialog("Добавить товар в магазин", null);
        Optional<StoreInventory> result = dialog.showAndWait();
        result.ifPresent(si -> {
            String token = authService.getAccessToken();
            try {
                JsonObject json = new JsonObject();
                json.addProperty("store_id", selectedStore.getId());
                json.addProperty("part_id", si.getPartId());
                json.addProperty("price", si.getPrice());
                json.addProperty("stock_quantity", si.getStockQuantity());
                json.addProperty("in_stock", si.isInStock());

                HttpResponse<String> response = client.post("/rest/v1/store_inventory", gson.toJson(json), token);
                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    showInfo("Успех", "Товар добавлен в магазин!");
                    loadInventoryForStore(selectedStore.getId());
                } else {
                    showError("Ошибка", "Не удалось добавить: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", ex.getMessage());
            }
        });
    }

    private void editInventory() {
        if (selectedStore == null) {
            showInfo("Внимание", "Сначала выберите магазин.");
            return;
        }
        StoreInventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите товар из таблицы цен.");
            return;
        }
        Dialog<StoreInventory> dialog = createInventoryDialog("Редактировать цену", selected);
        Optional<StoreInventory> result = dialog.showAndWait();
        result.ifPresent(si -> {
            String token = authService.getAccessToken();
            try {
                JsonObject json = new JsonObject();
                json.addProperty("price", si.getPrice());
                json.addProperty("stock_quantity", si.getStockQuantity());
                json.addProperty("in_stock", si.isInStock());

                HttpResponse<String> response = client.patch(
                    "/rest/v1/store_inventory?id=eq." + selected.getId(), gson.toJson(json), token
                );
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    showInfo("Успех", "Цена обновлена!");
                    loadInventoryForStore(selectedStore.getId());
                } else {
                    showError("Ошибка", "Не удалось обновить: " + response.body());
                }
            } catch (Exception ex) {
                showError("Ошибка", ex.getMessage());
            }
        });
    }

    private void deleteInventory() {
        if (selectedStore == null) return;
        StoreInventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Внимание", "Выберите товар из таблицы цен.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление");
        confirm.setHeaderText("Удалить товар из магазина?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String token = authService.getAccessToken();
                try {
                    HttpResponse<String> response = client.delete(
                        "/rest/v1/store_inventory?id=eq." + selected.getId(), token
                    );
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        showInfo("Успех", "Удалено!");
                        loadInventoryForStore(selectedStore.getId());
                    } else {
                        showError("Ошибка", response.body());
                    }
                } catch (Exception ex) {
                    showError("Ошибка", ex.getMessage());
                }
            }
        });
    }

    // ==================== Загрузка изображений ====================

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
            String originalName = selectedFile.getName();
            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String uniqueName = "part_" + System.currentTimeMillis() + extension;

            String token = authService.getAccessToken();
            HttpResponse<String> response = client.uploadFile(
                    PARTS_BUCKET,
                    uniqueName,
                    selectedFile.toPath(),
                    token
            );

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return client.getPublicUrl(PARTS_BUCKET, uniqueName);
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
            String token = authService.getAccessToken();
            String partsMarker = "/storage/v1/object/public/" + PARTS_BUCKET + "/";
            String storesMarker = "/storage/v1/object/public/" + STORES_BUCKET + "/";

            if (imageUrl.contains(partsMarker)) {
                String filePath = imageUrl.substring(imageUrl.indexOf(partsMarker) + partsMarker.length());
                client.deleteFile(PARTS_BUCKET, filePath, token);
            } else if (imageUrl.contains(storesMarker)) {
                String filePath = imageUrl.substring(imageUrl.indexOf(storesMarker) + storesMarker.length());
                client.deleteFile(STORES_BUCKET, filePath, token);
            }
        } catch (Exception e) {
            // игнорируем ошибку удаления файла из хранилища
        }
    }

    // ==================== Диалоги ====================

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
                    // Превью недоступно, но URL сохранен
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

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("Изображение:"), 0, row);

        VBox imageBox = new VBox(8);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        HBox imageBtns = new HBox(8, uploadImageBtn, removeImageBtn);
        imageBox.getChildren().addAll(imagePreview, imageUrlLabel, imageBtns);
        grid.add(imageBox, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(650);

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
                    showError("Ошибка", "Проверьте числовые поля (цена, старая цена).");
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
                        icon.isEmpty() ? "\uD83D\uDCE6" : icon
                );
            }
            return null;
        });

        return dialog;
    }

    private Dialog<Store> createStoreDialog(String title, Store existing) {
        Dialog<Store> dialog = new Dialog<>();
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
        nameField.setPromptText("Название магазина");
        TextField cityField = new TextField();
        cityField.setPromptText("Город");
        TextField addressField = new TextField();
        addressField.setPromptText("Адрес");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Телефон");

        // Логотип
        TextField logoUrlHidden = new TextField();
        logoUrlHidden.setVisible(false);
        logoUrlHidden.setManaged(false);

        ImageView logoPreview = new ImageView();
        logoPreview.setFitWidth(80);
        logoPreview.setFitHeight(80);
        logoPreview.setPreserveRatio(true);

        Label logoStatus = new Label("Нет логотипа");
        logoStatus.setStyle("-fx-text-fill: #999;");

        Button uploadLogoBtn = new Button("Загрузить логотип");
        uploadLogoBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

        Button removeLogoBtn = new Button("Удалить");
        removeLogoBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        removeLogoBtn.setVisible(false);

        uploadLogoBtn.setOnAction(e -> {
            String url = uploadStoreLogoToSupabase();
            if (url != null) {
                logoUrlHidden.setText(url);
                logoStatus.setText("Логотип загружен");
                removeLogoBtn.setVisible(true);
                try { logoPreview.setImage(new Image(url, 80, 80, true, true)); }
                catch (Exception ignored) {}
            }
        });

        removeLogoBtn.setOnAction(e -> {
            String currentUrl = logoUrlHidden.getText();
            if (currentUrl != null && !currentUrl.isEmpty()) {
                deleteImageFromStorage(currentUrl);
            }
            logoUrlHidden.setText("");
            logoPreview.setImage(null);
            logoStatus.setText("Нет логотипа");
            removeLogoBtn.setVisible(false);
        });

        if (existing != null) {
            nameField.setText(existing.getName());
            cityField.setText(existing.getCity());
            addressField.setText(existing.getAddress());
            phoneField.setText(existing.getPhone());
            if (existing.getLogoUrl() != null && !existing.getLogoUrl().isEmpty()) {
                logoUrlHidden.setText(existing.getLogoUrl());
                logoStatus.setText("Есть логотип");
                removeLogoBtn.setVisible(true);
                try { logoPreview.setImage(new Image(existing.getLogoUrl(), 80, 80, true, true)); }
                catch (Exception ignored) {}
            }
        }

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Город:"), 0, 1);
        grid.add(cityField, 1, 1);
        grid.add(new Label("Адрес:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Телефон:"), 0, 3);
        grid.add(phoneField, 1, 3);

        grid.add(new Separator(), 0, 4, 2, 1);
        grid.add(new Label("Логотип:"), 0, 5);

        VBox logoBox = new VBox(8);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        HBox logoBtns = new HBox(8, uploadLogoBtn, removeLogoBtn);
        logoBox.getChildren().addAll(logoPreview, logoStatus, logoBtns);
        grid.add(logoBox, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Ошибка", "Введите название магазина.");
                    return null;
                }
                Store store = new Store(
                    existing != null ? existing.getId() : "",
                    name,
                    addressField.getText().trim(),
                    cityField.getText().trim(),
                    phoneField.getText().trim()
                );
                store.setLogoUrl(logoUrlHidden.getText().trim());
                return store;
            }
            return null;
        });

        return dialog;
    }

    private String uploadStoreLogoToSupabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите логотип магазина");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        Stage stage = (Stage) storesTable.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) return null;

        try {
            String originalName = selectedFile.getName();
            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".png";
            String uniqueName = "store_" + System.currentTimeMillis() + extension;
            String token = authService.getAccessToken();

            HttpResponse<String> response = client.uploadFile(STORES_BUCKET, uniqueName, selectedFile.toPath(), token);
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return client.getPublicUrl(STORES_BUCKET, uniqueName);
            } else {
                showError("Ошибка загрузки", "Статус: " + response.statusCode() + "\n" + response.body());
                return null;
            }
        } catch (Exception e) {
            showError("Ошибка загрузки", "Не удалось загрузить логотип: " + e.getMessage());
            return null;
        }
    }

    private Dialog<StoreInventory> createInventoryDialog(String title, StoreInventory existing) {
        Dialog<StoreInventory> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Part> partCombo = new ComboBox<>();
        partCombo.setItems(productsList);
        partCombo.setPromptText("Выберите товар");
        partCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Part item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getArticle() + ")");
            }
        });
        partCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Part item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getArticle() + ")");
            }
        });

        TextField priceField = new TextField();
        priceField.setPromptText("Цена в этом магазине");
        TextField stockField = new TextField();
        stockField.setPromptText("Количество на складе");
        CheckBox inStockCheck = new CheckBox("В наличии");
        inStockCheck.setSelected(true);

        if (existing != null) {
            Part existingPart = productsList.stream()
                .filter(p -> p.getId() == existing.getPartId())
                .findFirst().orElse(null);
            partCombo.setValue(existingPart);
            partCombo.setDisable(true);
            priceField.setText(String.valueOf(existing.getPrice()));
            stockField.setText(String.valueOf(existing.getStockQuantity()));
            inStockCheck.setSelected(existing.isInStock());
        }

        grid.add(new Label("Товар:"), 0, 0);
        grid.add(partCombo, 1, 0);
        grid.add(new Label("Цена:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Количество:"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(inStockCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(450);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Part selectedPart = partCombo.getValue();
                if (selectedPart == null) {
                    showError("Ошибка", "Выберите товар.");
                    return null;
                }
                try {
                    double price = Double.parseDouble(priceField.getText().trim());
                    int stock = stockField.getText().trim().isEmpty() ? 0
                            : Integer.parseInt(stockField.getText().trim());
                    return new StoreInventory(
                        existing != null ? existing.getId() : "",
                        selectedStore.getId(),
                        selectedStore.getName(),
                        selectedPart.getId(),
                        price,
                        stock,
                        inStockCheck.isSelected()
                    );
                } catch (NumberFormatException e) {
                    showError("Ошибка", "Проверьте числовые поля (цена, количество).");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    // ==================== Вспомогательные методы ====================

    private void loadData() {
        loadProducts();
        loadCategories();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
