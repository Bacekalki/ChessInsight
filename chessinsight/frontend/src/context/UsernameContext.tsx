import React, { createContext, useContext, useState, useEffect } from 'react';

interface UsernameContextType {
  username: string;
  setUsername: (username: string) => void;
}

const STORAGE_KEY = 'chessinsight_username';

const UsernameContext = createContext<UsernameContextType | undefined>(undefined);

export const UsernameProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [username, setUsernameState] = useState<string>(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'bacekalki') return '';
    return stored || '';
  });

  const setUsername = (newUsername: string) => {
    const trimmed = newUsername.trim();
    setUsernameState(trimmed);
    localStorage.setItem(STORAGE_KEY, trimmed);
  };

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'bacekalki') {
      localStorage.setItem(STORAGE_KEY, '');
      setUsernameState('');
    }
  }, []);

  return (
    <UsernameContext.Provider value={{ username, setUsername }}>
      {children}
    </UsernameContext.Provider>
  );
};

export function useUsername(): UsernameContextType {
  const context = useContext(UsernameContext);
  if (!context) {
    throw new Error('useUsername must be used within a UsernameProvider');
  }
  return context;
}
