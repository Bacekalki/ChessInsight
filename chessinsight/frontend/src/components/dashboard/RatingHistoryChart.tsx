import React from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';
import { RatingHistoryDto } from '../../types/analytics.types';
import { PlatformFilter, TimeControlFilter } from '../../types/game.types';
import { TrendingUp, Zap, Flame, Timer, Globe2 } from 'lucide-react';

interface RatingHistoryChartProps {
  data: RatingHistoryDto[];
  selectedTimeControl: TimeControlFilter;
  onTimeControlChange: (tc: TimeControlFilter) => void;
  selectedPlatform: PlatformFilter;
  onPlatformChange: (platform: PlatformFilter) => void;
  username?: string;
}

const TIME_CONTROL_OPTIONS: { label: string; value: TimeControlFilter; icon: React.ReactNode }[] = [
  { label: 'Bullet', value: 'BULLET', icon: <Zap className="w-3.5 h-3.5" /> },
  { label: 'Blitz', value: 'BLITZ', icon: <Flame className="w-3.5 h-3.5" /> },
  { label: 'Rapid', value: 'RAPID', icon: <Timer className="w-3.5 h-3.5" /> },
];

const PLATFORM_OPTIONS: { label: string; value: PlatformFilter }[] = [
  { label: 'Chess.com', value: 'CHESS_COM' },
  { label: 'Lichess', value: 'LICHESS' },
];

export const RatingHistoryChart: React.FC<RatingHistoryChartProps> = ({
  data,
  selectedTimeControl,
  onTimeControlChange,
  selectedPlatform,
  onPlatformChange,
  username,
}) => {
  const hasData = data && data.length > 0;
  const platformName = selectedPlatform === 'CHESS_COM' ? 'Chess.com' : 'Lichess';

  // Calculate statistics for badges
  const ratings = hasData ? data.map((d) => d.rating) : [];
  const currentRating = hasData ? data[data.length - 1].rating : null;
  const minRating = hasData ? Math.min(...ratings) : 0;
  const maxRating = hasData ? Math.max(...ratings) : 0;
  const ratingDelta = hasData && data.length > 1 ? currentRating! - data[0].rating : 0;

  // Dynamic Y-axis bounds with padding
  const yDomain: [number, number] = hasData
    ? [Math.max(0, minRating - 40), maxRating + 40]
    : [1000, 2000];

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
      {/* Header & Filter Selectors */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 border-b border-slate-800 pb-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <TrendingUp className="w-4 h-4 text-emerald-400" />
            <h3 className="font-semibold text-slate-100 text-base">
              Rating Progression
              <span className="text-xs font-normal text-slate-400 ml-2">
                ({platformName} &bull; {selectedTimeControl.toLowerCase()})
              </span>
            </h3>
          </div>
          <p className="text-xs text-slate-400">
            Historical rating progression tracked separately per platform
          </p>
        </div>

        {/* Dual Selectors: Platform and Time Control */}
        <div className="flex flex-wrap items-center gap-2.5">
          {/* Platform Selector */}
          <div className="flex items-center bg-slate-950 p-1 rounded-lg border border-slate-800">
            <div className="flex items-center gap-1 px-2 text-slate-500 text-xs">
              <Globe2 className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Platform:</span>
            </div>
            {PLATFORM_OPTIONS.map((plat) => {
              const isActive = selectedPlatform === plat.value;
              return (
                <button
                  key={plat.value}
                  onClick={() => onPlatformChange(plat.value)}
                  className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-600 text-white shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
                  }`}
                >
                  {plat.label}
                </button>
              );
            })}
          </div>

          {/* Time Control Selector */}
          <div className="flex items-center bg-slate-950 p-1 rounded-lg border border-slate-800">
            {TIME_CONTROL_OPTIONS.map((opt) => {
              const isActive = selectedTimeControl === opt.value;
              return (
                <button
                  key={opt.value}
                  onClick={() => onTimeControlChange(opt.value)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-800 text-emerald-400 border border-slate-700 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
                  }`}
                >
                  {opt.icon}
                  {opt.label}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Rating Stat Badges */}
      {hasData && (
        <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 pt-1">
          <div className="flex items-center gap-1.5 bg-slate-950 px-2.5 py-1 rounded border border-slate-800">
            <span>Platform:</span>
            <span className="font-semibold text-slate-200">{platformName}</span>
          </div>
          <div className="flex items-center gap-1.5 bg-slate-950 px-2.5 py-1 rounded border border-slate-800">
            <span>Current:</span>
            <span className="font-bold text-emerald-400">{currentRating}</span>
          </div>
          <div className="flex items-center gap-1.5 bg-slate-950 px-2.5 py-1 rounded border border-slate-800">
            <span>Peak:</span>
            <span className="font-bold text-amber-400">{maxRating}</span>
          </div>
          <div className="flex items-center gap-1.5 bg-slate-950 px-2.5 py-1 rounded border border-slate-800">
            <span>Lowest:</span>
            <span className="font-bold text-slate-300">{minRating}</span>
          </div>
          {data.length > 1 && (
            <div className="flex items-center gap-1.5 bg-slate-950 px-2.5 py-1 rounded border border-slate-800">
              <span>Overall Change:</span>
              <span
                className={`font-bold ${
                  ratingDelta > 0
                    ? 'text-emerald-400'
                    : ratingDelta < 0
                    ? 'text-rose-400'
                    : 'text-slate-400'
                }`}
              >
                {ratingDelta > 0 ? `+${ratingDelta}` : ratingDelta}
              </span>
            </div>
          )}
        </div>
      )}

      {/* Chart Canvas or Empty State */}
      {!hasData ? (
        <div className="h-64 flex flex-col items-center justify-center border border-dashed border-slate-800 rounded-lg text-slate-400 text-center p-6 space-y-2">
          <TrendingUp className="w-8 h-8 text-slate-600" />
          <p className="text-sm font-medium text-slate-300">
            No rating records found for {selectedTimeControl.toLowerCase()} on {platformName}
          </p>
          <p className="text-xs text-slate-500 max-w-sm">
            Import rated {selectedTimeControl.toLowerCase()} games from {platformName}
            {username ? ` for player "${username}"` : ''} to view your progression curve.
          </p>
        </div>
      ) : (
        <div className="h-72 w-full pt-2">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 10, right: 15, left: -10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
              <XAxis
                dataKey="playedAt"
                stroke="#64748b"
                fontSize={11}
                tickLine={false}
                axisLine={{ stroke: '#334155' }}
              />
              <YAxis
                domain={yDomain}
                stroke="#64748b"
                fontSize={11}
                tickLine={false}
                axisLine={{ stroke: '#334155' }}
              />
              <Tooltip
                content={({ active, payload, label }) => {
                  if (active && payload && payload.length) {
                    return (
                      <div className="bg-slate-900 border border-slate-700 px-3 py-2 rounded-lg shadow-xl text-xs space-y-1">
                        <div className="text-slate-400 font-mono text-[11px]">{label}</div>
                        <div className="text-slate-300 text-[11px]">{platformName} &bull; {selectedTimeControl}</div>
                        <div className="text-emerald-400 font-bold text-sm">
                          Rating: {payload[0].value}
                        </div>
                      </div>
                    );
                  }
                  return null;
                }}
              />
              <Line
                type="monotone"
                dataKey="rating"
                stroke="#10b981"
                strokeWidth={2.5}
                dot={{ r: 3, fill: '#10b981', stroke: '#0f172a', strokeWidth: 1 }}
                activeDot={{ r: 6, fill: '#34d399', stroke: '#064e3b', strokeWidth: 2 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
};
