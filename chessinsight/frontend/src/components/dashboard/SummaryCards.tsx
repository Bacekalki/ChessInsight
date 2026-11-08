import React from 'react';
import { ChessAnalyticsDto } from '../../types/analytics.types';
import { Swords, Trophy, XCircle, MinusCircle, Percent } from 'lucide-react';

interface SummaryCardsProps {
  summary: ChessAnalyticsDto | null;
}

export const SummaryCards: React.FC<SummaryCardsProps> = ({ summary }) => {
  const totalGames = summary?.totalGames ?? 0;
  const wins = summary?.wins ?? 0;
  const losses = summary?.losses ?? 0;
  const draws = summary?.draws ?? 0;
  const winRate = summary ? `${summary.winRate.toFixed(1)}%` : '0.0%';

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
      {/* Total Games */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm hover:border-slate-700 transition-colors">
        <div className="flex items-center justify-between text-slate-400 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">Total Games</span>
          <Swords className="w-4 h-4 text-emerald-400" />
        </div>
        <div className="text-2xl font-bold text-white">{totalGames}</div>
        <p className="text-xs text-slate-500 mt-1">Recorded in database</p>
      </div>

      {/* Win Rate */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm hover:border-slate-700 transition-colors">
        <div className="flex items-center justify-between text-slate-400 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">Win Rate</span>
          <Percent className="w-4 h-4 text-emerald-400" />
        </div>
        <div className="text-2xl font-bold text-emerald-400">{winRate}</div>
        <p className="text-xs text-slate-500 mt-1">Overall percentage</p>
      </div>

      {/* Wins */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm hover:border-slate-700 transition-colors">
        <div className="flex items-center justify-between text-slate-400 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">Wins</span>
          <Trophy className="w-4 h-4 text-amber-400" />
        </div>
        <div className="text-2xl font-bold text-amber-400">{wins}</div>
        <p className="text-xs text-slate-500 mt-1">Victories</p>
      </div>

      {/* Losses */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm hover:border-slate-700 transition-colors">
        <div className="flex items-center justify-between text-slate-400 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">Losses</span>
          <XCircle className="w-4 h-4 text-rose-400" />
        </div>
        <div className="text-2xl font-bold text-rose-400">{losses}</div>
        <p className="text-xs text-slate-500 mt-1">Defeats</p>
      </div>

      {/* Draws */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm hover:border-slate-700 transition-colors">
        <div className="flex items-center justify-between text-slate-400 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">Draws</span>
          <MinusCircle className="w-4 h-4 text-slate-400" />
        </div>
        <div className="text-2xl font-bold text-slate-300">{draws}</div>
        <p className="text-xs text-slate-500 mt-1">Tied games</p>
      </div>
    </div>
  );
};
