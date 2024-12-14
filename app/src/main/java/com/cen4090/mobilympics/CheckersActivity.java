package com.cen4090.mobilympics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CheckersActivity extends BaseGameActivity {
    @Override
    protected void onGameDataReceived(String data){
        if(data.startsWith("RESTART")){
            ResetGame();
            return;
        }
        int selectedRow = Integer.parseInt(data.substring(0, 1));
        int selectedCol = Integer.parseInt(data.substring(1, 2));
        int row = Integer.parseInt(data.substring(2, 3));
        int col = Integer.parseInt(data.substring(3, 4));
        game.makeMove(selectedRow, selectedCol, row, col);
        updateBoard();
        game.toggleTurn();

        if (game.isGameOver()) {
            Toast.makeText(this, "Game Over! " + (game.isRedTurn() ? "Black Wins!" : "Red Wins!"), Toast.LENGTH_LONG).show();
        }

        buttons[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray));
    }
    private void ResetGame(){
        game = new Game();
        game.toggleTurn();
        updateBoard();
        restartButton.setVisibility(View.INVISIBLE);
    }
    private void updateBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = game.getBoard().getPieceAt(row, col);
                if (piece != null) {
                    buttons[row][col].setText(piece.isKing() ? "K" : "O");
                    buttons[row][col].setTextColor(piece.isRed() ? getResources().getColor(android.R.color.holo_red_dark) : getResources().getColor(android.R.color.black));
                    buttons[row][col].invalidate();

                } else {
                    buttons[row][col].setText(" ");
                    buttons[row][col].setBackgroundColor((row + col) % 2 == 0 ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray));
                    buttons[row][col].invalidate();
                }
            }
        }
    }
    private void onBoardCellClick(int row, int col) {
        Piece piece = game.getBoard().getPieceAt(row, col);

        if (selectedRow == row && selectedCol == col) {
            buttons[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray));
            selectedRow = -1;
            selectedCol = -1;
            return;
        }

        if (selectedRow == -1 && selectedCol == -1) {
            if (piece != null && (piece.isRed() && game.isRedTurn() || !piece.isRed() && !game.isRedTurn())) {
                selectedRow = row;
                selectedCol = col;
                buttons[row][col].setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            }
        } else {
            if (piece == null) {
                boolean validMove = game.makeMove(selectedRow, selectedCol, row, col);
                if (validMove) {
                    updateBoard();
                    game.toggleTurn();
                    StringBuilder str = new StringBuilder();
                    str.append(selectedRow);
                    str.append(selectedCol);
                    str.append(row);
                    str.append(col);
                    wifiService.sendData(str.toString());

                    if (game.isGameOver()) {
                        Toast.makeText(this, "Game Over! " + (game.isRedTurn() ? "Black Wins!" : "Red Wins!"), Toast.LENGTH_LONG).show();
                    }

                    buttons[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray));
                    selectedRow = -1;
                    selectedCol = -1;
                }
            } else {
                if (piece.isRed() != game.isRedTurn()) {
                    if (selectedRow != -1 && selectedCol != -1) {
                        buttons[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray));
                    }
                    selectedRow = -1;
                    selectedCol = -1;
                    return;
                }
                selectedRow = row;
                selectedCol = col;
                buttons[row][col].setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            }
        }
    }
    public class Piece{
        private boolean isKing;
        private final boolean isRed;

        public Piece(boolean isRed) {
            this.isRed = isRed;
            this.isKing = false;
        }

        public boolean isKing() {
            return isKing;
        }

        public void crown() {
            this.isKing = true;
        }

        public boolean isRed() {
            return isRed;
        }

        public boolean isBlack() {
            return !isRed;
        }
    }

    public class Board {
        private final Piece[][] board;

        public Board() {
            board = new Piece[8][8];
            //initializeBoard();
            //}

            //public void initializeBoard(){
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 8; col++) {
                    if ((row + col) % 2 != 0) {
                        board[row][col] = new Piece(true); // Red pieces
                    }
                }
            }
            for (int row = 5; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ((row + col) % 2 != 0) {
                        board[row][col] = new Piece(false); // Black pieces
                    }
                }
            }
        }

        public Piece getPieceAt(int row, int col) {
            return board[row][col];
        }

        public void movePiece(int startRow, int startCol, int endRow, int endCol) {
            board[endRow][endCol] = board[startRow][startCol];
            board[startRow][startCol] = null;
        }

        public void removePiece(int row, int col) {
            board[row][col] = null;
        }

        public void promotePiece(int row, int col) {
            if (row == 7 && board[row][col].isRed()) {
                board[row][col].crown();
            } else if (row == 0 && board[row][col].isBlack()) {
                board[row][col].crown();
            }
        }

    }
    public class Game {
        private final Board board;
        private boolean isRedTurn;
        public int winner = 0;
        public Game() {
            board = new Board();
            isRedTurn = true;
        }

        public boolean isRedTurn() {
            return isRedTurn;
        }

        public void toggleTurn() {
            isRedTurn = !isRedTurn;
            turnIndicator.setTextColor(isRedTurn ? getResources().getColor(android.R.color.holo_red_dark) : getResources().getColor(android.R.color.black));
            if(isRedTurn){
                if(amRed){
                    turnIndicator.setText("It/'s your (red) turn!");
                }else{
                    turnIndicator.setText("It/'s your opponent/'s (red) turn!");
                }
            }else{
                if(amRed){
                    turnIndicator.setText("It/'s your opponent/'s (black) turn!");
                }else{
                    turnIndicator.setText("It/'s your (black) turn!");
                }
            }
        }

        // Your existing makeMove, isGameOver, and other methods remain the same
        public Board getBoard(){
            return board;
        }
        public boolean makeMove(int startRow, int startCol, int endRow, int endCol) {
            Piece piece = game.getBoard().getPieceAt(startRow, startCol);
            if (piece == null) {
                return false; // No piece at the start position
            }

            if (piece.isKing()) {
                // King can move one square or jump over another piece
                if (Math.abs(startRow - endRow) == 1 && Math.abs(startCol - endCol) == 1) {
                    game.getBoard().movePiece(startRow, startCol, endRow, endCol);
                    game.getBoard().promotePiece(endRow, endCol);
                    return true;
                } else if (Math.abs(startRow - endRow) == 2 && Math.abs(startCol - endCol) == 2) {
                    int midRow = (startRow + endRow) / 2;
                    int midCol = (startCol + endCol) / 2;
                    Piece midPiece = game.getBoard().getPieceAt(midRow, midCol);

                    if (midPiece != null && midPiece.isRed() != piece.isRed()) {
                        game.getBoard().movePiece(startRow, startCol, endRow, endCol);
                        game.getBoard().removePiece(midRow, midCol);
                        game.getBoard().promotePiece(endRow, endCol);
                        return true;
                    }
                }
            } else {
                // Regular piece movement or capture
                if (Math.abs(startRow - endRow) == 1 && Math.abs(startCol - endCol) == 1) {
                    // Regular move
                    if ((piece.isRed() && endRow > startRow) || (!piece.isRed() && endRow < startRow)) {
                        game.getBoard().movePiece(startRow, startCol, endRow, endCol);
                        game.getBoard().promotePiece(endRow, endCol);
                        return true;
                    }
                } else if (Math.abs(startRow - endRow) == 2 && Math.abs(startCol - endCol) == 2) {
                    // Capture move
                    int midRow = (startRow + endRow) / 2;
                    int midCol = (startCol + endCol) / 2;
                    Piece midPiece = game.getBoard().getPieceAt(midRow, midCol);

                    if (midPiece != null && midPiece.isRed() != piece.isRed()) {
                        if ((piece.isRed() && endRow > startRow) || (!piece.isRed() && endRow < startRow)) {
                            game.getBoard().movePiece(startRow, startCol, endRow, endCol);
                            game.getBoard().removePiece(midRow, midCol);
                            game.getBoard().promotePiece(endRow, endCol);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        public boolean isGameOver() {
            boolean redHasPieces = false;
            boolean blackHasPieces = false;

            // Check if either player has no pieces left
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = game.getBoard().getPieceAt(row, col);
                    if (piece != null) {
                        if (piece.isRed()) {
                            redHasPieces = true;
                        } else {
                            blackHasPieces = true;
                        }
                    }
                }
            }

            // Check if game is over due to missing pieces
            if (!redHasPieces) {
                endGame(2, "Black wins!");
                return true;
            } else if (!blackHasPieces) {
                endGame(1, "Red Wins!");
                return true;
            }

            // Check if current player can make any moves
            boolean currentPlayerCanMove = canPlayerMove(isRedTurn() ? true : false);
            if (!currentPlayerCanMove) {
                endGame(2, "It's a draw!");
                return true;
            }

            return false;
        }
        private boolean canPlayerMove(boolean isRedPlayer) {
            // Loop through the board to check for a piece of the current player
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.getPieceAt(row, col);
                    // If the piece is not null and belongs to the current player
                    if (piece != null && piece.isRed() == isRedPlayer) {
                        // Check if this piece has any valid moves
                        if (canPieceMove(row, col, piece)) {
                            return true;
                        }
                    }
                }
            }
            return false; // No valid move found for the player
        }
        private boolean canPieceMove(int row, int col, Piece piece) {
            // Directions for regular move (up/down and left/right)
            int[] directions = {-1, 1};

            // If the piece is a king, it can move in all four diagonal directions
            if (piece.isKing()) {
                for (int dr : directions) {
                    for (int dc : directions) {
                        int newRow = row + dr;
                        int newCol = col + dc;
                        // Check if the move is within bounds and valid
                        if (isValidMove(row, col, newRow, newCol, piece)) {
                            return true;
                        }
                    }
                }
            } else {
                // Regular piece moves diagonally forward (1 square)
                if (piece.isRed()) {
                    // Red piece moves down the board
                    int newRow = row + 1;
                    for (int dc : directions) {
                        int newCol = col + dc;
                        if (isValidMove(row, col, newRow, newCol, piece)) {
                            return true;
                        }
                    }
                } else {
                    // Black piece moves up the board
                    int newRow = row - 1;
                    for (int dc : directions) {
                        int newCol = col + dc;
                        if (isValidMove(row, col, newRow, newCol, piece)) {
                            return true;
                        }
                    }
                }
            }

            return false;  // No valid move found for this piece
        }
        private boolean isValidMove(int startRow, int startCol, int endRow, int endCol, Piece piece) {
            // Check if the destination is out of bounds
            if (endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
                return false;  // Move is out of bounds
            }

            // Check if the destination square is already occupied
            Piece targetPiece = board.getPieceAt(endRow, endCol);
            if (targetPiece != null) {
                return false;  // Destination is already occupied
            }

            // Regular pieces can only move diagonally and one square at a time
            if (!piece.isKing()) {
                if (Math.abs(startRow - endRow) == 1 && Math.abs(startCol - endCol) == 1) {
                    // Ensure regular pieces move in the correct direction
                    if (piece.isRed() && endRow > startRow) {
                        return true;  // Red pieces can only move down the board
                    } else if (!piece.isRed() && endRow < startRow) {
                        return true;  // Black pieces can only move up the board
                    }
                }
            } else {
                // Kings can move diagonally in any direction (1 square)
                if (Math.abs(startRow - endRow) == 1 && Math.abs(startCol - endCol) == 1) {
                    return true;
                }
            }

            return false;  // Move doesn't meet any valid conditions
        }

        private void endGame(int outcome, final String message) {
            if(outcome == 1){
                turnIndicator.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }else{
                turnIndicator.setTextColor(getResources().getColor(android.R.color.black));
            }
            turnIndicator.setText(message);
            restartButton.setVisibility(View.VISIBLE);
        }
    }
    private Game game;
    private Button[][] buttons;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Button restartButton;
    private TextView turnIndicator;
    private boolean amRed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_checkers); // Use a custom layout file
        restartButton = findViewById(R.id.play_again_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View v) {
                                                    ResetGame();
                                                    wifiService.sendData("RESTART");
                                             }
                                         });
        turnIndicator = findViewById(R.id.turn_indicator);
        game = new Game();
        buttons = new Button[8][8];
        for(int row = 0; row < 8; row++){
            for(int col = 0; col < 8; col++){
                String name = "box_" + row + "_" + col;
                int id = getResources().getIdentifier(name, "id", getPackageName());

                buttons[row][col] = (Button) findViewById(id);;
                buttons[row][col].setText(" "); // Default state for empty squares
                int finalRow = row;
                int finalCol = col;
                buttons[row][col].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(amRed == game.isRedTurn()){
                        onBoardCellClick(finalRow, finalCol);
                        }
                    }
                });
            }
        }
        boolean isHost = getIntent().getBooleanExtra("isHost", false);
        if(isHost){
            amRed = false;
        }else{
            amRed = true;
        }
        updateBoard();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}