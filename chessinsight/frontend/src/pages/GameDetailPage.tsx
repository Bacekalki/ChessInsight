import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { gamesApi } from '../api/gamesApi';
import { ChessGameDto, GameAnalysisResponse, GameAnalysisMove } from '../types/game.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorMessage } from '../components/common/ErrorMessage';
import { ArrowLeft, Download, Calendar, Clock, Users, BookOpen, Globe2, List, SkipBack, ChevronLeft, ChevronRight, SkipForward, Play, Pause, BarChart3, Target, Star, Check, CheckCheck, AlertCircle, AlertTriangle, XCircle } from 'lucide-react';
import { Chess } from 'chess.js';
import { Chessboard } from 'react-chessboard';
import { getMoveClassificationDisplay, formatAccuracy } from '../utils/moveClassification';
import { uciToSan, uciSequenceToSan } from '../utils/chessNotation';
import { useUsername } from '../context/UsernameContext';

type MoveClassificationBadgeConfig = {
  Icon: React.ComponentType<{ className?: string }>;
  badgeClass: string;
};

// Solid, high-contrast badge styles for the six display categories.
// Mate-related backend classifications are already mapped into these categories
// by getMoveClassificationDisplay(), so no extra badge categories are introduced.
const MOVE_CLASSIFICATION_BADGES: Record<string, MoveClassificationBadgeConfig> = {
  Best: { Icon: Star, badgeClass: 'bg-emerald-500 text-white ring-emerald-200/70' },
  Excellent: { Icon: CheckCheck, badgeClass: 'bg-emerald-300 text-emerald-950 ring-emerald-100/70' },
  Good: { Icon: Check, badgeClass: 'bg-cyan-400 text-cyan-950 ring-cyan-100/70' },
  Inaccuracy: { Icon: AlertCircle, badgeClass: 'bg-amber-400 text-amber-950 ring-amber-100/70' },
  Mistake: { Icon: AlertTriangle, badgeClass: 'bg-orange-500 text-white ring-orange-200/70' },
  Blunder: { Icon: XCircle, badgeClass: 'bg-rose-500 text-white ring-rose-200/70' },
};

// Convert an algebraic square to 0-based visual column/row for the current board orientation.
const getBoardCoordinates = (square: string, orientation: 'white' | 'black') => {
  const file = square.charCodeAt(0) - 97; // a=0 ... h=7
  const rank = Number(square[1]) - 1; // rank 1=0 ... rank 8=7
  if (!Number.isFinite(file) || !Number.isFinite(rank) || file < 0 || file > 7 || rank < 0 || rank > 7) {
    return null;
  }
  const col = orientation === 'white' ? file : 7 - file;
  const row = orientation === 'white' ? 7 - rank : rank;
  return { col, row };
};

export const GameDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { username } = useUsername();
  const gameId = id ? Number(id) : NaN;

  const [game, setGame] = useState<ChessGameDto | null>(null);
  const [moves, setMoves] = useState<string[]>([]);
  const [loadingGame, setLoadingGame] = useState<boolean>(true);
  const [loadingMoves, setLoadingMoves] = useState<boolean>(true);
  const [gameError, setGameError] = useState<Error | null>(null);
  const [movesError, setMovesError] = useState<Error | null>(null);

  const [chess, setChess] = useState<Chess>(new Chess());
  const [currentMoveIndex, setCurrentMoveIndex] = useState<number>(-1);
  const [lastMove, setLastMove] = useState<{ from: string; to: string } | null>(null);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const autoplayRef = useRef<number | null>(null);
  const [analysisDepth, setAnalysisDepth] = useState<number>(12);

  const [analysis, setAnalysis] = useState<GameAnalysisResponse | null>(null);
  const [analysisLoading, setAnalysisLoading] = useState<boolean>(false);
  const [analysisError, setAnalysisError] = useState<Error | null>(null);

  const fetchGame = useCallback(async () => {
    if (Number.isNaN(gameId)) {
      setGameError(new Error('Invalid game ID'));
      setLoadingGame(false);
      return;
    }
    setLoadingGame(true);
    setGameError(null);
    try {
      const data = await gamesApi.getGameById(gameId);
      setGame(data);
    } catch (err: unknown) {
      setGameError(err instanceof Error ? err : new Error(String(err)));
      setGame(null);
    } finally {
      setLoadingGame(false);
    }
  }, [gameId]);

  const fetchMoves = useCallback(async () => {
    if (Number.isNaN(gameId)) {
      setLoadingMoves(false);
      return;
    }
    setLoadingMoves(true);
    setMovesError(null);
    try {
      const data = await gamesApi.getGameMoves(gameId);
      const moveList = Array.isArray(data) ? data : [];
      setMoves(moveList);
      
      const newChess = new Chess();
      setChess(newChess);
      setCurrentMoveIndex(-1);
    } catch (err: unknown) {
      setMovesError(err instanceof Error ? err : new Error(String(err)));
      setMoves([]);
    } finally {
      setLoadingMoves(false);
    }
  }, [gameId]);

  const fetchAnalysis = useCallback(async () => {
    if (Number.isNaN(gameId)) return;
    setAnalysisLoading(true);
    setAnalysisError(null);
    try {
      const data = await gamesApi.getGameAnalysis(gameId, analysisDepth);
      // Handle both object and array responses defensively
      if (data && typeof data === 'object' && !Array.isArray(data)) {
        setAnalysis(data as GameAnalysisResponse);
      } else if (Array.isArray(data)) {
        // Fallback if backend returns array
        setAnalysis({
          whiteAccuracy: 0,
          blackAccuracy: 0,
          moves: data,
        } as GameAnalysisResponse);
      } else {
        setAnalysis(null);
      }
    } catch (err: unknown) {
      setAnalysisError(err instanceof Error ? err : new Error(String(err)));
      setAnalysis(null);
    } finally {
      setAnalysisLoading(false);
    }
  }, [gameId, analysisDepth]);

  useEffect(() => {
    setGame(null);
    setMoves([]);
    setGameError(null);
    setMovesError(null);
    setChess(new Chess());
    setCurrentMoveIndex(-1);
    setLastMove(null);
    setIsPlaying(false);
    setAnalysis(null);
    setAnalysisError(null);
    if (autoplayRef.current) {
      clearInterval(autoplayRef.current);
      autoplayRef.current = null;
    }
    fetchGame();
    fetchMoves();
    fetchAnalysis();
  }, [fetchGame, fetchMoves]); // analysisDepth changes trigger fetchAnalysis separately

  useEffect(() => {
    fetchAnalysis();
  }, [analysisDepth, fetchAnalysis]);

  useEffect(() => {
    if (isPlaying && currentMoveIndex < moves.length - 1) {
      autoplayRef.current = window.setInterval(() => {
        setCurrentMoveIndex((prev) => {
          if (prev < moves.length - 1) {
            return prev + 1;
          }
          setIsPlaying(false);
          return prev;
        });
      }, 800);
    } else {
      if (autoplayRef.current) {
        clearInterval(autoplayRef.current);
        autoplayRef.current = null;
      }
      if (currentMoveIndex >= moves.length - 1) {
        setIsPlaying(false);
      }
    }

    return () => {
      if (autoplayRef.current) {
        clearInterval(autoplayRef.current);
        autoplayRef.current = null;
      }
    };
  }, [isPlaying, currentMoveIndex, moves.length]);

  useEffect(() => {
    const newChess = new Chess();
    let latestMove: { from: string; to: string } | null = null;
    for (let i = 0; i <= currentMoveIndex && i < moves.length; i++) {
      try {
        const applied = newChess.move(moves[i]);
        if (i === currentMoveIndex && applied) {
          latestMove = { from: applied.from, to: applied.to };
        }
      } catch (e) {
        // Skip invalid move if any
      }
    }
    setChess(newChess);
    setLastMove(latestMove);
  }, [currentMoveIndex, moves]);

  const goToStart = () => {
    setCurrentMoveIndex(-1);
    setIsPlaying(false);
  };

  const goToPrevious = () => {
    if (currentMoveIndex >= 0) {
      setCurrentMoveIndex(currentMoveIndex - 1);
      setIsPlaying(false);
    }
  };

  const goToNext = () => {
    if (currentMoveIndex < moves.length - 1) {
      setCurrentMoveIndex(currentMoveIndex + 1);
      setIsPlaying(false);
    }
  };

  const goToEnd = () => {
    setCurrentMoveIndex(moves.length - 1);
    setIsPlaying(false);
  };

  const togglePlayPause = () => {
    if (isPlaying) {
      setIsPlaying(false);
    } else {
      if (currentMoveIndex >= moves.length - 1) {
        setCurrentMoveIndex(-1);
      }
      setIsPlaying(true);
    }
  };

  const jumpToMove = (index: number) => {
    setCurrentMoveIndex(index);
    setIsPlaying(false);
  };

  const jumpToPly = (ply: number) => {
    // ply is half-move number starting at 1
    const index = ply - 1;
    if (index >= -1 && index < moves.length) {
      setCurrentMoveIndex(index);
      setIsPlaying(false);
    }
  };

  const handleDownload = async () => {
    if (Number.isNaN(gameId)) return;
    try {
      await gamesApi.downloadPgn(gameId);
    } catch (err: unknown) {
      alert(`Download failed: ${err instanceof Error ? err.message : String(err)}`);
    }
  };

  const formatResult = (result: string) => {
    if (result === 'WHITE_WIN') return 'White Wins';
    if (result === 'BLACK_WIN') return 'Black Wins';
    if (result === 'DRAW') return 'Draw';
    if (result === 'UNKNOWN') return 'Unknown';
    return result.replace('_', ' ');
  };

  const renderMoves = () => {
    if (loadingMoves) {
      return <LoadingSpinner message="Loading moves..." />;
    }
    if (movesError) {
      return <ErrorMessage error={movesError} onRetry={fetchMoves} />;
    }
    if (!moves || moves.length === 0) {
      return (
        <div className="text-center text-slate-400 text-sm py-6 border border-dashed border-slate-800 rounded-lg">
          No moves available for this game.
        </div>
      );
    }

    const movePairs: Array<{ moveNumber: number; whiteIndex: number; white: string; blackIndex?: number; black?: string }> = [];
    for (let i = 0; i < moves.length; i += 2) {
      movePairs.push({
        moveNumber: Math.floor(i / 2) + 1,
        whiteIndex: i,
        white: moves[i],
        blackIndex: moves[i + 1] !== undefined ? i + 1 : undefined,
        black: moves[i + 1],
      });
    }

    return (
      <div className="bg-slate-950 border border-slate-800 rounded-lg overflow-hidden max-h-96 overflow-y-auto">
        <div className="divide-y divide-slate-800">
          {movePairs.map((pair) => (
            <div key={pair.moveNumber} className="flex items-center text-sm">
              <div className="w-10 px-2 py-1.5 text-slate-500 text-xs text-right">{pair.moveNumber}.</div>
              <button
                onClick={() => jumpToMove(pair.whiteIndex)}
                className={`flex-1 px-2 py-1.5 text-left font-mono hover:bg-slate-800 transition-colors ${
                  currentMoveIndex === pair.whiteIndex ? 'bg-emerald-900/40 text-emerald-400' : 'text-slate-200'
                }`}
              >
                {pair.white}
              </button>
              {pair.black !== undefined ? (
                <button
                  onClick={() => jumpToMove(pair.blackIndex!)}
                  className={`flex-1 px-2 py-1.5 text-left font-mono hover:bg-slate-800 transition-colors ${
                    currentMoveIndex === pair.blackIndex ? 'bg-emerald-900/40 text-emerald-400' : 'text-slate-200'
                  }`}
                >
                  {pair.black}
                </button>
              ) : (
                <div className="flex-1 px-2 py-1.5"></div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const getSideToMove = () => {
    return chess.turn() === 'w' ? 'White to move' : 'Black to move';
  };

  const getCurrentEvaluation = () => {
    if (!analysis) return null;
    if (currentMoveIndex < 0) {
      return analysis.initialPositionEvaluation || null;
    }
    if (analysis.moves && analysis.moves[currentMoveIndex]) {
      return analysis.moves[currentMoveIndex].positionEvaluation || null;
    }
    return null;
  };

  const formatEvaluationText = (centipawns: number | null | undefined, mateIn: number | null | undefined) => {
    if (mateIn !== null && mateIn !== undefined) {
      return mateIn > 0 ? `M${mateIn}` : `-M${Math.abs(mateIn)}`;
    }
    if (centipawns !== null && centipawns !== undefined) {
      const pawns = centipawns / 100;
      if (pawns === 0) return '0.00';
      const sign = pawns > 0 ? '+' : '';
      const text = `${sign}${pawns.toFixed(2)}`;
      // Clean to max 2 decimals
      return text;
    }
    return '—';
  };

  const formatEvaluation = (evalObj: any) => {
    if (!evalObj) return { text: '—', percent: 50 };
    if (evalObj.mateIn !== null && evalObj.mateIn !== undefined) {
      const mate = evalObj.mateIn;
      return { text: mate > 0 ? `M${mate}` : `-M${Math.abs(mate)}`, percent: mate > 0 ? 90 : 10 };
    }
    if (evalObj.centipawns !== null && evalObj.centipawns !== undefined) {
      const pawns = evalObj.centipawns / 100;
      const clamped = Math.max(-5, Math.min(5, pawns));
      // Map -5 to +5 to 0-100 percent (50 is 0)
      const percent = 50 + (clamped / 5) * 50;
      const sign = pawns > 0 ? '+' : '';
      const text = `${sign}${pawns.toFixed(2)}`;
      // Clean trailing zeros
      const cleanText = text.replace(/\.?0+$/, (m) => m.includes('.') ? m.replace(/0+$/, '').replace(/\.$/, '') : m);
      return { text: cleanText === '+0' || cleanText === '-0' ? '0.00' : (cleanText.includes('.') ? cleanText : cleanText + '.00'), percent: Math.max(5, Math.min(95, percent)) };
    }
    return { text: '—', percent: 50 };
  };

  const getCurrentPositionFen = () => {
    return chess.fen();
  };

  // Orient the board so the initially entered ChessInsight username's pieces are at the bottom.
  // The username comes from the app-level UsernameContext (the same value used to load this user's games).
  const normalizePlayerName = (name: string | null | undefined): string =>
    (name ?? '').trim().toLowerCase();

  const boardOrientation: 'white' | 'black' = (() => {
    const activeUser = normalizePlayerName(username);
    if (!activeUser || !game) return 'white';

    if (activeUser === normalizePlayerName(game.whiteUsername)) return 'white';
    if (activeUser === normalizePlayerName(game.blackUsername)) return 'black';
    return 'white';
  })();

  const getEngineBestMoveForCurrentPosition = () => {
    if (!analysis) return null;
    if (currentMoveIndex < 0) {
      if (analysis.moves && analysis.moves.length > 0 && analysis.moves[0]) {
        return analysis.moves[0].engineBestMove || null;
      }
      return null;
    }
    if (analysis.moves && analysis.moves[currentMoveIndex]) {
      return analysis.moves[currentMoveIndex].engineBestMove || null;
    }
    return null;
  };

  const getCurrentCandidateLines = () => {
    if (!analysis || currentMoveIndex < 0) return null;
    if (analysis.moves && analysis.moves[currentMoveIndex]) {
      return analysis.moves[currentMoveIndex].candidateLines || null;
    }
    return null;
  };

  const getSelectedMoveBadge = (): (MoveClassificationBadgeConfig & { label: string }) | null => {
    if (!analysis || !analysis.moves || currentMoveIndex < 0 || currentMoveIndex >= analysis.moves.length) {
      return null;
    }
    const move = analysis.moves[currentMoveIndex];
    if (!move || !move.classification) return null;

    const display = getMoveClassificationDisplay(move.classification, move.centipawnLoss);
    const config = MOVE_CLASSIFICATION_BADGES[display.label];
    if (!config) return null;

    return { ...config, label: display.label };
  };

  const getMoveQualityStats = () => {
    const stats = {
      white: { best: 0, excellent: 0, good: 0, inaccuracy: 0, mistake: 0, blunder: 0 },
      black: { best: 0, excellent: 0, good: 0, inaccuracy: 0, mistake: 0, blunder: 0 },
    };
    if (!analysis || !analysis.moves) return stats;
    for (const move of analysis.moves) {
      if (!move.classification) continue;
      const cls = move.classification.toUpperCase();
      if (cls === 'UNAVAILABLE') continue;
      const display = getMoveClassificationDisplay(move.classification, move.centipawnLoss);
      const label = display.label.toLowerCase();
      const key = move.mover === 'WHITE' ? 'white' : 'black';
      if (label === 'best') stats[key].best++;
      else if (label === 'excellent') stats[key].excellent++;
      else if (label === 'good') stats[key].good++;
      else if (label === 'inaccuracy') stats[key].inaccuracy++;
      else if (label === 'mistake') stats[key].mistake++;
      else if (label === 'blunder') stats[key].blunder++;
    }
    return stats;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-800 pb-4">
        <div className="space-y-1">
          <button
            onClick={() => navigate('/games')}
            className="flex items-center gap-2 text-xs text-slate-400 hover:text-slate-200 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            Back to Games Explorer
          </button>
          <h1 className="text-2xl font-bold tracking-tight text-white">Game Replay</h1>
        </div>
        <button
          onClick={handleDownload}
          disabled={Number.isNaN(gameId)}
          className="flex items-center gap-2 px-3 py-1.5 bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-slate-200 rounded-lg text-xs font-semibold transition-colors border border-slate-700"
        >
          <Download className="w-4 h-4" />
          Download PGN
        </button>
      </div>

      {loadingGame && <LoadingSpinner message="Loading game details..." />}
      <ErrorMessage error={gameError} onRetry={fetchGame} />
      {!loadingGame && !gameError && !game && (
        <div className="text-center text-slate-400 text-sm py-6">Game not found.</div>
      )}

      {!loadingGame && !gameError && game && (
        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
            <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
              <Users className="w-4 h-4 text-emerald-400" />
              <h3 className="font-semibold text-slate-100 text-base">Players & Result</h3>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="text-sm text-slate-200">
                  <span className="font-medium">{game.whiteUsername || 'Unknown'}</span>
                  <span className="text-slate-500 ml-2">({game.whiteRating ?? '?'})</span>
                  <span className="text-slate-500 ml-1">vs</span>
                  <span className="font-medium ml-1">{game.blackUsername || 'Unknown'}</span>
                  <span className="text-slate-500 ml-2">({game.blackRating ?? '?'})</span>
                </div>
                <div className="text-xs text-slate-400">
                  Result: <span className="text-slate-200">{formatResult(game.result)}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
            <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
              <Calendar className="w-4 h-4 text-emerald-400" />
              <h3 className="font-semibold text-slate-100 text-base">Game Information</h3>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-slate-400 text-xs">
                  <Calendar className="w-3.5 h-3.5" />
                  Date
                </div>
                <div className="text-slate-200">{game.playedAt || 'Unknown'}</div>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-slate-400 text-xs">
                  <Clock className="w-3.5 h-3.5" />
                  Time Control
                </div>
                <div className="text-slate-200">{game.timeControl || 'Unknown'}</div>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-slate-400 text-xs">
                  <BookOpen className="w-3.5 h-3.5" />
                  Opening
                </div>
                <div className="text-slate-200">{game.openingName || 'Unknown Opening'}</div>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-slate-400 text-xs">
                  <Globe2 className="w-3.5 h-3.5" />
                  Platform
                </div>
                <div className="text-slate-200">{game.platform ? game.platform.replace('_', ' ') : 'Unknown'}</div>
              </div>
            </div>
          </div>

          {/* Analysis Section */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
            <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
              <BarChart3 className="w-4 h-4 text-emerald-400" />
              <h3 className="font-semibold text-slate-100 text-base">Game Analysis</h3>
            </div>
            {analysisLoading && <LoadingSpinner message="Analyzing game with Stockfish..." />}
            <ErrorMessage error={analysisError} onRetry={fetchAnalysis} />
            {!analysisLoading && !analysisError && !analysis && (
              <div className="text-center text-slate-400 text-sm py-6">No analysis available for this game.</div>
            )}
            {!analysisLoading && !analysisError && analysis && (
              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div className="bg-slate-950 border border-slate-800 rounded-lg p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-xs font-semibold text-slate-900">
                        {(game.whiteUsername && game.whiteUsername[0] ? game.whiteUsername[0].toUpperCase() : 'W')}
                      </div>
                      <div>
                        <div className="text-sm text-slate-200 font-medium">{game.whiteUsername || 'White'}</div>
                        {game.whiteRating !== null && game.whiteRating !== undefined && (
                          <div className="text-[10px] text-slate-500">{game.whiteRating}</div>
                        )}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-slate-400">Accuracy</div>
                      <div className="text-xl font-bold text-emerald-400">{formatAccuracy(analysis.whiteAccuracy)}%</div>
                    </div>
                  </div>
                  <div className="bg-slate-950 border border-slate-800 rounded-lg p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-xs font-semibold text-slate-300">
                        {(game.blackUsername && game.blackUsername[0] ? game.blackUsername[0].toUpperCase() : 'B')}
                      </div>
                      <div>
                        <div className="text-sm text-slate-200 font-medium">{game.blackUsername || 'Black'}</div>
                        {game.blackRating !== null && game.blackRating !== undefined && (
                          <div className="text-[10px] text-slate-500">{game.blackRating}</div>
                        )}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-slate-400">Accuracy</div>
                      <div className="text-xl font-bold text-emerald-400">{formatAccuracy(analysis.blackAccuracy)}%</div>
                    </div>
                  </div>
                </div>
                {analysis.moves && analysis.moves.length > 0 && (
                  <>
                    <div className="bg-slate-950 border border-slate-800 rounded-lg overflow-hidden">
                      <div className="px-3 py-2 border-b border-slate-800 text-xs text-slate-400 flex items-center gap-2">
                        <List className="w-3.5 h-3.5" />
                        Move Analysis
                      </div>
                      <div className="max-h-80 overflow-y-auto divide-y divide-slate-800">
                        {analysis.moves.map((move: GameAnalysisMove, idx: number) => {
                          const display = getMoveClassificationDisplay(move.classification, move.centipawnLoss);
                          const isSelected = currentMoveIndex === idx;
                          return (
                            <button
                              key={idx}
                              onClick={() => jumpToPly(move.ply)}
                              className={`w-full flex items-center gap-3 px-3 py-2 hover:bg-slate-800/40 transition-colors text-left ${isSelected ? 'bg-emerald-500/20 ring-1 ring-inset ring-emerald-400' : ''}`}
                            >
                              <div className="w-10 text-xs text-slate-500 text-right">{Math.ceil(move.ply / 2)}{move.mover === 'WHITE' ? '.' : '...'}</div>
                              <div className="w-16 font-mono text-sm text-slate-200">{move.playedMove}</div>
                              <span className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${display.bg} ${display.color} border ${display.border}`}>
                                {display.label}
                              </span>
                              {move.centipawnLoss !== null && move.centipawnLoss !== undefined && (
                                <span className="text-[10px] text-slate-400">CPL: {move.centipawnLoss}</span>
                              )}
                              {move.engineBestMove && move.engineBestMove !== move.playedMove && (
                                <span className="text-[10px] text-slate-500 hidden sm:inline">Best: {(() => {
                                  try {
                                    const chessBefore = new Chess();
                                    for (let i = 0; i < move.ply - 1 && i < moves.length; i++) {
                                      try {
                                        chessBefore.move(moves[i]);
                                      } catch (e) {
                                        // ignore
                                      }
                                    }
                                    const bestSan = uciToSan(chessBefore.fen(), move.engineBestMove);
                                    return bestSan || move.engineBestMove;
                                  } catch (e) {
                                    return move.engineBestMove;
                                  }
                                })()}</span>
                              )}
                            </button>
                          );
                        })}
                      </div>
                    </div>
                    <div className="bg-slate-950 border border-slate-800 rounded-lg overflow-hidden">
                      <div className="px-3 py-2 border-b border-slate-800 text-xs text-slate-400 flex items-center gap-2">
                        <BarChart3 className="w-3.5 h-3.5" />
                        Move Quality Statistics
                      </div>
                      <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                          <thead>
                            <tr className="border-b border-slate-800">
                              <th className="text-left px-3 py-2 text-xs text-slate-400 font-medium">Classification</th>
                              <th className="text-right px-3 py-2 text-xs text-slate-400 font-medium">White</th>
                              <th className="text-right px-3 py-2 text-xs text-slate-400 font-medium">Black</th>
                            </tr>
                          </thead>
                          <tbody>
                            {(() => {
                              const stats = getMoveQualityStats();
                              const rows = [
                                { key: 'best', label: 'Best' },
                                { key: 'excellent', label: 'Excellent' },
                                { key: 'good', label: 'Good' },
                                { key: 'inaccuracy', label: 'Inaccuracy' },
                                { key: 'mistake', label: 'Mistake' },
                                { key: 'blunder', label: 'Blunder' },
                              ];
                              return rows.map((row) => (
                                <tr key={row.key} className="border-b border-slate-800/50 hover:bg-slate-800/30">
                                  <td className="px-3 py-1.5 text-slate-300">{row.label}</td>
                                  <td className="px-3 py-1.5 text-right text-slate-200">{(stats.white as any)[row.key]}</td>
                                  <td className="px-3 py-1.5 text-right text-slate-200">{(stats.black as any)[row.key]}</td>
                                </tr>
                              ));
                            })()}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </>
                )}
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-4">
                <h3 className="font-semibold text-slate-100 text-base">Board</h3>
                <div className="flex items-center gap-3">
                  <div className="flex items-center gap-2 text-xs">
                    <span className="text-slate-400">Depth</span>
                    <select
                      value={analysisDepth}
                      onChange={(e) => setAnalysisDepth(Number(e.target.value))}
                      className="bg-slate-950 border border-slate-800 rounded px-2 py-1 text-slate-200 focus:outline-none focus:border-emerald-500"
                    >
                      <option value={10}>10</option>
                      <option value={12}>12</option>
                      <option value={15}>15</option>
                      <option value={18}>18</option>
                    </select>
                  </div>
                  <div className="text-xs text-slate-400">{getSideToMove()}</div>
                </div>
              </div>
              <div className="w-full flex gap-2 items-stretch justify-center">
                {(() => {
                  const evalData = formatEvaluation(getCurrentEvaluation());
                  return (
                    <div className="w-6 flex flex-col items-center justify-center">
                      <div className="text-[10px] text-slate-300 font-mono mb-1 whitespace-nowrap">{evalData.text}</div>
                      <div className="w-4 h-[400px] max-h-[500px] bg-slate-700 rounded overflow-hidden flex flex-col transition-all duration-300">
                        <div 
                          className="bg-slate-100 transition-all duration-300" 
                          style={{ height: `${evalData.percent}%` }}
                        ></div>
                        <div className="flex-1 bg-slate-900"></div>
                      </div>
                    </div>
                  );
                })()}
                <div className="flex flex-col items-center gap-2 flex-1">
                  {(() => {
                    const topColor = boardOrientation === 'white' ? 'BLACK' : 'WHITE';
                    const bottomColor = boardOrientation === 'white' ? 'WHITE' : 'BLACK';

                    const renderPlayerPanel = (color: 'WHITE' | 'BLACK') => {
                      const isWhite = color === 'WHITE';
                      const displayName = isWhite ? game.whiteUsername : game.blackUsername;
                      const rating = isWhite ? game.whiteRating : game.blackRating;
                      const fallbackInitial = isWhite ? 'W' : 'B';
                      const activeUser = normalizePlayerName(username);
                      const isActiveUser = !!activeUser && activeUser === normalizePlayerName(displayName);
                      return (
                        <div
                          className={`w-full max-w-[500px] flex items-center justify-between bg-slate-950 border rounded-lg px-3 py-2 ${
                            isActiveUser ? 'border-emerald-800/60 ring-1 ring-inset ring-emerald-500/20' : 'border-slate-800'
                          }`}
                        >
                          <div className="flex items-center gap-2">
                            <div
                              className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold ${
                                isWhite ? 'bg-slate-100 text-slate-900' : 'bg-slate-800 text-slate-300'
                              }`}
                            >
                              {displayName && displayName[0] ? displayName[0].toUpperCase() : fallbackInitial}
                            </div>
                            <div className="flex flex-col">
                              <span className="text-sm text-slate-200 font-medium">{displayName || 'Unknown'}</span>
                              {rating !== null && rating !== undefined && (
                                <span className="text-[10px] text-slate-500">{rating}</span>
                              )}
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 uppercase tracking-wider">
                              {isWhite ? 'White' : 'Black'}
                            </span>
                            {isActiveUser && (
                              <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-900/40 text-emerald-400 border border-emerald-800/40">
                                You
                              </span>
                            )}
                            {chess.turn() === (isWhite ? 'w' : 'b') && (
                              <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-900/40 text-emerald-400 border border-emerald-800/40">
                                To move
                              </span>
                            )}
                            {isWhite && (
                              <span
                                className={`text-[10px] px-1.5 py-0.5 rounded uppercase tracking-wider ${
                                  game.result === 'WHITE_WIN'
                                    ? 'bg-emerald-950/60 text-emerald-400 border border-emerald-800/40'
                                    : game.result === 'BLACK_WIN'
                                    ? 'bg-rose-950/60 text-rose-400 border border-rose-800/40'
                                    : game.result === 'DRAW'
                                    ? 'bg-amber-950/60 text-amber-400 border border-amber-800/40'
                                    : 'bg-slate-800 text-slate-400 border border-slate-700'
                                }`}
                              >
                                {formatResult(game.result)}
                              </span>
                            )}
                          </div>
                        </div>
                      );
                    };

                    return (
                      <>
                        {renderPlayerPanel(topColor)}
                        <div className="relative w-full max-w-[500px]">
                          {(() => {
                            const bestMove = getEngineBestMoveForCurrentPosition();
                            const customArrows: any[] = [];
                            const squareStyles: any = {};
                            if (bestMove && bestMove.length >= 4) {
                              const from = bestMove.substring(0, 2);
                              const to = bestMove.substring(2, 4);
                              squareStyles[from] = { backgroundColor: 'rgba(96, 165, 250, 0.4)' };
                              squareStyles[to] = { backgroundColor: 'rgba(96, 165, 250, 0.4)' };
                              customArrows.push([from, to, 'rgba(96, 165, 250, 0.8)']);
                            }
                            return (
                              <Chessboard
                                position={chess.fen()}
                                boardOrientation={boardOrientation}
                                arePiecesDraggable={false}
                                customArrows={customArrows}
                                customSquareStyles={squareStyles}
                              />
                            );
                          })()}
                          {(() => {
                            const badge = getSelectedMoveBadge();
                            const position = lastMove
                              ? getBoardCoordinates(lastMove.to, boardOrientation)
                              : null;
                            if (!badge || !position) return null;
                            const BadgeIcon = badge.Icon;
                            return (
                              <div
                                className={`pointer-events-none absolute z-20 flex h-4 w-4 items-center justify-center rounded-full shadow-md ring-1 sm:h-5 sm:w-5 ${badge.badgeClass}`}
                                style={{
                                  left: `${(position.col + 1) * 12.5}%`,
                                  top: `${position.row * 12.5}%`,
                                  transform: 'translate(-85%, 10%)',
                                }}
                                role="img"
                                aria-label={`${badge.label} move`}
                              >
                                <BadgeIcon className="h-2.5 w-2.5 sm:h-3 sm:w-3" />
                              </div>
                            );
                          })()}
                        </div>
                        {renderPlayerPanel(bottomColor)}
                      </>
                    );
                  })()}
                </div>
              </div>
              {moves.length > 0 && (
                <div className="flex items-center justify-center gap-2 pt-2">
                  <button
                    onClick={goToStart}
                    disabled={currentMoveIndex < 0}
                    className="p-2 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
                    title="Start"
                  >
                    <SkipBack className="w-4 h-4" />
                  </button>
                  <button
                    onClick={goToPrevious}
                    disabled={currentMoveIndex < 0}
                    className="p-2 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
                    title="Previous"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <button
                    onClick={togglePlayPause}
                    className="p-2 rounded bg-emerald-600 hover:bg-emerald-500 text-white transition-colors"
                    title={isPlaying ? 'Pause' : 'Play'}
                  >
                    {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                  </button>
                  <button
                    onClick={goToNext}
                    disabled={currentMoveIndex >= moves.length - 1}
                    className="p-2 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
                    title="Next"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                  <button
                    onClick={goToEnd}
                    disabled={currentMoveIndex >= moves.length - 1}
                    className="p-2 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
                    title="End"
                  >
                    <SkipForward className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>

            <div className="space-y-4">
              <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
                <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
                  <List className="w-4 h-4 text-emerald-400" />
                  <h3 className="font-semibold text-slate-100 text-base">Moves</h3>
                </div>
                {renderMoves()}
              </div>
              <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
                <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
                  <Target className="w-4 h-4 text-emerald-400" />
                  <h3 className="font-semibold text-slate-100 text-base">Stockfish Candidate Lines</h3>
                </div>
                {currentMoveIndex < 0 ? (
                  <div className="text-center text-slate-400 text-sm py-6">Play or select a move to view candidate lines.</div>
                ) : (
                  (() => {
                    const candidates = getCurrentCandidateLines();
                    if (!candidates || candidates.length === 0) {
                      return <div className="text-center text-slate-400 text-sm py-6">No candidate lines available for this position.</div>;
                    }
                    return (
                      <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                          <thead>
                            <tr className="border-b border-slate-800">
                              <th className="text-left px-2 py-2 text-xs text-slate-400 font-medium">Rank</th>
                              <th className="text-left px-2 py-2 text-xs text-slate-400 font-medium">First move</th>
                              <th className="text-left px-2 py-2 text-xs text-slate-400 font-medium">Evaluation</th>
                              <th className="text-left px-2 py-2 text-xs text-slate-400 font-medium">Continuation</th>
                            </tr>
                          </thead>
                          <tbody>
                            {candidates.slice(0, 3).map((cand: any, idx: number) => {
                              try {
                                const fen = getCurrentPositionFen();
                                let bestMoveSan = '-';
                                if (cand.bestMove) {
                                  try {
                                    bestMoveSan = uciToSan(fen, cand.bestMove) || cand.bestMove;
                                  } catch {
                                    bestMoveSan = cand.bestMove;
                                  }
                                }
                                let pvSan = '';
                                if (cand.principalVariation) {
                                  try {
                                    const pvInput = Array.isArray(cand.principalVariation) 
                                      ? cand.principalVariation.join(' ') 
                                      : String(cand.principalVariation);
                                    pvSan = uciSequenceToSan(fen, pvInput);
                                  } catch {
                                    pvSan = Array.isArray(cand.principalVariation) 
                                      ? cand.principalVariation.join(' ') 
                                      : String(cand.principalVariation);
                                  }
                                }
                                return (
                                  <tr key={idx} className="border-b border-slate-800/50 hover:bg-slate-800/30">
                                    <td className="px-2 py-2 text-slate-300">{cand.rank || idx + 1}</td>
                                    <td className="px-2 py-2 font-mono text-slate-200">{bestMoveSan}</td>
                                    <td className="px-2 py-2 text-slate-200">{formatEvaluationText(cand.centipawns, cand.mateIn)}</td>
                                    <td className="px-2 py-2 font-mono text-slate-300 text-xs break-words">{pvSan || (Array.isArray(cand.principalVariation) ? cand.principalVariation.join(' ') : cand.principalVariation)}</td>
                                  </tr>
                                );
                              } catch (e) {
                                return (
                                  <tr key={idx} className="border-b border-slate-800/50">
                                    <td className="px-2 py-2 text-slate-300">{cand.rank || idx + 1}</td>
                                    <td className="px-2 py-2 font-mono text-slate-200">{cand.bestMove || '-'}</td>
                                    <td className="px-2 py-2 text-slate-200">{formatEvaluationText(cand.centipawns, cand.mateIn)}</td>
                                    <td className="px-2 py-2 font-mono text-slate-400 text-xs break-all">{Array.isArray(cand.principalVariation) ? cand.principalVariation.join(' ') : (cand.principalVariation || '')}</td>
                                  </tr>
                                );
                              }
                            })}
                          </tbody>
                        </table>
                      </div>
                    );
                  })()
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
