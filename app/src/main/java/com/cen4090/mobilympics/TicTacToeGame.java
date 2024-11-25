package com.cen4090.mobilympics;


/* TIC TAC TOE Game Class */
public class TicTacToeGame {
    //Grid Size Constant
    public static final int GRID_SIZE = 3;
    //Game Board Array
    private final char[][] mGameBoard;

    //Player type
    public enum PLAYERS {X, O}
    public PLAYERS CURRENT_PLAYER;

    //Constructor - new instances start with X
    public TicTacToeGame(){
        mGameBoard = new char[GRID_SIZE][GRID_SIZE];
        CURRENT_PLAYER = PLAYERS.X;
    }

    //NewGame - Sets all squares to the blank state, represented by the space character ' '
    public void newGame(){
        for (int row = 0; row < GRID_SIZE; row++){
            for (int col = 0; col < GRID_SIZE; col++){
                mGameBoard[row][col] = ' ';
            }
        }
    }

    //GetSquareContents - returns the character in a given square
    public char getSquareContents(int row, int col){
        return mGameBoard[row][col];
    }

    //SelectSquare - places the appropriate character in the selected square, and checks for
    //  invalid moves.
    public boolean selectSquare(int row, int col, PLAYERS currentPlayer){
        char charToBePlaced;
        switch (currentPlayer){
            case X:
                charToBePlaced = 'X';
                break;
            case O:
                charToBePlaced = 'O';
                break;
            default:
                charToBePlaced = ' ';
                break;
        }

        if (mGameBoard[row][col] == ' '){
            mGameBoard[row][col] = charToBePlaced;
            return true;
        }
        return false;
    }

    //IsGameOver - Logic for determining whether the gameState is over or not. It checks for X win,
    //  O win, or Tie Game
    public int isGameOver(){
        String s = getState();

        //Perhaps this Game Logic could be simplified with a regular expression or something clever.
        //I'm not feeling particularly clever today.

        //X Wins
        if (s.charAt(0) == 'X' && s.charAt(3) == 'X' && s.charAt(6) == 'X'){    //Vert Left Col
            return 1;
        } else if (s.charAt(1) == 'X' && s.charAt(4) == 'X' && s.charAt(7) == 'X'){ //Vert Mid Col
            return 1;
        } else if (s.charAt(2) == 'X' && s.charAt(5) == 'X' && s.charAt(8) == 'X'){ //Vert Right Col
            return 1;
        } else if (s.charAt(0) == 'X' && s.charAt(1) == 'X' && s.charAt(2) == 'X'){ //Horiz Top Row
            return 1;
        } else if (s.charAt(3) == 'X' && s.charAt(4) == 'X' && s.charAt(5) == 'X'){ //Horiz Mid Row
            return 1;
        } else if (s.charAt(6) == 'X' && s.charAt(7) == 'X' && s.charAt(8) == 'X'){ //Horiz Bot Row
            return 1;
        } else if (s.charAt(0) == 'X' && s.charAt(4) == 'X' && s.charAt(8) == 'X'){ //Diag \
            return 1;
        } else if (s.charAt(2) == 'X' && s.charAt(4) == 'X' && s.charAt(6) == 'X'){ //Diag /
            return 1;
        }

        //O Wins
        if (s.charAt(0) == 'O' && s.charAt(3) == 'O' && s.charAt(6) == 'O'){    //Vert Left Col
            return 2;
        } else if (s.charAt(1) == 'O' && s.charAt(4) == 'O' && s.charAt(7) == 'O'){ //Vert Mid Col
            return 2;
        } else if (s.charAt(2) == 'O' && s.charAt(5) == 'O' && s.charAt(8) == 'O'){ //Vert Right Col
            return 2;
        } else if (s.charAt(0) == 'O' && s.charAt(1) == 'O' && s.charAt(2) == 'O'){ //Horiz Top Row
            return 2;
        } else if (s.charAt(3) == 'O' && s.charAt(4) == 'O' && s.charAt(5) == 'O'){ //Horiz Mid Row
            return 2;
        } else if (s.charAt(6) == 'O' && s.charAt(7) == 'O' && s.charAt(8) == 'O'){ //Horiz Bot Row
            return 2;
        } else if (s.charAt(0) == 'O' && s.charAt(4) == 'O' && s.charAt(8) == 'O'){ //Diag \
            return 2;
        } else if (s.charAt(2) == 'O' && s.charAt(4) == 'O' && s.charAt(6) == 'O'){ //Diag /
            return 2;
        }

        //Scratch Game
        CharSequence charSequence = " ";
        if (!s.contains(charSequence)){
            return 3;
        }

        //Game is not Over
        return -1;
    }

    //GetState - Builds a string representing the current state of the game board using 'X',
    //  'O', and ' '.
    public String getState(){
        StringBuilder boardString = new StringBuilder();
        for (int row = 0; row < GRID_SIZE; row++){
            for (int col = 0; col < GRID_SIZE; col++){
                boardString.append(mGameBoard[row][col]);
            }
        }
        return boardString.toString();
    }

    //SetState - Sets the current gameState based on stored values.
    public void setState(String gameState){
        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++){
            for (int col = 0; col < GRID_SIZE; col++){
                mGameBoard[row][col] = gameState.charAt(index);
                index++;
            }
        }
    }

    //SetCurrentPlayer - Swaps the CURRENT_PLAYER variable to the other PLAYER.
    public void setCURRENT_PLAYER(){
        if (CURRENT_PLAYER == PLAYERS.X){
            CURRENT_PLAYER = PLAYERS.O;
        } else if (CURRENT_PLAYER == PLAYERS.O){
            CURRENT_PLAYER = PLAYERS.X;
        }
    }
}
