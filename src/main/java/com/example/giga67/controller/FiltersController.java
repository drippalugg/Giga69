package com.example.giga67.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FiltersController {

    @FXML private TextField articleField;
    @FXML private TextField brandField;
    @FXML private TextField priceMinField;
    @FXML private TextField priceMaxField;
    @FXML private CheckBox discountOnlyCheck;

    private FilterData result = null;

    public record FilterData(
            String article,
            String brand,
            Double priceMin,
            Double priceMax,
            boolean discountOnly
    ) {}

    public FilterData getResult() {
        return result;
    }

    @FXML
    private void handleApply() {
        String article = safeTrim(articleField.getText());
        String brand = safeTrim(brandField.getText());

        Double priceMin = parseDoubleOrNull(priceMinField.getText());
        Double priceMax = parseDoubleOrNull(priceMaxField.getText());

        result = new FilterData(
                article.isEmpty() ? null : article,
                brand.isEmpty() ? null : brand,
                priceMin,
                priceMax,
                discountOnlyCheck.isSelected()
        );

        close();
    }

    @FXML
    private void handleReset() {
        articleField.clear();
        brandField.clear();
        priceMinField.clear();
        priceMaxField.clear();
        discountOnlyCheck.setSelected(false);
        result = new FilterData(null, null, null, null, false);
        close();
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private Double parseDoubleOrNull(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void close() {
        Stage stage = (Stage) articleField.getScene().getWindow();
        stage.close();
    }
}
