package com.example.giga67.controller;

import com.example.giga67.model.Part;
import com.example.giga67.model.Review;
import com.example.giga67.model.StoreInventory;
import com.example.giga67.service.CartManager;
import com.example.giga67.service.ReviewService;
import com.example.giga67.service.StoreService;
import com.example.giga67.service.SupabaseAuthService;
import com.example.giga67.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ProductController {
    @FXML private Label nameLabel;
    @FXML private Label brandLabel;
    @FXML private Label articleLabel;
    @FXML private Label priceLabel;
    @FXML private Label oldPriceLabel;
    @FXML private Label discountLabel;
    @FXML private Label descriptionLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button favoriteButton;
    @FXML private ImageView productImageView;
    @FXML private Label specificationsLabel;
    @FXML private HBox ratingBox;
    @FXML private VBox storesContainer;
    @FXML private VBox reviewsContainer;

    private Part currentPart;
    private CartManager cartManager;
    private ReviewService reviewService;
    private StoreService storeService;
    private SupabaseAuthService authService;

    // Звёзды в форме отзыва
    private final Label[] starLabels = new Label[5];
    private int selectedRating = 0;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        reviewService = ReviewService.getInstance();
        storeService = StoreService.getInstance();
        authService = SupabaseAuthService.getInstance();

        if (quantitySpinner != null) {
            quantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1)
            );
        }
    }

    public void setProduct(Part part) {
        this.currentPart = part;
        displayProduct();
        loadStoresAsync();
        loadReviewsAsync();
    }

    // ──────────────────────────────────────────────────────────────────
    // Отображение основной информации
    // ──────────────────────────────────────────────────────────────────

    private void displayProduct() {
        if (currentPart == null) return;

        if (nameLabel != null) nameLabel.setText(currentPart.getName());
        if (brandLabel != null) brandLabel.setText("Бренд: " + currentPart.getBrand());
        if (articleLabel != null) articleLabel.setText("Артикул: " + currentPart.getArticle());
        if (priceLabel != null) priceLabel.setText(String.format("%.0f ₽", currentPart.getPrice()));

        if (currentPart.hasDiscount()) {
            if (oldPriceLabel != null) {
                oldPriceLabel.setText(String.format("%.0f ₽", currentPart.getOldPrice()));
                oldPriceLabel.setVisible(true);
            }
            if (discountLabel != null) {
                discountLabel.setText("-" + currentPart.getDiscountPercent() + "%");
                discountLabel.setVisible(true);
            }
        } else {
            if (oldPriceLabel != null) oldPriceLabel.setVisible(false);
            if (discountLabel != null) discountLabel.setVisible(false);
        }

        if (descriptionLabel != null) {
            descriptionLabel.setText(
                currentPart.getDescription() != null ? currentPart.getDescription() : "Описание товара"
            );
        }

        if (productImageView != null) {
            String imageUrl = currentPart.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try { productImageView.setImage(new Image(imageUrl, true)); }
                catch (Exception ignored) {}
            }
        }

        if (specificationsLabel != null) {
            String specs = currentPart.getSpecifications();
            if (specs != null && !specs.isEmpty()) {
                specificationsLabel.setText(specs);
                specificationsLabel.setVisible(true);
                specificationsLabel.setManaged(true);
            } else {
                specificationsLabel.setVisible(false);
                specificationsLabel.setManaged(false);
            }
        }

        updateRatingBox();
        updateFavoriteButton();
    }

    private void updateRatingBox() {
        if (ratingBox == null || currentPart == null) return;
        ratingBox.getChildren().clear();

        int count = currentPart.getReviewsCount();
        if (count > 0) {
            double avg = currentPart.getRatingAvg();
            Label starsLabel = new Label(currentPart.getStars());
            starsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #F4A83A;");

            Label avgLabel = new Label(String.format("%.1f", avg));
            avgLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            Label countLabel = new Label("(" + count + " " + reviewWord(count) + ")");
            countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #777777;");

            ratingBox.getChildren().addAll(starsLabel, avgLabel, countLabel);
        } else {
            Label noRating = new Label("Нет отзывов");
            noRating.setStyle("-fx-font-size: 14px; -fx-text-fill: #AAAAAA;");
            ratingBox.getChildren().add(noRating);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Асинхронная загрузка магазинов и отзывов
    // ──────────────────────────────────────────────────────────────────

    private void loadStoresAsync() {
        if (currentPart == null) return;
        new Thread(() -> {
            List<StoreInventory> stores = storeService.getStoreInventoryForPart(currentPart.getId());
            Platform.runLater(() -> buildStoresUI(stores));
        }).start();
    }

    private void loadReviewsAsync() {
        if (currentPart == null) return;
        new Thread(() -> {
            List<Review> reviews = reviewService.getReviewsForPart(currentPart.getId());
            Platform.runLater(() -> buildReviewsUI(reviews));
        }).start();
    }

    // ──────────────────────────────────────────────────────────────────
    // Построение UI магазинов
    // ──────────────────────────────────────────────────────────────────

    private void buildStoresUI(List<StoreInventory> stores) {
        if (storesContainer == null) return;
        storesContainer.getChildren().clear();

        if (stores.isEmpty()) return;

        Label title = new Label("Купить в магазинах");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        VBox.setMargin(title, new Insets(0, 0, 12, 0));
        storesContainer.getChildren().add(title);

        for (StoreInventory si : stores) {
            storesContainer.getChildren().add(buildStoreCard(si));
        }
    }

    private HBox buildStoreCard(StoreInventory si) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);"
        );
        VBox.setMargin(card, new Insets(0, 0, 8, 0));

        // Логотип или иконка магазина
        javafx.scene.Node storeIcon;
        String logoUrl = si.getStoreLogoUrl();
        if (logoUrl != null && !logoUrl.isEmpty()) {
            ImageView logoView = new ImageView();
            logoView.setFitWidth(40);
            logoView.setFitHeight(40);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            Rectangle clip = new Rectangle(40, 40);
            clip.setArcWidth(8);
            clip.setArcHeight(8);
            logoView.setClip(clip);
            try {
                logoView.setImage(new Image(logoUrl, 40, 40, true, true, true));
            } catch (Exception ignored) {}
            storeIcon = logoView;
        } else {
            Label icon = new Label("\uD83C\uDFEA");
            icon.setStyle("-fx-font-size: 24px;");
            storeIcon = icon;
        }

        // Название + адрес
        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label nameL = new Label(si.getStoreName());
        nameL.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111;");
        nameBox.getChildren().add(nameL);

        String fullAddr = si.getFullAddress();
        if (!fullAddr.isEmpty()) {
            Label addrL = new Label(fullAddr);
            addrL.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            nameBox.getChildren().add(addrL);
        }

        if (si.getStorePhone() != null && !si.getStorePhone().isEmpty()) {
            Label phoneL = new Label(si.getStorePhone());
            phoneL.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            nameBox.getChildren().add(phoneL);
        }

        // Цена
        Label priceL = new Label(String.format("%.0f ₽", si.getPrice()));
        priceL.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #667EEA;");

        // Наличие
        boolean inStock = si.isInStock() && si.getStockQuantity() > 0;
        Label stockL = new Label(si.getStockText());
        stockL.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " +
                (inStock ? "#27AE60" : "#EF5350") + ";");

        card.getChildren().addAll(storeIcon, nameBox, priceL, stockL);
        return card;
    }

    // ──────────────────────────────────────────────────────────────────
    // Построение UI отзывов
    // ──────────────────────────────────────────────────────────────────

    private void buildReviewsUI(List<Review> reviews) {
        if (reviewsContainer == null) return;
        reviewsContainer.getChildren().clear();

        Label title = new Label("Отзывы");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        VBox.setMargin(title, new Insets(0, 0, 12, 0));
        reviewsContainer.getChildren().add(title);

        if (authService.isLoggedIn()) {
            String userId = authService.getCurrentUser().getId();
            Review userReview = reviews.stream()
                    .filter(r -> r.getUserId().equals(userId))
                    .findFirst().orElse(null);
            reviewsContainer.getChildren().add(buildReviewForm(userReview));
        } else {
            Label loginHint = new Label("Войдите в аккаунт, чтобы оставить отзыв");
            loginHint.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999;");
            VBox.setMargin(loginHint, new Insets(0, 0, 16, 0));
            reviewsContainer.getChildren().add(loginHint);
        }

        if (reviews.isEmpty()) {
            Label empty = new Label("Пока нет отзывов. Будьте первым!");
            empty.setStyle("-fx-font-size: 15px; -fx-text-fill: #AAAAAA;");
            VBox.setMargin(empty, new Insets(8, 0, 0, 0));
            reviewsContainer.getChildren().add(empty);
        } else {
            for (Review review : reviews) {
                reviewsContainer.getChildren().add(buildReviewCard(review));
            }
        }
    }

    private VBox buildReviewForm(Review existingReview) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(16));
        form.setStyle(
            "-fx-background-color: #F8F9FE;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #667EEA;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 12;"
        );
        VBox.setMargin(form, new Insets(0, 0, 16, 0));

        Label titleL = new Label(existingReview == null ? "Написать отзыв" : "Ваш отзыв");
        titleL.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Выбор звёзд
        int initialRating = existingReview != null ? existingReview.getRating() : 0;
        selectedRating = initialRating;

        HBox starsRow = new HBox(4);
        starsRow.setAlignment(Pos.CENTER_LEFT);
        Label starsHint = new Label("Оценка: ");
        starsHint.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        starsRow.getChildren().add(starsHint);

        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < initialRating ? "★" : "☆");
            star.setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                    (i < initialRating ? "#F4A83A" : "#CCCCCC") + "; -fx-cursor: hand;");
            starLabels[i] = star;
            final int idx = i + 1;
            star.setOnMouseEntered(e -> updateStarHover(idx));
            star.setOnMouseExited(e -> updateStarDisplay(selectedRating));
            star.setOnMouseClicked(e -> {
                selectedRating = idx;
                updateStarDisplay(selectedRating);
            });
            starsRow.getChildren().add(star);
        }

        // Поле комментария
        TextArea commentArea = new TextArea(existingReview != null ? existingReview.getComment() : "");
        commentArea.setPromptText("Ваш комментарий (необязательно)...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        commentArea.setStyle(
            "-fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8;" +
            "-fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-background-color: white;"
        );

        // Кнопки
        HBox buttonsRow = new HBox(10);
        buttonsRow.setAlignment(Pos.CENTER_RIGHT);

        if (existingReview != null) {
            Button deleteBtn = new Button("Удалить");
            deleteBtn.setStyle(
                "-fx-background-color: #FFE5E5; -fx-text-fill: #FF4757; -fx-font-size: 14px;" +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;"
            );
            String reviewId = existingReview.getId();
            deleteBtn.setOnAction(e -> {
                String token = authService.getAccessToken();
                new Thread(() -> {
                    boolean ok = reviewService.deleteReview(reviewId, token);
                    if (ok) Platform.runLater(this::loadReviewsAsync);
                }).start();
            });
            buttonsRow.getChildren().add(deleteBtn);
        }

        Button submitBtn = new Button(existingReview == null ? "Отправить" : "Сохранить");
        submitBtn.setStyle(
            "-fx-background-color: #667EEA; -fx-text-fill: white; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;"
        );

        if (existingReview == null) {
            submitBtn.setOnAction(e -> {
                if (selectedRating == 0) {
                    titleL.setText("Сначала выберите оценку!");
                    titleL.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF4757;");
                    return;
                }
                String token = authService.getAccessToken();
                String userId = authService.getCurrentUser().getId();
                String comment = commentArea.getText().trim();
                int rating = selectedRating;
                new Thread(() -> {
                    boolean ok = reviewService.addReview(currentPart.getId(), userId, rating, comment, token);
                    if (ok) Platform.runLater(this::loadReviewsAsync);
                }).start();
            });
        } else {
            String reviewId = existingReview.getId();
            submitBtn.setOnAction(e -> {
                if (selectedRating == 0) return;
                String token = authService.getAccessToken();
                String comment = commentArea.getText().trim();
                int rating = selectedRating;
                new Thread(() -> {
                    boolean ok = reviewService.updateReview(reviewId, rating, comment, token);
                    if (ok) Platform.runLater(this::loadReviewsAsync);
                }).start();
            });
        }

        buttonsRow.getChildren().add(submitBtn);
        form.getChildren().addAll(titleL, starsRow, commentArea, buttonsRow);
        return form;
    }

    private VBox buildReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);"
        );
        VBox.setMargin(card, new Insets(0, 0, 8, 0));

        // Шапка: имя + звёзды + дата
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userName = new Label(review.getUserName());
        userName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222;");
        HBox.setHgrow(userName, Priority.ALWAYS);

        Label stars = new Label(review.getStars());
        stars.setStyle("-fx-font-size: 15px; -fx-text-fill: #F4A83A;");

        Label date = new Label(review.getFormattedDate());
        date.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAAAAA;");

        header.getChildren().addAll(userName, stars, date);

        if (review.isEdited()) {
            Label edited = new Label("(изм.)");
            edited.setStyle("-fx-font-size: 11px; -fx-text-fill: #BBBBBB;");
            header.getChildren().add(edited);
        }

        card.getChildren().add(header);

        if (review.getComment() != null && !review.getComment().isEmpty()) {
            Label comment = new Label(review.getComment());
            comment.setWrapText(true);
            comment.setStyle("-fx-font-size: 14px; -fx-text-fill: #444;");
            card.getChildren().add(comment);
        }

        return card;
    }

    // ──────────────────────────────────────────────────────────────────
    // Работа со звёздами
    // ──────────────────────────────────────────────────────────────────

    private void updateStarHover(int hoverIndex) {
        for (int i = 0; i < 5; i++) {
            if (starLabels[i] == null) continue;
            boolean filled = i < hoverIndex;
            starLabels[i].setText(filled ? "★" : "☆");
            starLabels[i].setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                    (filled ? "#F4A83A" : "#CCCCCC") + "; -fx-cursor: hand;");
        }
    }

    private void updateStarDisplay(int rating) {
        for (int i = 0; i < 5; i++) {
            if (starLabels[i] == null) continue;
            boolean filled = i < rating;
            starLabels[i].setText(filled ? "★" : "☆");
            starLabels[i].setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                    (filled ? "#F4A83A" : "#CCCCCC") + "; -fx-cursor: hand;");
        }
    }

    // Вспомогательное
    private String reviewWord(int count) {
        if (count % 100 >= 11 && count % 100 <= 19) return "отзывов";
        return switch (count % 10) {
            case 1 -> "отзыв";
            case 2, 3, 4 -> "отзыва";
            default -> "отзывов";
        };
    }

    private void updateFavoriteButton() {
        if (favoriteButton != null && currentPart != null) {
            if (cartManager.isFavorite(currentPart)) {
                favoriteButton.setText("💖 В избранном");
            } else {
                favoriteButton.setText("💖 В избранное");
            }
        }
    }

    @FXML
    private void addToCart() {
        if (currentPart == null) return;
        int quantity = quantitySpinner != null ? quantitySpinner.getValue() : 1;
        cartManager.addToCart(currentPart, quantity);
        SceneNavigator.goToCart();
    }

    @FXML
    private void toggleFavorite() {
        if (currentPart == null) return;
        if (cartManager.isFavorite(currentPart)) {
            cartManager.removeFromFavorites(currentPart);
        } else {
            cartManager.addToFavorites(currentPart);
        }
        updateFavoriteButton();
    }

    @FXML
    private void goBack() {
        SceneNavigator.goToMain();
    }
}
