# Web-based Chess

A modern web-based chess application with a **Spring Boot REST backend** and a **vanilla HTML/CSS/JS frontend** — no frameworks, no dependencies beyond Java.

---

## Features

- **Complete chess rules** — legal move generation, check/checkmate/stalemate detection, en passant, castling, pawn promotion
- **Interactive UI** — click to select pieces, highlighted legal moves, capture indicators, last-move highlight
- **Pawn promotion dialog** — modal popup with all four promotion choices, correctly coloured per side
- **REST API** — clean JSON endpoints for game state, legal moves, and move execution
- **Zero frontend dependencies** — pure HTML, CSS, and vanilla JavaScript; no build step required

---

## Tech Stack

| Layer     | Technology                        |
|-----------|-----------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Maven   |
| Frontend  | HTML5, CSS3, Vanilla JavaScript   |
| API style | REST / JSON                       |

---

## Getting Started

### Prerequisites

- **Java 17+** — verify with `java -version`
- **Maven 3.8+ or 4.x** — verify with `mvn -v`

### Run (Linux / macOS / Git Bash)

```bash
git clone https://github.com/amer1211/Web-based-Chess.git
cd Web-based-Chess
mvn spring-boot:run
```

### Run (Windows PowerShell)

```powershell
git clone https://github.com/amer1211/Web-based-Chess.git
cd Web-based-Chess

# If Maven is in a path with spaces (e.g. C:\Program Files\...) use & and quotes:
& "C:\Program Files\apache-maven-4.0.0-rc-5\bin\mvn" spring-boot:run
```

Once started, open your browser at **http://localhost:8080**

---

## REST API

### Start a new game

```
GET /api/game/start
```

Returns the initial board state and game metadata.

### Get legal moves for a piece

```
GET /api/game/legal-moves?x={file}&y={rank}
```

| Parameter | Type | Description                          |
|-----------|------|--------------------------------------|
| `x`       | int  | File index — 0 (a) to 7 (h)         |
| `y`       | int  | Rank index — 0 (rank 8) to 7 (rank 1)|

### Make a move

```
POST /api/game/move
Content-Type: application/json
```

```json
{
  "start":     { "x": 4, "y": 6 },
  "end":       { "x": 4, "y": 4 },
  "promotion": "QUEEN"
}
```

`promotion` is optional — only required when a pawn reaches the back rank.
Valid values: `QUEEN`, `ROOK`, `BISHOP`, `KNIGHT`.

---

## Project Structure

```
src/
└── main/
    ├── java/com/chess/
    │   ├── ChessApplication.java          # Spring Boot entry point
    │   ├── controller/ChessController.java # REST endpoints
    │   ├── service/
    │   │   ├── GameService.java            # Game lifecycle & move handling
    │   │   └── BoardMapper.java            # Board model → DTO conversion
    │   ├── model/                          # Core chess domain
    │   │   ├── Board.java, GameEngine.java, Move.java
    │   │   ├── Piece.java (abstract)
    │   │   └── Pawn, Rook, Knight, Bishop, Queen, King
    │   └── dto/                            # API data transfer objects
    │       ├── BoardStateDto.java
    │       ├── GameResponseDto.java
    │       ├── LegalMovesResponseDto.java
    │       ├── MoveRequestDto.java
    │       └── CoordinateDto.java
    └── resources/static/                  # Frontend (served by Spring Boot)
        ├── index.html
        ├── app.js
        └── styles.css
```

---

## Building an Executable JAR

```bash
mvn clean package
java -jar target/chess-1.0.0-SNAPSHOT.jar
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-change`
3. Commit your changes: `git commit -m "feat: describe my change"`
4. Push the branch: `git push origin feature/my-change`
5. Open a Pull Request

---

## License

This project is for personal and educational use.
