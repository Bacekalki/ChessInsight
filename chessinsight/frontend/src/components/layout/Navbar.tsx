import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Swords, UploadCloud, User, Check, X, BarChart3 } from 'lucide-react';
import { useUsername } from '../../context/UsernameContext';

export const Navbar: React.FC = () => {
  const { username, setUsername } = useUsername();
  const [isEditingUser, setIsEditingUser] = useState(false);
  const [draftUser, setDraftUser] = useState(username);

  const handleSaveUsername = () => {
    setUsername(draftUser.trim());
    setIsEditingUser(false);
  };

  const handleCancelUsername = () => {
    setDraftUser(username);
    setIsEditingUser(false);
  };

  return (
    <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <div className="flex items-center gap-8">
            <NavLink to="/" className="flex items-center gap-2.5 group">
              <div className="h-9 w-9 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 group-hover:border-emerald-500/60 transition-colors">
                <svg
                  className="w-5 h-5 fill-current"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M19 22H5v-2h14v2M17.5 18H6.5l.8-4.8c-.8-.7-1.3-1.8-1.3-3.2 0-2.2 1.8-4 4-4 .4 0 .7.1 1 .2V4a2 2 0 0 1 2-2 2 2 0 0 1 2 2v2.2c.3-.1.6-.2 1-.2 2.2 0 4 1.8 4 4 0 1.4-.5 2.5-1.3 3.2l.8 4.8z" />
                </svg>
              </div>
              <div className="flex flex-col">
                <span className="font-bold text-lg text-slate-100 tracking-tight leading-none">
                  Chess<span className="text-emerald-400">Insight</span>
                </span>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider font-semibold">
                  Analytics
                </span>
              </div>
            </NavLink>

            {/* Navigation links */}
            <nav className="hidden md:flex items-center gap-1">
              <NavLink
                to="/"
                end
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-800 text-emerald-400 border border-slate-700'
                      : 'text-slate-300 hover:bg-slate-800/60 hover:text-slate-100'
                  }`
                }
              >
                <LayoutDashboard className="w-4 h-4" />
                Dashboard
              </NavLink>

              <NavLink
                to="/games"
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-800 text-emerald-400 border border-slate-700'
                      : 'text-slate-300 hover:bg-slate-800/60 hover:text-slate-100'
                  }`
                }
              >
                <Swords className="w-4 h-4" />
                Games
              </NavLink>

              <NavLink
                to="/analytics"
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-800 text-emerald-400 border border-slate-700'
                      : 'text-slate-300 hover:bg-slate-800/60 hover:text-slate-100'
                  }`
                }
              >
                <BarChart3 className="w-4 h-4" />
                Analytics
              </NavLink>

              <NavLink
                to="/import"
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-800 text-emerald-400 border border-slate-700'
                      : 'text-slate-300 hover:bg-slate-800/60 hover:text-slate-100'
                  }`
                }
              >
                <UploadCloud className="w-4 h-4" />
                Import Games
              </NavLink>
            </nav>
          </div>

          {/* Configurable Username Widget */}
          <div className="flex items-center gap-3">
            {isEditingUser ? (
              <div className="flex items-center gap-1.5 bg-slate-800 p-1 rounded-lg border border-slate-700">
                <input
                  type="text"
                  value={draftUser}
                  onChange={(e) => setDraftUser(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleSaveUsername();
                    if (e.key === 'Escape') handleCancelUsername();
                  }}
                  autoFocus
                  placeholder="Player username..."
                  className="bg-slate-900 text-slate-100 text-xs px-2.5 py-1 rounded border border-slate-700 focus:outline-none focus:border-emerald-500 w-32"
                />
                <button
                  onClick={handleSaveUsername}
                  title="Save username"
                  className="p-1 hover:bg-emerald-500/20 text-emerald-400 rounded transition-colors"
                >
                  <Check className="w-3.5 h-3.5" />
                </button>
                <button
                  onClick={handleCancelUsername}
                  title="Cancel"
                  className="p-1 hover:bg-slate-700 text-slate-400 rounded transition-colors"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2 bg-slate-800/80 border border-slate-700/80 px-3 py-1.5 rounded-lg text-xs">
                <User className="w-3.5 h-3.5 text-slate-400" />
                <span className="text-slate-400">Player:</span>
                <span className={`font-semibold ${username.trim() ? 'text-emerald-400' : 'text-slate-500 italic'}`}>{username.trim() || 'Not set'}</span>
                <button
                  onClick={() => {
                    setDraftUser(username);
                    setIsEditingUser(true);
                  }}
                  className="text-[11px] text-slate-400 hover:text-slate-200 underline ml-1 cursor-pointer"
                >
                  {username.trim() ? 'Change' : 'Set'}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
