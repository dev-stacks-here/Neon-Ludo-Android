package com.devstacks.neonludo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SetupActivity extends AppCompatActivity {

    private String colorP1 = "GREEN", colorP2 = "RED", colorP3 = "YELLOW", colorP4 = "BLUE";
    private int totalPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SharedPreferences rom = getSharedPreferences("NeonLudoSave", MODE_PRIVATE);
        totalPlayers = rom.getInt("SavedPlayerCount", 4);

        if (totalPlayers == 2) {
            findViewById(R.id.layoutP3).setVisibility(View.GONE);
            findViewById(R.id.layoutP4).setVisibility(View.GONE);
        } else if (totalPlayers == 3) {
            findViewById(R.id.layoutP4).setVisibility(View.GONE);
        }

        setupColorButtons(1, R.id.p1_cGreen, R.id.p1_cRed, R.id.p1_cYellow, R.id.p1_cBlue);
        setupColorButtons(2, R.id.p2_cGreen, R.id.p2_cRed, R.id.p2_cYellow, R.id.p2_cBlue);
        setupColorButtons(3, R.id.p3_cGreen, R.id.p3_cRed, R.id.p3_cYellow, R.id.p3_cBlue);
        setupColorButtons(4, R.id.p4_cGreen, R.id.p4_cRed, R.id.p4_cYellow, R.id.p4_cBlue);

        EditText etNameP1 = findViewById(R.id.etNameP1); EditText etNameP2 = findViewById(R.id.etNameP2);
        EditText etNameP3 = findViewById(R.id.etNameP3); EditText etNameP4 = findViewById(R.id.etNameP4);

        findViewById(R.id.btnStartFromSetup).setOnClickListener(v -> {
            SharedPreferences.Editor editor = rom.edit();
            editor.putString("NameP1", etNameP1.getText().toString().isEmpty() ? "Player 1" : etNameP1.getText().toString());
            editor.putString("NameP2", etNameP2.getText().toString().isEmpty() ? "Player 2" : etNameP2.getText().toString());
            editor.putString("NameP3", etNameP3.getText().toString().isEmpty() ? "Player 3" : etNameP3.getText().toString());
            editor.putString("NameP4", etNameP4.getText().toString().isEmpty() ? "Player 4" : etNameP4.getText().toString());

            editor.putString("ColorP1", colorP1);
            editor.putString("ColorP2", colorP2);
            editor.putString("ColorP3", colorP3);
            editor.putString("ColorP4", colorP4);
            editor.apply();

            startActivity(new Intent(SetupActivity.this, GameActivity.class));
        });
    }

    private void setupColorButtons(int playerNum, int gId, int rId, int yId, int bId) {
        AppCompatButton btnG = findViewById(gId); AppCompatButton btnR = findViewById(rId);
        AppCompatButton btnY = findViewById(yId); AppCompatButton btnB = findViewById(bId);

        View.OnClickListener listener = v -> {
            String selected = "GREEN";
            if (v.getId() == rId) selected = "RED";
            if (v.getId() == yId) selected = "YELLOW";
            if (v.getId() == bId) selected = "BLUE";

            // BUG FIX: Check if another active player already owns this color!
            if (playerNum != 1 && colorP1.equals(selected)) { showTakenError(selected); return; }
            if (playerNum != 2 && colorP2.equals(selected)) { showTakenError(selected); return; }
            if (totalPlayers >= 3 && playerNum != 3 && colorP3.equals(selected)) { showTakenError(selected); return; }
            if (totalPlayers == 4 && playerNum != 4 && colorP4.equals(selected)) { showTakenError(selected); return; }

            // If it's free, wipe the old checkmarks and set the new one
            btnG.setText(""); btnR.setText(""); btnY.setText(""); btnB.setText("");
            ((AppCompatButton)v).setText("✓");

            if (playerNum == 1) colorP1 = selected;
            if (playerNum == 2) colorP2 = selected;
            if (playerNum == 3) colorP3 = selected;
            if (playerNum == 4) colorP4 = selected;
        };

        btnG.setOnClickListener(listener); btnR.setOnClickListener(listener);
        btnY.setOnClickListener(listener); btnB.setOnClickListener(listener);
    }

    private void showTakenError(String color) {
        Toast.makeText(this, color + " is already taken by another player!", Toast.LENGTH_SHORT).show();
    }
}