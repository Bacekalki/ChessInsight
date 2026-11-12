

export type MoveClassificationDisplay = {
  label: string;
  color: string;
  bg: string;
  border: string;
};

type DisplayCategory = 'BEST' | 'EXCELLENT' | 'GOOD' | 'INACCURACY' | 'MISTAKE' | 'BLUNDER' | 'UNAVAILABLE';

function displayForCategory(category: DisplayCategory): MoveClassificationDisplay {
  switch (category) {
    case 'BEST':
      return { label: 'Best', color: 'text-emerald-400', bg: 'bg-emerald-950/60', border: 'border-emerald-800/40' };
    case 'EXCELLENT':
      return { label: 'Excellent', color: 'text-emerald-300', bg: 'bg-emerald-950/50', border: 'border-emerald-800/40' };
    case 'GOOD':
      return { label: 'Good', color: 'text-cyan-400', bg: 'bg-cyan-950/60', border: 'border-cyan-800/40' };
    case 'INACCURACY':
      return { label: 'Inaccuracy', color: 'text-amber-400', bg: 'bg-amber-950/60', border: 'border-amber-800/40' };
    case 'MISTAKE':
      return { label: 'Mistake', color: 'text-orange-400', bg: 'bg-orange-950/60', border: 'border-orange-800/40' };
    case 'BLUNDER':
      return { label: 'Blunder', color: 'text-rose-400', bg: 'bg-rose-950/60', border: 'border-rose-800/40' };
    case 'UNAVAILABLE':
    default:
      return { label: 'Unavailable', color: 'text-slate-400', bg: 'bg-slate-800/60', border: 'border-slate-700' };
  }
}

function classifyByCentipawnLoss(centipawnLoss: number): DisplayCategory {
  const loss = Math.max(0, centipawnLoss);
  if (loss <= 10) return 'BEST';
  if (loss <= 25) return 'EXCELLENT';
  if (loss <= 50) return 'GOOD';
  if (loss <= 100) return 'INACCURACY';
  if (loss <= 200) return 'MISTAKE';
  return 'BLUNDER';
}

export function getMoveClassificationDisplay(
  classification: string | null | undefined,
  centipawnLoss?: number | null
): MoveClassificationDisplay {
  const cls = classification?.toUpperCase();

  if (!cls || cls === 'UNAVAILABLE') {
    return displayForCategory('UNAVAILABLE');
  }

  // Standard categories
  if (cls === 'BEST') return displayForCategory('BEST');
  if (cls === 'EXCELLENT') return displayForCategory('EXCELLENT');
  if (cls === 'GOOD') return displayForCategory('GOOD');
  if (cls === 'INACCURACY') return displayForCategory('INACCURACY');
  if (cls === 'MISTAKE') return displayForCategory('MISTAKE');
  if (cls === 'BLUNDER') return displayForCategory('BLUNDER');

  // Mate-related classifications mapped into the six existing categories
  if (cls === 'MATE_FOUND' || cls === 'MATE_CLOSER' || cls === 'MATE_AVOIDED') {
    return displayForCategory('BEST');
  }
  if (cls === 'MATE_LOST' || cls === 'MATE_BLUNDER') {
    return displayForCategory('BLUNDER');
  }
  if (cls === 'MATE_FARTHER') {
    // Severity indicated by backend CPL when available: large loss => BLUNDER, otherwise MISTAKE
    if (typeof centipawnLoss === 'number' && Number.isFinite(centipawnLoss)) {
      return displayForCategory(centipawnLoss > 200 ? 'BLUNDER' : 'MISTAKE');
    }
    return displayForCategory('MISTAKE');
  }
  if (cls === 'MATE_EVALUATION') {
    // Use backend CPL and normal classification where available
    if (typeof centipawnLoss === 'number' && Number.isFinite(centipawnLoss)) {
      return displayForCategory(classifyByCentipawnLoss(centipawnLoss));
    }
    return displayForCategory('GOOD');
  }

  // Unknown classifications fall back safely without introducing new categories
  return displayForCategory('UNAVAILABLE');
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
