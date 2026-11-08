import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { ApiClientError } from '../../api/client';

interface ErrorMessageProps {
  error: Error | ApiClientError | string | null;
  onRetry?: () => void;
  title?: string;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({
  error,
  onRetry,
  title = 'Something went wrong',
}) => {
  if (!error) return null;

  const isApiError = error instanceof ApiClientError;
  const message = typeof error === 'string' ? error : error.message;
  const errorTitle = isApiError ? `${error.errorTitle} (${error.status})` : title;

  return (
    <div className="rounded-lg border border-red-800/50 bg-red-950/30 p-4 text-red-200">
      <div className="flex items-start gap-3">
        <AlertCircle className="h-5 w-5 text-red-400 mt-0.5 shrink-0" />
        <div className="flex-1">
          <h4 className="font-semibold text-red-300 text-sm">{errorTitle}</h4>
          <p className="mt-1 text-sm text-red-200/90">{message}</p>
          {isApiError && error.timestamp && (
            <p className="mt-1 text-xs text-red-400/70">
              Timestamp: {new Date(error.timestamp).toLocaleString()}
            </p>
          )}
        </div>
        {onRetry && (
          <button
            onClick={onRetry}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md bg-red-900/40 hover:bg-red-900/60 text-red-200 border border-red-700/50 transition-colors"
          >
            <RefreshCw className="h-3.5 w-3.5" />
            Retry
          </button>
        )}
      </div>
    </div>
  );
};
