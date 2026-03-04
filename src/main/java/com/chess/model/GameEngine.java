package com.chess.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Core game engine. Tracks the board, turn, move history,
 * and is responsible for generating strictly legal moves
 * and detecting game-over conditions.
 */
public class GameEngine {

    private final Board board;
    private boolean isWhiteTurn;
    private final List<Move> history;

    /**
     * Square that can be captured via en passant on the next move, or null.
     * This is the square the capturing pawn would move to.
     */
    private Coordinate enPassantTarget;

    public GameEngine() {
        this.board = new Board();
        this.board.initializeStandardPosition();
        this.isWhiteTurn = true; // White starts
        this.history = new ArrayList<>();
        this.enPassantTarget = null;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public List<Move> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Generates all strictly legal moves for the current player.
     * These are pseudo-legal moves that do not leave the current
     * player's king in check.
     */
    public List<Move> generateStrictLegalMoves() {
        Color currentColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
        List<Move> legalMoves = new ArrayList<>();

        // Normal and promotion moves from pseudo-legal move generators.
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Piece piece = board.getPiece(x, y);
                if (piece == null || piece.getColor() != currentColor) {
                    continue;
                }

                List<Coordinate> pseudoMoves = piece.calculatePseudoLegalMoves(board);
                for (Coordinate dest : pseudoMoves) {
                    Piece captured = board.getPiece(dest);
                    Coordinate start = new Coordinate(x, y);

                    if (piece instanceof Pawn && isPromotionRank(dest, currentColor)) {
                        // Generate one move per promotion piece type.
                        for (Move.PromotionType type : EnumSet.of(
                                Move.PromotionType.QUEEN,
                                Move.PromotionType.ROOK,
                                Move.PromotionType.BISHOP,
                                Move.PromotionType.KNIGHT
                        )) {
                            Move move = new Move(start, dest, piece, captured, type);
                            if (isMoveLegal(move, currentColor)) {
                                legalMoves.add(move);
                            }
                        }
                    } else {
                        Move move = new Move(start, dest, piece, captured);
                        if (isMoveLegal(move, currentColor)) {
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }

        // Special moves
        addCastlingMoves(currentColor, legalMoves);
        addEnPassantMoves(currentColor, legalMoves);

        return legalMoves;
    }

    /**
     * Applies a strictly legal move to the current game state,
     * updates turn, en passant state, and move history.
     */
    public void applyMove(Move move) {
        Color movingColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
        if (move.getMovedPiece() == null || move.getMovedPiece().getColor() != movingColor) {
            throw new IllegalArgumentException("Move does not belong to the current player");
        }

        if (!isMoveLegal(move, movingColor)) {
            throw new IllegalArgumentException("Attempted to apply an illegal move: " + move);
        }

        Coordinate previousEnPassantTarget = this.enPassantTarget;
        this.enPassantTarget = null;

        applyMoveInternal(board, move, previousEnPassantTarget);

        // Set new en passant target if a pawn just moved two squares.
        Coordinate start = move.getStart();
        Coordinate end = move.getEnd();
        Piece movedPiece = move.getMovedPiece();
        if (movedPiece instanceof Pawn && Math.abs(start.y() - end.y()) == 2) {
            int middleY = (start.y() + end.y()) / 2;
            this.enPassantTarget = new Coordinate(start.x(), middleY);
        }

        history.add(move);
        isWhiteTurn = !isWhiteTurn;
    }

    /**
     * Returns true if the current player has no legal moves and is in check.
     */
    public boolean isCheckmate() {
        Color currentColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
        if (!isKingInCheck(board, currentColor)) {
            return false;
        }
        return generateStrictLegalMoves().isEmpty();
    }

    /**
     * Returns true if the current player has no legal moves and is not in check.
     */
    public boolean isStalemate() {
        Color currentColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
        if (isKingInCheck(board, currentColor)) {
            return false;
        }
        return generateStrictLegalMoves().isEmpty();
    }

    // Internal helpers 

    private boolean isMoveLegal(Move move, Color movingColor) {
        Board boardCopy = copyBoard(board);
        applyMoveInternal(boardCopy, move, enPassantTarget);
        return !isKingInCheck(boardCopy, movingColor);
    }

    private Board copyBoard(Board original) {
        Board copy = new Board();
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Piece piece = original.getPiece(x, y);
                if (piece != null) {
                    Piece cloned = clonePiece(piece);
                    copy.setPiece(x, y, cloned);
                } else {
                    copy.setPiece(x, y, null);
                }
            }
        }
        return copy;
    }

    private Piece clonePiece(Piece piece) {
        Color color = piece.getColor();
        Coordinate coord = piece.getCoordinate();
        Piece clone;

        if (piece instanceof Pawn) {
            clone = new Pawn(color, coord);
        } else if (piece instanceof Rook) {
            clone = new Rook(color, coord);
        } else if (piece instanceof Knight) {
            clone = new Knight(color, coord);
        } else if (piece instanceof Bishop) {
            clone = new Bishop(color, coord);
        } else if (piece instanceof Queen) {
            clone = new Queen(color, coord);
        } else if (piece instanceof King) {
            clone = new King(color, coord);
        } else {
            throw new IllegalStateException("Unknown piece type: " + piece.getClass());
        }

        clone.setFirstMove(piece.isFirstMove());
        return clone;
    }

    private boolean isPromotionRank(Coordinate dest, Color color) {
        int promotionRank = (color == Color.WHITE) ? 0 : Board.SIZE - 1;
        return dest.y() == promotionRank;
    }

    private boolean isKingInCheck(Board board, Color kingColor) {
        Coordinate kingPos = null;
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Piece piece = board.getPiece(x, y);
                if (piece instanceof King && piece.getColor() == kingColor) {
                    kingPos = new Coordinate(x, y);
                    break;
                }
            }
        }

        if (kingPos == null) {
            // Should not happen in a normal game; treat as not in check.
            return false;
        }

        Color opponentColor = (kingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        return isSquareAttacked(board, kingPos, opponentColor);
    }

    private boolean isSquareAttacked(Board board, Coordinate square, Color attackerColor) {
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Piece piece = board.getPiece(x, y);
                if (piece == null || piece.getColor() != attackerColor) {
                    continue;
                }

                if (piece instanceof Pawn) {
                    int direction = (attackerColor == Color.WHITE) ? -1 : 1;
                    int targetY = y + direction;
                    if (targetY == square.y()) {
                        int leftX = x - 1;
                        int rightX = x + 1;
                        if (leftX == square.x() || rightX == square.x()) {
                            if (!board.isOutOfBounds(square.x(), square.y())) {
                                return true;
                            }
                        }
                    }
                } else {
                    List<Coordinate> attacks = piece.calculatePseudoLegalMoves(board);
                    for (Coordinate coord : attacks) {
                        if (coord.x() == square.x() && coord.y() == square.y()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void addCastlingMoves(Color color, List<Move> legalMoves) {
        if (isKingInCheck(board, color)) {
            // Cannot castle out of check.
            return;
        }

        int backRankY = (color == Color.WHITE) ? Board.SIZE - 1 : 0;
        int kingX = -1;
        King king = null;

        for (int x = 0; x < Board.SIZE; x++) {
            Piece piece = board.getPiece(x, backRankY);
            if (piece instanceof King && piece.getColor() == color) {
                king = (King) piece;
                kingX = x;
                break;
            }
        }

        if (king == null || !king.isFirstMove()) {
            return;
        }

        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Kingside castling (rook on h-file, x = 7)
        Piece kingsideRook = board.getPiece(Board.SIZE - 1, backRankY);
        if (kingsideRook instanceof Rook
                && kingsideRook.getColor() == color
                && kingsideRook.isFirstMove()) {

            boolean pathClear = true;
            for (int x = kingX + 1; x < Board.SIZE - 1; x++) {
                if (board.getPiece(x, backRankY) != null) {
                    pathClear = false;
                    break;
                }
            }

            if (pathClear) {
                Coordinate through = new Coordinate(kingX + 1, backRankY);
                Coordinate destination = new Coordinate(kingX + 2, backRankY);

                if (!isSquareAttacked(board, through, opponent)
                        && !isSquareAttacked(board, destination, opponent)) {
                    Move castleMove = new Move(
                            new Coordinate(kingX, backRankY),
                            destination,
                            king,
                            null
                    );
                    if (isMoveLegal(castleMove, color)) {
                        legalMoves.add(castleMove);
                    }
                }
            }
        }

        // Queenside castling (rook on a-file, x = 0)
        Piece queensideRook = board.getPiece(0, backRankY);
        if (queensideRook instanceof Rook
                && queensideRook.getColor() == color
                && queensideRook.isFirstMove()) {

            boolean pathClear = true;
            for (int x = 1; x < kingX; x++) {
                if (board.getPiece(x, backRankY) != null) {
                    pathClear = false;
                    break;
                }
            }

            if (pathClear) {
                Coordinate through = new Coordinate(kingX - 1, backRankY);
                Coordinate destination = new Coordinate(kingX - 2, backRankY);

                if (!isSquareAttacked(board, through, opponent)
                        && !isSquareAttacked(board, destination, opponent)) {
                    Move castleMove = new Move(
                            new Coordinate(kingX, backRankY),
                            destination,
                            king,
                            null
                    );
                    if (isMoveLegal(castleMove, color)) {
                        legalMoves.add(castleMove);
                    }
                }
            }
        }
    }

    private void addEnPassantMoves(Color color, List<Move> legalMoves) {
        if (enPassantTarget == null) {
            return;
        }

        int epX = enPassantTarget.x();
        int epY = enPassantTarget.y();
        int direction = (color == Color.WHITE) ? -1 : 1;
        int pawnY = epY - direction;

        for (int dx : new int[]{-1, 1}) {
            int pawnX = epX + dx;
            if (board.isOutOfBounds(pawnX, pawnY)) {
                continue;
            }

            Piece potentialPawn = board.getPiece(pawnX, pawnY);
            if (potentialPawn instanceof Pawn && potentialPawn.getColor() == color) {
                Piece captured = board.getPiece(epX, pawnY);
                Move move = new Move(
                        new Coordinate(pawnX, pawnY),
                        enPassantTarget,
                        potentialPawn,
                        captured
                );
                if (isMoveLegal(move, color)) {
                    legalMoves.add(move);
                }
            }
        }
    }

    /**
     * Applies a move to the given board, handling normal moves,
     * captures, castling, en passant, and promotion.
     */
    private void applyMoveInternal(Board board, Move move, Coordinate currentEnPassantTarget) {
        Coordinate start = move.getStart();
        Coordinate end = move.getEnd();
        Piece moved = board.getPiece(start);

        if (moved == null) {
            throw new IllegalStateException("No piece at move start square: " + start);
        }

        // Castling: king moves two squares horizontally on the back rank.
        if (moved instanceof King
                && start.y() == end.y()
                && Math.abs(start.x() - end.x()) == 2) {

            board.setPiece(start, null);
            board.setPiece(end, moved);
            moved.setFirstMove(false);

            int y = start.y();
            if (end.x() > start.x()) {
                // Kingside: rook from h-file to f-file.
                Piece rook = board.getPiece(Board.SIZE - 1, y);
                board.setPiece(Board.SIZE - 1, y, null);
                board.setPiece(end.x() - 1, y, rook);
                if (rook != null) {
                    rook.setFirstMove(false);
                }
            } else {
                // Queenside: rook from a-file to d-file.
                Piece rook = board.getPiece(0, y);
                board.setPiece(0, y, null);
                board.setPiece(end.x() + 1, y, rook);
                if (rook != null) {
                    rook.setFirstMove(false);
                }
            }
            return;
        }

        // En passant: pawn moves diagonally into the en-passant target square,
        // capturing the pawn that just advanced two squares.
        if (moved instanceof Pawn
                && start.x() != end.x()
                && currentEnPassantTarget != null
                && end.x() == currentEnPassantTarget.x()
                && end.y() == currentEnPassantTarget.y()
                && board.getPiece(end) == null) {

            int direction = (moved.getColor() == Color.WHITE) ? -1 : 1;
            int capturedY = end.y() - direction;
            Piece capturedPawn = board.getPiece(end.x(), capturedY);
            if (capturedPawn != null) {
                board.setPiece(end.x(), capturedY, null);
            }

            board.setPiece(start, null);
            board.setPiece(end, moved);
            moved.setFirstMove(false);
            return;
        }

        // Normal move or capture (including promotion).
        board.setPiece(start, null);
        board.setPiece(end, moved);
        moved.setFirstMove(false);

        // Pawn promotion.
        if (moved instanceof Pawn && isPromotionRank(end, moved.getColor())) {
            Move.PromotionType type = move.getPromotionType();
            if (type == null || type == Move.PromotionType.NONE) {
                type = Move.PromotionType.QUEEN; // Default to queen if unspecified.
            }

            Piece promoted;
            Color color = moved.getColor();
            Coordinate promotionSquare = end;

            switch (type) {
                case ROOK -> promoted = new Rook(color, promotionSquare);
                case BISHOP -> promoted = new Bishop(color, promotionSquare);
                case KNIGHT -> promoted = new Knight(color, promotionSquare);
                case QUEEN, NONE -> promoted = new Queen(color, promotionSquare);
                default -> promoted = new Queen(color, promotionSquare);
            }

            promoted.setFirstMove(false);
            board.setPiece(promotionSquare, promoted);
        }
    }
}

