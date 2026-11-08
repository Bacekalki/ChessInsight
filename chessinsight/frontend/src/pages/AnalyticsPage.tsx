import React, { useEffect, useState, useCallback } from 'react';
import { useUsername } from '../context/UsernameContext';
import { analyticsApi } from '../api/analyticsApi';
import {
  OpeningAnalyticsDto,
  ColorPerformanceDto,
  TimeControlAnalyticsDto,
  OpponentStrengthDto,
} from '../types/analytics.types';
import { ColorFilter, TimeControlFilter } from '../types/game.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorMessage } from '../components/common/ErrorMessage';
import { BookOpen, Palette, Clock, Users, BarChart3 } from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  const { username } = useUsername();

  const [openings, setOpenings] = useState<OpeningAnalyticsDto[]>([]);
  const [openingsLoading, setOpeningsLoading] = useState<boolean>(true);
  const [openingsError, setOpeningsError] = useState<Error | null>(null);
  const [openingColor, setOpeningColor] = useState<ColorFilter | ''>('');
  const [openingTimeControl, setOpeningTimeControl] = useState<TimeControlFilter | ''>('');
  const [minGames, setMinGames] = useState<number>(5);
  const [sortBy, setSortBy] = useState<'games' | 'winrate'>('games');
  const [direction, setDirection] = useState<'asc' | 'desc'>('desc');

  const [colorPerf, setColorPerf] = useState<ColorPerformanceDto[]>([]);
  const [colorLoading, setColorLoading] = useState<boolean>(true);
  const [colorError, setColorError] = useState<Error | null>(null);

  const [timeControlStats, setTimeControlStats] = useState<TimeControlAnalyticsDto[]>([]);
  const [timeControlLoading, setTimeControlLoading] = useState<boolean>(true);
  const [timeControlError, setTimeControlError] = useState<Error | null>(null);

  const [opponentStrength, setOpponentStrength] = useState<OpponentStrengthDto[]>([]);
  const [opponentLoading, setOpponentLoading] = useState<boolean>(true);
  const [opponentError, setOpponentError] = useState<Error | null>(null);

  const fetchOpenings = useCallback(async () => {
    if (!username.trim()) {
      setOpenings([]);
      setOpeningsLoading(false);
      return;
    }
    setOpeningsLoading(true);
    setOpeningsError(null);
    try {
      const data = await analyticsApi.getOpeningStats({
        username,
        color: openingColor || undefined,
        timeControl: openingTimeControl || undefined,
        minGames,
        sortBy,
        direction,
      });
      setOpenings(data);
    } catch (err: unknown) {
      setOpeningsError(err instanceof Error ? err : new Error(String(err)));
      setOpenings([]);
    } finally {
      setOpeningsLoading(false);
    }
  }, [username, openingColor, openingTimeControl, minGames, sortBy, direction]);

  const fetchColorPerf = useCallback(async () => {
    if (!username.trim()) {
      setColorPerf([]);
      setColorLoading(false);
      return;
    }
    setColorLoading(true);
    setColorError(null);
    try {
      const data = await analyticsApi.getColorPerformance(username);
      setColorPerf(data);
    } catch (err: unknown) {
      setColorError(err instanceof Error ? err : new Error(String(err)));
      setColorPerf([]);
    } finally {
      setColorLoading(false);
    }
  }, [username]);

  const fetchTimeControlStats = useCallback(async () => {
    if (!username.trim()) {
      setTimeControlStats([]);
      setTimeControlLoading(false);
      return;
    }
    setTimeControlLoading(true);
    setTimeControlError(null);
    try {
      const data = await analyticsApi.getTimeControlStats(username);
      setTimeControlStats(data);
    } catch (err: unknown) {
      setTimeControlError(err instanceof Error ? err : new Error(String(err)));
      setTimeControlStats([]);
    } finally {
      setTimeControlLoading(false);
    }
  }, [username]);

  const fetchOpponentStrength = useCallback(async () => {
    if (!username.trim()) {
      setOpponentStrength([]);
      setOpponentLoading(false);
      return;
    }
    setOpponentLoading(true);
    setOpponentError(null);
    try {
      const data = await analyticsApi.getOpponentStrength(username);
      setOpponentStrength(data);
    } catch (err: unknown) {
      setOpponentError(err instanceof Error ? err : new Error(String(err)));
      setOpponentStrength([]);
    } finally {
      setOpponentLoading(false);
    }
  }, [username]);

  useEffect(() => {
    fetchOpenings();
  }, [fetchOpenings]);

  useEffect(() => {
    fetchColorPerf();
  }, [fetchColorPerf]);

  useEffect(() => {
    fetchTimeControlStats();
  }, [fetchTimeControlStats]);

  useEffect(() => {
    fetchOpponentStrength();
  }, [fetchOpponentStrength]);

  const formatCategory = (cat: string) => {
    if (cat === 'LOWER_RATED') return 'Lower Rated';
    if (cat === 'SIMILAR_RATED') return 'Similar Rated';
    if (cat === 'HIGHER_RATED') return 'Higher Rated';
    return cat;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Analytics</h1>
          <p className="text-sm text-slate-400">
            {username.trim() ? (
              <>Detailed performance insights for player{' '}<span className="font-semibold text-emerald-400">{username}</span></>
            ) : (
              'Enter a username to view detailed performance insights.'
            )}
          </p>
        </div>
      </div>

      {!username.trim() ? (
        <div className="border border-dashed border-slate-800 bg-slate-900/40 rounded-xl p-8 text-center">
          <p className="text-slate-300 font-medium">No username entered</p>
          <p className="text-xs text-slate-500 mt-1">Please enter your Chess.com username in the top right to view analytics.</p>
        </div>
      ) : (
      <>
      {/* Openings */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 border-b border-slate-800 pb-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <BookOpen className="w-4 h-4 text-emerald-400" />
              <h3 className="font-semibold text-slate-100 text-base">Opening Performance</h3>
            </div>
            <p className="text-xs text-slate-400">Results by opening with optional filters</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <select
              value={openingColor}
              onChange={(e) => setOpeningColor(e.target.value as ColorFilter | '')}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="">All Colors</option>
              <option value="WHITE">White</option>
              <option value="BLACK">Black</option>
            </select>
            <select
              value={openingTimeControl}
              onChange={(e) => setOpeningTimeControl(e.target.value as TimeControlFilter | '')}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="">All Time Controls</option>
              <option value="BULLET">Bullet</option>
              <option value="BLITZ">Blitz</option>
              <option value="RAPID">Rapid</option>
            </select>
            <select
              value={minGames}
              onChange={(e) => setMinGames(Number(e.target.value))}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value={1}>Min 1 game</option>
              <option value={3}>Min 3 games</option>
              <option value={5}>Min 5 games</option>
              <option value={10}>Min 10 games</option>
            </select>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as 'games' | 'winrate')}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="games">Sort by Games</option>
              <option value="winrate">Sort by Win Rate</option>
            </select>
            <select
              value={direction}
              onChange={(e) => setDirection(e.target.value as 'asc' | 'desc')}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </div>
        </div>
        {openingsLoading && <LoadingSpinner message="Loading openings..." />}
        <ErrorMessage error={openingsError} onRetry={fetchOpenings} />
        {!openingsLoading && !openingsError && openings.length === 0 && (
          <div className="text-center text-slate-400 text-sm py-6">No opening data found.</div>
        )}
        {!openingsLoading && !openingsError && openings.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-800 text-xs text-slate-400">
                  <th className="text-left py-2 pr-4">Opening</th>
                  <th className="text-right py-2 px-3">Games</th>
                  <th className="text-right py-2 px-3">Wins</th>
                  <th className="text-right py-2 px-3">Losses</th>
                  <th className="text-right py-2 px-3">Draws</th>
                  <th className="text-right py-2 pl-3">Win Rate</th>
                </tr>
              </thead>
              <tbody>
                {openings.map((o, idx) => (
                  <tr key={idx} className="border-b border-slate-800/60 hover:bg-slate-800/30">
                    <td className="py-2 pr-4 text-slate-200">{o.openingName || 'Unknown'}</td>
                    <td className="py-2 px-3 text-right text-slate-300">{o.totalGames}</td>
                    <td className="py-2 px-3 text-right text-emerald-400">{o.wins}</td>
                    <td className="py-2 px-3 text-right text-rose-400">{o.losses}</td>
                    <td className="py-2 px-3 text-right text-amber-400">{o.draws}</td>
                    <td className="py-2 pl-3 text-right text-slate-200">{o.winRate.toFixed(1)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Color Performance */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
          <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
            <Palette className="w-4 h-4 text-emerald-400" />
            <h3 className="font-semibold text-slate-100 text-base">Color Performance</h3>
          </div>
          {colorLoading && <LoadingSpinner message="Loading color stats..." />}
          <ErrorMessage error={colorError} onRetry={fetchColorPerf} />
          {!colorLoading && !colorError && colorPerf.length === 0 && (
            <div className="text-center text-slate-400 text-sm py-6">No color data found.</div>
          )}
          {!colorLoading && !colorError && colorPerf.length > 0 && (
            <div className="space-y-3">
              {colorPerf.map((c, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 bg-slate-950 rounded-lg border border-slate-800">
                  <div className="flex items-center gap-3">
                    <div className={`w-3 h-3 rounded-full ${c.color === 'WHITE' ? 'bg-slate-100' : 'bg-slate-700'}`}></div>
                    <span className="text-slate-200 font-medium">{c.color}</span>
                  </div>
                  <div className="flex items-center gap-4 text-xs text-slate-400">
                    <span>Games: <span className="text-slate-200">{c.totalGames}</span></span>
                    <span>W/L/D: <span className="text-emerald-400">{c.wins}</span>/<span className="text-rose-400">{c.losses}</span>/<span className="text-amber-400">{c.draws}</span></span>
                    <span>Win Rate: <span className="text-slate-200">{c.winRate.toFixed(1)}%</span></span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Time Control Stats */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
          <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
            <Clock className="w-4 h-4 text-emerald-400" />
            <h3 className="font-semibold text-slate-100 text-base">Time Control Breakdown</h3>
          </div>
          {timeControlLoading && <LoadingSpinner message="Loading time control stats..." />}
          <ErrorMessage error={timeControlError} onRetry={fetchTimeControlStats} />
          {!timeControlLoading && !timeControlError && timeControlStats.length === 0 && (
            <div className="text-center text-slate-400 text-sm py-6">No time control data found.</div>
          )}
          {!timeControlLoading && !timeControlError && timeControlStats.length > 0 && (
            <div className="space-y-3">
              {timeControlStats.map((t, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 bg-slate-950 rounded-lg border border-slate-800">
                  <span className="text-slate-200 font-medium">{t.timeControl}</span>
                  <div className="flex items-center gap-4 text-xs text-slate-400">
                    <span>Games: <span className="text-slate-200">{t.totalGames}</span></span>
                    <span>W/L/D: <span className="text-emerald-400">{t.wins}</span>/<span className="text-rose-400">{t.losses}</span>/<span className="text-amber-400">{t.draws}</span></span>
                    <span>Win Rate: <span className="text-slate-200">{t.winRate.toFixed(1)}%</span></span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Opponent Strength */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
        <div className="flex items-center gap-2 border-b border-slate-800 pb-4">
          <Users className="w-4 h-4 text-emerald-400" />
          <h3 className="font-semibold text-slate-100 text-base">Performance vs Opponent Strength</h3>
        </div>
        {opponentLoading && <LoadingSpinner message="Loading opponent strength..." />}
        <ErrorMessage error={opponentError} onRetry={fetchOpponentStrength} />
        {!opponentLoading && !opponentError && opponentStrength.length === 0 && (
          <div className="text-center text-slate-400 text-sm py-6">No opponent strength data found.</div>
        )}
        {!opponentLoading && !opponentError && opponentStrength.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {opponentStrength.map((o, idx) => (
              <div key={idx} className="p-4 bg-slate-950 rounded-lg border border-slate-800 space-y-2">
                <div className="flex items-center gap-2">
                  <BarChart3 className="w-4 h-4 text-emerald-400" />
                  <span className="text-slate-200 font-medium">{formatCategory(o.category)}</span>
                </div>
                <div className="text-xs text-slate-400 space-y-1">
                  <div>Total Games: <span className="text-slate-200">{o.totalGames}</span></div>
                  <div>W/L/D: <span className="text-emerald-400">{o.wins}</span>/<span className="text-rose-400">{o.losses}</span>/<span className="text-amber-400">{o.draws}</span></div>
                  <div>Win Rate: <span className="text-slate-200">{o.winRate.toFixed(1)}%</span></div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
      </>
      )}
    </div>
  );
};
