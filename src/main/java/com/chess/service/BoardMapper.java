package com.chess.service;

import com.chess.dto.BoardStateDto;
import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Piece;

/**
 * Maps the domain Board to API DTOs. Piece codes: "wP", "bK", etc.; empty = "".
 */
public final class BoardMapper {

    private BoardMapper() {}

    public static BoardStateDto toBoardStateDto(Board board) {
        String[][] squares = new String[Board.SIZE][Board.SIZE];
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Piece piece = board.getPiece(x, y);
                squares[y][x] = pieceToCode(piece);
            }
        }
        return new BoardStateDto(squares);
    }

    public static String pieceToCode(Piece piece) {
        if (piece == null) {
            return "";
        }
        String color = piece.getColor() == Color.WHITE ? "w" : "b";
        String letter = switch (piece.getClass().getSimpleName()) {
            case "Pawn" -> "P";
            case "Knight" -> "N";
            case "Bishop" -> "B";
            case "Rook" -> "R";
            case "Queen" -> "Q";
            case "King" -> "K";
            default -> "";
        };
        return color + letter;
    }
}
