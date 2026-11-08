/**
 * Game result enum matching GameResult.java
 */
export type GameResult = 'WHITE_WIN' | 'BLACK_WIN' | 'DRAW' | 'UNKNOWN';

/**
 * Filter result parameter accepted by search endpoints (WIN, LOSS, DRAW)
 */
export type GameResultFilter = 'WIN' | 'LOSS' | 'DRAW';

/**
 * Time control classification matching TimeControlType.java
 */
export type TimeControlType = 'BULLET' | 'BLITZ' | 'RAPID' | 'UNKNOWN';

/**
 * Time control filter parameter (BULLET, BLITZ, RAPID)
 */
export type TimeControlFilter = 'BULLET' | 'BLITZ' | 'RAPID';

/**
 * Player color filter (WHITE, BLACK)
 */
export type ColorFilter = 'WHITE' | 'BLACK';

/**
 * Game platform source matching GamePlatform.java
 */
export type GamePlatform = 'CHESS_COM' | 'LICHESS' | 'MANUAL';

/**
 * Platform filter for rating history (CHESS_COM, LICHESS)
 */
export type PlatformFilter = 'CHESS_COM' | 'LICHESS';

/**
 * Game metadata DTO matching ChessGameDto.java
 */
export interface ChessGameDto {
  id: number;
  playedAt: string; // ISO Date YYYY-MM-DD
  whiteUsername: string;
  blackUsername: string;
  whiteRating: number | null;
  blackRating: number | null;
  result: GameResult;
  timeControl: string;
  openingName: string | null;
  platform?: GamePlatform | null;
}

/**
 * Query parameters for searching games via GET /api/games
 */
export interface GameQueryParams {
  username: string;
  opening?: string;
  result?: GameResultFilter;
  timeControl?: TimeControlFilter;
  platform?: GamePlatform;
  color?: ColorFilter;
  from?: string; // YYYY-MM-DD
  to?: string;   // YYYY-MM-DD
  page?: number;
  size?: number;
}

export interface PositionEvaluation {
  centipawns: number | null;
  mateIn: number | null;
}

export interface CandidateLine {
  rank: number;
  depth: number;
  bestMove: string;
  principalVariation: string;
  centipawns: number | null;
  mateIn: number | null;
}

export interface GameAnalysisMove {
  ply: number;
  playedMove: string;
  mover: 'WHITE' | 'BLACK';
  engineBestMove: string | null;
  centipawnLoss: number | null;
  classification: string | null;
  positionEvaluation?: PositionEvaluation | null;
  candidateLines?: CandidateLine[] | null;
}

export interface GameAnalysisResponse {
  whiteAccuracy: number;
  blackAccuracy: number;
  initialPositionEvaluation?: PositionEvaluation | null;
  moves: GameAnalysisMove[];
}
