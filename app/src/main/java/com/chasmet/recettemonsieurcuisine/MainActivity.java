package com.chasmet.recettemonsieurcuisine;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private final List<Recipe> allRecipes = new ArrayList<>();
    private LinearLayout countryFilters;
    private LinearLayout categoryFilters;
    private LinearLayout recipeContainer;
    private TextView resultCount;
    private EditText search;
    private String selectedCountry = "all";
    private String selectedCategory = "all";
    private boolean favoritesOnly = false;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("recipe_prefs", MODE_PRIVATE);

        countryFilters = findViewById(R.id.countryFilters);
        categoryFilters = findViewById(R.id.categoryFilters);
        recipeContainer = findViewById(R.id.recipeContainer);
        resultCount = findViewById(R.id.resultCount);
        search = findViewById(R.id.search);

        allRecipes.addAll(RecipeRepository.loadAll(this));
        buildCountryFilters();
        buildCategoryFilters();

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderRecipes(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        renderRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recipeContainer != null) renderRecipes();
    }

    private void buildCountryFilters() {
        countryFilters.removeAllViews();
        addCountryFilter("all", "🌍 Toutes");

        Map<String, String> countries = new LinkedHashMap<>();
        for (Recipe recipe : allRecipes) {
            countries.put(recipe.country, recipe.flag + " " + recipe.countryLabel);
        }
        for (Map.Entry<String, String> entry : countries.entrySet()) {
            addCountryFilter(entry.getKey(), entry.getValue());
        }

        Button fav = Ui.filterButton(this, "★ Favoris");
        fav.setOnClickListener(v -> {
            favoritesOnly = !favoritesOnly;
            Ui.setFilterSelected(fav, favoritesOnly);
            renderRecipes();
        });
        countryFilters.addView(fav);
    }

    private void addCountryFilter(final String key, String label) {
        Button button = Ui.filterButton(this, label);
        button.setTag(key);
        button.setOnClickListener(v -> {
            selectedCountry = key;
            refreshSelectedCountryButtons();
            renderRecipes();
        });
        countryFilters.addView(button);
        if ("all".equals(key)) Ui.setFilterSelected(button, true);
    }

    private void refreshSelectedCountryButtons() {
        for (int i = 0; i < countryFilters.getChildCount(); i++) {
            View child = countryFilters.getChildAt(i);
            if (child instanceof Button && child.getTag() != null) {
                Ui.setFilterSelected((Button) child, selectedCountry.equals(child.getTag().toString()));
            }
        }
    }

    private void buildCategoryFilters() {
        categoryFilters.removeAllViews();
        addCategoryFilter("all", "Tout");
        addCategoryFilter("plats", "🍲 Plats");
        addCategoryFilter("entrees", "🥗 Entrées");
        addCategoryFilter("desserts", "🍰 Desserts");
    }

    private void addCategoryFilter(final String key, String label) {
        Button button = Ui.filterButton(this, label);
        button.setTag(key);
        button.setOnClickListener(v -> {
            selectedCategory = key;
            for (int i = 0; i < categoryFilters.getChildCount(); i++) {
                View child = categoryFilters.getChildAt(i);
                if (child instanceof Button) {
                    Ui.setFilterSelected((Button) child, selectedCategory.equals(child.getTag()));
                }
            }
            renderRecipes();
        });
        categoryFilters.addView(button);
        if ("all".equals(key)) Ui.setFilterSelected(button, true);
    }

    private void renderRecipes() {
        recipeContainer.removeAllViews();
        String query = normalize(search.getText() == null ? "" : search.getText().toString());

        int shown = 0;
        for (Recipe recipe : allRecipes) {
            if (!"all".equals(selectedCountry) && !selectedCountry.equals(recipe.country)) continue;
            if (!"all".equals(selectedCategory) && !selectedCategory.equals(recipe.category)) continue;
            if (favoritesOnly && !prefs.getBoolean("fav_" + recipe.id, false)) continue;

            String searchable = normalize(recipe.title + " " + recipe.countryLabel + " " +
                    recipe.categoryLabel + " " + recipe.description);
            if (!query.isEmpty() && !searchable.contains(query)) continue;

            addRecipeCard(recipe);
            shown++;
        }

        resultCount.setText(shown + (shown > 1 ? " recettes" : " recette"));
        if (shown == 0) {
            TextView empty = Ui.text(this,
                    "Aucune recette avec ces filtres. Essaie un autre pays ou une autre catégorie.",
                    16, false);
            Ui.margin(empty, 4, 20, 4, 20);
            recipeContainer.addView(empty);
        }
    }

    private void addRecipeCard(final Recipe recipe) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, Ui.dp(this, 12));
        card.setLayoutParams(cardParams);

        TextView overline = Ui.text(this, recipe.flag + " " + recipe.countryLabel + "  •  " +
                recipe.categoryLabel, 13, true);
        overline.setTextColor(Color.rgb(201, 71, 45));
        card.addView(overline);

        TextView name = Ui.text(this, recipe.title, 22, true);
        Ui.margin(name, 0, 7, 0, 0);
        card.addView(name);

        String meta = "⏱ " + (recipe.prepMinutes + recipe.cookMinutes) + " min   •   " +
                "👥 " + recipe.servings + "   •   " + recipe.difficulty;
        TextView metaView = Ui.text(this, meta, 14, false);
        metaView.setTextColor(Color.rgb(107, 98, 90));
        Ui.margin(metaView, 0, 8, 0, 0);
        card.addView(metaView);

        TextView desc = Ui.text(this, recipe.description, 15, false);
        Ui.margin(desc, 0, 10, 0, 0);
        card.addView(desc);

        if (prefs.getInt("progress_" + recipe.id, 0) > 0) {
            TextView resume = Ui.text(this, "▶ Recette commencée — progression enregistrée", 13, true);
            resume.setTextColor(Color.rgb(46, 125, 50));
            Ui.margin(resume, 0, 10, 0, 0);
            card.addView(resume);
        }

        card.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
            intent.putExtra("assetPath", recipe.assetPath);
            startActivity(intent);
        });
        recipeContainer.addView(card);
    }

    private static String normalize(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
