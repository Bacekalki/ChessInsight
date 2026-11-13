import React, { createContext, useContext, useState } from 'react';

interface UsernameContextType {
  username: string;
  setUsername: (username: string) => void;
}

const STORAGE_KEY = 'chessinsight_username';

const UsernameContext = createContext<UsernameContextType | undefined>(undefined);

export const UsernameProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [username, setUsernameState] = useState<string>(() => {
    // No default username: start empty unless the user previously saved one.
    return localStorage.getItem(STORAGE_KEY) || '';
  });

  const setUsername = (newUsername: string) => {
    const trimmed = newUsername.trim();
    setUsernameState(trimmed);
    localStorage.setItem(STORAGE_KEY, trimmed);
  };

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
