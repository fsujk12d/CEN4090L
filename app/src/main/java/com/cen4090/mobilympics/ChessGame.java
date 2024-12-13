// ChessGame.java
public class ChessGame {
    public static final int BOARD_SIZE = 8;
    
    public enum PieceType {
        PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING, EMPTY
    }
    
    public enum PieceColor {
        WHITE, BLACK, NONE
    }
    
    public static class ChessPiece {
        public PieceType type;
        public PieceColor color;
        
        public ChessPiece(PieceType type, PieceColor color) {
            this.type = type;
            this.color = color;
        }
        
        public ChessPiece() {
            this.type = PieceType.EMPTY;
            this.color = PieceColor.NONE;
        }
    }
    
    private ChessPiece[][] board;
    private PieceColor currentTurn;
    private boolean gameOver;
    
    public ChessGame() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        currentTurn = PieceColor.WHITE;
        gameOver = false;
    }
    
    private void initializeBoard() {
        // Initialize empty board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new ChessPiece();
            }
        }
        
        // Set up black pieces
        board[0][0] = new ChessPiece(PieceType.ROOK, PieceColor.BLACK);
        board[0][1] = new ChessPiece(PieceType.KNIGHT, PieceColor.BLACK);
        board[0][2] = new ChessPiece(PieceType.BISHOP, PieceColor.BLACK);
        board[0][3] = new ChessPiece(PieceType.QUEEN, PieceColor.BLACK);
        board[0][4] = new ChessPiece(PieceType.KING, PieceColor.BLACK);
        board[0][5] = new ChessPiece(PieceType.BISHOP, PieceColor.BLACK);
        board[0][6] = new ChessPiece(PieceType.KNIGHT, PieceColor.BLACK);
        board[0][7] = new ChessPiece(PieceType.ROOK, PieceColor.BLACK);
        
        // Set up black pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new ChessPiece(PieceType.PAWN, PieceColor.BLACK);
        }
        
        // Set up white pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[6][i] = new ChessPiece(PieceType.PAWN, PieceColor.WHITE);
        }
        
        // Set up white pieces
        board[7][0] = new ChessPiece(PieceType.ROOK, PieceColor.WHITE);
        board[7][1] = new ChessPiece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][2] = new ChessPiece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][3] = new ChessPiece(PieceType.QUEEN, PieceColor.WHITE);
        board[7][4] = new ChessPiece(PieceType.KING, PieceColor.WHITE);
        board[7][5] = new ChessPiece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][6] = new ChessPiece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][7] = new ChessPiece(PieceType.ROOK, PieceColor.WHITE);
    }
    
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (gameOver) return false;
        
        ChessPiece piece = board[fromRow][fromCol];
        
        // Check if piece belongs to current player
        if (piece.color != currentTurn) return false;
        
        // Check if move is valid for this piece type
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false;
        
        // Make the move
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = new ChessPiece();
        
        // Switch turns
        currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        
        return true;
    }
    
    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = board[fromRow][fromCol];
        ChessPiece targetSquare = board[toRow][toCol];
        
        // Can't capture own piece
        if (targetSquare.color == piece.color) return false;
        
        // Basic movement validation based on piece type
        switch (piece.type) {
            case PAWN:
                return isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case ROOK:
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case KNIGHT:
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case BISHOP:
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case QUEEN:
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case KING:
                return isValidKingMove(fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }
    
    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        int direction = (board[fromRow][fromCol].color == PieceColor.WHITE) ? -1 : 1;
        int startRow = (board[fromRow][fromCol].color == PieceColor.WHITE) ? 6 : 1;
        
        // Basic one square move
        if (fromCol == toCol && toRow == fromRow + direction && board[toRow][toCol].type == PieceType.EMPTY) {
            return true;
        }
        
        // Initial two square move
        if (fromRow == startRow && fromCol == toCol && toRow == fromRow + 2 * direction &&
            board[toRow][toCol].type == PieceType.EMPTY && board[fromRow + direction][toCol].type == PieceType.EMPTY) {
            return true;
        }
        
        // Capture move
        return Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction && 
               board[toRow][toCol].type != PieceType.EMPTY && board[toRow][toCol].color != board[fromRow][fromCol].color;
    }
    
    // Implement other piece movement validation methods similarly
    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (fromRow == toRow || fromCol == toCol) && !isPieceBetween(fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) == Math.abs(toCol - fromCol) && !isPieceBetween(fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (isValidRookMove(fromRow, fromCol, toRow, toCol) || isValidBishopMove(fromRow, fromCol, toRow, toCol));
    }
    
    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }
    
    private boolean isPieceBetween(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow - fromRow, 0);
        int colStep = Integer.compare(toCol - fromCol, 0);
        
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        
        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol].type != PieceType.EMPTY) {
                return true;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return false;
    }
    
    public ChessPiece getPiece(int row, int col) {
        return board[row][col];
    }
    
    public PieceColor getCurrentTurn() {
        return currentTurn;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
}