public class ChessActivity extends BaseGameActivity {
    private GridLayout chessBoard;
    private TextView player1Info;
    private TextView player2Info;
    private TextView statusText;
    private ChessGame game;
    private Button[][] boardButtons;
    
    private boolean isMyTurn;
    private boolean isHost;
    private ChessGame.PieceColor myColor;
    
    private int selectedRow = -1;
    private int selectedCol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);
        
        // Initialize views
        chessBoard = findViewById(R.id.chess_board);
        player1Info = findViewById(R.id.player1_info);
        player2Info = findViewById(R.id.player2_info);
        statusText = findViewById(R.id.status_text);
        
        // Get host status from intent
        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        myColor = isHost ? ChessGame.PieceColor.WHITE : ChessGame.PieceColor.BLACK;
        isMyTurn = isHost; // White (host) goes first
        
        // Initialize game
        game = new ChessGame();
        initializeBoard();
        updateUI();
        
        if (!isMyTurn) {
            statusText.setText("Opponent's turn");
        }
    }
    
    private void initializeBoard() {
        boardButtons = new Button[ChessGame.BOARD_SIZE][ChessGame.BOARD_SIZE];
        
        for (int i = 0; i < ChessGame.BOARD_SIZE; i++) {
            for (int j = 0; j < ChessGame.BOARD_SIZE; j++) {
                Button button = new Button(this);
                button.setLayoutParams(new GridLayout.LayoutParams());
                button.getLayoutParams().width = getResources().getDisplayMetrics().densityDpi * 35 / 160;
                button.getLayoutParams().height = getResources().getDisplayMetrics().densityDpi * 35 / 160;
                
                final int row = i;
                final int col = j;
                
                button.setOnClickListener(v -> onSquareClick(row, col));
                
                // Set alternating background colors
                if ((i + j) % 2 == 0) {
                    button.setBackgroundColor(Color.rgb(240, 217, 181)); // Light square
                } else {
                    button.setBackgroundColor(Color.rgb(181, 136, 99)); // Dark square
                }
                
                chessBoard.addView(button);
                boardButtons[i][j] = button;
            }
        }
    }
    
    private void onSquareClick(int row, int col) {
        if (!isMyTurn) return;
        
        ChessGame.ChessPiece piece = game.getPiece(row, col);
        
        // If no piece is selected and clicked on own piece, select it
        if (selectedRow == -1 && selectedCol == -1) {
            if (piece.color == myColor) {
                selectedRow = row;
                selectedCol = col;
                boardButtons[row][col].setBackgroundColor(Color.YELLOW); // Highlight selected piece
            }
            return;
        }
        
        // If a piece is already selected
        if (selectedRow != -1 && selectedCol != -1) {
            // Try to make the move
            if (game.makeMove(selectedRow, selectedCol, row, col)) {
                // Send move to opponent
                String moveData = "MOVE:" + selectedRow + "," + selectedCol + "," + row + "," + col;
                if (wifiService != null) {
                    wifiService.sendData(moveData);
                }
                
                isMyTurn = false;
                updateUI();
                statusText.setText("Opponent's turn");
            }
            
            // Reset selection
            resetSquareColors();
            selectedRow = -1;
            selectedCol = -1;
        }
    }
    
    private void resetSquareColors() {
        for (int i = 0; i < ChessGame.BOARD_SIZE; i++) {
            for (int j = 0; j < ChessGame.BOARD_SIZE; j++) {
                if ((i + j) % 2 == 0) {
                    boardButtons[i][j].setBackgroundColor(Color.rgb(240, 217, 181)); // Light square
                } else {
                    boardButtons[i][j].setBackgroundColor(Color.rgb(181, 136, 99)); // Dark square
                }
            }
        }
    }
    
    private void updateUI() {
        for (int i = 0; i < ChessGame.BOARD_SIZE; i++) {
            for (int j = 0; j < ChessGame.BOARD_SIZE; j++) {
                ChessGame.ChessPiece piece = game.getPiece(i, j);
                boardButtons[i][j].setText(getPieceSymbol(piece));
            }
        }
    }
    
    private String getPieceSymbol(ChessGame.ChessPiece piece) {
        if (piece.type == ChessGame.PieceType.EMPTY) return "";
        
        String symbol = "";
        switch (piece.type) {
            case KING:   symbol = "♔"; break;
            case QUEEN:  symbol = "♕"; break;
            case ROOK:   symbol = "♖"; break;
            case BISHOP: symbol = "♗"; break;
            case KNIGHT: symbol = "♘"; break;
            case PAWN:   symbol = "♙"; break;
        }
        
        if (piece.color == ChessGame.PieceColor.BLACK) {
            // Convert to black piece unicode
            return String.valueOf((char)(symbol.charAt(0) + 6));
        }
        return symbol;
    }
    
    @Override
    protected void onGameDataReceived(String data) {
        if (data.startsWith("MOVE:")) {
            String[] moveData = data.substring(5).split(",");
            int fromRow = Integer.parseInt(moveData[0]);
            int fromCol = Integer.parseInt(moveData[1]);
            int toRow = Integer.parseInt(moveData[2]);
            int toCol = Integer.parseInt(moveData[3]);
            
            // Make opponent's move
            game.makeMove(fromRow, fromCol, toRow, toCol);
            isMyTurn = true;
            
            runOnUiThread(() -> {
                updateUI();
                statusText.setText("Your turn");
            });
        }
    }
    
    private void showGameOverDialog(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
        });
    }
}