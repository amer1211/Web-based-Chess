package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Color color, Coordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public List<Coordinate> calculatePseudoLegalMoves(Board board) {
        int[][] offsets = {
                {-1, -1}, {0, -1}, {1, -1},
                {-1, 0},           {1, 0},
                {-1, 1},  {0, 1},  {1, 1}
        };
        
        List<Coordinate> legalMoves = new ArrayList<>();
        Coordinate from = getCoordinate();
        int oldX = from.x();
        int oldY = from.y();
        Color currColor = getColor();

        for (int[] dir: offsets) {
            int dx = dir[0];
            int dy = dir[1];

            int newX = oldX + dx;
            int newY = oldY + dy;

            if (board.isOutOfBounds(newX, newY)) {
                continue;
            }

            Piece target = board.getPiece(newX, newY);
            if (target == null || target.getColor() != currColor) {
                legalMoves.add(new Coordinate(newX, newY));
            }
        }
        return legalMoves;
    }
}
