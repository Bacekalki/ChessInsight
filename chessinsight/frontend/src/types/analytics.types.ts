import { ColorFilter, TimeControlFilter } from './game.types';

/**
 * Summary analytics DTO matching ChessAnalyticsDto.java
 */
export interface ChessAnalyticsDto {
  username: string;
  totalGames: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
}

/**
 * Opening analytics DTO matching OpeningAnalyticsDto.java
 */
export interface OpeningAnalyticsDto {
  openingName: string;
  totalGames: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
}

/**
 * Color performance DTO matching ColorPerformanceDto.java
 */
export interface ColorPerformanceDto {
  color: 'WHITE' | 'BLACK';
  totalGames: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
}

/**
 * Opponent strength category DTO matching OpponentStrengthDto.java
 */
export interface OpponentStrengthDto {
  category: 'LOWER_RATED' | 'SIMILAR_RATED' | 'HIGHER_RATED';
  totalGames: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
}

/**
 * Rating history record matching RatingHistoryDto.java
 */
export interface RatingHistoryDto {
  playedAt: string; // ISO Date YYYY-MM-DD
  rating: number;
}

/**
 * Time control performance DTO matching TimeControlAnalyticsDto.java
 */
export interface TimeControlAnalyticsDto {
  timeControl: string;
  totalGames: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
}

/**
 * Query parameters for opening statistics endpoint
 */
export interface OpeningStatsParams {
  username: string;
  color?: ColorFilter;
  timeControl?: TimeControlFilter;
  minGames?: number;
  sortBy?: 'games' | 'winrate';
  direction?: 'asc' | 'desc';
}
