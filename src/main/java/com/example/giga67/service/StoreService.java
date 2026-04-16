package com.example.giga67.service;

import com.example.giga67.model.StoreInventory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class StoreService {
    private static StoreService instance;
    private final SupabaseClient client;
    private final Gson gson;

    private StoreService() {
        this.client = SupabaseClient.getInstance();
        this.gson = new Gson();
    }

    public static synchronized StoreService getInstance() {
        if (instance == null) {
            instance = new StoreService();
        }
        return instance;
    }

    public List<StoreInventory> getStoreInventoryForPart(int partId) {
        List<StoreInventory> result = new ArrayList<>();
        try {
            HttpResponse<String> response = client.get(
                "/rest/v1/store_inventory?select=*,stores(name,address,city,phone,logo_url)&part_id=eq." + partId + "&order=price.asc"
            );
            if (response.statusCode() == 200) {
                JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                for (var element : arr) {
                    JsonObject obj = element.getAsJsonObject();
                    String id = obj.get("id").getAsString();
                    String storeId = obj.get("store_id").getAsString();
                    int pId = obj.get("part_id").getAsInt();
                    double price = obj.get("price").getAsDouble();
                    int stockQty = obj.get("stock_quantity").getAsInt();
                    boolean inStock = obj.get("in_stock").getAsBoolean();

                    String storeName = "Магазин";
                    String storeAddress = "";
                    String storeCity = "";
                    String storePhone = "";
                    String storeLogoUrl = "";

                    if (obj.has("stores") && !obj.get("stores").isJsonNull()) {
                        JsonObject storeObj = obj.getAsJsonObject("stores");
                        if (storeObj.has("name") && !storeObj.get("name").isJsonNull()) {
                            storeName = storeObj.get("name").getAsString();
                        }
                        if (storeObj.has("address") && !storeObj.get("address").isJsonNull()) {
                            storeAddress = storeObj.get("address").getAsString();
                        }
                        if (storeObj.has("city") && !storeObj.get("city").isJsonNull()) {
                            storeCity = storeObj.get("city").getAsString();
                        }
                        if (storeObj.has("phone") && !storeObj.get("phone").isJsonNull()) {
                            storePhone = storeObj.get("phone").getAsString();
                        }
                        if (storeObj.has("logo_url") && !storeObj.get("logo_url").isJsonNull()) {
                            storeLogoUrl = storeObj.get("logo_url").getAsString();
                        }
                    }

                    StoreInventory si = new StoreInventory(id, storeId, storeName, pId, price, stockQty, inStock);
                    si.setStoreAddress(storeAddress);
                    si.setStoreCity(storeCity);
                    si.setStorePhone(storePhone);
                    si.setStoreLogoUrl(storeLogoUrl);
                    result.add(si);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
