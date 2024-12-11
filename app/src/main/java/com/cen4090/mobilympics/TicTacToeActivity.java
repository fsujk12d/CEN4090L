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

public class TicTacToeActivity extends AppCompatActivity {

    GridLayout gameBoard;
    TicTacToeGame game;
    TextView playerTurnIndicator;
    Button replayButton;
    Toolbar toolbar;
    TextView winnerText;


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

        //Set up Toolbar here
        //TODO - setup toolbar

        //Set references for UI widgets
        gameBoard = findViewById(R.id.ttt_game_grid);
        playerTurnIndicator = findViewById(R.id.ttt_turn_indicator);
        replayButton = findViewById(R.id.ttt_play_again_button);
        winnerText = findViewById(R.id.ttt_winner_message);

        setGameListeners();

        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.newGame();
                updateUI();
                replayButton.setVisibility(View.INVISIBLE);
                winnerText.setVisibility(View.INVISIBLE);
                setGameListeners();
            }
        });

        game = new TicTacToeGame();
        game.newGame();

    }


    public void setGameListeners(){
        for (int buttonIndex = 0; buttonIndex < gameBoard.getChildCount(); buttonIndex++){
            Button gridButton = (Button) gameBoard.getChildAt(buttonIndex);
            gridButton.setOnClickListener(this::onGameButtonClick);
        }
    }

    private void onGameButtonClick(View view){
        //Set row and col indices
        int buttonIndex = gameBoard.indexOfChild(view);
        int row = buttonIndex / TicTacToeGame.GRID_SIZE;
        int col = buttonIndex % TicTacToeGame.GRID_SIZE;

        //If a valid move was made, change CURRENT_PLAYER and update UI
        if (game.selectSquare(row, col, game.CURRENT_PLAYER)){
            updateUI();
            game.setCURRENT_PLAYER();
            switch (game.CURRENT_PLAYER){
                case X:
                    playerTurnIndicator.setText(R.string.x_turn);
                    break;
                case O:
                    playerTurnIndicator.setText(R.string.o_turn);
                    break;
            }
        } else {        //Show invalid move toast
            Toast.makeText(this, R.string.invalid_move, Toast.LENGTH_SHORT).show();
        }

        //Display WINNER message if game is over
        int gameOverState = game.isGameOver();
        if (gameOverState > 0){

            replayButton.setVisibility(View.VISIBLE);

            switch (gameOverState){
                case 1:
                    winnerText.setText(R.string.x_wins);
                    break;
                case 2:
                    winnerText.setText(R.string.o_wins);
                    break;
                case 3:
                    winnerText.setText(R.string.cat_wins);
                    break;
                default:
                    break;
            }
            winnerText.setVisibility(View.VISIBLE);
            removeGameListeners();
            /*SharedPreferences pref = getSharedPreferences("resume", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("gameState", "");
            editor.putBoolean("resumable", false);
            editor.apply();*/
        }
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