import { useCallback, useEffect, useState } from 'react'
import { HashRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Loader, RefreshCw } from 'lucide-react'
import { authApi } from './services/api'
import { BackupRunsProvider } from './context/BackupRunsContext'
import { useTranslation } from './context/TranslationContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import BackupTablePage from './pages/BackupTablePage'
import DashboardPage from './pages/DashboardPage'
import HistoryPage from './pages/HistoryPage'
import SettingsPage from './pages/SettingsPage'
import SubscriptionPage from './pages/SubscriptionPage'

export default function App() {
  const { t } = useTranslation()
  const [firstAccess, setFirstAccess] = useState<boolean | null>(null)
  const [failed, setFailed] = useState(false)

  const load = useCallback(() => {
    setFailed(false)
    authApi.status()
      .then(s => setFirstAccess(s.firstAccess))
      .catch(() => setFailed(true))
  }, [])

  useEffect(load, [load])

  if (firstAccess === null) {
    return (
      <div style={{
        height: '100vh', display: 'flex', flexDirection: 'column', gap: 14,
        alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)',
      }}>
        {failed ? (
          <>
            <div>{t('ReactUI.BackendUnreachable', 'Unable to reach the BackupManager service.')}</div>
            <button className="btn btn-ghost" onClick={load}>
              <RefreshCw size={13} /> {t('ReactUI.RetryButton', 'Retry')}
            </button>
          </>
        ) : (
          <><Loader size={18} className="spin" />{t('ReactUI.LoadingText', 'Loading…')}</>
        )}
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
          <Route element={<BackupRunsProvider><Layout /></BackupRunsProvider>}>
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
