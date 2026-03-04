package com.chess.model;

/**
 * Represents a single move in the game.
 * Stores starting and ending coordinates, the piece that moved,
 * and the piece that was captured (if any).
 */
public class Move {

    private final Coordinate start;
    private final Coordinate end;
    private final Piece movedPiece;
    private final Piece capturedPiece;
    private final PromotionType promotionType;

    public enum PromotionType {
        NONE,
        QUEEN,
        ROOK,
        BISHOP,
        KNIGHT
    }

    public Move(Coordinate start, Coordinate end, Piece movedPiece, Piece capturedPiece) {
        this(start, end, movedPiece, capturedPiece, PromotionType.NONE);
    }

    public Move(
            Coordinate start,
            Coordinate end,
            Piece movedPiece,
            Piece capturedPiece,
            PromotionType promotionType
    ) {
        this.start = start;
        this.end = end;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotionType = promotionType == null ? PromotionType.NONE : promotionType;
    }

    public Coordinate getStart() {
        return start;
    }

    public Coordinate getEnd() {
        return end;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public PromotionType getPromotionType() {
        return promotionType;
    }

    @Override
    public String toString() {
        return "Move{" +
                "start=" + start +
                ", end=" + end +
                ", movedPiece=" + (movedPiece != null ? movedPiece.getClass().getSimpleName() : "null") +
                ", capturedPiece=" + (capturedPiece != null ? capturedPiece.getClass().getSimpleName() : "null") +
                ", promotionType=" + promotionType +
                '}';
    }
}

