package com.cen4090.mobilympics;

public class MancalaGame {
    // Constants
    public static final int TOTAL_PITS = 14; // 6 pits per player + 2 stores
    public static final int PLAYER_1_STORE = 7;
    public static final int PLAYER_2_STORE = 14;

    // Game board representing the pits and stores
    private final int[] mGameBoard;
    private int currentPlayer;

    // Constructor
    public MancalaGame() {
        mGameBoard = new int[TOTAL_PITS + 1]; // 1-based indexing
        newGame();
    }

    // Starts a new game
    public void newGame() {
        for (int i = 1; i <= TOTAL_PITS; i++) {
            mGameBoard[i] = (i == PLAYER_1_STORE || i == PLAYER_2_STORE) ? 0 : 4;
        }
        currentPlayer = 1; // Player 1 starts
    }

    // Gets the stones in a specific pit or store
    public int getPitContents(int index) {
        return mGameBoard[index];
    }

    // Makes a move from the selected pit
    public boolean makeMove(int pit) {
        if (!isValidMove(pit)) {
            return false; // Invalid move
        }

        int stones = mGameBoard[pit];
        mGameBoard[pit] = 0; // Empty the selected pit
        int currentPit = pit;

        // Distribute stones across pits and stores
        while (stones > 0) {
            currentPit = (currentPit % TOTAL_PITS) + 1;

            // Skip opponent's store
            if ((currentPlayer == 1 && currentPit == PLAYER_2_STORE) ||
                    (currentPlayer == 2 && currentPit == PLAYER_1_STORE)) {
                continue;
            }

            // Add one stone to the current pit or store
            mGameBoard[currentPit]++;
            stones--;
        }

        // If the last stone lands in the player's store, the player gets another turn
        if ((currentPlayer == 1 && currentPit == PLAYER_1_STORE) ||
                (currentPlayer == 2 && currentPit == PLAYER_2_STORE)) {
            return true; // Player gets another turn
        }

        // Handle capture logic
        if (isOwnPit(currentPit) && mGameBoard[currentPit] == 1) {
            int opponentPit = getOppositePit(currentPit);
            if (mGameBoard[opponentPit] > 0) {
                int capturedStones = mGameBoard[opponentPit] + 1; // Include last placed stone
                mGameBoard[opponentPit] = 0;
                mGameBoard[currentPit] = 0;
                mGameBoard[getStore(currentPlayer)] += capturedStones;
            }
        }

        // Switch to the other player
        switchPlayer();
        return true;
    }

    // Validates the move
    private boolean isValidMove(int pit) {
        return pit >= 1 && pit <= TOTAL_PITS &&
                isOwnPit(pit) &&
                mGameBoard[pit] > 0; // Non-empty pit
    }

    // Checks if the pit belongs to the current player
    private boolean isOwnPit(int pit) {
        return (currentPlayer == 1 && pit >= 1 && pit <= 6) ||
                (currentPlayer == 2 && pit >= 8 && pit <= 13);
    }

    // Returns the store index for the given player
    private int getStore(int player) {
        return (player == 1) ? PLAYER_1_STORE : PLAYER_2_STORE;
    }

    // Returns the opposite pit index for capturing
    private int getOppositePit(int pit) {
        return 15 - pit; // Opposite pit index based on the board layout
    }

    // Switches the current player
    private void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    // Returns the current player
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    // Checks if the game is over
    public boolean isGameOver() {
        boolean player1SideEmpty = true;
        boolean player2SideEmpty = true;

        for (int i = 1; i <= 6; i++) {
            if (mGameBoard[i] > 0) {
                player1SideEmpty = false;
                break;
            }
        }
        for (int i = 8; i <= 13; i++) {
            if (mGameBoard[i] > 0) {
                player2SideEmpty = false;
                break;
            }
        }

        return player1SideEmpty || player2SideEmpty;
    }

    // Determines the winner
    public int getWinner() {
        if (!isGameOver()) {
            return -1; // Game is not over
        }

        // Move all remaining stones to respective stores
        for (int i = 1; i <= 6; i++) {
            mGameBoard[PLAYER_1_STORE] += mGameBoard[i];
            mGameBoard[i] = 0;
        }
        for (int i = 8; i <= 13; i++) {
            mGameBoard[PLAYER_2_STORE] += mGameBoard[i];
            mGameBoard[i] = 0;
        }

        // Compare the stores
        if (mGameBoard[PLAYER_1_STORE] > mGameBoard[PLAYER_2_STORE]) {
            return 1; // Player 1 wins
        } else if (mGameBoard[PLAYER_1_STORE] < mGameBoard[PLAYER_2_STORE]) {
            return 2; // Player 2 wins
        } else {
            return 0; // Tie
        }
    }
}