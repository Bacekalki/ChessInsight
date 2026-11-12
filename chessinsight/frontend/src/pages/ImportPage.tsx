import React, { useRef, useState } from 'react';
import { useUsername } from '../context/UsernameContext';
import { importApi } from '../api/importApi';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorMessage } from '../components/common/ErrorMessage';
import { UploadCloud, CheckCircle2, FileText } from 'lucide-react';

export const ImportPage: React.FC = () => {
  const { username } = useUsername();
  const [platformUser, setPlatformUser] = useState(username);
  const [numberOfGames, setNumberOfGames] = useState<number>(50);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<Error | null>(null);
  const [successReport, setSuccessReport] = useState<{
    message: string;
    imported: number;
    skipped: number;
    total: number;
  } | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [dragActive, setDragActive] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validatePlatformImport = (): { username: string; count: number } | null => {
    const trimmedUser = platformUser.trim();
    if (!trimmedUser) {
      setError(new Error('Please enter a username.'));
      return null;
    }
    if (!Number.isFinite(numberOfGames) || !Number.isInteger(numberOfGames) || numberOfGames <= 0) {
      setError(new Error('Number of games must be a positive whole number.'));
      return null;
    }
    return { username: trimmedUser, count: numberOfGames };
  };

  const handleChessComImport = async () => {
    const validated = validatePlatformImport();
    if (!validated) return;
    setLoading(true);
    setError(null);
    setSuccessReport(null);
    try {
      const res = await importApi.importChessCom(validated.username, validated.count);
      setSuccessReport({
        message: 'Chess.com games imported successfully',
        imported: res.imported,
        skipped: res.skipped,
        total: res.totalProcessed,
      });
    } catch (err: unknown) {
      setError(err instanceof Error ? err : new Error(String(err)));
    } finally {
      setLoading(false);
    }
  };

  const handleLichessImport = async () => {
    const validated = validatePlatformImport();
    if (!validated) return;
    setLoading(true);
    setError(null);
    setSuccessReport(null);
    try {
      const res = await importApi.importLichess(validated.username, validated.count);
      setSuccessReport({
        message: 'Lichess games imported successfully',
        imported: res.imported,
        skipped: res.skipped,
        total: res.totalProcessed,
      });
    } catch (err: unknown) {
      setError(err instanceof Error ? err : new Error(String(err)));
    } finally {
      setLoading(false);
    }
  };

  const validateAndSetFile = (file: File | null) => {
    if (!file) {
      setSelectedFile(null);
      return;
    }
    const lowerName = file.name.toLowerCase();
    if (!lowerName.endsWith('.pgn')) {
      setError(new Error('Please select a valid .pgn file.'));
      setSelectedFile(null);
      return;
    }
    setError(null);
    setSelectedFile(file);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files && e.target.files[0] ? e.target.files[0] : null;
    validateAndSetFile(file);
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    if (!loading) {
      setDragActive(true);
    }
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (loading) return;
    const file = e.dataTransfer.files && e.dataTransfer.files[0] ? e.dataTransfer.files[0] : null;
    validateAndSetFile(file);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handlePgnUpload = async () => {
    if (!selectedFile || loading) return;
    setLoading(true);
    setError(null);
    setSuccessReport(null);
    try {
      const res = await importApi.uploadPgnFile(selectedFile);
      setSuccessReport({
        message: res.message,
        imported: res.importedGames,
        skipped: res.skippedGames,
        total: res.totalProcessedGames,
      });
      setSelectedFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err : new Error(String(err)));
    } finally {
      setLoading(false);
    }
  };

  const openFilePicker = () => {
    if (loading) return;
    fileInputRef.current?.click();
  };

  return (
    <div className="space-y-6">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl font-bold tracking-tight text-white">Import Games</h1>
        <p className="text-sm text-slate-400">
          Sync your historical games from Chess.com, Lichess, or upload PGN files.
        </p>
      </div>

      {loading && <LoadingSpinner message="Importing games..." />}

      <ErrorMessage error={error} />

      {successReport && (
        <div className="rounded-lg border border-emerald-800/50 bg-emerald-950/30 p-4 text-emerald-200 flex items-start gap-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
          <div className="space-y-2 text-sm w-full">
            <h4 className="font-semibold text-emerald-300">{successReport.message}</h4>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 text-xs">
              <div className="bg-emerald-950/40 border border-emerald-900/50 rounded-md px-3 py-2">
                <div className="text-emerald-300/80">Imported games</div>
                <div className="font-semibold text-emerald-100 text-sm">{successReport.imported}</div>
                <div className="text-[11px] text-emerald-300/60">Newly imported</div>
              </div>
              <div className="bg-emerald-950/40 border border-emerald-900/50 rounded-md px-3 py-2">
                <div className="text-emerald-300/80">Skipped games</div>
                <div className="font-semibold text-emerald-100 text-sm">{successReport.skipped}</div>
                <div className="text-[11px] text-emerald-300/60">Already in database</div>
              </div>
              <div className="bg-emerald-950/40 border border-emerald-900/50 rounded-md px-3 py-2">
                <div className="text-emerald-300/80">Total processed games</div>
                <div className="font-semibold text-emerald-100 text-sm">{successReport.total}</div>
                <div className="text-[11px] text-emerald-300/60">Processed in this import</div>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Platform Sync */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-4">
          <h2 className="text-base font-semibold text-white">Online Platform Import</h2>
          <p className="text-xs text-slate-400">
            Fetch games directly from public APIs without needing manual export.
          </p>

          <div className="space-y-2">
            <label className="text-xs font-medium text-slate-300">Platform Handle / Username</label>
            <input
              type="text"
              value={platformUser}
              onChange={(e) => setPlatformUser(e.target.value)}
              placeholder="Username..."
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-medium text-slate-300">Number of games</label>
            <input
              type="number"
              min={1}
              step={1}
              value={numberOfGames}
              onChange={(e) => {
                const val = e.target.value;
                setNumberOfGames(val === '' ? NaN : Number(val));
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
            />
            <p className="text-[11px] text-slate-500">Defaults to 50. Imports the most recent games.</p>
          </div>

          <div className="flex gap-3 pt-2">
            <button
              onClick={handleChessComImport}
              disabled={loading || !platformUser.trim()}
              className="flex-1 py-2 px-3 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded-lg text-xs font-semibold transition-colors"
            >
              Import from Chess.com
            </button>
            <button
              onClick={handleLichessImport}
              disabled={loading || !platformUser.trim()}
              className="flex-1 py-2 px-3 bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-slate-200 rounded-lg text-xs font-semibold transition-colors border border-slate-700"
            >
              Import from Lichess
            </button>
          </div>
        </div>

        {/* PGN File Upload */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-4 flex flex-col justify-between">
          <div className="space-y-2">
            <h2 className="text-base font-semibold text-white">PGN File Upload</h2>
            <p className="text-xs text-slate-400">
              Upload local .pgn database files directly to your ChessInsight instance.
            </p>
          </div>

          <div className="space-y-3">
            <input
              ref={fileInputRef}
              type="file"
              accept=".pgn"
              onChange={handleFileSelect}
              className="hidden"
            />
            <div
              onClick={openFilePicker}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              className={`border border-dashed rounded-lg p-6 text-center space-y-2 cursor-pointer transition-colors ${
                dragActive
                  ? 'border-emerald-500 bg-emerald-950/20'
                  : 'border-slate-700 bg-slate-950/50 hover:border-slate-500 hover:bg-slate-900/60'
              } ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              <UploadCloud className="w-8 h-8 text-slate-500 mx-auto" />
              <p className="text-xs text-slate-400">
                {dragActive ? 'Drop your PGN file here' : 'Drag and drop PGN files here, or click to browse'}
              </p>
              <p className="text-[11px] text-slate-600">Only .pgn files are accepted</p>
            </div>

            {selectedFile && (
              <div className="flex items-center justify-between bg-slate-950 border border-slate-800 rounded-lg px-3 py-2">
                <div className="flex items-center gap-2 text-sm text-slate-200">
                  <FileText className="w-4 h-4 text-emerald-400" />
                  <span className="truncate max-w-[200px]">{selectedFile.name}</span>
                </div>
                <button
                  onClick={handlePgnUpload}
                  disabled={loading}
                  className="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded text-xs font-semibold transition-colors"
                >
                  Upload
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
