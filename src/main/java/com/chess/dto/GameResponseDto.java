package com.chess.dto;

/**
 * Response after starting a game or making a move: board state and game status.
 */
public class GameResponseDto {

    private final BoardStateDto board;
    private final boolean whiteTurn;
    private final boolean gameOver;
    private final boolean checkmate;
    private final boolean stalemate;
    private final String message;

    public GameResponseDto(
            BoardStateDto board,
            boolean whiteTurn,
            boolean gameOver,
            boolean checkmate,
            boolean stalemate,
            String message
    ) {
        this.board = board;
        this.whiteTurn = whiteTurn;
        this.gameOver = gameOver;
        this.checkmate = checkmate;
        this.stalemate = stalemate;
        this.message = message;
    }

    public BoardStateDto getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public boolean isStalemate() {
        return stalemate;
    }

    public String getMessage() {
        return message;
    }
}
