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

import java.util.Locale;

public class CookActivity extends Activity {
    private Recipe recipe;
    private SharedPreferences prefs;
    private ProgressBar progress;
    private TextView progressText;
    private TextView phaseLabel;
    private TextView stepTitle;
    private TextView stepInstruction;
    private TextView machineSettings;
    private TextView timerText;
    private Button timerButton;
    private Button nextButton;
    private Button previousButton;

    private int index;
    private int total;
    private long remainingMs;
    private boolean timerRunning;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_cook);

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

        ((TextView) findViewById(R.id.cookRecipeTitle)).setText(recipe.title);
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

        total = recipe.ingredients.size() + recipe.steps.size();
        progress.setMax(Math.max(total, 1));
        index = prefs.getInt("progress_" + recipe.id, 0);
        if (index < 0 || index >= total) index = 0;

        timerButton.setOnClickListener(v -> toggleTimer());
        nextButton.setOnClickListener(v -> goNext());
        previousButton.setOnClickListener(v -> goPrevious());
        findViewById(R.id.quitButton).setOnClickListener(v -> finish());

        render();
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

        if (index < recipe.ingredients.size()) {
            Recipe.Ingredient ingredient = recipe.ingredients.get(index);
            phaseLabel.setText("INGRÉDIENT " + (index + 1) + " / " + recipe.ingredients.size());
            stepTitle.setText(ingredient.amount == null || ingredient.amount.isEmpty()
                    ? ingredient.name
                    : ingredient.amount);
            stepInstruction.setText("Ajoute maintenant :\n" + ingredient.name +
                    "\n\nQuand c'est fait, valide pour passer à l'élément suivant.");
            machineSettings.setText("Préparation • aucun réglage du robot à cette étape");
            remainingMs = 0;
            timerText.setText("—");
            timerButton.setEnabled(false);
            timerButton.setText("Pas de minuteur");
        } else {
            int stepIndex = index - recipe.ingredients.size();
            Recipe.Step step = recipe.steps.get(stepIndex);
            phaseLabel.setText("CUISSON / PRÉPARATION • ÉTAPE " + (stepIndex + 1) + " / " + recipe.steps.size());
            stepTitle.setText(step.title);
            stepInstruction.setText(step.instruction + (step.note.isEmpty() ? "" : "\n\nConseil : " + step.note));

            StringBuilder settings = new StringBuilder("Réglages Monsieur Cuisine");
            boolean hasSetting = false;
            if (step.durationSeconds > 0) {
                settings.append("\n⏱ ").append(formatTime(step.durationSeconds * 1000L));
                hasSetting = true;
            }
            if (step.temperatureC > 0) {
                settings.append("   •   🌡 ").append(step.temperatureC).append(" °C");
                hasSetting = true;
            }
            if (!step.speed.isEmpty()) {
                settings.append("\n⚙ Vitesse ").append(step.speed);
                hasSetting = true;
            }
            if (step.reverse) {
                settings.append("   •   ↶ Sens inverse");
                hasSetting = true;
            }
            if (step.turbo) {
                settings.append("   •   ⚡ Turbo");
                hasSetting = true;
            }
            if (!hasSetting) settings.append("\nManuel / sans réglage robot");
            machineSettings.setText(settings.toString());

            remainingMs = step.durationSeconds * 1000L;
            timerText.setText(step.durationSeconds > 0 ? formatTime(remainingMs) : "—");
            timerButton.setEnabled(step.durationSeconds > 0);
            timerButton.setText(step.durationSeconds > 0 ? "Démarrer le minuteur" : "Pas de minuteur");
        }
    }

    private void goNext() {
        cancelTimer();
        index++;
        if (index >= total) {
            prefs.edit().remove("progress_" + recipe.id).apply();
            completeRecipe();
        } else {
            prefs.edit().putInt("progress_" + recipe.id, index).apply();
            render();
        }
    }

    private void goPrevious() {
        if (index <= 0) return;
        cancelTimer();
        index--;
        prefs.edit().putInt("progress_" + recipe.id, index).apply();
        render();
    }

    private void completeRecipe() {
        cancelTimer();
        progress.setProgress(total);
        progressText.setText("100 % terminé");
        phaseLabel.setText("RECETTE TERMINÉE");
        stepTitle.setText("Bravo, c'est prêt !");
        stepInstruction.setText("La recette est terminée. Vérifie l'assaisonnement et sers selon la recette.");
        machineSettings.setText("✓ Progression effacée : tu pourras recommencer depuis le début.");
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
            @Override public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                timerText.setText(formatTime(remainingMs));
            }
            @Override public void onFinish() {
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
        long totalSeconds = Math.max(0, (millis + 999) / 1000);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.FRANCE, "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }
}
