package com.cen4090.mobilympics;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/* DotsNBoxes Activity */

public class DotsNBoxesActivity extends BaseGameActivity {
    private TextView tIndicator;
    private Button[][] vertBars = new Button[6][7];
    private Button[][] horizBars = new Button[7][6];
    private int[][] vertBarsStates = new int[6][7];
    private int[][] horizBarsStates = new int[7][6];
    private int[][] boxStates = new int[6][6];
    private View[][] boxes = new View[6][6];
    private int player = 0;
    private int currentTurn = 1;
    private int winner = 0;
    private boolean boardLocked = false;
    private String getGameState(){
        StringBuilder data = new StringBuilder();
        for(int i = 0; i < 6; i++){
            for(int j = 0; j <= 6; j++){
                data.append(vertBarsStates[i][j]);
            }
        }
        for(int i = 0; i <= 6; i++){
            for(int j = 0; j < 6; j++){
                data.append(horizBarsStates[i][j]);
            }
        }
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                data.append(boxStates[i][j]);
            }
        }
        data.append(currentTurn);
        data.append(winner);
        return data.toString();
    }
    private void setGameState(String str){
        int chr = 0;
        for(int i = 0; i < 6; i++){
            for(int j = 0; j <= 6; j++){
                try {
                    vertBarsStates[i][j] = Integer.parseInt(str.substring(chr, ++chr));
                }
                catch(NumberFormatException e){
                    Toast.makeText(this /* MyActivity */, "error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
        for(int i = 0; i <= 6; i++){
            for(int j = 0; j < 6; j++){
                try {
                    horizBarsStates[i][j] = Integer.parseInt(str.substring(chr, ++chr));
                }
                catch(NumberFormatException e){
                    Toast.makeText(this /* MyActivity */, "error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                try {
                    boxStates[i][j] = Integer.parseInt(str.substring(chr, ++chr));
                }
                catch(NumberFormatException e){
                    Toast.makeText(this /* MyActivity */, "error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
        try {
            currentTurn = Integer.parseInt(str.substring(chr, ++chr));
        }
        catch(NumberFormatException e){
            Toast.makeText(this /* MyActivity */, "error:" + e, Toast.LENGTH_SHORT).show();
        }
        try {
            winner = Integer.parseInt(str.substring(chr, ++chr));
        }
        catch(NumberFormatException e){
            Toast.makeText(this /* MyActivity */, "error:" + e, Toast.LENGTH_SHORT).show();
        }
        updateBoard();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        boolean isHost = getIntent().getBooleanExtra("isHost", false);
        if(isHost){
            player = 2;
        }else{
            player = 1;
        }
        tIndicator = ((TextView)findViewById(getResources().getIdentifier("turn_indicator", "id", getPackageName())));
        for(int i = 0; i < 6; i++){
            for(int j = 0; j <= 6; j++){
                String name = "vert_bar_" + i + "_" + j;
                int id = getResources().getIdentifier(name, "id", getPackageName());
                vertBars[i][j] = ((Button)findViewById(id));
                int finalI = i;
                int finalJ = j;
                vertBars[i][j].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(player == currentTurn){
                            tryMove(finalI, finalJ, false);
                        }
                    }
                });
            }
        }
        for(int i = 0; i <= 6; i++){
            for(int j = 0; j < 6; j++){
                String name = "horiz_bar_" + i + "_" + j;
                int id = getResources().getIdentifier(name, "id", getPackageName());
                horizBars[i][j] = ((Button)findViewById(id));
                int finalI = i;
                int finalJ = j;
                horizBars[i][j].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(player == currentTurn){
                            tryMove(finalI, finalJ, true);
                        }
                    }
                });
            }
        }
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                String name = "box_" + i + "_" + j;
                int id = getResources().getIdentifier(name, "id", getPackageName());
                boxes[i][j] = ((View)findViewById(id));
            }
        }
        updateBoard();
    }
    @Override
    protected void onGameDataReceived(String data){
        int i = Integer.parseInt(data.substring(0, 1));
        int j = Integer.parseInt(data.substring(1, 2));
        boolean horizontal;
        if(Integer.parseInt(data.substring(2, 3)) == 1){
            horizontal = true;
        }else{
            horizontal = false;
        }
        makeMove(i, j, horizontal);
    }
    private void tryMove(int i, int j, boolean horizontal){
        StringBuilder moveStr = new StringBuilder();
        if(horizontal) {
            if (horizBarsStates[i][j] == 0) {
                makeMove(i, j, true);
                moveStr.append(i);
                moveStr.append(j);
                moveStr.append(1);
                wifiService.sendData(moveStr.toString());
            }
        }else {
            if (vertBarsStates[i][j] == 0) {
                makeMove(i, j, false);
                moveStr.append(i);
                moveStr.append(j);
                moveStr.append(0);
                wifiService.sendData(moveStr.toString());
            }
        }
    }
    private void makeMove(int i, int j, boolean horizontal) {
        boolean gameOver = true;
        boolean isCapture = false;
        if(boardLocked){
            return;
        }else{
            boardLocked = true;
        }
        if(horizontal){
            if(horizBarsStates[i][j] == 0){
                horizBarsStates[i][j] = currentTurn;
                if(i == 0){
                    if(horizBarsStates[1][j] != 0 && vertBarsStates[0][j] != 0 && vertBarsStates[0][j + 1] != 0){
                        boxStates[0][j] = currentTurn;
                        isCapture = true;
                    }
                }else if(i == 6){
                    if(horizBarsStates[5][j] != 0 && vertBarsStates[5][j] != 0 && vertBarsStates[5][j + 1] != 0){
                        boxStates[5][j] = currentTurn;
                        isCapture = true;
                    }
                }else{
                    if(horizBarsStates[i + 1][j] != 0 && vertBarsStates[i][j] != 0 && vertBarsStates[i][j + 1] != 0){
                        boxStates[i][j] = currentTurn;
                        isCapture = true;
                    }
                    if(horizBarsStates[i - 1][j] != 0 && vertBarsStates[i - 1][j] != 0 && vertBarsStates[i - 1][j + 1] != 0){
                        boxStates[i - 1][j] = currentTurn;
                        isCapture = true;
                    }
                }
                int redTally = 0;
                int blueTally = 0;
                for(int a = 0; a < 6; a++){
                    for(int b = 0; b < 6; b++){
                        if(boxStates[a][b] == 0){
                            gameOver = false;
                            break;
                        }else if(boxStates[a][b] == 1){
                            redTally++;
                        } else if(boxStates[a][b] == 2){
                            blueTally++;
                        }
                    }
                }
                if(gameOver){
                    if(redTally > blueTally){
                        winner = 1;
                    } else if(blueTally > redTally){
                        winner = 2;
                    }else{
                        winner = 3;
                    }
                }else if(!isCapture){
                    changeTurns();
                }
                updateBoard();
            }
        }else{
            if(vertBarsStates[i][j] == 0){
                vertBarsStates[i][j] = currentTurn;
                if(j == 0){
                    if(vertBarsStates[i][1] != 0 && horizBarsStates[i][0] != 0 && horizBarsStates[i + 1][0] != 0){
                        boxStates[i][0] = currentTurn;
                        isCapture = true;
                    }
                }else if(j == 6){
                    if(vertBarsStates[i][5] != 0 && horizBarsStates[i][5] != 0 && horizBarsStates[i + 1][5] != 0){
                        boxStates[i][5] = currentTurn;
                        isCapture = true;
                    }
                }else{
                    if(vertBarsStates[i][j + 1] != 0 && horizBarsStates[i][j] != 0 && horizBarsStates[i + 1][j] != 0){
                        boxStates[i][j] = currentTurn;
                        isCapture = true;
                    }
                    if(vertBarsStates[i][j - 1] != 0 && horizBarsStates[i][j - 1] != 0 && horizBarsStates[i + 1][j - 1] != 0){
                        boxStates[i][j - 1] = currentTurn;
                        isCapture = true;
                    }
                }
                int redTally = 0;
                int blueTally = 0;
                for(int a = 0; a < 6; a++){
                    for(int b = 0; b < 6; b++){
                        if(boxStates[a][b] == 0){
                            gameOver = false;
                            break;
                        }else if(boxStates[a][b] == 1){
                            redTally++;
                        } else if(boxStates[a][b] == 2){
                            blueTally++;
                        }
                    }
                }
                if(gameOver){
                    if(redTally > blueTally){
                        winner = 1;
                    } else if(blueTally > redTally){
                        winner = 2;
                    }else{
                        winner = 3;
                    }
                }else if(!isCapture){
                    changeTurns();
                }
                updateBoard();
            }
        }
        setGameState(getGameState()); //This is a terrible idea
        boardLocked = false;
    }

    private void changeTurns() {
        currentTurn = (currentTurn * 2) % 3;
    }

    private void updateBoard() {
        for(int i = 0; i < 6; i++){
            for(int j = 0; j <= 6; j++){
                //vertBars
                if(vertBarsStates[i][j] == 0){
                    ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.DNB_bar_default));
                    vertBars[i][j].setBackgroundTintList(colorStateList);
                    vertBars[i][j].invalidate();
                }else if(vertBarsStates[i][j] == 1){
                    ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.DNB_bar_red));
                    vertBars[i][j].setBackgroundTintList(colorStateList);
                    vertBars[i][j].invalidate();
                }else if(vertBarsStates[i][j] == 2){
                    ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.DNB_bar_blue));
                    vertBars[i][j].setBackgroundTintList(colorStateList);
                    vertBars[i][j].invalidate();
                }else{
                    Toast.makeText(this /* MyActivity */, "error?", Toast.LENGTH_SHORT).show();
                }
            }
        }
        for(int i = 0; i <= 6; i++){
            for(int j = 0; j < 6; j++){
                //horizBars
                if(horizBarsStates[i][j] == 0){
                    ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.DNB_bar_default));
                    horizBars[i][j].setBackgroundTintList(colorStateList);
                    horizBars[i][j].invalidate();
                }else if(horizBarsStates[i][j] == 1){
                    ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.DNB_bar_red));
                    horizBars[i][j].setBackgroundTintList(colorStateList);
                    horizBars[i][j].invalidate();
                }else if(horizBarsStates[i][j] == 2){
                    ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.DNB_bar_blue));
                    horizBars[i][j].setBackgroundTintList(colorStateList);
                    horizBars[i][j].invalidate();
                }else{
                    Toast.makeText(this /* MyActivity */, "error?", Toast.LENGTH_SHORT).show();
                }
            }
        }
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 6; j++){
                //boxes
                if(boxStates[i][j] == 0){
                    boxes[i][j].setBackgroundColor(ContextCompat.getColor(this, R.color.DNB_box_default));
                    boxes[i][j].invalidate();
                }else if(boxStates[i][j] == 1){
                    boxes[i][j].setBackgroundColor(ContextCompat.getColor(this, R.color.DNB_box_red));
                    boxes[i][j].invalidate();
                }else if(boxStates[i][j] == 2){
                    boxes[i][j].setBackgroundColor(ContextCompat.getColor(this, R.color.DNB_box_blue));
                    boxes[i][j].invalidate();
                }else{
                    Toast.makeText(this /* MyActivity */, "error?", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(winner == 3){
            tIndicator.setText(R.string.DNB_tie);
        }else if(winner == 2){
            tIndicator.setText(R.string.DNB_bwin);
        }else if(winner == 1){
            tIndicator.setText(R.string.DNB_rwin);
        }else{
            if(currentTurn == 2){
                tIndicator.setTextColor(ContextCompat.getColor(this, R.color.DNB_bar_blue));
                if(player == 2){
                    tIndicator.setText(R.string.DNB_b_your_turn);
                }else{
                    tIndicator.setText(R.string.DNB_b_opp_turn);
                }
            }else if(currentTurn == 1){
                tIndicator.setTextColor(ContextCompat.getColor(this, R.color.DNB_bar_red));
                if(player == 1){
                    tIndicator.setText(R.string.DNB_r_your_turn);
                }else{
                    tIndicator.setText(R.string.DNB_r_opp_turn);
                }
            }
        }
    }
}