package com.chasmet.recettemonsieurcuisine;

import java.text.Normalizer;
import java.util.Locale;

public final class PortionMode {
    public static final String STANDARD = "standard";
    public static final String MEAT_180 = "meat180";
    public static final int MEAT_TOTAL_GRAMS = 180;

    private PortionMode() {}

    public static boolean isMeatIngredient(String name) {
        String n = normalize(name);
        if (n.contains("bouillon") || n.contains("fond de") || n.contains("sauce") || n.contains("arome") || n.contains("epice")) return false;
        String[] keys = {"boeuf","poulet","dinde","veau","porc","agneau","mouton","canard","viande","steak","jambon","saucisse","merguez","lapin","pintade"};
        for (String k : keys) if (n.contains(normalize(k))) return true;
        return false;
    }

    public static boolean isMeatRecipe(Recipe recipe) {
        if (isMeatIngredient(recipe.title)) return true;
        for (Recipe.Ingredient ingredient : recipe.ingredients) if (isMeatIngredient(ingredient.name)) return true;
        return false;
    }

    public static String displayAmount(Recipe recipe, Recipe.Ingredient ingredient, String mode) {
        if (MEAT_180.equals(mode) && isMeatIngredient(ingredient.name)) return MEAT_TOTAL_GRAMS + " g total";
        return ingredient.amount == null ? "" : ingredient.amount;
    }

    public static String displayIngredient(Recipe recipe, Recipe.Ingredient ingredient, String mode) {
        String amount = displayAmount(recipe, ingredient, mode);
        return amount == null || amount.trim().isEmpty() ? ingredient.name : amount + " • " + ingredient.name;
    }

    private static String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
