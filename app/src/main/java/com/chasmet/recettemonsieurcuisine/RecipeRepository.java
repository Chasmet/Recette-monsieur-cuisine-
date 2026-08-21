package com.chasmet.recettemonsieurcuisine;

import android.content.Context;
import android.content.res.AssetManager;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class RecipeRepository {
    private RecipeRepository() {}

    public static List<Recipe> loadAll(Context context) {
        List<Recipe> recipes = new ArrayList<>();
        try {
            walk(context.getAssets(), "recipes", recipes);
        } catch (Exception ignored) {
        }
        Collections.sort(recipes, new Comparator<Recipe>() {
            @Override public int compare(Recipe a, Recipe b) {
                int byCountry = a.countryLabel.compareToIgnoreCase(b.countryLabel);
                if (byCountry != 0) return byCountry;
                int byCategory = a.categoryLabel.compareToIgnoreCase(b.categoryLabel);
                if (byCategory != 0) return byCategory;
                return a.title.compareToIgnoreCase(b.title);
            }
        });
        return recipes;
    }

    private static void walk(AssetManager assets, String path, List<Recipe> output) throws Exception {
        String[] entries = assets.list(path);
        if (entries == null) return;
        for (String entry : entries) {
            String child = path + "/" + entry;
            if ("recipe.json".equals(entry)) {
                output.add(load(assets, child));
            } else {
                String[] nested = assets.list(child);
                if (nested != null && nested.length > 0) {
                    walk(assets, child, output);
                }
            }
        }
    }

    public static Recipe load(Context context, String assetPath) throws Exception {
        return load(context.getAssets(), assetPath);
    }

    private static Recipe load(AssetManager assets, String assetPath) throws Exception {
        InputStream input = assets.open(assetPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) text.append(line);
        reader.close();
        return new Recipe(assetPath, new JSONObject(text.toString()));
    }
}
