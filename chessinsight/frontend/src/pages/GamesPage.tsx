import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUsername } from '../context/UsernameContext';
import { gamesApi } from '../api/gamesApi';
import { ChessGameDto, GamePlatform, GameResultFilter, TimeControlFilter, ColorFilter } from '../types/game.types';
import { PagedModel } from '../types/api.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorMessage } from '../components/common/ErrorMessage';
import { Calendar, Clock, Download, Filter, ChevronLeft, ChevronRight } from 'lucide-react';

export const GamesPage: React.FC = () => {
  const { username } = useUsername();
  const navigate = useNavigate();
  const [pagedData, setPagedData] = useState<PagedModel<ChessGameDto>>({
    content: [],
    page: { size: 10, totalElements: 0, totalPages: 0, number: 0 },
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);

  const [opening, setOpening] = useState<string>('');
  const [result, setResult] = useState<GameResultFilter | ''>('');
  const [timeControl, setTimeControl] = useState<TimeControlFilter | ''>('');
  const [platform, setPlatform] = useState<GamePlatform | ''>('');
  const [color, setColor] = useState<ColorFilter | ''>('');
  const [from, setFrom] = useState<string>('');
  const [to, setTo] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);

  const fetchGames = useCallback(async () => {
    if (!username.trim()) {
      setPagedData({ content: [], page: { size, totalElements: 0, totalPages: 0, number: 0 } });
      setLoading(false);
      setError(null);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await gamesApi.getGames({
        username,
        opening: opening.trim() || undefined,
        result: result || undefined,
        timeControl: timeControl || undefined,
        platform: platform || undefined,
        color: color || undefined,
        from: from || undefined,
        to: to || undefined,
        page,
        size,
      });
      setPagedData(response);
    } catch (err: unknown) {
      setError(err instanceof Error ? err : new Error(String(err)));
    } finally {
      setLoading(false);
    }
  }, [username, opening, result, timeControl, platform, color, from, to, page, size]);

  useEffect(() => {
    fetchGames();
  }, [fetchGames]);

  const handleFilterChange = () => {
    setPage(0);
  };

  const handleDownload = async (id: number) => {
    try {
      await gamesApi.downloadPgn(id);
    } catch (err: unknown) {
      alert(`Download failed: ${err instanceof Error ? err.message : String(err)}`);
    }
  };

  const canGoPrevious = pagedData.page.number > 0;
  const canGoNext = pagedData.page.number + 1 < pagedData.page.totalPages;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Games Explorer</h1>
          <p className="text-sm text-slate-400">
            {username.trim() ? (
              <>Browse and view games for player{' '}<span className="font-semibold text-emerald-400">{username}</span></>
            ) : (
              'Enter a username to browse and view games.'
            )}
          </p>
        </div>
        {username.trim() && (
          <div className="text-xs bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-lg text-slate-400">
            Total Games Found: <span className="font-bold text-white">{pagedData.page.totalElements}</span>
          </div>
        )}
      </div>

      {!username.trim() && (
        <div className="border border-dashed border-slate-800 bg-slate-900/40 rounded-xl p-8 text-center">
          <p className="text-slate-300 font-medium">No username entered</p>
          <p className="text-xs text-slate-500 mt-1">Please enter your Chess.com username in the top right to browse games.</p>
        </div>
      )}

      {username.trim() && (
      <>
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-4">
        <div className="flex items-center gap-2 text-sm font-medium text-slate-200">
          <Filter className="w-4 h-4 text-emerald-400" />
          Filters
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
          <div className="space-y-1">
            <label className="text-xs text-slate-400">Opening</label>
            <input
              type="text"
              value={opening}
              onChange={(e) => {
                setOpening(e.target.value);
                handleFilterChange();
              }}
              placeholder="e.g. Sicilian"
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">Result</label>
            <select
              value={result}
              onChange={(e) => {
                setResult(e.target.value as GameResultFilter | '');
                handleFilterChange();
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="">Any</option>
              <option value="WIN">Win</option>
              <option value="LOSS">Loss</option>
              <option value="DRAW">Draw</option>
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">Time Control</label>
            <select
              value={timeControl}
              onChange={(e) => {
                setTimeControl(e.target.value as TimeControlFilter | '');
                handleFilterChange();
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="">Any</option>
              <option value="BULLET">Bullet</option>
              <option value="BLITZ">Blitz</option>
              <option value="RAPID">Rapid</option>
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">Platform</label>
            <select
              value={platform}
              onChange={(e) => {
                setPlatform(e.target.value as GamePlatform | '');
                handleFilterChange();
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="">Any</option>
              <option value="CHESS_COM">Chess.com</option>
              <option value="LICHESS">Lichess</option>
              <option value="MANUAL">Manual</option>
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">Color</label>
            <select
              value={color}
              onChange={(e) => {
                setColor(e.target.value as ColorFilter | '');
                handleFilterChange();
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value="">Any</option>
              <option value="WHITE">White</option>
              <option value="BLACK">Black</option>
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">From</label>
            <input
              type="date"
              value={from}
              onChange={(e) => {
                setFrom(e.target.value);
                handleFilterChange();
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">To</label>
            <input
              type="date"
              value={to}
              onChange={(e) => {
                setTo(e.target.value);
                handleFilterChange();
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div className="space-y-1">
            <label className="text-xs text-slate-400">Page Size</label>
            <select
              value={size}
              onChange={(e) => {
                setSize(Number(e.target.value));
                setPage(0);
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>

      {loading && <LoadingSpinner message="Querying games..." />}

      <ErrorMessage error={error} onRetry={fetchGames} />

      {!loading && !error && pagedData.content.length === 0 && (
        <div className="border border-dashed border-slate-800 bg-slate-900/40 rounded-xl p-8 text-center">
          <p className="text-slate-300 font-medium">No games found for player "{username}"</p>
          <p className="text-xs text-slate-500 mt-1">
            Try adjusting filters or import games using the Import tab.
          </p>
        </div>
      )}

      {!loading && !error && pagedData.content.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
          <div className="px-5 py-3 border-b border-slate-800 flex items-center justify-between text-xs text-slate-400">
            <span>
              Page {pagedData.page.number + 1} of {pagedData.page.totalPages} ({pagedData.page.totalElements} total)
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={!canGoPrevious}
                className="flex items-center gap-1 px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
              >
                <ChevronLeft className="w-3 h-3" />
                Prev
              </button>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={!canGoNext}
                className="flex items-center gap-1 px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
              >
                Next
                <ChevronRight className="w-3 h-3" />
              </button>
            </div>
          </div>
          <div className="divide-y divide-slate-800">
            {pagedData.content.map((game) => (
              <div
                key={game.id}
                onClick={() => navigate(`/games/${game.id}`)}
                className="p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-slate-800/40 transition-colors cursor-pointer"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2 text-sm font-medium">
                    <span className="text-slate-100">{game.whiteUsername}</span>
                    <span className="text-xs text-slate-500 font-mono">({game.whiteRating ?? '?'})</span>
                    <span className="text-xs text-slate-500">vs</span>
                    <span className="text-slate-100">{game.blackUsername}</span>
                    <span className="text-xs text-slate-500 font-mono">({game.blackRating ?? '?'})</span>
                  </div>
                  <div className="flex items-center gap-3 text-xs text-slate-400 flex-wrap">
                    <span className="flex items-center gap-1">
                      <Clock className="w-3 h-3 text-slate-500" />
                      {game.timeControl}
                    </span>
                    <span>&bull;</span>
                    <span>{game.openingName || 'Unknown Opening'}</span>
                    <span>&bull;</span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3 h-3 text-slate-500" />
                      {game.playedAt}
                    </span>
                    {game.platform && (
                      <>
                        <span>&bull;</span>
                        <span className="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 text-[10px] uppercase tracking-wider">
                          {game.platform.replace('_', ' ')}
                        </span>
                      </>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  {(() => {
                    const user = username?.trim().toLowerCase();
                    const white = game.whiteUsername?.trim().toLowerCase();
                    const black = game.blackUsername?.trim().toLowerCase();
                    let outcome: 'WIN' | 'LOSS' | 'DRAW' | 'UNKNOWN' = 'UNKNOWN';
                    if (game.result === 'DRAW') {
                      outcome = 'DRAW';
                    } else if (game.result === 'WHITE_WIN') {
                      if (user && white && user === white) outcome = 'WIN';
                      else if (user && black && user === black) outcome = 'LOSS';
                      else outcome = 'UNKNOWN';
                    } else if (game.result === 'BLACK_WIN') {
                      if (user && black && user === black) outcome = 'WIN';
                      else if (user && white && user === white) outcome = 'LOSS';
                      else outcome = 'UNKNOWN';
                    }
                    if (outcome === 'WIN') {
                      return (
                        <span className="px-2 py-0.5 rounded text-[11px] font-semibold uppercase tracking-wider bg-emerald-950/60 text-emerald-400 border border-emerald-800/40">
                          Win
                        </span>
                      );
                    }
                    if (outcome === 'LOSS') {
                      return (
                        <span className="px-2 py-0.5 rounded text-[11px] font-semibold uppercase tracking-wider bg-rose-950/60 text-rose-400 border border-rose-800/40">
                          Loss
                        </span>
                      );
                    }
                    if (outcome === 'DRAW') {
                      return (
                        <span className="px-2 py-0.5 rounded text-[11px] font-semibold uppercase tracking-wider bg-slate-800 text-slate-300 border border-slate-700">
                          Draw
                        </span>
                      );
                    }
                    return (
                      <span className="px-2 py-0.5 rounded text-[11px] font-semibold uppercase tracking-wider bg-slate-800 text-slate-400 border border-slate-700">
                        {game.result.replace('_', ' ')}
                      </span>
                    );
                  })()}
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDownload(game.id);
                    }}
                    title="Download PGN"
                    className="p-1.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
                  >
                    <Download className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div className="px-5 py-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
            <span>
              Page {pagedData.page.number + 1} of {pagedData.page.totalPages}
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={!canGoPrevious}
                className="flex items-center gap-1 px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
              >
                <ChevronLeft className="w-3 h-3" />
                Prev
              </button>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={!canGoNext}
                className="flex items-center gap-1 px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-slate-300 transition-colors"
              >
                Next
                <ChevronRight className="w-3 h-3" />
              </button>
            </div>
          </div>
        </div>
      )}
      </>
      )}
    </div>
  );
};
