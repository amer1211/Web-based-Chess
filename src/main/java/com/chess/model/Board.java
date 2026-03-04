package com.chess.model;

/**
 * 8x8 chessboard. Indices are [y][x] with (0,0) at the bottom-left from White's perspective
 * (rank 1, file a). So y=0 is Black's back rank, y=7 is White's back rank.
 */
public class Board {

    public static final int SIZE = 8;

    private final Piece[][] squares;

    public Board() {
        this.squares = new Piece[SIZE][SIZE];
    }

    public Piece getPiece(Coordinate coordinate) {
        return getPiece(coordinate.x(), coordinate.y());
    }

    public Piece getPiece(int x, int y) {
        if (isOutOfBounds(x, y)) {
            return null;
        }
        return squares[y][x];
    }

    public void setPiece(Coordinate coordinate, Piece piece) {
        setPiece(coordinate.x(), coordinate.y(), piece);
    }

    public void setPiece(int x, int y, Piece piece) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("Coordinates out of bounds: (" + x + ", " + y + ")");
        }
        squares[y][x] = piece;
        if (piece != null) {
            piece.setCoordinate(new Coordinate(x, y));
        }
    }

    /**
     * Moves a piece from start to end. The square at end is overwritten (capture or empty).
     * The piece's coordinate is updated.
     */
    public void movePiece(Coordinate start, Coordinate end) {
        Piece piece = getPiece(start);
        if (piece == null) {
            throw new IllegalArgumentException("No piece at start position " + start);
        }
        setPiece(start, null);
        setPiece(end, piece);
    }

    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= SIZE || y < 0 || y >= SIZE;
    }

    public boolean isOutOfBounds(Coordinate coordinate) {
        return isOutOfBounds(coordinate.x(), coordinate.y());
    }

    /**
     * Initializes the board with the standard 32 pieces in their starting positions.
     * White at y=6 (pawns) and y=7 (pieces); Black at y=1 (pawns) and y=0 (pieces).
     */
    public void initializeStandardPosition() {
        // Clear board
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                squares[y][x] = null;
            }
        }

        // Black back rank (y = 0)
        setPiece(0, 0, new Rook(Color.BLACK, new Coordinate(0, 0)));
        setPiece(1, 0, new Knight(Color.BLACK, new Coordinate(1, 0)));
        setPiece(2, 0, new Bishop(Color.BLACK, new Coordinate(2, 0)));
        setPiece(3, 0, new Queen(Color.BLACK, new Coordinate(3, 0)));
        setPiece(4, 0, new King(Color.BLACK, new Coordinate(4, 0)));
        setPiece(5, 0, new Bishop(Color.BLACK, new Coordinate(5, 0)));
        setPiece(6, 0, new Knight(Color.BLACK, new Coordinate(6, 0)));
        setPiece(7, 0, new Rook(Color.BLACK, new Coordinate(7, 0)));

        // Black pawns (y = 1)
        for (int x = 0; x < SIZE; x++) {
            setPiece(x, 1, new Pawn(Color.BLACK, new Coordinate(x, 1)));
        }

        // White pawns (y = 6)
        for (int x = 0; x < SIZE; x++) {
            setPiece(x, 6, new Pawn(Color.WHITE, new Coordinate(x, 6)));
        }

        // White back rank (y = 7)
        setPiece(0, 7, new Rook(Color.WHITE, new Coordinate(0, 7)));
        setPiece(1, 7, new Knight(Color.WHITE, new Coordinate(1, 7)));
        setPiece(2, 7, new Bishop(Color.WHITE, new Coordinate(2, 7)));
        setPiece(3, 7, new Queen(Color.WHITE, new Coordinate(3, 7)));
        setPiece(4, 7, new King(Color.WHITE, new Coordinate(4, 7)));
        setPiece(5, 7, new Bishop(Color.WHITE, new Coordinate(5, 7)));
        setPiece(6, 7, new Knight(Color.WHITE, new Coordinate(6, 7)));
        setPiece(7, 7, new Rook(Color.WHITE, new Coordinate(7, 7)));
    }

    /**
     * Returns the raw grid for internal use (e.g. by Piece move calculation).
     * Indices are [y][x].
     */
    public Piece[][] getSquares() {
        return squares;
    }
}
