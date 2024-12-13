package com.cen4090.mobilympics;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/* MANCALA Activity - Game Screen for Mancala Game */

public class MancalaActivity extends BaseGameActivity {

    private MancalaGame mancalaGame;
    private TextView player1Store, player2Store, turnIndicator;
    private Button[] pitButtons;

    private boolean isHost;
    private boolean isMyTurn;
    private int myPlayerNumber; // 1 or 2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mancala);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mancala_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get host status and set player numbers
        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        myPlayerNumber = isHost ? 1 : 2;
        isMyTurn = isHost; // Player 1 (host) goes first

        // Initialize game logic and UI components
        mancalaGame = new MancalaGame();
        mancalaGame.newGame();

        // Link UI components
        player1Store = findViewById(R.id.pit_7);  // Player 1's store
        player2Store = findViewById(R.id.pit_14); // Player 2's store
        turnIndicator = findViewById(R.id.mancala_turn_indicator);

        // Link pit buttons
        initializePitButtons();

        // Update the board for the initial state
        updateBoard();
        updateTurnIndicator();

        // Disable opponent's pits
        enableMyPits(isMyTurn);
    }

    private void initializePitButtons() {
        pitButtons = new Button[]{
                findViewById(R.id.pit_1), findViewById(R.id.pit_2), findViewById(R.id.pit_3),
                findViewById(R.id.pit_4), findViewById(R.id.pit_5), findViewById(R.id.pit_6),
                null, // Player 1's store is not clickable
                findViewById(R.id.pit_8), findViewById(R.id.pit_9), findViewById(R.id.pit_10),
                findViewById(R.id.pit_11), findViewById(R.id.pit_12), findViewById(R.id.pit_13),
                null // Player 2's store is not clickable
        };

        for (int i = 0; i < pitButtons.length; i++) {
            if (pitButtons[i] != null) {
                final int pitIndex = i + 1;
                pitButtons[i].setOnClickListener(v -> onGameButtonClick(pitIndex));
            }
        }
    }

    private void onGameButtonClick(int pitIndex) {
        if (!isMyTurn) return;

        boolean validMove = mancalaGame.makeMove(pitIndex);
        if (validMove) {
            // Send move to opponent
            if (wifiService != null) {
                wifiService.sendData("MOVE:" + pitIndex);
            }

            updateBoard();

            // Check if we get another turn (landed in our store)
            boolean getAnotherTurn =
                    (myPlayerNumber == 1 && pitIndex == MancalaGame.PLAYER_1_STORE) ||
                            (myPlayerNumber == 2 && pitIndex == MancalaGame.PLAYER_2_STORE);

            if (!getAnotherTurn) {
                isMyTurn = false;
                enableMyPits(false);
                updateTurnIndicator();
            }
        }
    }

    private void enableMyPits(boolean enable) {
        int start = (myPlayerNumber == 1) ? 0 : 7;
        int end = (myPlayerNumber == 1) ? 6 : 13;

        for (int i = start; i < end; i++) {
            if (pitButtons[i] != null) {
                pitButtons[i].setEnabled(enable && mancalaGame.getPitContents(i + 1) > 0);
            }
        }
    }

    private void updateTurnIndicator() {
        if (mancalaGame.isGameOver()) {
            int winner = mancalaGame.getWinner();
            String winnerMessage;
            if (winner == 0) {
                winnerMessage = "It's a tie!";
            } else {
                winnerMessage = (winner == myPlayerNumber) ? "You Win!" : "Opponent Wins!";
            }
            turnIndicator.setText(winnerMessage);
        } else {
            turnIndicator.setText(isMyTurn ? "Your Turn" : "Opponent's Turn");
        }
    }

    private void updateBoard() {
        try {
            // Update pit buttons
            for (int i = 1; i <= 13; i++) {
                if (pitButtons[i - 1] != null) {
                    pitButtons[i - 1].setText(String.valueOf(mancalaGame.getPitContents(i)));
                }
            }

            // Update stores
            player1Store.setText(String.valueOf(mancalaGame.getPitContents(MancalaGame.PLAYER_1_STORE)));
            player2Store.setText(String.valueOf(mancalaGame.getPitContents(MancalaGame.PLAYER_2_STORE)));

            if (mancalaGame.isGameOver()) {
                handleGameOver();
            }
        } catch (Exception e) {
            Log.e("MancalaDebug", "Error updating board: " + e.getMessage(), e);
        }
    }

    private void handleGameOver() {
        disableAllButtons();
        updateTurnIndicator();

        // Notify opponent of game over
        if (wifiService != null) {
            wifiService.sendData("GAME_OVER");
        }
    }

    private void disableAllButtons() {
        for (Button button : pitButtons) {
            if (button != null) {
                button.setEnabled(false);
            }
        }
    }

    @Override
    protected void onGameDataReceived(String data) {
        if (data.startsWith("MOVE:")) {
            int pitIndex = Integer.parseInt(data.substring(5));

            // Make the opponent's move
            boolean validMove = mancalaGame.makeMove(pitIndex);

            if (validMove) {
                runOnUiThread(() -> {
                    updateBoard();

                    // Check if opponent gets another turn (landed in their store)
                    boolean opponentGetsAnotherTurn =
                            (myPlayerNumber == 2 && pitIndex == MancalaGame.PLAYER_1_STORE) ||
                                    (myPlayerNumber == 1 && pitIndex == MancalaGame.PLAYER_2_STORE);

                    if (!opponentGetsAnotherTurn) {
                        isMyTurn = true;
                        enableMyPits(true);
                        updateTurnIndicator();
                    }
                });
            }
        } else if (data.equals("GAME_OVER")) {
            runOnUiThread(this::handleGameOver);
        }
    }
}

/* ARLIE'S VERSION - Above is Jason's VERY quick attempt at homogenizing with the wifiService.
public class MancalaActivity extends AppCompatActivity {

    private MancalaGame mancalaGame; // Game logic
    private TextView player1Store, player2Store, turnIndicator;
    private Button[] pitButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mancala);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize game logic and UI components
        mancalaGame = new MancalaGame();
        mancalaGame.newGame();

        // Link UI components
        player1Store = findViewById(R.id.pit_7);  // Player 1's store
        player2Store = findViewById(R.id.pit_14); // Player 2's store
        turnIndicator = findViewById(R.id.mancala_turn_indicator);

        // Link pit buttons
        initializePitButtons();

        // Update the board for the initial state
        updateBoard();
    }

    // Initialize pit buttons and add click listeners
    private void initializePitButtons() {
        pitButtons = new Button[]{
                findViewById(R.id.pit_1), findViewById(R.id.pit_2), findViewById(R.id.pit_3),
                findViewById(R.id.pit_4), findViewById(R.id.pit_5), findViewById(R.id.pit_6),
                null, // Player 1's store is not clickable
                findViewById(R.id.pit_8), findViewById(R.id.pit_9), findViewById(R.id.pit_10),
                findViewById(R.id.pit_11), findViewById(R.id.pit_12), findViewById(R.id.pit_13),
                null // Player 2's store is not clickable
        };

        for (int i = 0; i < pitButtons.length; i++) {
            if (pitButtons[i] != null) {
                final int pitIndex = i + 1; // Pit IDs are 1-based
                pitButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onGameButtonClick(pitIndex);
                    }
                });
            }
        }
    }

    private void onGameButtonClick(int pitIndex) {
        boolean validMove = mancalaGame.makeMove(pitIndex);
        if (validMove) {
            updateBoard();
        } else {
            Log.d("MancalaDebug", "Invalid move from pit " + pitIndex);
        }
    }

    private void updateBoard() {
        try {
            // Update pit buttons
            for (int i = 1; i <= 13; i++) { // Iterate through all pits except stores
                if (pitButtons[i - 1] != null) {
                    pitButtons[i - 1].setText(String.valueOf(mancalaGame.getPitContents(i)));
                }
            }

            // Update stores
            player1Store.setText(String.valueOf(mancalaGame.getPitContents(MancalaGame.PLAYER_1_STORE)));
            player2Store.setText(String.valueOf(mancalaGame.getPitContents(MancalaGame.PLAYER_2_STORE)));

            // Update turn indicator
            int currentPlayer = mancalaGame.getCurrentPlayer();
            turnIndicator.setText("Player " + currentPlayer + "'s Turn");

            // Log the current store values for debugging
            Log.d("MancalaDebug", "Player 1 Store: " + mancalaGame.getPitContents(MancalaGame.PLAYER_1_STORE) +
                    ", Player 2 Store: " + mancalaGame.getPitContents(MancalaGame.PLAYER_2_STORE));

            // Handle game over
            if (mancalaGame.isGameOver()) {
                handleGameOver();
            }
        } catch (Exception e) {
            Log.e("MancalaDebug", "Error updating board: " + e.getMessage(), e);
        }
    }

    private void handleGameOver() {
        int winner = mancalaGame.getWinner();
        String winnerMessage = (winner == 0) ? "It's a tie!" : "Player " + winner + " wins!";
        turnIndicator.setText(winnerMessage);
        disableAllButtons();
    }

    private void disableAllButtons() {
        for (Button button : pitButtons) {
            if (button != null) {
                button.setEnabled(false);
            }
        }
    }
}*/
