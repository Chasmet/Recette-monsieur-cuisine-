package com.chasmet.recettemonsieurcuisine;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RecipeActivity extends Activity {
    private Recipe recipe;
    private List<Recipe.Ingredient> displayIngredients;
    private SharedPreferences prefs;
    private Button favoriteButton, startButton;
    private String portionMode = PortionMode.STANDARD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        prefs = getSharedPreferences("recipe_prefs", MODE_PRIVATE);

        String assetPath = getIntent().getStringExtra("assetPath");
        portionMode = getIntent().getStringExtra("portionMode");
        if (portionMode == null) portionMode = PortionMode.STANDARD;
        if (assetPath == null) {
            finish();
            return;
        }

        try {
            recipe = RecipeRepository.load(this, assetPath);
        } catch (Exception e) {
            Toast.makeText(this, "Impossible d'ouvrir la recette.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        displayIngredients = PortionMode.ingredientsForMode(recipe, portionMode);
        boolean mode180 = PortionMode.MEAT_180.equals(portionMode);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.recipeTitle)).setText(recipe.title + (mode180 ? " — 180 g total" : ""));

        String modeLine = mode180
                ? "\n🥩 Viande : 180 g au TOTAL pour toute la recette\n⚖ Toutes les autres quantités sont recalculées proportionnellement"
                : "";
        ((TextView) findViewById(R.id.meta)).setText(
                recipe.flag + " " + recipe.countryLabel + " • " + recipe.categoryLabel
                        + "\n⏱ Préparation " + recipe.prepMinutes + " min • Cuisson " + recipe.cookMinutes
                        + " min • 👥 " + recipe.servings + modeLine
        );

        String description = recipe.description;
        if (mode180) {
            description += "\n\nMode 180 g : la recette complète est recalculée à partir de 180 g de viande au total, pas seulement la ligne viande.";
        }
        ((TextView) findViewById(R.id.description)).setText(description);

        ImageView hero = findViewById(R.id.hero);
        boolean featured = mode180 && FeaturedMeatImages.load(this, hero, recipe);
        if (!featured) AssetImages.load(this, hero, recipe.imagePath);

        LinearLayout ingredients = findViewById(R.id.ingredientContainer);
        for (Recipe.Ingredient ingredient : displayIngredients) {
            TextView item = Ui.text(this, "•  " + formatIngredient(ingredient), 16, false);
            item.setPadding(Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 8));
            ingredients.addView(item, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }

        favoriteButton = findViewById(R.id.favoriteButton);
        refreshFavoriteButton();
        favoriteButton.setOnClickListener(v -> {
            boolean next = !prefs.getBoolean("fav_" + recipe.id, false);
            prefs.edit().putBoolean("fav_" + recipe.id, next).apply();
            refreshFavoriteButton();
        });

        findViewById(R.id.copyButton).setOnClickListener(v -> copyIngredients());
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CookActivity.class);
            intent.putExtra("assetPath", recipe.assetPath);
            intent.putExtra("portionMode", portionMode);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recipe != null && startButton != null) {
            int progress = prefs.getInt(progressKey(), 0);
            startButton.setText(progress > 0 ? "▶ Reprendre la recette" : "▶ Démarrer la cuisson guidée");
        }
    }

    private String progressKey() {
        return "progress_" + recipe.id + "_" + portionMode
                + (PortionMode.MEAT_180.equals(portionMode) ? "_scaled_v2" : "");
    }

    private void refreshFavoriteButton() {
        boolean favorite = prefs.getBoolean("fav_" + recipe.id, false);
        favoriteButton.setText(favorite ? "★ Dans mes favoris" : "♡ Ajouter aux favoris");
    }

    private void copyIngredients() {
        StringBuilder text = new StringBuilder(recipe.title)
                .append(PortionMode.MEAT_180.equals(portionMode) ? " — 180 g de viande au total\n" : "\n");
        for (Recipe.Ingredient ingredient : displayIngredients) {
            text.append("• ").append(formatIngredient(ingredient)).append("\n");
        }
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("Liste de courses", text.toString()));
        Toast.makeText(this, "Liste copiée.", Toast.LENGTH_SHORT).show();
    }

    private static String formatIngredient(Recipe.Ingredient ingredient) {
        String amount = ingredient.amount == null ? "" : ingredient.amount.trim();
        return amount.isEmpty() ? ingredient.name : amount + " • " + ingredient.name;
    }
}
