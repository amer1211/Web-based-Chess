(() => {
  const API_BASE = '/api/game';

  const boardElement = document.getElementById('chessboard');
  const statusElement = document.getElementById('game-status');
  const newGameButton = document.getElementById('new-game-btn');

  /** Current client-side selection state */
  let selected = null; // { x, y } or null
  let legalDestinations = []; // array of { x, y }
  let lastBoardState = null; // BoardStateDto-like object from API

  function codeToUnicode(code) {
    switch (code) {
      case 'wP': return '♙';
      case 'wR': return '♖';
      case 'wN': return '♘';
      case 'wB': return '♗';
      case 'wQ': return '♕';
      case 'wK': return '♔';
      case 'bP': return '♟';
      case 'bR': return '♜';
      case 'bN': return '♞';
      case 'bB': return '♝';
      case 'bQ': return '♛';
      case 'bK': return '♚';
      default: return '';
    }
  }

  function isDestination(x, y) {
    return legalDestinations.some(m => m.x === x && m.y === y);
  }

  function renderBoard(boardState) {
    if (!boardState || !boardState.squares) {
      return;
    }
    lastBoardState = boardState;
    boardElement.innerHTML = '';

    // The backend uses y=0 as Black's back rank, y=7 as White's back rank.
    // Render with White at the bottom: display rows 7 down to 0.
    for (let displayRow = 7; displayRow >= 0; displayRow--) {
      const y = displayRow;
      for (let x = 0; x < 8; x++) {
        const code = boardState.squares[y][x] || '';
        const square = document.createElement('div');
        square.classList.add('square');

        const isDark = (x + y) % 2 === 1;
        square.classList.add(isDark ? 'dark' : 'light');

        square.dataset.x = String(x);
        square.dataset.y = String(y);

        if (selected && selected.x === x && selected.y === y) {
          square.classList.add('selected');
        }

        if (isDestination(x, y)) {
          const hasPiece = !!code;
          square.classList.add(hasPiece ? 'capture-move' : 'legal-move');
        }

        if (code) {
          const pieceSpan = document.createElement('span');
          pieceSpan.classList.add('piece');
          pieceSpan.textContent = codeToUnicode(code);
          square.appendChild(pieceSpan);
        }

        square.addEventListener('click', onSquareClick);
        boardElement.appendChild(square);
      }
    }
  }

  function renderStatus(state) {
    if (!state) {
      statusElement.textContent = 'Loading...';
      return;
    }

    if (state.gameOver) {
      if (state.checkmate) {
        statusElement.textContent = state.message || 'Checkmate!';
        statusElement.style.color = 'var(--danger)';
      } else if (state.stalemate) {
        statusElement.textContent = state.message || 'Stalemate.';
        statusElement.style.color = 'var(--text-muted)';
      } else {
        statusElement.textContent = state.message || 'Game over.';
        statusElement.style.color = 'var(--text-muted)';
      }
    } else {
      statusElement.textContent = state.whiteTurn ? "White's Turn" : "Black's Turn";
      statusElement.style.color = 'var(--text-main)';
    }
  }

  async function fetchJson(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || `Request failed with status ${response.status}`);
    }
    return response.json();
  }

  async function startNewGame() {
    try {
      selected = null;
      legalDestinations = [];
      const state = await fetchJson(`${API_BASE}/start`);
      renderBoard(state.board);
      renderStatus(state);
    } catch (err) {
      console.error(err);
      statusElement.textContent = 'Failed to start new game.';
    }
  }

  async function onSquareClick(event) {
    const target = event.currentTarget;
    const x = parseInt(target.dataset.x, 10);
    const y = parseInt(target.dataset.y, 10);

    // If there is already a selection and the user clicks a legal destination,
    // attempt to perform the move.
    if (selected && isDestination(x, y)) {
      await performMove(selected, { x, y });
      return;
    }

    // Otherwise, treat it as a (re)selection.
    await selectSquare(x, y);
  }

  async function selectSquare(x, y) {
    if (!lastBoardState || !lastBoardState.squares) {
      return;
    }

    const code = lastBoardState.squares[y][x];
    if (!code) {
      // Clicking an empty square clears selection.
      selected = null;
      legalDestinations = [];
      renderBoard(lastBoardState);
      return;
    }

    try {
      const movesDto = await fetchJson(`${API_BASE}/legal-moves?x=${x}&y=${y}`);
      const moves = Array.isArray(movesDto.moves) ? movesDto.moves : [];
      if (moves.length === 0) {
        // No legal moves for this piece.
        selected = null;
        legalDestinations = [];
      } else {
        selected = { x, y };
        legalDestinations = moves.map(m => ({ x: m.x, y: m.y }));
      }
      renderBoard(lastBoardState);
    } catch (err) {
      console.error(err);
      statusElement.textContent = 'Failed to retrieve legal moves.';
    }
  }

  async function performMove(start, end) {
    try {
      const payload = {
        start: { x: start.x, y: start.y },
        end: { x: end.x, y: end.y }
        // promotion can be added here later if you implement UI for it
      };

      const state = await fetchJson(`${API_BASE}/move`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      selected = null;
      legalDestinations = [];

      renderBoard(state.board);
      renderStatus(state);
    } catch (err) {
      console.error(err);
      statusElement.textContent = err.message || 'Illegal move.';
      // Keep current selection and legal moves so the user can try another move.
    }
  }

  async function init() {
    newGameButton.addEventListener('click', () => {
      startNewGame();
    });

    await startNewGame();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init().catch(console.error);
  }
})();

