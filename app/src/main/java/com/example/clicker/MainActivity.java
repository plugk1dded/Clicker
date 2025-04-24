package com.example.clicker;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private long points = 0;
    private int pointsPerClick = 1;
    private int autoClickerPoints = 0;
    private int multiplierLevel = 1;

    private int clickUpgradeCost = 10;
    private int autoClickerCost = 50;
    private int multiplierCost = 200;

    private TextView pointsTextView;
    private TextView statsTextView;
    private Button clickButton;
    private Button upgradeClickButton;
    private Button upgradeAutoClickButton;
    private Button upgradeMultiplierButton;

    private int totalClicks = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoClickerRunnable;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "ClickerGamePrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadGameState();
        setupClickListeners();
        setupAutoClicker();
        updateUI();
    }

    private void initializeViews() {
        pointsTextView = findViewById(R.id.pointsTextView);
        statsTextView = findViewById(R.id.statsTextView);
        clickButton = findViewById(R.id.clickButton);
        upgradeClickButton = findViewById(R.id.upgradeClickButton);
        upgradeAutoClickButton = findViewById(R.id.upgradeAutoClickButton);
        upgradeMultiplierButton = findViewById(R.id.upgradeMultiplierButton);
    }

    private void setupClickListeners() {
        clickButton.setOnClickListener(v -> {
            points += pointsPerClick;
            totalClicks++;
            animateClick(v);
            updateUI();
        });

        upgradeClickButton.setOnClickListener(v -> {
            if (points >= clickUpgradeCost) {
                points -= clickUpgradeCost;
                pointsPerClick++;
                clickUpgradeCost = clickUpgradeCost * 2;
                updateUI();
                Toast.makeText(this, "Клик улучшен!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Недостаточно очков!", Toast.LENGTH_SHORT).show();
            }
        });

        upgradeAutoClickButton.setOnClickListener(v -> {
            if (points >= autoClickerCost) {
                points -= autoClickerCost;
                autoClickerPoints++;
                autoClickerCost = autoClickerCost * 2;
                updateUI();
                Toast.makeText(this, "Авто-кликер улучшен!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Недостаточно очков!", Toast.LENGTH_SHORT).show();
            }
        });

        upgradeMultiplierButton.setOnClickListener(v -> {
            if (points >= multiplierCost) {
                points -= multiplierCost;
                multiplierLevel++;
                multiplierCost = multiplierCost * 3;
                updateUI();
                Toast.makeText(this, "Множитель улучшен!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Недостаточно очков!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoClicker() {
        autoClickerRunnable = new Runnable() {
            @Override
            public void run() {
                if (autoClickerPoints > 0) {
                    points += (long) autoClickerPoints * multiplierLevel;
                    updateUI();
                }
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(autoClickerRunnable);
    }

    @SuppressLint("DefaultLocale")
    private void updateUI() {
        pointsTextView.setText(String.format("Очков: %d", points));

        statsTextView.setText(String.format("Всего кликов: %d | Очков в секунду: %d",
                totalClicks, autoClickerPoints * multiplierLevel));

        upgradeClickButton.setText(String.format("Улучшить клик\nСтоимость: %d", clickUpgradeCost));
        upgradeAutoClickButton.setText(String.format("Улучшить авто-кликер\nСтоимость: %d", autoClickerCost));
        upgradeMultiplierButton.setText(String.format("Улучшить множитель\nСтоимость: %d", multiplierCost));
    }

    private void animateClick(View view) {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
        scaleDown.setDuration(100);

        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        scaleUp.setDuration(100);

        scaleDown.start();

        handler.postDelayed(scaleUp::start, 100);
    }

    private void saveGameState() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong("points", points);
        editor.putInt("pointsPerClick", pointsPerClick);
        editor.putInt("autoClickerPoints", autoClickerPoints);
        editor.putInt("multiplierLevel", multiplierLevel);

        editor.putInt("clickUpgradeCost", clickUpgradeCost);
        editor.putInt("autoClickerCost", autoClickerCost);
        editor.putInt("multiplierCost", multiplierCost);

        editor.putInt("totalClicks", totalClicks);

        editor.apply();
    }

    private void loadGameState() {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        points = preferences.getLong("points", 0);
        pointsPerClick = preferences.getInt("pointsPerClick", 1);
        autoClickerPoints = preferences.getInt("autoClickerPoints", 0);
        multiplierLevel = preferences.getInt("multiplierLevel", 1);
        clickUpgradeCost = preferences.getInt("clickUpgradeCost", 10);
        autoClickerCost = preferences.getInt("autoClickerCost", 50);
        multiplierCost = preferences.getInt("multiplierCost", 200);
        totalClicks = preferences.getInt("totalClicks", 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoClickerRunnable);
    }
}