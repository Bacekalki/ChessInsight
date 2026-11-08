import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { UsernameProvider } from './context/UsernameContext';
import { Layout } from './components/layout/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { GamesPage } from './pages/GamesPage';
import { ImportPage } from './pages/ImportPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { GameDetailPage } from './pages/GameDetailPage';

export const App: React.FC = () => {
  return (
    <UsernameProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<DashboardPage />} />
            <Route path="games" element={<GamesPage />} />
            <Route path="games/:id" element={<GameDetailPage />} />
            <Route path="analytics" element={<AnalyticsPage />} />
            <Route path="import" element={<ImportPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </UsernameProvider>
  );
};
