import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  message?: string;
  size?: 'sm' | 'md' | 'lg';
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  message = 'Loading...',
  size = 'md',
}) => {
  const iconSize = size === 'sm' ? 18 : size === 'lg' ? 36 : 24;

  return (
    <div className="flex flex-col items-center justify-center p-8 space-y-3 text-slate-400">
      <Loader2 size={iconSize} className="animate-spin text-emerald-500" />
      <span className="text-sm font-medium text-slate-300">{message}</span>
    </div>
  );
};
