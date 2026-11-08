

export type MoveClassificationDisplay = {
  label: string;
  color: string;
  bg: string;
  border: string;
};

export function getMoveClassificationDisplay(classification: string | null | undefined): MoveClassificationDisplay {
  const cls = classification?.toUpperCase();
  
  if (!cls || cls === 'UNAVAILABLE') {
    return {
      label: 'Unavailable',
      color: 'text-slate-400',
      bg: 'bg-slate-800/60',
      border: 'border-slate-700',
    };
  }
  
  if (cls === 'BEST' || cls === 'MATE_FOUND' || cls === 'MATE_CLOSER') {
    return {
      label: 'Best',
      color: 'text-emerald-400',
      bg: 'bg-emerald-950/60',
      border: 'border-emerald-800/40',
    };
  }
  
  if (cls === 'EXCELLENT') {
    return {
      label: 'Excellent',
      color: 'text-emerald-300',
      bg: 'bg-emerald-950/50',
      border: 'border-emerald-800/40',
    };
  }
  
  if (cls === 'GOOD' || cls === 'MATE_FARTHER' || cls === 'MATE_AVOIDED' || cls === 'MATE_EVALUATION') {
    return {
      label: 'Good',
      color: 'text-cyan-400',
      bg: 'bg-cyan-950/60',
      border: 'border-cyan-800/40',
    };
  }
  
  if (cls === 'INACCURACY') {
    return {
      label: 'Inaccuracy',
      color: 'text-amber-400',
      bg: 'bg-amber-950/60',
      border: 'border-amber-800/40',
    };
  }
  
  if (cls === 'MISTAKE') {
    return {
      label: 'Mistake',
      color: 'text-orange-400',
      bg: 'bg-orange-950/60',
      border: 'border-orange-800/40',
    };
  }
  
  if (cls === 'BLUNDER' || cls === 'MATE_BLUNDER' || cls === 'MATE_LOST') {
    return {
      label: 'Blunder',
      color: 'text-rose-400',
      bg: 'bg-rose-950/60',
      border: 'border-rose-800/40',
    };
  }
  
  return {
    label: 'Unavailable',
    color: 'text-slate-400',
    bg: 'bg-slate-800/60',
    border: 'border-slate-700',
  };
}

export function formatAccuracy(accuracy: number | null | undefined): string {
  if (accuracy === null || accuracy === undefined || isNaN(accuracy)) {
    return 'N/A';
  }
  // Show to at most two decimal places, remove unnecessary trailing zeros
  const rounded = Math.round(accuracy * 100) / 100;
  // Remove trailing zeros
  return rounded % 1 === 0 ? rounded.toString() : rounded.toString().replace(/0+$/, '').replace(/\.$/, '');
}
