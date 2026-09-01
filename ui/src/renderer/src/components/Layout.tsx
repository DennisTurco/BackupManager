import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, Database, ScrollText, Settings, Sun, Moon, Github, CreditCard, Loader, CheckCircle, Lock, ChevronsLeft, ChevronsRight } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useTheme } from '../context/ThemeContext'
import { useConfig } from '../context/ConfigContext'
import { useSubscription } from '../context/SubscriptionContext'
import { useTranslation } from '../context/TranslationContext'
import { historyApi, backupApi } from '../services/api'

interface Toast { id: number; text: string }

export default function Layout() {
  const { theme, toggle } = useTheme()
  const cfg = useConfig()
  const m = cfg.menuItems
  const { isLocked: subscriptionLocked } = useSubscription()
  const { t } = useTranslation()

  const [collapsed, setCollapsed] = useState(() => {
    try { return localStorage.getItem('sidebarCollapsed') === 'true' } catch { return false }
  })
  const toggleCollapsed = () => {
    setCollapsed(prev => {
      const next = !prev
      try { localStorage.setItem('sidebarCollapsed', String(next)) } catch { /* ignore */ }
      return next
    })
  }

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

  // Detect completions by watching each backup's lastBackupDate rather than diffing the
  // running-list poll — a fast backup can start and finish between two 2s polls of that list
  // and would otherwise never be seen as "running" at all, so no toast would ever fire for it.
  const prevLastBackupDatesRef = useRef<Map<number, string | null> | null>(null)

  useEffect(() => {
    const prev = prevLastBackupDatesRef.current

    if (prev) {
      for (const backup of backups) {
        const prevDate = prev.get(backup.id)
        if (prevDate !== undefined && prevDate !== backup.lastBackupDate && backup.lastBackupDate) {
          addToast(t('ReactUI.BackupCompletedToast', 'Backup "{name}" completed').replace('{name}', backup.name))
        }
      }
    }

    prevLastBackupDatesRef.current = new Map(backups.map(b => [b.id, b.lastBackupDate]))
  }, [backups]) // eslint-disable-line react-hooks/exhaustive-deps

  const mainNav = [
    m.BackupList !== false && { to: '/backups',   label: t('ReactUI.NavBackupConfigurations', 'Backup Configurations'), icon: Database },
    m.Dashboard  !== false && { to: '/dashboard', label: t('ReactUI.NavDashboard', 'Dashboard'),                        icon: LayoutDashboard, locked: subscriptionLocked },
  ].filter(Boolean) as { to: string; label: string; icon: React.ElementType; locked?: boolean }[]

  const otherNav = [
    m.History !== false && { to: '/history',      label: t('ReactUI.NavHistory', 'History'),           icon: ScrollText },
    { to: '/subscription', label: t('ReactUI.NavSubscription', 'Subscription'), icon: CreditCard },
    m.Settings !== false && { to: '/settings',    label: t('ReactUI.NavSettings', 'Settings'),          icon: Settings },
  ].filter(Boolean) as { to: string; label: string; icon: React.ElementType; locked?: boolean }[]

  return (
    <div style={{ display: 'flex', height: '100vh', background: 'var(--bg-0)' }}>
      {/* ── Sidebar ──────────────────────────────────────────────── */}
      <aside style={{
        width: collapsed ? 60 : 220, flexShrink: 0,
        background: 'var(--bg-1)',
        borderRight: '1px solid var(--border)',
        display: 'flex', flexDirection: 'column',
        userSelect: 'none',
        transition: 'width 0.15s ease',
        overflow: 'hidden',
      }}>
        {/* Logo / app name */}
        <div style={{
          padding: collapsed ? '18px 0 14px' : '18px 16px 14px',
          borderBottom: '1px solid var(--border)',
          display: 'flex', alignItems: 'center', gap: 10,
          justifyContent: collapsed ? 'center' : 'flex-start',
        }}>
          <img
            src="/icon.png"
            alt=""
            style={{ width: 30, height: 30, borderRadius: 8, flexShrink: 0, objectFit: 'contain' }}
          />
          {!collapsed && (
            <div style={{ overflow: 'hidden', whiteSpace: 'nowrap' }}>
              <div style={{ fontWeight: 700, fontSize: 13, color: 'var(--text)' }}>Backup Manager</div>
              {cfg.email && (
                <div style={{ fontSize: 10, color: 'var(--text-dim)' }}>{cfg.email}</div>
              )}
            </div>
          )}
        </div>

        {/* Navigation */}
        <nav style={{ flex: 1, padding: '8px 6px', overflowY: 'auto', overflowX: 'hidden' }}>
          {!collapsed && <div className="section-label" style={{ padding: '12px 10px 6px' }}>Main</div>}
          {mainNav.map(({ to, label, icon: Icon, locked }) => (
            <SidebarLink key={to} to={to} label={label} icon={<Icon size={15} />} locked={locked} collapsed={collapsed} />
          ))}

          {!collapsed && <div className="section-label" style={{ padding: '16px 10px 6px' }}>Other</div>}
          {otherNav.map(({ to, label, icon: Icon }) => (
            <SidebarLink key={to} to={to} label={label} icon={<Icon size={15} />} collapsed={collapsed} />
          ))}
        </nav>

        {/* Footer */}
        <div style={{
          borderTop: '1px solid var(--border)',
          padding: '10px',
          display: 'flex', alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          flexDirection: collapsed ? 'column' : 'row',
          gap: collapsed ? 6 : 0,
        }}>
          {!collapsed && <span style={{ fontSize: 11, color: 'var(--text-dim)' }}>{cfg.version ? `v${cfg.version}` : ''}</span>}
          <div style={{ display: 'flex', gap: 4, flexDirection: collapsed ? 'column' : 'row' }}>
            {!collapsed && (
              <IconBtn title={t('ReactUI.GithubTooltip', 'GitHub')} onClick={() => {}}>
                <Github size={14} />
              </IconBtn>
            )}
            <IconBtn title={t('ReactUI.ToggleThemeTooltip', 'Toggle theme')} onClick={toggle}>
              {theme === 'dark' ? <Sun size={14} /> : <Moon size={14} />}
            </IconBtn>
            <IconBtn title={collapsed ? t('ReactUI.ExpandSidebar', 'Espandi sidebar') : t('ReactUI.CollapseSidebar', 'Comprimi sidebar')} onClick={toggleCollapsed}>
              {collapsed ? <ChevronsRight size={14} /> : <ChevronsLeft size={14} />}
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
              ? t('ReactUI.BackupProgressToast', 'Backup in progress… ({percent}%)').replace('{percent}', String(running[0].progress ?? 0))
              : t('ReactUI.BackupsRunningToast', '{count} backups running').replace('{count}', String(running.length))}
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

function SidebarLink({ to, label, icon, locked, collapsed }: {
  to: string; label: string; icon: React.ReactNode; locked?: boolean; collapsed?: boolean
}) {
  const { t } = useTranslation()
  const lockedSuffix = t('ReactUI.LockedNavSuffix', 'Pro feature, subscription expired')
  const tooltip = collapsed
    ? (locked ? `${label} — ${lockedSuffix}` : label)
    : (locked ? `${label} — ${lockedSuffix}` : undefined)

  return (
    <NavLink
      to={to}
      title={tooltip}
      style={({ isActive }) => ({
        display: 'flex', alignItems: 'center', gap: 9,
        padding: collapsed ? '9px 0' : '7px 10px',
        justifyContent: collapsed ? 'center' : 'flex-start',
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
          <span style={{ color: isActive ? 'var(--accent)' : 'var(--text-dim)', flexShrink: 0, position: 'relative' }}>
            {icon}
            {locked && collapsed && (
              <Lock size={9} color="var(--text-dim)" style={{ position: 'absolute', bottom: -3, right: -5 }} />
            )}
          </span>
          {!collapsed && <span style={{ flex: 1, whiteSpace: 'nowrap' }}>{label}</span>}
          {locked && !collapsed && <Lock size={11} color="var(--text-dim)" style={{ flexShrink: 0 }} />}
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
