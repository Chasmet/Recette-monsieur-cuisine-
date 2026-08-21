package com.chasmet.recettemonsieurcuisine;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public final String assetPath;
    public final String id;
    public final String title;
    public final String country;
    public final String countryLabel;
    public final String flag;
    public final String category;
    public final String categoryLabel;
    public final int servings;
    public final int prepMinutes;
    public final int cookMinutes;
    public final String difficulty;
    public final String description;
    public final String imagePath;
    public final List<Ingredient> ingredients;
    public final List<Step> steps;

    public Recipe(String assetPath, JSONObject json) {
        this.assetPath = assetPath;
        id = json.optString("id");
        title = json.optString("title");
        country = json.optString("country");
        countryLabel = json.optString("countryLabel");
        flag = json.optString("flag");
        category = json.optString("category");
        categoryLabel = json.optString("categoryLabel");
        servings = json.optInt("servings", 4);
        prepMinutes = json.optInt("prepMinutes", 10);
        cookMinutes = json.optInt("cookMinutes", 20);
        difficulty = json.optString("difficulty", "Facile");
        description = json.optString("description");
        imagePath = assetPath.substring(0, assetPath.lastIndexOf('/') + 1) + json.optString("image", "image.svg");

        ingredients = new ArrayList<>();
        JSONArray ingredientArray = json.optJSONArray("ingredients");
        if (ingredientArray != null) {
            for (int i = 0; i < ingredientArray.length(); i++) {
                JSONObject item = ingredientArray.optJSONObject(i);
                if (item != null) {
                    ingredients.add(new Ingredient(item.optString("amount"), item.optString("name")));
                }
            }
        }

        steps = new ArrayList<>();
        JSONArray stepArray = json.optJSONArray("steps");
        if (stepArray != null) {
            for (int i = 0; i < stepArray.length(); i++) {
                JSONObject item = stepArray.optJSONObject(i);
                if (item != null) {
                    steps.add(new Step(
                            item.optString("title", "Étape " + (i + 1)),
                            item.optString("instruction"),
                            item.optInt("durationSeconds", 0),
                            item.optInt("temperatureC", 0),
                            item.optString("speed", ""),
                            item.optBoolean("reverse", false),
                            item.optBoolean("turbo", false),
                            item.optString("note", "")
                    ));
                }
            }
        }
    }

    public static class Ingredient {
        public final String amount;
        public final String name;

        public Ingredient(String amount, String name) {
            this.amount = amount;
            this.name = name;
        }

        public String display() {
            if (amount == null || amount.trim().isEmpty()) return name;
            return amount + " • " + name;
        }
    }

    public static class Step {
        public final String title;
        public final String instruction;
        public final int durationSeconds;
        public final int temperatureC;
        public final String speed;
        public final boolean reverse;
        public final boolean turbo;
        public final String note;

        public Step(String title, String instruction, int durationSeconds, int temperatureC,
                    String speed, boolean reverse, boolean turbo, String note) {
            this.title = title;
            this.instruction = instruction;
            this.durationSeconds = durationSeconds;
            this.temperatureC = temperatureC;
            this.speed = speed;
            this.reverse = reverse;
            this.turbo = turbo;
            this.note = note;
        }
    }
}
