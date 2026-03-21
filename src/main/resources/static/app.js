(() => {
  const API_BASE = '/api/game';

  const boardEl          = document.getElementById('chessboard');
  const statusEl         = document.getElementById('game-status');
  const newGameBtn       = document.getElementById('new-game-btn');
  const promotionModal   = document.getElementById('promotion-modal');
  const promotionChoices = document.getElementById('promotion-choices');

  let selected         = null;  // { x, y } or null
  let legalDests       = [];    // [{ x, y }, ...]
  let lastBoardState   = null;
  let lastMove         = null;  // { start: {x,y}, end: {x,y} } — for highlighting
  let pendingPromotion = null;  // { start, end } while modal is open

  // Unicode symbols — white pieces (hollow) and black pieces (filled).
  // We fix the color via CSS .piece.white / .piece.black, NOT by symbol choice.
  const UNICODE = {
    wP: '♙', wR: '♖', wN: '♘', wB: '♗', wQ: '♕', wK: '♔',
    bP: '♟', bR: '♜', bN: '♞', bB: '♝', bQ: '♛', bK: '♚',
  };

  // Promotion piece sets for the modal (type key → Unicode symbol)
  const PROMOTE_WHITE = [['QUEEN','♕'], ['ROOK','♖'], ['BISHOP','♗'], ['KNIGHT','♘']];
  const PROMOTE_BLACK = [['QUEEN','♛'], ['ROOK','♜'], ['BISHOP','♝'], ['KNIGHT','♞']];

  /* ── helpers ──────────────────────────────────────────────────────────── */

  const isDest     = (x, y) => legalDests.some(m => m.x === x && m.y === y);
  const isLastMove = (x, y) =>
    lastMove &&
    ((lastMove.start.x === x && lastMove.start.y === y) ||
     (lastMove.end.x   === x && lastMove.end.y   === y));

  /**
   * White pawns start at y=6, move toward y=0 (Black's back rank).
   * Black pawns start at y=1, move toward y=7 (White's back rank).
   */
  const isPawnPromotion = (code, endY) =>
    (code === 'wP' && endY === 0) ||
    (code === 'bP' && endY === 7);

  /* ── rendering ────────────────────────────────────────────────────────── */

  function renderBoard(boardState) {
    if (!boardState?.squares) return;
    lastBoardState = boardState;
    boardEl.innerHTML = '';

    // y=7 is White's back rank → render it at the bottom (displayRow 7 → 0)
    for (let displayRow = 7; displayRow >= 0; displayRow--) {
      const y = displayRow;
      for (let x = 0; x < 8; x++) {
        const code   = boardState.squares[y][x] || '';
        const square = document.createElement('div');

        square.classList.add('square', (x + y) % 2 === 1 ? 'dark' : 'light');
        square.dataset.x = x;
        square.dataset.y = y;

        if (selected?.x === x && selected?.y === y) square.classList.add('selected');
        if (isLastMove(x, y))                        square.classList.add('last-move');
        if (isDest(x, y))    square.classList.add(code ? 'capture-move' : 'legal-move');

        if (code) {
          const piece = document.createElement('span');
          // *** BUG FIX: assign team-specific CSS class for explicit colouring ***
          piece.classList.add('piece', code[0] === 'w' ? 'white' : 'black');
          piece.textContent = UNICODE[code] ?? '';
          square.appendChild(piece);
        }

        square.addEventListener('click', onSquareClick);
        boardEl.appendChild(square);
      }
    }
  }

  function renderStatus(state) {
    statusEl.className = 'status-badge';
    if (!state) {
      statusEl.textContent = 'Loading…';
      return;
    }
    if (state.gameOver) {
      if (state.checkmate) {
        statusEl.textContent = state.message || 'Checkmate!';
        statusEl.classList.add('status-danger');
      } else {
        statusEl.textContent = state.message || 'Stalemate.';
        statusEl.classList.add('status-muted');
      }
    } else {
      const who = state.whiteTurn ? 'White' : 'Black';
      statusEl.textContent = `${who}'s Turn`;
      statusEl.classList.add(state.whiteTurn ? 'status-white' : 'status-black');
    }
  }

  /* ── API helpers ──────────────────────────────────────────────────────── */

  async function fetchJson(url, options) {
    const res = await fetch(url, options);
    if (!res.ok) throw new Error((await res.text()) || `HTTP ${res.status}`);
    return res.json();
  }

  /* ── game actions ─────────────────────────────────────────────────────── */

  async function startNewGame() {
    selected = null; legalDests = []; lastMove = null;
    try {
      const state = await fetchJson(`${API_BASE}/start`);
      renderBoard(state.board);
      renderStatus(state);
    } catch (err) {
      console.error(err);
      statusEl.textContent = 'Failed to start game.';
    }
  }

  async function onSquareClick(e) {
    const sq = e.currentTarget;
    const x  = +sq.dataset.x;
    const y  = +sq.dataset.y;

    if (selected && isDest(x, y)) {
      const code = lastBoardState.squares[selected.y][selected.x];
      if (isPawnPromotion(code, y)) {
        openPromotionModal(selected, { x, y }, code[0] === 'w');
      } else {
        await performMove(selected, { x, y });
      }
    } else {
      await selectSquare(x, y);
    }
  }

  async function selectSquare(x, y) {
    if (!lastBoardState?.squares) return;
    const code = lastBoardState.squares[y][x];
    if (!code) {
      selected = null; legalDests = [];
      renderBoard(lastBoardState);
      return;
    }
    try {
      const dto   = await fetchJson(`${API_BASE}/legal-moves?x=${x}&y=${y}`);
      const moves = Array.isArray(dto.moves) ? dto.moves : [];
      selected  = moves.length ? { x, y } : null;
      legalDests = moves.map(m => ({ x: m.x, y: m.y }));
      renderBoard(lastBoardState);
    } catch (err) {
      console.error(err);
      statusEl.textContent = 'Failed to fetch legal moves.';
    }
  }

  async function performMove(start, end, promotion) {
    try {
      const payload = {
        start,
        end,
        ...(promotion ? { promotion } : {}),
      };
      const state = await fetchJson(`${API_BASE}/move`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(payload),
      });
      lastMove   = { start, end };
      selected   = null;
      legalDests = [];
      renderBoard(state.board);
      renderStatus(state);
    } catch (err) {
      console.error(err);
      statusEl.textContent = err.message || 'Illegal move.';
    }
  }

  /* ── promotion modal ──────────────────────────────────────────────────── */

  function openPromotionModal(start, end, isWhite) {
    pendingPromotion    = { start, end };
    promotionChoices.innerHTML = '';
    const pieces = isWhite ? PROMOTE_WHITE : PROMOTE_BLACK;
    for (const [type, symbol] of pieces) {
      const btn = document.createElement('button');
      btn.className   = `promotion-btn ${isWhite ? 'white' : 'black'}`;
      btn.textContent = symbol;
      btn.title       = type;
      btn.addEventListener('click', () => {
        closePromotionModal();
        performMove(pendingPromotion.start, pendingPromotion.end, type);
      });
      promotionChoices.appendChild(btn);
    }
    promotionModal.classList.remove('hidden');
  }

  function closePromotionModal() {
    promotionModal.classList.add('hidden');
    pendingPromotion = null;
  }

  /* ── init ─────────────────────────────────────────────────────────────── */

  function init() {
    newGameBtn.addEventListener('click', startNewGame);
    promotionModal.addEventListener('click', e => {
      if (e.target === promotionModal) closePromotionModal();
    });
    startNewGame();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
