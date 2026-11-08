import { Chess } from 'chess.js';

export function uciToSan(fen: string, uci: string): string | null {
  try {
    if (!fen || !uci || typeof uci !== 'string' || uci.length < 4) {
      return typeof uci === 'string' ? uci : null;
    }
    const chess = new Chess(fen);
    const from = uci.substring(0, 2);
    const to = uci.substring(2, 4);
    const promotion = uci.length === 5 ? uci.substring(4, 5).toLowerCase() : undefined;
    const move = chess.move({ from, to, promotion });
    if (!move) {
      return uci;
    }
    return move.san;
  } catch {
    return typeof uci === 'string' ? uci : null;
  }
}

export function uciSequenceToSan(fen: string, uciSequence: string): string {
  if (!uciSequence || typeof uciSequence !== 'string' || uciSequence.trim() === '') {
    return typeof uciSequence === 'string' ? uciSequence : '';
  }
  try {
    const chess = new Chess(fen);
    const moves = uciSequence.trim().split(/\s+/);
    const sanMoves: string[] = [];
    for (const uci of moves) {
      if (!uci || typeof uci !== 'string' || uci.length < 4) {
        continue;
      }
      try {
        const from = uci.substring(0, 2);
        const to = uci.substring(2, 4);
        const promotion = uci.length === 5 ? uci.substring(4, 5).toLowerCase() : undefined;
        const move = chess.move({ from, to, promotion });
        if (!move) {
          break;
        }
        sanMoves.push(move.san);
      } catch {
        break;
      }
    }
    if (sanMoves.length === 0) {
      return uciSequence;
    }
    return sanMoves.join(' ');
  } catch {
    return uciSequence;
  }
}
