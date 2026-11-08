import React, { useEffect, useState, useCallback } from 'react';
import { useUsername } from '../context/UsernameContext';
import { analyticsApi } from '../api/analyticsApi';
import { ChessAnalyticsDto, RatingHistoryDto } from '../types/analytics.types';
import { PlatformFilter, TimeControlFilter } from '../types/game.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorMessage } from '../components/common/ErrorMessage';
import { SummaryCards } from '../components/dashboard/SummaryCards';
import { RatingHistoryChart } from '../components/dashboard/RatingHistoryChart';

export const DashboardPage: React.FC = () => {
  const { username } = useUsername();

  // Summary State
  const [summary, setSummary] = useState<ChessAnalyticsDto | null>(null);
  const [summaryLoading, setSummaryLoading] = useState<boolean>(true);
  const [summaryError, setSummaryError] = useState<Error | null>(null);

  // Rating Progression State
  const [selectedTimeControl, setSelectedTimeControl] = useState<TimeControlFilter>('BLITZ');
  const [selectedPlatform, setSelectedPlatform] = useState<PlatformFilter>('CHESS_COM');
  const [ratingHistory, setRatingHistory] = useState<RatingHistoryDto[]>([]);
  const [ratingLoading, setRatingLoading] = useState<boolean>(true);
  const [ratingError, setRatingError] = useState<Error | null>(null);

  // Fetch summary metrics
  const fetchSummary = useCallback(async () => {
    if (!username.trim()) {
      setSummary(null);
      setSummaryLoading(false);
      return;
    }
    setSummaryLoading(true);
    setSummaryError(null);
    try {
      const data = await analyticsApi.getSummary(username);
      setSummary(data);
    } catch (err: unknown) {
      setSummaryError(err instanceof Error ? err : new Error(String(err)));
      setSummary(null);
    } finally {
      setSummaryLoading(false);
    }
  }, [username]);

  // Fetch rating history
  const fetchRatingHistory = useCallback(async () => {
    if (!username.trim()) {
      setRatingHistory([]);
      setRatingLoading(false);
      return;
    }
    setRatingLoading(true);
    setRatingError(null);
    try {
      const data = await analyticsApi.getRatingHistory(
        username,
        selectedTimeControl,
        selectedPlatform
      );
      setRatingHistory(data);
    } catch (err: unknown) {
      setRatingError(err instanceof Error ? err : new Error(String(err)));
      setRatingHistory([]);
    } finally {
      setRatingLoading(false);
    }
  }, [username, selectedTimeControl, selectedPlatform]);

  // Trigger fetches on dependency changes
  useEffect(() => {
    fetchSummary();
  }, [fetchSummary]);

  useEffect(() => {
    fetchRatingHistory();
  }, [fetchRatingHistory]);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Dashboard Overview</h1>
          <p className="text-sm text-slate-400">
            {username.trim() ? (
              <>Analytics and rating progression for player{' '}<span className="font-semibold text-emerald-400">{username}</span></>
            ) : (
              'Enter a username to view analytics and rating progression.'
            )}
          </p>
        </div>
      </div>

      {!username.trim() ? (
        <div className="border border-dashed border-slate-800 bg-slate-900/40 rounded-xl p-8 text-center">
          <p className="text-slate-300 font-medium">No username entered</p>
          <p className="text-xs text-slate-500 mt-1">Please enter your Chess.com username in the top right to load your dashboard.</p>
        </div>
      ) : (
        <>
          {/* Summary Metrics Section */}
          <div className="space-y-4">
            {summaryLoading && <LoadingSpinner message="Loading player metrics..." />}
            <ErrorMessage error={summaryError} onRetry={fetchSummary} title="Failed to load summary" />
            {!summaryLoading && !summaryError && <SummaryCards summary={summary} />}
          </div>

          {/* Rating History Chart Section */}
          <div className="space-y-4">
            {ratingLoading && <LoadingSpinner message="Loading rating progression..." />}
            <ErrorMessage
              error={ratingError}
              onRetry={fetchRatingHistory}
              title="Failed to load rating history"
            />
            {!ratingLoading && !ratingError && (
              <RatingHistoryChart
                data={ratingHistory}
                selectedTimeControl={selectedTimeControl}
                onTimeControlChange={setSelectedTimeControl}
                selectedPlatform={selectedPlatform}
                onPlatformChange={setSelectedPlatform}
                username={username}
              />
            )}
          </div>
        </>
      )}

    </div>
  );
};
