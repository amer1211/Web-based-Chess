## Web-based Chess

Modern web-based chess application with a **Spring Boot REST backend** and a **vanilla HTML/JS frontend**.  
The backend exposes endpoints to manage game state and legal moves, while the frontend renders an interactive chessboard in the browser.

### Features

- **Full chess rules**: board representation, legal move calculation, turn management.
- **REST API**:
  - `GET /api/game/start` – start a new game and return initial board state.
  - `GET /api/game/legal-moves?x={file}&y={rank}` – fetch legal moves for a piece.
  - `POST /api/game/move` – apply a move (with optional promotion) and return updated state.
- **Web UI**:
  - Static frontend served from `src/main/resources/static`.
  - Clickable board, highlighted legal moves, and status messages.
- **Production‑ready basics**:
  - Spring Boot 3.2.
  - Actuator endpoint at `/actuator` for basic health/info.

### Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Maven.
- **Frontend**: HTML, CSS, vanilla JavaScript.

### Getting Started

#### Prerequisites

- Java **17** or later installed (`java -version`).
- Maven **3.8+** installed (`mvn -v`).

#### Clone and run

```bash
git clone https://github.com/amer1211/Web-based-Chess.git
cd Web-based-Chess
mvn spring-boot:run
```

Once the application has started, open your browser at:

- **UI**: `http://localhost:8080/`
- **Actuator**: `http://localhost:8080/actuator`

### API Overview

- **Start a new game**

  `GET /api/game/start`

  Returns the initial board state and game metadata as JSON.

- **Get legal moves**

  `GET /api/game/legal-moves?x={file}&y={rank}`

  - `x`: file index (0–7, typically a–h).
  - `y`: rank index (0–7, typically 8–1).

- **Make a move**

  `POST /api/game/move`

  Example request body:

  ```json
  {
    "start": { "x": 4, "y": 6 },
    "end": { "x": 4, "y": 4 },
    "promotion": "QUEEN"
  }
  ```

### Project Structure

- `src/main/java/com/chess`
  - `ChessApplication` – Spring Boot entry point.
  - `controller/ChessController` – REST API endpoints.
  - `service/GameService` – high-level game operations.
  - `model/*` – core chess domain (board, pieces, moves, engine).
  - `dto/*` – data transfer objects used by the API.
- `src/main/resources/static`
  - `index.html` – main chess UI.
  - `app.js` – frontend logic for rendering and interaction.
  - `styles.css` – board and layout styling.

### Building a JAR

To build an executable JAR:

```bash
mvn clean package
```

Then run:

```bash
java -jar target/chess-1.0.0-SNAPSHOT.jar
```

### Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/my-change`.
3. Commit your changes: `git commit -m "Describe my change"`.
4. Push the branch: `git push origin feature/my-change`.
5. Open a Pull Request.

### License

This project is currently for personal/educational use.  
Add a license file (e.g. MIT, Apache-2.0) if you plan to share or use it in production.

