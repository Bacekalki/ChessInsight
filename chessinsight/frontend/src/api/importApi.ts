import { apiClient } from './client';
import { GenericImportResponse, PgnImportReport } from '../types/import.types';

export const importApi = {
  /**
   * Upload a PGN file (multipart form data)
   */
  async uploadPgnFile(file: File): Promise<GenericImportResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return apiClient<GenericImportResponse>('/api/games/import', {
      method: 'POST',
      body: formData,
    });
  },

  /**
   * Import player archives from Chess.com
   */
  async importChessCom(username: string, numberOfGames: number = 50): Promise<PgnImportReport> {
    const params = new URLSearchParams({
      username,
      numberOfGames: String(numberOfGames),
    });
    return apiClient<PgnImportReport>(
      `/api/games/import/chesscom?${params.toString()}`,
      {
        method: 'POST',
      }
    );
  },

  /**
   * Import player games from Lichess
   */
  async importLichess(username: string, numberOfGames: number = 50): Promise<PgnImportReport> {
    const params = new URLSearchParams({
      username,
      numberOfGames: String(numberOfGames),
    });
    return apiClient<PgnImportReport>(
      `/api/games/import/lichess?${params.toString()}`,
      {
        method: 'POST',
      }
    );
  },
};
