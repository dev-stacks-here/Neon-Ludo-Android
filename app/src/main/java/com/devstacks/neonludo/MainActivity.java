package com.devstacks.neonludo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnPlayer2).setOnClickListener(v -> startGame(2));
        findViewById(R.id.btnPlayer3).setOnClickListener(v -> startGame(3));
        findViewById(R.id.btnPlayer4).setOnClickListener(v -> startGame(4));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedGamesList();
    }

    private void startGame(int players) {
        SharedPreferences rom = getSharedPreferences("NeonLudoSave", MODE_PRIVATE);
        rom.edit().putInt("SavedPlayerCount", players).apply();
        startActivity(new Intent(MainActivity.this, SetupActivity.class));
    }

    private void loadSavedGamesList() {
        LinearLayout container = findViewById(R.id.savedGamesContainer);
        container.removeAllViews();

        SharedPreferences rom = getSharedPreferences("NeonLudoSave", MODE_PRIVATE);
        String allSaves = rom.getString("AllSaveIDs", "");

        if (allSaves.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No saved games yet.");
            emptyText.setTextColor(Color.GRAY);
            container.addView(emptyText);
            return;
        }

        String[] saveIDs = allSaves.split(",");

        for (int i = saveIDs.length - 1; i >= 0; i--) {
            String id = saveIDs[i];

            if (id.trim().isEmpty()) continue;

            String saveName = rom.getString(id + "_Name", "Unknown Save");
            long timestamp = rom.getLong(id + "_Timestamp", 0);
            String timeAgoText = "";

            // NEW: Advanced Relative Time Algorithm!
            if (timestamp != 0) {
                long diff = System.currentTimeMillis() - timestamp;

                long MINUTE = 60 * 1000L;
                long HOUR = 60 * MINUTE;
                long DAY = 24 * HOUR;
                long WEEK = 7 * DAY;
                long MONTH = 30 * DAY;
                long YEAR = 365 * DAY;

                if (diff < MINUTE) {
                    timeAgoText = "Saved just now";
                } else if (diff < HOUR) {
                    long mins = diff / MINUTE;
                    timeAgoText = "Saved " + mins + (mins == 1 ? " minute ago" : " minutes ago");
                } else if (diff < DAY) {
                    long hours = diff / HOUR;
                    timeAgoText = "Saved " + hours + (hours == 1 ? " hour ago" : " hours ago");
                } else if (diff < 2 * DAY) {
                    timeAgoText = "Saved yesterday";
                } else if (diff < WEEK) {
                    long days = diff / DAY;
                    timeAgoText = "Saved " + days + " days ago";
                } else if (diff < MONTH) {
                    long weeks = diff / WEEK;
                    timeAgoText = "Saved " + weeks + (weeks == 1 ? " week ago" : " weeks ago");
                } else if (diff < YEAR) {
                    long months = diff / MONTH;
                    timeAgoText = "Saved " + months + (months == 1 ? " month ago" : " months ago");
                } else {
                    long years = diff / YEAR;
                    timeAgoText = "Saved " + years + (years == 1 ? " year ago" : " years ago");
                }
            } else {
                timeAgoText = rom.getString(id + "_Date", "");
            }

            // Forces Android to keep the date text glued together on one line!
            timeAgoText = timeAgoText.replace(" ", "\u00A0");

            Button saveBtn = new Button(this);
            saveBtn.setAllCaps(false);
            saveBtn.setText(saveName + "\n\n" + timeAgoText);
            saveBtn.setBackgroundColor(Color.parseColor("#222222"));
            saveBtn.setTextColor(Color.WHITE);
            saveBtn.setPadding(40, 40, 40, 40);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(50, 0, 50, 30);
            saveBtn.setLayoutParams(params);

            saveBtn.setOnClickListener(v -> {
                rom.edit().putString("LoadGameID", id).apply();
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            });

            container.addView(saveBtn);
        }
    }
}