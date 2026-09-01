import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, Database, ScrollText, Settings, Sun, Moon, Github, CreditCard, Loader, CheckCircle } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useTheme } from '../context/ThemeContext'
import { useConfig } from '../context/ConfigContext'
import { historyApi, backupApi } from '../services/api'
import type { BackupRequest } from '../types'

interface Toast { id: number; text: string }

export default function Layout() {
  const { theme, toggle } = useTheme()
  const cfg = useConfig()
  const m = cfg.menuItems

  const [toasts, setToasts] = useState<Toast[]>([])
  const addToast = (text: string) => {
    const id = Date.now()
    setToasts(prev => [...prev, { id, text }])
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 4000)
  }

  const { data: backups = [] } = useQuery({
    queryKey: ['backups'],
    queryFn: backupApi.getAll,
    refetchInterval: 5000,
  })

  // Dedicated running endpoint — only returns IN_PROGRESS records, much lighter than full history
  const { data: running = [] } = useQuery({
    queryKey: ['running-backups'],
    queryFn: historyApi.getRunning,
    refetchInterval: 2000,
  })

  // Map of requestId → configurationId for the previous poll, so we can name completed backups
  const prevRunningRef = useRef<Map<number, number>>(new Map())

  useEffect(() => {
    const prev = prevRunningRef.current

    // Any request that was running before but is no longer in the running list → completed
    prev.forEach((configId, requestId) => {
      if (!running.some((r: BackupRequest) => r.backupRequestId === requestId)) {
        const backup = backups.find(b => b.id === configId)
        addToast(backup ? `Backup "${backup.name}" completed` : 'Backup completed')
      }
    })

    prevRunningRef.current = new Map(
      running.map((r: BackupRequest) => [r.backupRequestId, r.backupConfigurationId])
    )
  }, [running])

  const mainNav = [
    m.BackupList !== false && { to: '/backups',   label: 'Backup Configurations', icon: Database },
    m.Dashboard  !== false && { to: '/dashboard', label: 'Dashboard',              icon: LayoutDashboard },
  ].filter(Boolean) as { to: string; label: string; icon: React.ElementType }[]

  const otherNav = [
    m.History !== false && { to: '/history',      label: 'History',      icon: ScrollText },
    { to: '/subscription', label: 'Subscription', icon: CreditCard },
    m.Settings !== false && { to: '/settings',    label: 'Settings',     icon: Settings },
  ].filter(Boolean) as { to: string; label: string; icon: React.ElementType }[]

  return (
    <div style={{ display: 'flex', height: '100vh', background: 'var(--bg-0)' }}>
      {/* ── Sidebar ──────────────────────────────────────────────── */}
      <aside style={{
        width: 220, flexShrink: 0,
        background: 'var(--bg-1)',
        borderRight: '1px solid var(--border)',
        display: 'flex', flexDirection: 'column',
        userSelect: 'none',
      }}>
        {/* Logo / app name */}
        <div style={{
          padding: '18px 16px 14px',
          borderBottom: '1px solid var(--border)',
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <div style={{
            width: 30, height: 30, borderRadius: 8,
            background: 'var(--accent)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0,
          }}>
            <Database size={16} color="#fff" />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 13, color: 'var(--text)' }}>Backup Manager</div>
          </div>
        </div>

        {/* Navigation */}
        <nav style={{ flex: 1, padding: '8px 6px', overflowY: 'auto' }}>
          <div className="section-label" style={{ padding: '12px 10px 6px' }}>Main</div>
          {mainNav.map(({ to, label, icon: Icon }) => (
            <SidebarLink key={to} to={to} label={label} icon={<Icon size={15} />} />
          ))}

          <div className="section-label" style={{ padding: '16px 10px 6px' }}>Other</div>
          {otherNav.map(({ to, label, icon: Icon }) => (
            <SidebarLink key={to} to={to} label={label} icon={<Icon size={15} />} />
          ))}
        </nav>

        {/* Footer */}
        <div style={{
          borderTop: '1px solid var(--border)',
          padding: '10px 10px',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        }}>
          <span style={{ fontSize: 11, color: 'var(--text-dim)' }}>{cfg.version ? `v${cfg.version}` : ''}</span>
          <div style={{ display: 'flex', gap: 4 }}>
            <IconBtn title="GitHub" onClick={() => {}}>
              <Github size={14} />
            </IconBtn>
            <IconBtn title="Toggle theme" onClick={toggle}>
              {theme === 'dark' ? <Sun size={14} /> : <Moon size={14} />}
            </IconBtn>
          </div>
        </div>
      </aside>

      {/* ── Content ───────────────────────────────────────────────── */}
      <main style={{
        flex: 1, overflow: 'auto',
        background: 'var(--bg-1)',
        padding: '20px 24px',
        display: 'flex', flexDirection: 'column',
      }}>
        {running.length > 0 && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 10,
            background: 'rgba(33,150,243,.12)',
            border: '1px solid rgba(33,150,243,.3)',
            borderRadius: 6, padding: '8px 14px',
            marginBottom: 14, fontSize: 12, color: 'var(--accent)',
          }}>
            <Loader size={13} className="spin" />
            {running.length === 1
              ? `Backup in progress… (${running[0].progress ?? 0}%)`
              : `${running.length} backups running`}
          </div>
        )}
        <Outlet />
      </main>

      {/* ── Toast stack ───────────────────────────────────────────── */}
      <div style={{ position: 'fixed', bottom: 20, right: 20, zIndex: 9999, display: 'flex', flexDirection: 'column-reverse', gap: 8, pointerEvents: 'none' }}>
        {toasts.map(t => (
          <div key={t.id} style={{
            display: 'flex', alignItems: 'center', gap: 10,
            background: 'var(--bg-2)',
            border: '1px solid var(--border)',
            borderRadius: 8, padding: '10px 16px',
            fontSize: 13, color: 'var(--text)',
            boxShadow: '0 4px 16px rgba(0,0,0,.25)',
            minWidth: 260, maxWidth: 380,
            animation: 'slideInRight 0.2s ease',
          }}>
            <CheckCircle size={15} color="var(--success)" style={{ flexShrink: 0 }} />
            {t.text}
          </div>
        ))}
      </div>
    </div>
  )
}

function SidebarLink({ to, label, icon }: { to: string; label: string; icon: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      style={({ isActive }) => ({
        display: 'flex', alignItems: 'center', gap: 9,
        padding: '7px 10px',
        borderRadius: 6,
        marginBottom: 1,
        fontSize: 13,
        fontWeight: isActive ? 600 : 400,
        textDecoration: 'none',
        background: isActive ? 'rgba(33,150,243,.15)' : 'transparent',
        color: isActive ? 'var(--accent)' : 'var(--text-muted)',
        transition: 'background 0.12s, color 0.12s',
      })}
    >
      {({ isActive }) => (
        <>
          <span style={{ color: isActive ? 'var(--accent)' : 'var(--text-dim)', flexShrink: 0 }}>
            {icon}
          </span>
          {label}
        </>
      )}
    </NavLink>
  )
}

function IconBtn({ title, onClick, children }: { title: string; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      title={title}
      onClick={onClick}
      style={{
        background: 'none', border: 'none', cursor: 'pointer',
        color: 'var(--text-muted)', padding: '4px 6px', borderRadius: 4,
        display: 'flex', alignItems: 'center',
        transition: 'color 0.12s, background 0.12s',
      }}
      onMouseEnter={e => {
        (e.currentTarget as HTMLButtonElement).style.background = 'var(--bg-3)'
        ;(e.currentTarget as HTMLButtonElement).style.color = 'var(--text)'
      }}
      onMouseLeave={e => {
        (e.currentTarget as HTMLButtonElement).style.background = 'none'
        ;(e.currentTarget as HTMLButtonElement).style.color = 'var(--text-muted)'
      }}
    >
      {children}
    </button>
  )
}
