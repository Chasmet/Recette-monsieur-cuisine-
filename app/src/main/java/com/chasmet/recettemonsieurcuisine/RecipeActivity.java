package com.chasmet.recettemonsieurcuisine;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecipeActivity extends Activity {
    private Recipe recipe;
    private SharedPreferences prefs;
    private Button favoriteButton;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        prefs = getSharedPreferences("recipe_prefs", MODE_PRIVATE);

        String assetPath = getIntent().getStringExtra("assetPath");
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

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.recipeTitle)).setText(recipe.title);
        ((TextView) findViewById(R.id.meta)).setText(
                recipe.flag + " " + recipe.countryLabel + " • " + recipe.categoryLabel +
                "\n⏱ Préparation " + recipe.prepMinutes + " min • Cuisson " + recipe.cookMinutes +
                " min • 👥 " + recipe.servings + " • " + recipe.difficulty
        );
        ((TextView) findViewById(R.id.description)).setText(recipe.description);

        WebView hero = findViewById(R.id.hero);
        WebSettings settings = hero.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        hero.setVerticalScrollBarEnabled(false);
        hero.setHorizontalScrollBarEnabled(false);
        hero.loadUrl("file:///android_asset/" + recipe.imagePath);

        LinearLayout ingredients = findViewById(R.id.ingredientContainer);
        for (Recipe.Ingredient ingredient : recipe.ingredients) {
            TextView item = Ui.text(this, "•  " + ingredient.display(), 16, false);
            item.setPadding(Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 8));
            ingredients.addView(item, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
            Intent intent = new Intent(RecipeActivity.this, CookActivity.class);
            intent.putExtra("assetPath", recipe.assetPath);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recipe != null && startButton != null) {
            int progress = prefs.getInt("progress_" + recipe.id, 0);
            if (progress > 0) {
                startButton.setText("▶ Reprendre la recette");
            } else {
                startButton.setText("Démarrer la recette");
            }
        }
    }

    private void refreshFavoriteButton() {
        boolean favorite = prefs.getBoolean("fav_" + recipe.id, false);
        favoriteButton.setText(favorite ? "★ Dans mes favoris" : "♡ Ajouter aux favoris");
    }

    private void copyIngredients() {
        StringBuilder text = new StringBuilder(recipe.title).append("\n");
        for (Recipe.Ingredient ingredient : recipe.ingredients) {
            text.append("• ").append(ingredient.display()).append("\n");
        }
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("Liste de courses", text.toString()));
        Toast.makeText(this, "Liste copiée.", Toast.LENGTH_SHORT).show();
    }
}
