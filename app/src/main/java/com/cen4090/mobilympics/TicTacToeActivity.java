package com.cen4090.mobilympics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/* TicTacToe Activity - Game screen showing TicTacToe board, player turn indicator,
 * winner messages, replay buttons, etc*/

public class TicTacToeActivity extends BaseGameActivity {

    GridLayout gameBoard;
    TicTacToeGame game;
    TextView playerTurnIndicator;
    Button replayButton;
    Toolbar toolbar;
    TextView winnerText;

    private boolean isMyTurn;
    private boolean isHost;
    private char mySymbol;
    private char opponentSymbol;
    private int gameOverState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tic_tac_toe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        isHost = getIntent().getBooleanExtra("isHost", false);

        if (isHost){
            mySymbol = 'X';
            opponentSymbol = 'O';
            isMyTurn = true;
        } else {
            mySymbol = 'O';
            opponentSymbol = 'X';
            isMyTurn = false;
        }

        //Set up Toolbar here
        //TODO - setup toolbar

        //Set references for UI widgets
        gameBoard = findViewById(R.id.ttt_game_grid);
        playerTurnIndicator = findViewById(R.id.ttt_turn_indicator);
        replayButton = findViewById(R.id.ttt_play_again_button);
        winnerText = findViewById(R.id.ttt_winner_message);

        game = new TicTacToeGame();
        game.newGame();

        updateTurnIndicator();
        if (!isMyTurn){
            removeGameListeners();
        } else {
            setGameListeners();
        }

        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiService != null){
                    wifiService.sendData("RESTART");
                }
                handleRestart();
            }
        });
    }

    private void handleRestart(){
        game.newGame();
        updateUI();
        replayButton.setVisibility(View.INVISIBLE);
        winnerText.setVisibility(View.INVISIBLE);

        if (isMyTurn){
            setGameListeners();
        } else {
            removeGameListeners();
        }
        updateTurnIndicator();
    }

    private void updateTurnIndicator(){
        String turnMsg;
        if (isMyTurn){
            turnMsg = "Your turn (" + mySymbol + ")";
        } else {
            turnMsg = "Opponent's turn (" + opponentSymbol + ")";
        }
        playerTurnIndicator.setText(turnMsg);
    }

    @Override
    protected void onGameDataReceived(String data) {
        if (data.startsWith("MOVE:")){
            String[] moveData = data.substring(5).split(",");
            int row = Integer.parseInt(moveData[0]);
            int col = Integer.parseInt(moveData[1]);

            TicTacToeGame.PLAYERS opponentPlayer = (opponentSymbol == 'X') ? TicTacToeGame.PLAYERS.X : TicTacToeGame.PLAYERS.O;

            game.selectSquare(row, col, opponentPlayer);
            updateUI();
            isMyTurn = true;
            setGameListeners();
            updateTurnIndicator();

            gameOverState = checkGameOver();
        } else if (data.startsWith("RESTART")){
            handleRestart();
        }
    }


    public void setGameListeners(){
        for (int buttonIndex = 0; buttonIndex < gameBoard.getChildCount(); buttonIndex++){
            Button gridButton = (Button) gameBoard.getChildAt(buttonIndex);
            gridButton.setOnClickListener(this::onGameButtonClick);
        }
    }

    private void onGameButtonClick(View view){
        if (!isMyTurn){
            return;
        }

        //Set row and col indices
        int buttonIndex = gameBoard.indexOfChild(view);
        int row = buttonIndex / TicTacToeGame.GRID_SIZE;
        int col = buttonIndex % TicTacToeGame.GRID_SIZE;

        TicTacToeGame.PLAYERS currentPlayer = (mySymbol == 'X') ? TicTacToeGame.PLAYERS.X : TicTacToeGame.PLAYERS.O;

        //If a valid move was made, change CURRENT_PLAYER and update UI
        if (game.selectSquare(row, col, currentPlayer)){
            String moveData = "MOVE:" + row + "," + col;

            if (wifiService != null){
                wifiService.sendData(moveData);
            }

            updateUI();
            isMyTurn = false;
            removeGameListeners();
            updateTurnIndicator();

            checkGameOver();
        } else {        //Show invalid move toast
            Toast.makeText(this, R.string.invalid_move, Toast.LENGTH_SHORT).show();
        }
    }

    private int checkGameOver(){
        int gOState = game.isGameOver();
        if (gOState > 0){
            replayButton.setVisibility(View.VISIBLE);
            switch (gameOverState){
                case 1:
                    winnerText.setText(mySymbol == 'X' ? "You Win!" : "Opponent Wins!");
                    break;
                case 2:
                    winnerText.setText(mySymbol == 'O' ? "You Win!" : "Opponent Wins!");
                    break;
                case 3:
                    winnerText.setText(R.string.cat_wins);
                    break;
            }
            winnerText.setVisibility(View.VISIBLE);
            removeGameListeners();
        }
        return gOState;
    }

    private void updateUI(){
        for (int buttonIndex = 0; buttonIndex < gameBoard.getChildCount(); buttonIndex++){
            Button gridButton = (Button) gameBoard.getChildAt(buttonIndex);

            int row = buttonIndex / TicTacToeGame.GRID_SIZE;
            int col = buttonIndex % TicTacToeGame.GRID_SIZE;

            gridButton.setText(String.valueOf(game.getSquareContents(row, col)));
        }
    }

    public void removeGameListeners(){
        for (int buttonIndex = 0; buttonIndex < gameBoard.getChildCount(); buttonIndex++){
            Button gridButton = (Button) gameBoard.getChildAt(buttonIndex);
            gridButton.setOnClickListener(null);
        }
    }
}