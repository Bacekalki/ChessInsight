import { apiClient, downloadFile } from './client';
import { ChessGameDto, GameQueryParams } from '../types/game.types';
import { PagedModel } from '../types/api.types';

export const gamesApi = {
  /**
   * Search and filter games with Spring Data pagination
   */
  async getGames(params: GameQueryParams): Promise<PagedModel<ChessGameDto>> {
    const searchParams = new URLSearchParams();
    searchParams.set('username', params.username);

    if (params.opening) searchParams.set('opening', params.opening);
    if (params.result) searchParams.set('result', params.result);
    if (params.timeControl) searchParams.set('timeControl', params.timeControl);
    if (params.platform) searchParams.set('platform', params.platform);
    if (params.color) searchParams.set('color', params.color);
    if (params.from) searchParams.set('from', params.from);
    if (params.to) searchParams.set('to', params.to);
    if (params.page !== undefined) searchParams.set('page', params.page.toString());
    if (params.size !== undefined) searchParams.set('size', params.size.toString());

    return apiClient<PagedModel<ChessGameDto>>(`/api/games?${searchParams.toString()}`);
  },

  /**
   * Get single game metadata by ID
   */
  async getGameById(id: number): Promise<ChessGameDto> {
    return apiClient<ChessGameDto>(`/api/games/${id}`);
  },

  /**
   * Get parsed moves for a game as SAN array
   */
  async getGameMoves(id: number): Promise<string[]> {
    return apiClient<string[]>(`/api/games/${id}/moves`);
  },

  /**
   * Download the raw PGN file for a game
   */
  async downloadPgn(id: number): Promise<void> {
    return downloadFile(`/api/games/${id}/pgn`, `game-${id}.pgn`);
  },

  /**
   * Get Stockfish analysis for a game
   */
  async getGameAnalysis(id: number, depth: number = 12): Promise<any> {
    return apiClient<any>(`/api/analysis/games/${id}?depth=${depth}`);
  },
};
