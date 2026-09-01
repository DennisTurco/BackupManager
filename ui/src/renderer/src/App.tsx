import { useEffect, useState } from 'react'
import { HashRouter, Navigate, Route, Routes } from 'react-router-dom'
import { authApi } from './services/api'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import BackupTablePage from './pages/BackupTablePage'
import DashboardPage from './pages/DashboardPage'
import HistoryPage from './pages/HistoryPage'
import SettingsPage from './pages/SettingsPage'
import SubscriptionPage from './pages/SubscriptionPage'

export default function App() {
  const [firstAccess, setFirstAccess] = useState<boolean | null>(null)

  useEffect(() => {
    authApi.status().then((s) => setFirstAccess(s.firstAccess))
  }, [])

  if (firstAccess === null) {
    return (
      <div className="flex h-screen items-center justify-center bg-gray-950 text-white">
        Loading…
      </div>
    )
  }

  return (
    <HashRouter>
      <Routes>
        {firstAccess ? (
          <>
            <Route path="/login" element={<LoginPage onRegistered={() => setFirstAccess(false)} />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </>
        ) : (
          <Route element={<Layout />}>
            <Route index element={<Navigate to="/backups" replace />} />
            <Route path="/backups" element={<BackupTablePage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/subscription" element={<SubscriptionPage />} />
            <Route path="*" element={<Navigate to="/backups" replace />} />
          </Route>
        )}
      </Routes>
    </HashRouter>
  )
}
