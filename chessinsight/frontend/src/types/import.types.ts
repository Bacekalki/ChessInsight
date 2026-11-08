/**
 * Import report returned by Lichess import and internal PgnImportService
 * Matching PgnImportReport.java
 */
export interface PgnImportReport {
  imported: number;
  skipped: number;
  malformed: number;
  totalProcessed: number;
}

/**
 * Standard import response returned by /api/games/import and /api/games/import/chesscom
 */
export interface GenericImportResponse {
  message: string;
  importedGames: number;
  skippedGames: number;
  totalProcessedGames: number;
}
