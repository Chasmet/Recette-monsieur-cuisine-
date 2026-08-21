package com.chasmet.recettemonsieurcuisine;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class CookActivity extends Activity {
    private Recipe recipe;
    private List<Recipe.Ingredient> cookingIngredients;
    private SharedPreferences prefs;
    private ProgressBar progress;
    private TextView progressText, phaseLabel, stepTitle, stepInstruction, machineSettings, timerText;
    private Button timerButton, nextButton, previousButton;
    private int index, total;
    private long remainingMs;
    private boolean timerRunning;
    private CountDownTimer timer;
    private String portionMode = PortionMode.STANDARD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_cook);
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

        cookingIngredients = PortionMode.ingredientsForMode(recipe, portionMode);
        boolean mode180 = PortionMode.MEAT_180.equals(portionMode);
        ((TextView) findViewById(R.id.cookRecipeTitle)).setText(recipe.title + (mode180 ? " — 180 g total" : ""));

        progress = findViewById(R.id.progress);
        progressText = findViewById(R.id.progressText);
        phaseLabel = findViewById(R.id.phaseLabel);
        stepTitle = findViewById(R.id.stepTitle);
        stepInstruction = findViewById(R.id.stepInstruction);
        machineSettings = findViewById(R.id.machineSettings);
        timerText = findViewById(R.id.timerText);
        timerButton = findViewById(R.id.timerButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);

        total = cookingIngredients.size() + recipe.steps.size();
        progress.setMax(Math.max(total, 1));
        index = prefs.getInt(key(), 0);
        if (index < 0 || index >= total) index = 0;

        timerButton.setOnClickListener(v -> toggleTimer());
        nextButton.setOnClickListener(v -> goNext());
        previousButton.setOnClickListener(v -> goPrevious());
        findViewById(R.id.quitButton).setOnClickListener(v -> finish());
        render();
    }

    private String key() {
        return "progress_" + recipe.id + "_" + portionMode
                + (PortionMode.MEAT_180.equals(portionMode) ? "_scaled_v2" : "");
    }

    private void render() {
        cancelTimer();
        if (index >= total) {
            completeRecipe();
            return;
        }

        progress.setProgress(index);
        progressText.setText("Progression : " + (index + 1) + " / " + total);
        previousButton.setEnabled(index > 0);

        if (index < cookingIngredients.size()) {
            Recipe.Ingredient ingredient = cookingIngredients.get(index);
            String amount = ingredient.amount == null ? "" : ingredient.amount;
            phaseLabel.setText("INGRÉDIENT " + (index + 1) + " / " + cookingIngredients.size());
            stepTitle.setText(amount.isEmpty() ? ingredient.name : amount);
            stepInstruction.setText(
                    "Ajoute maintenant :\n" + ingredient.name
                            + "\n\nQuantité : " + (amount.isEmpty() ? "selon indication" : amount)
                            + "\n\nQuand c'est fait, valide pour passer à l'élément suivant."
            );

            if (PortionMode.MEAT_180.equals(portionMode)) {
                machineSettings.setText(
                        PortionMode.isMeatIngredient(ingredient.name)
                                ? "Mode 180 g • la somme de toutes les viandes de cette recette est exactement 180 g"
                                : "Mode 180 g • quantité recalculée proportionnellement à la base de 180 g de viande total"
                );
            } else {
                machineSettings.setText("Préparation • aucun réglage du robot à cette étape");
            }

            remainingMs = 0;
            timerText.setText("—");
            timerButton.setEnabled(false);
            timerButton.setText("Pas de minuteur");
            return;
        }

        int stepIndex = index - cookingIngredients.size();
        Recipe.Step step = recipe.steps.get(stepIndex);
        phaseLabel.setText("CUISSON / PRÉPARATION • ÉTAPE " + (stepIndex + 1) + " / " + recipe.steps.size());
        stepTitle.setText(step.title);

        String instruction = PortionMode.adaptText(recipe, step.instruction, portionMode);
        String note = PortionMode.adaptText(recipe, step.note, portionMode);
        stepInstruction.setText(instruction + (note.isEmpty() ? "" : "\n\nConseil : " + note));

        StringBuilder settings = new StringBuilder("Réglages Monsieur Cuisine");
        boolean has = false;
        if (step.durationSeconds > 0) {
            settings.append("\n⏱ ").append(formatTime(step.durationSeconds * 1000L));
            has = true;
        }
        if (step.temperatureC > 0) {
            settings.append("   •   🌡 ").append(step.temperatureC).append(" °C");
            has = true;
        }
        if (!step.speed.isEmpty()) {
            settings.append("\n⚙ Vitesse ").append(step.speed);
            has = true;
        }
        if (step.reverse) {
            settings.append("   •   ↶ Sens inverse");
            has = true;
        }
        if (step.turbo) {
            settings.append("   •   ⚡ Turbo");
            has = true;
        }
        if (!has) settings.append("\nManuel / sans réglage robot");
        if (PortionMode.MEAT_180.equals(portionMode)) {
            settings.append("\n⚖ Quantités adaptées au mode 180 g total");
        }
        machineSettings.setText(settings.toString());

        remainingMs = step.durationSeconds * 1000L;
        timerText.setText(step.durationSeconds > 0 ? formatTime(remainingMs) : "—");
        timerButton.setEnabled(step.durationSeconds > 0);
        timerButton.setText(step.durationSeconds > 0 ? "Démarrer le minuteur" : "Pas de minuteur");
    }

    private void goNext() {
        cancelTimer();
        index++;
        if (index >= total) {
            prefs.edit().remove(key()).apply();
            completeRecipe();
        } else {
            prefs.edit().putInt(key(), index).apply();
            render();
        }
    }

    private void goPrevious() {
        if (index <= 0) return;
        cancelTimer();
        index--;
        prefs.edit().putInt(key(), index).apply();
        render();
    }

    private void completeRecipe() {
        cancelTimer();
        progress.setProgress(total);
        progressText.setText("100 % terminé");
        phaseLabel.setText("RECETTE TERMINÉE");
        stepTitle.setText("C'est prêt !");
        stepInstruction.setText("La recette est terminée. Vérifie l'assaisonnement puis sers selon la recette.");
        machineSettings.setText("✓ Progression effacée : tu peux recommencer depuis le début.");
        timerText.setText("✓");
        timerButton.setEnabled(false);
        nextButton.setText("Terminer");
        nextButton.setOnClickListener(v -> finish());
        previousButton.setEnabled(false);
        signalFinished();
    }

    private void toggleTimer() {
        if (timerRunning) {
            if (timer != null) timer.cancel();
            timerRunning = false;
            timerButton.setText("Reprendre le minuteur");
            return;
        }
        if (remainingMs <= 0) return;
        timerRunning = true;
        timerButton.setText("Pause");
        timer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                timerText.setText(formatTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                timerRunning = false;
                timerText.setText("00:00");
                timerButton.setText("Minuteur terminé");
                timerButton.setEnabled(false);
                signalFinished();
            }
        }.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timerRunning = false;
    }

    private void signalFinished() {
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90);
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 700);
        } catch (Exception ignored) {}

        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        } catch (Exception ignored) {}
    }

    private static String formatTime(long millis) {
        long seconds = Math.max(0, (millis + 999) / 1000);
        long minutes = seconds / 60;
        return String.format(Locale.FRANCE, "%02d:%02d", minutes, seconds % 60);
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }
}
