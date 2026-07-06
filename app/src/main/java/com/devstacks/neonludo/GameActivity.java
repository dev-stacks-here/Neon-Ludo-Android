package com.devstacks.neonludo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private int currentPlayerTurn = 1;
    private int totalPlayers;
    private Random diceRandomizer = new Random();
    private boolean hasRolled = false;
    private int lastDiceRoll = 0;
    private int consecutiveSixes = 0;

    private boolean isAnimating = false;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable autoRollTask;

    private int[] greenMem = {-1, -1, -1, -1};
    private int[] redMem = {-1, -1, -1, -1};
    private int[] yellowMem = {-1, -1, -1, -1};
    private int[] blueMem = {-1, -1, -1, -1};

    private int greenOwner = 0, redOwner = 0, yellowOwner = 0, blueOwner = 0;
    private String nameP1, nameP2, nameP3, nameP4;
    private boolean greenWon = false, redWon = false, yellowWon = false, blueWon = false;
    private ArrayList<Integer> winOrder = new ArrayList<>();

    private TextView tvTurnIndicator;
    private Button btnDiceGreen, btnDiceRed, btnDiceYellow, btnDiceBlue;

    private TextView[] greenTokens = new TextView[4];
    private TextView[] redTokens = new TextView[4];
    private TextView[] yellowTokens = new TextView[4];
    private TextView[] blueTokens = new TextView[4];

    private final int[][] greenPath = {{1,6},{2,6},{3,6},{4,6},{5,6}, {6,5},{6,4},{6,3},{6,2},{6,1},{6,0}, {7,0},{8,0}, {8,1},{8,2},{8,3},{8,4},{8,5}, {9,6},{10,6},{11,6},{12,6},{13,6},{14,6}, {14,7},{14,8}, {13,8},{12,8},{11,8},{10,8},{9,8}, {8,9},{8,10},{8,11},{8,12},{8,13},{8,14}, {7,14},{6,14}, {6,13},{6,12},{6,11},{6,10},{6,9}, {5,8},{4,8},{3,8},{2,8},{1,8},{0,8}, {0,7}, {1,7},{2,7},{3,7},{4,7},{5,7},{6,7}};
    private final int[][] redPath = {{6,13},{6,12},{6,11},{6,10},{6,9}, {5,8},{4,8},{3,8},{2,8},{1,8},{0,8}, {0,7},{0,6}, {1,6},{2,6},{3,6},{4,6},{5,6}, {6,5},{6,4},{6,3},{6,2},{6,1},{6,0}, {7,0},{8,0}, {8,1},{8,2},{8,3},{8,4},{8,5}, {9,6},{10,6},{11,6},{12,6},{13,6},{14,6}, {14,7},{14,8}, {13,8},{12,8},{11,8},{10,8},{9,8}, {8,9},{8,10},{8,11},{8,12},{8,13},{8,14}, {7,14}, {7,13},{7,12},{7,11},{7,10},{7,9},{7,8}};
    private final int[][] yellowPath = {{8,1},{8,2},{8,3},{8,4},{8,5}, {9,6},{10,6},{11,6},{12,6},{13,6},{14,6}, {14,7},{14,8}, {13,8},{12,8},{11,8},{10,8},{9,8}, {8,9},{8,10},{8,11},{8,12},{8,13},{8,14}, {7,14},{6,14}, {6,13},{6,12},{6,11},{6,10},{6,9}, {5,8},{4,8},{3,8},{2,8},{1,8},{0,8}, {0,7},{0,6}, {1,6},{2,6},{3,6},{4,6},{5,6}, {6,5},{6,4},{6,3},{6,2},{6,1},{6,0}, {7,0}, {7,1},{7,2},{7,3},{7,4},{7,5},{7,6}};
    private final int[][] bluePath = {{13,8},{12,8},{11,8},{10,8},{9,8}, {8,9},{8,10},{8,11},{8,12},{8,13},{8,14}, {7,14},{6,14}, {6,13},{6,12},{6,11},{6,10},{6,9}, {5,8},{4,8},{3,8},{2,8},{1,8},{0,8}, {0,7},{0,6}, {1,6},{2,6},{3,6},{4,6},{5,6}, {6,5},{6,4},{6,3},{6,2},{6,1},{6,0}, {7,0},{8,0}, {8,1},{8,2},{8,3},{8,4},{8,5}, {9,6},{10,6},{11,6},{12,6},{13,6},{14,6}, {14,7}, {13,7},{12,7},{11,7},{10,7},{9,7},{8,7}};

    private final int[][] safeHouses = {{1,6}, {6,2}, {8,1}, {12,6}, {13,8}, {8,12}, {6,13}, {2,8}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                triggerSavePopup();
            }
        });

        tvTurnIndicator = findViewById(R.id.tvTurnIndicator);
        btnDiceGreen = findViewById(R.id.btnDiceGreen); btnDiceRed = findViewById(R.id.btnDiceRed);
        btnDiceYellow = findViewById(R.id.btnDiceYellow); btnDiceBlue = findViewById(R.id.btnDiceBlue);

        SharedPreferences rom = getSharedPreferences("NeonLudoSave", MODE_PRIVATE);
        String loadId = rom.getString("LoadGameID", "");

        if (!loadId.isEmpty()) {
            loadGameState(rom, loadId);
            rom.edit().remove("LoadGameID").apply();
        } else {
            initializeNewGame(rom);
        }

        setupTokens();

        btnDiceGreen.setOnClickListener(v -> attemptDiceRoll(greenOwner));
        btnDiceRed.setOnClickListener(v -> attemptDiceRoll(redOwner));
        btnDiceYellow.setOnClickListener(v -> attemptDiceRoll(yellowOwner));
        btnDiceBlue.setOnClickListener(v -> attemptDiceRoll(blueOwner));

        tvTurnIndicator.post(this::updateBoardVisuals);
        prepareTurn();
    }

    // --- SOUND & HAPTICS ENGINE ---

    private void playSound(int soundResId) {
        try {
            MediaPlayer mp = MediaPlayer.create(this, soundResId);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void vibratePhone(int milliseconds) {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(milliseconds);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- SAVE AND LOAD ENGINE ---

    private void triggerSavePopup() {
        if (isAnimating) return;
        if (autoRollTask != null) timerHandler.removeCallbacks(autoRollTask);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Game");

        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String defaultName = nameP1 + " vs " + nameP2;
        if (totalPlayers >= 3) defaultName += " vs " + nameP3;
        if (totalPlayers == 4) defaultName += " vs " + nameP4;
        defaultName += " - " + dateStr;

        final String finalDefaultName = defaultName;
        final EditText input = new EditText(this);
        input.setText(finalDefaultName);
        input.setPadding(40, 40, 40, 40);
        builder.setView(input);

        builder.setPositiveButton("Save & Exit", (dialog, which) -> {
            String finalName = input.getText().toString().trim();
            if (finalName.isEmpty()) finalName = finalDefaultName;
            saveGameState(finalName);
            finish();
        });
        builder.setNegativeButton("Exit Without Saving", (dialog, which) -> finish());
        builder.setNeutralButton("Cancel", (dialog, which) -> prepareTurn());
        builder.show();
    }

    private void saveGameState(String saveName) {
        SharedPreferences rom = getSharedPreferences("NeonLudoSave", MODE_PRIVATE);
        SharedPreferences.Editor editor = rom.edit();
        String saveId = String.valueOf(System.currentTimeMillis());
        editor.putString("AllSaveIDs", rom.getString("AllSaveIDs", "") + saveId + ",");
        editor.putString(saveId + "_Name", saveName);
        editor.putLong(saveId + "_Timestamp", System.currentTimeMillis());
        editor.putString(saveId + "_Date", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));

        editor.putInt(saveId + "_TotalPlayers", totalPlayers); editor.putInt(saveId + "_CurrentTurn", currentPlayerTurn);
        editor.putInt(saveId + "_greenOwner", greenOwner); editor.putInt(saveId + "_redOwner", redOwner);
        editor.putInt(saveId + "_yellowOwner", yellowOwner); editor.putInt(saveId + "_blueOwner", blueOwner);
        editor.putBoolean(saveId + "_greenWon", greenWon); editor.putBoolean(saveId + "_redWon", redWon);
        editor.putBoolean(saveId + "_yellowWon", yellowWon); editor.putBoolean(saveId + "_blueWon", blueWon);
        editor.putString(saveId + "_nameP1", nameP1); editor.putString(saveId + "_nameP2", nameP2);
        editor.putString(saveId + "_nameP3", nameP3); editor.putString(saveId + "_nameP4", nameP4);
        editor.putString(saveId + "_greenMem", arrayToString(greenMem)); editor.putString(saveId + "_redMem", arrayToString(redMem));
        editor.putString(saveId + "_yellowMem", arrayToString(yellowMem)); editor.putString(saveId + "_blueMem", arrayToString(blueMem));
        editor.putString(saveId + "_winOrder", winOrderToString());
        editor.apply();
        Toast.makeText(this, "Game Saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadGameState(SharedPreferences rom, String id) {
        totalPlayers = rom.getInt(id + "_TotalPlayers", 4); currentPlayerTurn = rom.getInt(id + "_CurrentTurn", 1);
        greenOwner = rom.getInt(id + "_greenOwner", 0); redOwner = rom.getInt(id + "_redOwner", 0);
        yellowOwner = rom.getInt(id + "_yellowOwner", 0); blueOwner = rom.getInt(id + "_blueOwner", 0);
        greenWon = rom.getBoolean(id + "_greenWon", false); redWon = rom.getBoolean(id + "_redWon", false);
        yellowWon = rom.getBoolean(id + "_yellowWon", false); blueWon = rom.getBoolean(id + "_blueWon", false);
        nameP1 = rom.getString(id + "_nameP1", "Player 1"); nameP2 = rom.getString(id + "_nameP2", "Player 2");
        nameP3 = rom.getString(id + "_nameP3", "Player 3"); nameP4 = rom.getString(id + "_nameP4", "Player 4");
        greenMem = stringToArray(rom.getString(id + "_greenMem", "-1,-1,-1,-1")); redMem = stringToArray(rom.getString(id + "_redMem", "-1,-1,-1,-1"));
        yellowMem = stringToArray(rom.getString(id + "_yellowMem", "-1,-1,-1,-1")); blueMem = stringToArray(rom.getString(id + "_blueMem", "-1,-1,-1,-1"));
        loadWinOrder(rom.getString(id + "_winOrder", ""));
        if (greenWon) renderCrown(greenTokens); if (redWon) renderCrown(redTokens);
        if (yellowWon) renderCrown(yellowTokens); if (blueWon) renderCrown(blueTokens);
    }

    private String arrayToString(int[] arr) { return arr[0] + "," + arr[1] + "," + arr[2] + "," + arr[3]; }
    private int[] stringToArray(String str) { String[] p = str.split(","); return new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])}; }
    private String winOrderToString() { if (winOrder.isEmpty()) return ""; StringBuilder sb = new StringBuilder(); for (int i : winOrder) sb.append(i).append(","); return sb.toString(); }
    private void loadWinOrder(String str) { winOrder.clear(); if (str.isEmpty()) return; for (String s : str.split(",")) if (!s.isEmpty()) winOrder.add(Integer.parseInt(s)); }

    private void initializeNewGame(SharedPreferences rom) {
        totalPlayers = rom.getInt("SavedPlayerCount", 4);
        nameP1 = rom.getString("NameP1", "Player 1"); nameP2 = rom.getString("NameP2", "Player 2");
        nameP3 = rom.getString("NameP3", "Player 3"); nameP4 = rom.getString("NameP4", "Player 4");
        for (int i = 1; i <= totalPlayers; i++) {
            String c = rom.getString("ColorP" + i, "");
            if (c.equals("GREEN")) greenOwner = i; else if (c.equals("RED")) redOwner = i;
            else if (c.equals("YELLOW")) yellowOwner = i; else if (c.equals("BLUE")) blueOwner = i;
        }
    }

    // --- GAME ENGINE, TOKEN VISUALS & HIGHLIGHTS ---

    private void setupTokens() {
        if (greenOwner == 0) hideTokens(new int[]{R.id.p1_t1, R.id.p1_t2, R.id.p1_t3, R.id.p1_t4});
        if (redOwner == 0) hideTokens(new int[]{R.id.p2_t1, R.id.p2_t2, R.id.p2_t3, R.id.p2_t4});
        if (yellowOwner == 0) hideTokens(new int[]{R.id.p3_t1, R.id.p3_t2, R.id.p3_t3, R.id.p3_t4});
        if (blueOwner == 0) hideTokens(new int[]{R.id.p4_t1, R.id.p4_t2, R.id.p4_t3, R.id.p4_t4});

        int[] gIds = {R.id.p1_t1, R.id.p1_t2, R.id.p1_t3, R.id.p1_t4};
        for (int i = 0; i < 4; i++) {
            TextView t = findViewById(gIds[i]); greenTokens[i] = t;
            t.setBackground(createTokenShape(Color.parseColor("#85FF9D"), false));
            styleTokenWithStar(t, "#85FF9D");
            int idx = i; t.setOnClickListener(v -> moveToken("GREEN", idx));
        }

        int[] rIds = {R.id.p2_t1, R.id.p2_t2, R.id.p2_t3, R.id.p2_t4};
        for (int i = 0; i < 4; i++) {
            TextView t = findViewById(rIds[i]); redTokens[i] = t;
            t.setBackground(createTokenShape(Color.parseColor("#FF6B6B"), false));
            styleTokenWithStar(t, "#FF6B6B");
            int idx = i; t.setOnClickListener(v -> moveToken("RED", idx));
        }

        int[] yIds = {R.id.p3_t1, R.id.p3_t2, R.id.p3_t3, R.id.p3_t4};
        for (int i = 0; i < 4; i++) {
            TextView t = findViewById(yIds[i]); yellowTokens[i] = t;
            t.setBackground(createTokenShape(Color.parseColor("#F9F871"), false));
            styleTokenWithStar(t, "#F9F871");
            int idx = i; t.setOnClickListener(v -> moveToken("YELLOW", idx));
        }

        int[] bIds = {R.id.p4_t1, R.id.p4_t2, R.id.p4_t3, R.id.p4_t4};
        for (int i = 0; i < 4; i++) {
            TextView t = findViewById(bIds[i]); blueTokens[i] = t;
            t.setBackground(createTokenShape(Color.parseColor("#82C3FF"), false));
            styleTokenWithStar(t, "#82C3FF");
            int idx = i; t.setOnClickListener(v -> moveToken("BLUE", idx));
        }
    }

    private void styleTokenWithStar(TextView t, String hexColor) {
        t.setText("★");
        t.setTextColor(Color.WHITE);
        t.setTextSize(10f);
        t.setGravity(android.view.Gravity.CENTER);
        t.setIncludeFontPadding(false);
    }

    private android.graphics.drawable.GradientDrawable createTokenShape(int color, boolean isMovable) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        shape.setColor(color);
        shape.setStroke(isMovable ? 7 : 3, isMovable ? Color.parseColor("#D4BA9E") : Color.WHITE);
        return shape;
    }

    private boolean isTokenMovable(TextView token) {
        if (!hasRolled || isAnimating) return false;
        int owner = 0; int[] mem = null; int idx = -1;
        for(int i=0; i<4; i++) {
            if (greenTokens[i] == token) { owner = greenOwner; mem = greenMem; idx = i; break; }
            if (redTokens[i] == token) { owner = redOwner; mem = redMem; idx = i; break; }
            if (yellowTokens[i] == token) { owner = yellowOwner; mem = yellowMem; idx = i; break; }
            if (blueTokens[i] == token) { owner = blueOwner; mem = blueMem; idx = i; break; }
        }
        if (owner == 0 || owner != currentPlayerTurn) return false;
        int pos = mem[idx];
        if (pos == -1 && lastDiceRoll == 6) return true;
        if (pos != -1 && pos != 56 && pos + lastDiceRoll <= 56) return true;
        return false;
    }

    private int getTokenColor(TextView token) {
        for(int i=0; i<4; i++) {
            if (greenTokens[i] == token) return Color.parseColor("#85FF9D");
            if (redTokens[i] == token) return Color.parseColor("#FF6B6B");
            if (yellowTokens[i] == token) return Color.parseColor("#F9F871");
            if (blueTokens[i] == token) return Color.parseColor("#82C3FF");
        }
        return Color.WHITE;
    }

    private void hideTokens(int[] ids) { for (int id : ids) findViewById(id).setVisibility(View.INVISIBLE); }

    // --- TURN LOGIC ---

    private void prepareTurn() {
        isAnimating = false;
        hasRolled = false;
        btnDiceGreen.setVisibility(View.INVISIBLE); btnDiceRed.setVisibility(View.INVISIBLE);
        btnDiceYellow.setVisibility(View.INVISIBLE); btnDiceBlue.setVisibility(View.INVISIBLE);

        String activeName = (currentPlayerTurn == 1) ? nameP1 : (currentPlayerTurn == 2) ? nameP2 : (currentPlayerTurn == 3) ? nameP3 : nameP4;
        tvTurnIndicator.setText(activeName + "'s Turn!");

        if (currentPlayerTurn == greenOwner) { btnDiceGreen.setVisibility(View.VISIBLE); btnDiceGreen.setText("🎲"); tvTurnIndicator.setTextColor(Color.parseColor("#85FF9D")); }
        else if (currentPlayerTurn == redOwner) { btnDiceRed.setVisibility(View.VISIBLE); btnDiceRed.setText("🎲"); tvTurnIndicator.setTextColor(Color.parseColor("#FF6B6B")); }
        else if (currentPlayerTurn == yellowOwner) { btnDiceYellow.setVisibility(View.VISIBLE); btnDiceYellow.setText("🎲"); tvTurnIndicator.setTextColor(Color.parseColor("#F9F871")); }
        else if (currentPlayerTurn == blueOwner) { btnDiceBlue.setVisibility(View.VISIBLE); btnDiceBlue.setText("🎲"); tvTurnIndicator.setTextColor(Color.parseColor("#82C3FF")); }

        if (autoRollTask != null) timerHandler.removeCallbacks(autoRollTask);
        autoRollTask = () -> attemptDiceRoll(currentPlayerTurn);
        timerHandler.postDelayed(autoRollTask, 5000);
    }

    private void attemptDiceRoll(int diceOwnerNum) {
        if (isAnimating || diceOwnerNum == 0 || diceOwnerNum != currentPlayerTurn || hasRolled) return;

        isAnimating = true;
        if (autoRollTask != null) timerHandler.removeCallbacks(autoRollTask);

        playSound(R.raw.dice_roll);

        lastDiceRoll = diceRandomizer.nextInt(6) + 1;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            int shuffles = 0;
            @Override
            public void run() {
                if (shuffles < 10) {
                    updateDiceVisuals(diceOwnerNum, diceRandomizer.nextInt(6) + 1);
                    shuffles++;
                    timerHandler.postDelayed(this, 50);
                } else {
                    updateDiceVisuals(diceOwnerNum, lastDiceRoll);
                    processFinalDiceResult(diceOwnerNum);
                }
            }
        }, 50);
    }

    private void processFinalDiceResult(int diceOwnerNum) {
        hasRolled = true;
        isAnimating = false;

        if (lastDiceRoll == 6) {
            consecutiveSixes++;
            if (consecutiveSixes == 3) {
                hasRolled = false;
                timerHandler.postDelayed(this::changeTurn, 1200);
                return;
            }
        } else {
            consecutiveSixes = 0;
        }

        if (!canMoveAnyPiece(diceOwnerNum, lastDiceRoll)) {
            timerHandler.postDelayed(this::changeTurn, 1200);
        } else {
            updateBoardVisuals();

            int onlyMovableIdx = getOnlyMovablePieceIndex(diceOwnerNum, lastDiceRoll);
            if (onlyMovableIdx != -1) {
                String colorStr = (diceOwnerNum == greenOwner) ? "GREEN" : (diceOwnerNum == redOwner) ? "RED" : (diceOwnerNum == yellowOwner) ? "YELLOW" : "BLUE";
                timerHandler.postDelayed(() -> moveToken(colorStr, onlyMovableIdx), 400);
            }
        }
    }

    private boolean canMoveAnyPiece(int playerNumber, int roll) {
        int[] mem = (playerNumber == greenOwner) ? greenMem : (playerNumber == redOwner) ? redMem : (playerNumber == yellowOwner) ? yellowMem : blueMem;
        for (int pos : mem) {
            if (pos == -1 && roll == 6) return true;
            if (pos != -1 && pos != 56 && pos + roll <= 56) return true;
        }
        return false;
    }

    private int getOnlyMovablePieceIndex(int playerNumber, int roll) {
        int[] mem = (playerNumber == greenOwner) ? greenMem : (playerNumber == redOwner) ? redMem : (playerNumber == yellowOwner) ? yellowMem : blueMem;
        int movableCount = 0;
        int lastMovableIdx = -1;
        for (int i = 0; i < 4; i++) {
            int pos = mem[i];
            if (pos == -1 && roll == 6) {
                movableCount++; lastMovableIdx = i;
            } else if (pos != -1 && pos != 56 && pos + roll <= 56) {
                movableCount++; lastMovableIdx = i;
            }
        }
        return (movableCount == 1) ? lastMovableIdx : -1;
    }

    private void changeTurn() {
        consecutiveSixes = 0;
        int startTurn = currentPlayerTurn;
        do {
            currentPlayerTurn++;
            if (currentPlayerTurn > totalPlayers) currentPlayerTurn = 1;
            boolean isWinner = false;
            if (currentPlayerTurn == greenOwner && greenWon) isWinner = true;
            if (currentPlayerTurn == redOwner && redWon) isWinner = true;
            if (currentPlayerTurn == yellowOwner && yellowWon) isWinner = true;
            if (currentPlayerTurn == blueOwner && blueWon) isWinner = true;
            if (!isWinner) break;
        } while (currentPlayerTurn != startTurn);
        prepareTurn();
    }

    private void moveToken(String colorString, int index) {
        if (isAnimating) return;

        int ownerPlayer = (colorString.equals("GREEN")) ? greenOwner : (colorString.equals("RED")) ? redOwner : (colorString.equals("YELLOW")) ? yellowOwner : blueOwner;
        if (ownerPlayer == 0 || currentPlayerTurn != ownerPlayer) return;
        if (!hasRolled) return;

        int[] memory = (colorString.equals("GREEN")) ? greenMem : (colorString.equals("RED")) ? redMem : (colorString.equals("YELLOW")) ? yellowMem : blueMem;
        int[][] path = (colorString.equals("GREEN")) ? greenPath : (colorString.equals("RED")) ? redPath : (colorString.equals("YELLOW")) ? yellowPath : bluePath;
        TextView[] tokensArr = (colorString.equals("GREEN")) ? greenTokens : (colorString.equals("RED")) ? redTokens : (colorString.equals("YELLOW")) ? yellowTokens : blueTokens;

        int currentPos = memory[index];
        if (currentPos == 56) return;

        for (int i = 0; i < 4; i++) {
            if (memory[i] == currentPos && !"dying".equals(tokensArr[i].getTag())) {
                index = i;
                break;
            }
        }

        TextView token = tokensArr[index];
        isAnimating = true;

        for (TextView[] arr : new TextView[][]{greenTokens, redTokens, yellowTokens, blueTokens}) {
            for (TextView t : arr) {
                if (t != null && !"dying".equals(t.getTag())) {
                    t.setBackground(createTokenShape(getTokenColor(t), false));
                    t.setTranslationZ(0f);
                }
            }
        }

        if (currentPos == -1) {
            if (lastDiceRoll == 6) {
                memory[index] = 0;
                vibratePhone(100);
                updateBoardVisuals();
                checkKillLogic(path[0][0], path[0][1], ownerPlayer);
                prepareTurn();
            } else {
                isAnimating = false;
            }
        } else {
            int newPos = currentPos + lastDiceRoll;
            if (newPos > 56) { isAnimating = false; return; }
            memory[index] = newPos;
            checkBrokenJoint(path[currentPos][0], path[currentPos][1], ownerPlayer);
            moveStepByStep(token, path, currentPos + 1, newPos, ownerPlayer);
        }
    }

    private void moveStepByStep(TextView token, int[][] path, int currentStep, int targetStep, int ownerPlayer) {
        if (currentStep > targetStep) {
            boolean killedSomeone = checkKillLogic(path[targetStep][0], path[targetStep][1], ownerPlayer);
            updateBoardVisuals();

            if (targetStep == 56) {
                if (checkWinCondition(ownerPlayer)) {
                    isAnimating = false;
                    changeTurn();
                    return;
                }
            }

            if (lastDiceRoll == 6 || killedSomeone || targetStep == 56) prepareTurn();
            else changeTurn();

            return;
        }

        playSound(R.raw.move);

        int gridX = path[currentStep][0]; int gridY = path[currentStep][1];
        View board = findViewById(R.id.ludoBoardImage);
        float boxW = board.getWidth() / 15f; float boxH = board.getHeight() / 15f;
        float moveX = (board.getLeft() + (gridX * boxW) + (boxW / 2f)) - (token.getLeft() + (token.getWidth() / 2f));
        float moveY = (board.getTop() + (gridY * boxH) + (boxH / 2f)) - (token.getTop() + (token.getHeight() / 2f));

        token.animate().scaleX(1f).scaleY(1f).translationX(moveX).translationY(moveY).setDuration(150).withEndAction(() -> {
            moveStepByStep(token, path, currentStep + 1, targetStep, ownerPlayer);
        });
    }

    // --- LEADERBOARD, VISUALS, & DEATH ANIMATION LOGIC ---

    private void animateDeath(TextView token, int[][] path, int currentStep) {
        token.setTag("dying");

        if (currentStep < 0) {
            token.setTag(null);
            token.animate().scaleX(1f).scaleY(1f).translationX(0).translationY(0).setDuration(300);
            return;
        }

        int gridX = path[currentStep][0]; int gridY = path[currentStep][1];
        View board = findViewById(R.id.ludoBoardImage);
        float boxW = board.getWidth() / 15f; float boxH = board.getHeight() / 15f;
        float moveX = (board.getLeft() + (gridX * boxW) + (boxW / 2f)) - (token.getLeft() + (token.getWidth() / 2f));
        float moveY = (board.getTop() + (gridY * boxH) + (boxH / 2f)) - (token.getTop() + (token.getHeight() / 2f));

        token.animate().scaleX(1f).scaleY(1f).translationX(moveX).translationY(moveY).setDuration(25).withEndAction(() -> {
            animateDeath(token, path, currentStep - 1);
        });
    }

    private boolean checkWinCondition(int ownerPlayer) {
        int[] mem = (ownerPlayer == greenOwner) ? greenMem : (ownerPlayer == redOwner) ? redMem : (ownerPlayer == yellowOwner) ? yellowMem : blueMem;
        for (int pos : mem) { if (pos != 56) return false; }
        if (winOrder.contains(ownerPlayer)) return true;

        if (ownerPlayer == greenOwner) greenWon = true; if (ownerPlayer == redOwner) redWon = true;
        if (ownerPlayer == yellowOwner) yellowWon = true; if (ownerPlayer == blueOwner) blueWon = true;

        winOrder.add(ownerPlayer);
        if (autoRollTask != null) timerHandler.removeCallbacks(autoRollTask);

        TextView[] tokens = (ownerPlayer == greenOwner) ? greenTokens : (ownerPlayer == redOwner) ? redTokens : (ownerPlayer == yellowOwner) ? yellowTokens : blueTokens;
        renderCrown(tokens);

        if (winOrder.size() == totalPlayers - 1) {
            ArrayList<Integer> allPlaying = new ArrayList<>();
            if (greenOwner != 0) allPlaying.add(greenOwner); if (redOwner != 0) allPlaying.add(redOwner);
            if (yellowOwner != 0) allPlaying.add(yellowOwner); if (blueOwner != 0) allPlaying.add(blueOwner);
            for (int p : allPlaying) { if (!winOrder.contains(p)) { winOrder.add(p); break; } }
            showLeaderboard();
        } else {
            String winnerName = (ownerPlayer == greenOwner) ? nameP1 : (ownerPlayer == redOwner) ? nameP2 : (ownerPlayer == yellowOwner) ? nameP3 : nameP4;
            String place = (winOrder.size() == 1) ? "1st" : (winOrder.size() == 2) ? "2nd" : "3rd";

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🏆 " + place + " Place! 🏆");
            builder.setMessage("Congratulations " + winnerName + "! You finished in " + place + " place!");
            builder.setCancelable(false);
            builder.setPositiveButton("Back to Main Menu", (dialog, which) -> { getSharedPreferences("NeonLudoSave", MODE_PRIVATE).edit().remove("LoadGameID").apply(); finish(); });
            builder.setNegativeButton("Keep Watching", (dialog, which) -> { });
            builder.show();
        }
        return true;
    }

    private void showLeaderboard() {
        StringBuilder sb = new StringBuilder();
        String[] ranks = {"🥇 1st Place: ", "🥈 2nd Place: ", "🥉 3rd Place: ", "💀 4th Place: "};

        for (int i = 0; i < winOrder.size(); i++) {
            int owner = winOrder.get(i);
            String name = (owner == greenOwner) ? nameP1 : (owner == redOwner) ? nameP2 : (owner == yellowOwner) ? nameP3 : nameP4;
            String colorStr = (owner == greenOwner) ? "Green" : (owner == redOwner) ? "Red" : (owner == yellowOwner) ? "Yellow" : "Blue";
            sb.append(ranks[i]).append(name).append(" (").append(colorStr).append(")\n\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🏆 GAME OVER! 🏆");
        builder.setMessage(sb.toString().trim());
        builder.setCancelable(false);
        builder.setPositiveButton("Back to Main Menu", (dialog, which) -> { getSharedPreferences("NeonLudoSave", MODE_PRIVATE).edit().remove("LoadGameID").apply(); finish(); });
        builder.show();
    }

    private void renderCrown(TextView[] tokens) {
        tokens[1].setVisibility(View.INVISIBLE); tokens[2].setVisibility(View.INVISIBLE); tokens[3].setVisibility(View.INVISIBLE);
        tokens[0].setVisibility(View.VISIBLE); tokens[0].setBackgroundResource(0);
        tokens[0].setText("👑"); tokens[0].setTextSize(40f);
        float density = getResources().getDisplayMetrics().density;
        tokens[0].animate().scaleX(1f).scaleY(1f).translationX(21 * density).translationY(21 * density).setDuration(500);
    }

    private void updateBoardVisuals() {
        View board = findViewById(R.id.ludoBoardImage);
        float boxW = board.getWidth() / 15f; float boxH = board.getHeight() / 15f;
        float density = getResources().getDisplayMetrics().density;

        java.util.ArrayList<TextView>[][] grid = new java.util.ArrayList[15][15];
        for(int i=0; i<15; i++) for(int j=0; j<15; j++) grid[i][j] = new java.util.ArrayList<>();

        if (!greenWon) placeTokensInGrid(grid, greenMem, greenPath, greenTokens);
        if (!redWon) placeTokensInGrid(grid, redMem, redPath, redTokens);
        if (!yellowWon) placeTokensInGrid(grid, yellowMem, yellowPath, yellowTokens);
        if (!blueWon) placeTokensInGrid(grid, blueMem, bluePath, blueTokens);

        float[][] offsets = {{-6f, -6f}, {6f, 6f}, {-6f, 6f}, {6f, -6f}, {0f, -10f}, {0f, 10f}};

        for(int i=0; i<15; i++) {
            for(int j=0; j<15; j++) {
                java.util.ArrayList<TextView> tokensHere = grid[i][j];
                int count = tokensHere.size();
                if (count == 0) continue;

                float baseMoveX = (board.getLeft() + (i * boxW) + (boxW / 2f));
                float baseMoveY = (board.getTop() + (j * boxH) + (boxH / 2f));

                if (count == 1) {
                    TextView t = tokensHere.get(0);

                    boolean movable = isTokenMovable(t);
                    t.setBackground(createTokenShape(getTokenColor(t), movable));

                    t.setTranslationZ(movable ? 15f : 0f);
                    float scale = movable ? 1.4f : 1f;

                    float moveX = baseMoveX - (t.getLeft() + t.getWidth() / 2f);
                    float moveY = baseMoveY - (t.getTop() + t.getHeight() / 2f);
                    t.animate().scaleX(scale).scaleY(scale).translationX(moveX).translationY(moveY).setDuration(250);
                } else {
                    for(int k=0; k<count; k++) {
                        TextView t = tokensHere.get(k);

                        boolean movable = isTokenMovable(t);
                        t.setBackground(createTokenShape(getTokenColor(t), movable));

                        t.setTranslationZ(movable ? 15f : 0f);
                        float scale = movable ? 0.85f : 0.6f;

                        float offX = (k < offsets.length) ? offsets[k][0] * density : 0;
                        float offY = (k < offsets.length) ? offsets[k][1] * density : 0;
                        float moveX = baseMoveX - (t.getLeft() + t.getWidth() / 2f) + offX;
                        float moveY = baseMoveY - (t.getTop() + t.getHeight() / 2f) + offY;
                        t.animate().scaleX(scale).scaleY(scale).translationX(moveX).translationY(moveY).setDuration(250);
                    }
                }
            }
        }
    }

    private void placeTokensInGrid(java.util.ArrayList<TextView>[][] grid, int[] mem, int[][] path, TextView[] tokens) {
        for(int i=0; i<4; i++) {
            if(tokens[i] == null) continue;
            if (mem[i] == -1) {
                if (!"dying".equals(tokens[i].getTag())) {
                    boolean movable = isTokenMovable(tokens[i]);
                    tokens[i].setBackground(createTokenShape(getTokenColor(tokens[i]), movable));

                    tokens[i].setTranslationZ(movable ? 15f : 0f);
                    float scale = movable ? 1.4f : 1f;

                    tokens[i].animate().scaleX(scale).scaleY(scale).translationX(0).translationY(0).setDuration(300);
                }
            } else {
                grid[path[mem[i]][0]][path[mem[i]][1]].add(tokens[i]);
            }
        }
    }

    private boolean checkKillLogic(int gridX, int gridY, int attackerNum) {
        for (int[] safe : safeHouses) { if (safe[0] == gridX && safe[1] == gridY) return false; }
        boolean killed = false;
        if (greenOwner != 0 && greenOwner != attackerNum && countPiecesAt(greenMem, greenPath, gridX, gridY) < 2) killed |= executeKill(greenMem, greenPath, greenTokens, gridX, gridY);
        if (redOwner != 0 && redOwner != attackerNum && countPiecesAt(redMem, redPath, gridX, gridY) < 2) killed |= executeKill(redMem, redPath, redTokens, gridX, gridY);
        if (yellowOwner != 0 && yellowOwner != attackerNum && countPiecesAt(yellowMem, yellowPath, gridX, gridY) < 2) killed |= executeKill(yellowMem, yellowPath, yellowTokens, gridX, gridY);
        if (blueOwner != 0 && blueOwner != attackerNum && countPiecesAt(blueMem, bluePath, gridX, gridY) < 2) killed |= executeKill(blueMem, bluePath, blueTokens, gridX, gridY);

        if (killed) {
            vibratePhone(500);
            playSound(R.raw.kill);
        }
        return killed;
    }

    private void checkBrokenJoint(int gridX, int gridY, int ownerPlayer) {
        // Safe houses (stars) are absolutely immune to squatter joint kills!
        for (int[] safe : safeHouses) {
            if (safe[0] == gridX && safe[1] == gridY) return;
        }

        int[] mem = (ownerPlayer == greenOwner) ? greenMem : (ownerPlayer == redOwner) ? redMem : (ownerPlayer == yellowOwner) ? yellowMem : blueMem;
        int[][] path = (ownerPlayer == greenOwner) ? greenPath : (ownerPlayer == redOwner) ? redPath : (ownerPlayer == yellowOwner) ? yellowPath : bluePath;
        TextView[] tokens = (ownerPlayer == greenOwner) ? greenTokens : (ownerPlayer == redOwner) ? redTokens : (ownerPlayer == yellowOwner) ? yellowTokens : blueTokens;

        if (countPiecesAt(mem, path, gridX, gridY) == 1) {
            boolean enemySquatter = false;
            if (greenOwner != 0 && greenOwner != ownerPlayer && countPiecesAt(greenMem, greenPath, gridX, gridY) > 0) enemySquatter = true;
            if (redOwner != 0 && redOwner != ownerPlayer && countPiecesAt(redMem, redPath, gridX, gridY) > 0) enemySquatter = true;
            if (yellowOwner != 0 && yellowOwner != ownerPlayer && countPiecesAt(yellowMem, yellowPath, gridX, gridY) > 0) enemySquatter = true;
            if (blueOwner != 0 && blueOwner != ownerPlayer && countPiecesAt(blueMem, bluePath, gridX, gridY) > 0) enemySquatter = true;

            if (enemySquatter) {
                executeKill(mem, path, tokens, gridX, gridY);
                vibratePhone(500);
                playSound(R.raw.kill);
            }
        }
    }

    private int countPiecesAt(int[] mem, int[][] path, int gridX, int gridY) {
        int count = 0;
        for (int i=0; i<4; i++) { if (mem[i] != -1 && path[mem[i]][0] == gridX && path[mem[i]][1] == gridY) count++; }
        return count;
    }

    private boolean executeKill(int[] enemyMem, int[][] enemyPath, TextView[] enemyTokens, int targetX, int targetY) {
        boolean killed = false;
        for (int i = 0; i < 4; i++) {
            if (enemyMem[i] != -1 && enemyPath[enemyMem[i]][0] == targetX && enemyPath[enemyMem[i]][1] == targetY) {
                int startPos = enemyMem[i];
                enemyMem[i] = -1;
                killed = true;
                animateDeath(enemyTokens[i], enemyPath, startPos);
            }
        }
        return killed;
    }

    private void updateDiceVisuals(int owner, int roll) {
        String[] dots = {"", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};
        if (owner == greenOwner) btnDiceGreen.setText(dots[roll]);
        else if (owner == redOwner) btnDiceRed.setText(dots[roll]);
        else if (owner == yellowOwner) btnDiceYellow.setText(dots[roll]);
        else if (owner == blueOwner) btnDiceBlue.setText(dots[roll]);
    }
}