import { apiClient } from './client';
import {
  ChessAnalyticsDto,
  ColorPerformanceDto,
  OpeningAnalyticsDto,
  OpeningStatsParams,
  OpponentStrengthDto,
  RatingHistoryDto,
  TimeControlAnalyticsDto,
} from '../types/analytics.types';
import { GamePlatform, TimeControlFilter } from '../types/game.types';

export const analyticsApi = {
  /**
   * Overall win rate, games, wins, losses, draws
   */
  async getSummary(username: string): Promise<ChessAnalyticsDto> {
    return apiClient<ChessAnalyticsDto>(
      `/api/analytics/summary?username=${encodeURIComponent(username)}`
    );
  },

  /**
   * Rating history for a specific time control and platform
   */
  async getRatingHistory(
    username: string,
    timeControl: TimeControlFilter,
    platform: GamePlatform
  ): Promise<RatingHistoryDto[]> {
    return apiClient<RatingHistoryDto[]>(
      `/api/analytics/rating-history?username=${encodeURIComponent(
        username
      )}&timeControl=${encodeURIComponent(timeControl)}&platform=${encodeURIComponent(platform)}`
    );
  },

  /**
   * White vs Black performance metrics
   */
  async getColorPerformance(username: string): Promise<ColorPerformanceDto[]> {
    return apiClient<ColorPerformanceDto[]>(
      `/api/analytics/color?username=${encodeURIComponent(username)}`
    );
  },

  /**
   * Performance breakdown across time control types
   */
  async getTimeControlStats(username: string): Promise<TimeControlAnalyticsDto[]> {
    return apiClient<TimeControlAnalyticsDto[]>(
      `/api/analytics/time-controls?username=${encodeURIComponent(username)}`
    );
  },

  /**
   * Performance grouped by opponent rating brackets
   */
  async getOpponentStrength(username: string): Promise<OpponentStrengthDto[]> {
    return apiClient<OpponentStrengthDto[]>(
      `/api/analytics/opponent-strength?username=${encodeURIComponent(username)}`
    );
  },

  /**
   * Opening repertoire performance with optional filters
   */
  async getOpeningStats(params: OpeningStatsParams): Promise<OpeningAnalyticsDto[]> {
    const searchParams = new URLSearchParams();
    searchParams.set('username', params.username);

    if (params.color) searchParams.set('color', params.color);
    if (params.timeControl) searchParams.set('timeControl', params.timeControl);
    if (params.minGames !== undefined) searchParams.set('minGames', params.minGames.toString());
    if (params.sortBy) searchParams.set('sortBy', params.sortBy);
    if (params.direction) searchParams.set('direction', params.direction);

    return apiClient<OpeningAnalyticsDto[]>(`/api/analytics/openings?${searchParams.toString()}`);
  },
};
