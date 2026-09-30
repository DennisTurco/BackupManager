import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  LayoutDashboard, Database, History, Settings, Sun, Moon, CreditCard, Loader,
  Lock, ChevronsLeft, ChevronsRight, Bug, Globe, UserRound,
} from 'lucide-react'
import { useTheme } from '../context/ThemeContext'
import { useConfig } from '../context/ConfigContext'
import { useSubscription } from '../context/SubscriptionContext'
import { useTranslation } from '../context/TranslationContext'
import { useBackupRuns } from '../context/BackupRunsContext'
import { authApi } from '../services/api'

interface NavItem { to: string; label: string; icon: React.ElementType; locked?: boolean; badge?: number }

export default function Layout() {
  const { theme, toggle } = useTheme()
  const cfg = useConfig()
  const m = cfg.menuItems
  const { isLocked: subscriptionLocked } = useSubscription()
  const { t } = useTranslation()
  const { running, backups } = useBackupRuns()
  const navigate = useNavigate()

  const { data: user } = useQuery({ queryKey: ['user'], queryFn: authApi.getUser, staleTime: Infinity })

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

  const mainNav = [
    m.BackupList !== false && { to: '/backups', label: t('ReactUI.NavBackupConfigurations', 'Backup Configurations'), icon: Database, badge: running.length || undefined },
    m.Dashboard !== false && { to: '/dashboard', label: t('ReactUI.NavDashboard', 'Dashboard'), icon: LayoutDashboard, locked: subscriptionLocked },
    m.History !== false && { to: '/history', label: t('ReactUI.NavHistory', 'History'), icon: History },
  ].filter(Boolean) as NavItem[]

  const otherNav = [
    { to: '/subscription', label: t('ReactUI.NavSubscription', 'Subscription'), icon: CreditCard },
    m.Settings !== false && { to: '/settings', label: t('ReactUI.NavSettings', 'Settings'), icon: Settings },
  ].filter(Boolean) as NavItem[]

  const nameOf = (configId: number) => backups.find(b => b.id === configId)?.name ?? `#${configId}`

  return (
    <div style={{ display: 'flex', height: '100vh', background: 'var(--bg-0)' }}>
      {/* ── Sidebar ──────────────────────────────────────────────── */}
      <aside style={{
        width: collapsed ? 60 : 224, flexShrink: 0,
        background: 'var(--bg-1)',
        borderRight: '1px solid var(--border)',
        display: 'flex', flexDirection: 'column',
        userSelect: 'none',
        transition: 'width 0.15s ease',
        overflow: 'hidden',
      }}>
        <div style={{
          height: 60, padding: collapsed ? 0 : '0 16px',
          borderBottom: '1px solid var(--border)',
          display: 'flex', alignItems: 'center', gap: 10,
          justifyContent: collapsed ? 'center' : 'flex-start',
          flexShrink: 0,
        }}>
          <img src="/icon.png" alt="" style={{ width: 28, height: 28, borderRadius: 7, flexShrink: 0, objectFit: 'contain' }} />
          {!collapsed && (
            <div style={{ overflow: 'hidden', whiteSpace: 'nowrap' }}>
              <div style={{ fontWeight: 700, fontSize: 13, color: 'var(--text)' }}>Backup Manager</div>
              {cfg.version && <div style={{ fontSize: 10, color: 'var(--text-dim)' }}>v{cfg.version}</div>}
            </div>
          )}
        </div>

        <nav style={{ flex: 1, padding: '8px 8px', overflowY: 'auto', overflowX: 'hidden' }}>
          {!collapsed && <div className="section-label" style={{ padding: '10px 10px 6px' }}>{t('ReactUI.NavSectionMain', 'Main')}</div>}
          {mainNav.map(item => <SidebarLink key={item.to} item={item} collapsed={collapsed} />)}

          {collapsed
            ? <div style={{ height: 1, background: 'var(--border)', margin: '10px 8px' }} />
            : <div className="section-label" style={{ padding: '16px 10px 6px' }}>{t('ReactUI.NavSectionOther', 'Other')}</div>}
          {otherNav.map(item => <SidebarLink key={item.to} item={item} collapsed={collapsed} />)}
        </nav>

        {/* Footer: user + quick actions */}
        <div style={{ borderTop: '1px solid var(--border)', padding: 8, display: 'flex', flexDirection: 'column', gap: 6 }}>
          {user && !collapsed && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 6px', minWidth: 0 }} title={user.email}>
              <div style={{
                width: 26, height: 26, borderRadius: '50%', flexShrink: 0,
                background: 'var(--accent-soft)', color: 'var(--accent)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 11, fontWeight: 700,
              }}>
                {initials(user.name, user.surname) || <UserRound size={13} />}
              </div>
              <div style={{ minWidth: 0 }}>
                <div className="truncate" style={{ fontSize: 12, fontWeight: 600 }}>{user.name} {user.surname}</div>
                <div className="truncate" style={{ fontSize: 10, color: 'var(--text-dim)' }}>{user.email}</div>
              </div>
            </div>
          )}
          <div style={{
            display: 'flex', gap: 2,
            flexDirection: collapsed ? 'column' : 'row',
            alignItems: 'center', justifyContent: collapsed ? 'center' : 'flex-start',
          }}>
            {cfg.links.issuePage && m.BugReport !== false && (
              <a className="icon-btn" href={cfg.links.issuePage} target="_blank" rel="noreferrer" title={t('ReactUI.ReportBug', 'Report a bug')}>
                <Bug size={15} />
              </a>
            )}
            {cfg.links.infoPage && m.InfoPage !== false && (
              <a className="icon-btn" href={cfg.links.infoPage} target="_blank" rel="noreferrer" title={t('ReactUI.ProjectPage', 'Project page')}>
                <Globe size={15} />
              </a>
            )}
            <button className="icon-btn" title={t('ReactUI.ToggleThemeTooltip', 'Toggle theme')} onClick={toggle}>
              {theme === 'dark' ? <Sun size={15} /> : <Moon size={15} />}
            </button>
            {!collapsed && <div style={{ flex: 1 }} />}
            <button
              className="icon-btn"
              title={collapsed ? t('ReactUI.ExpandSidebar', 'Expand sidebar') : t('ReactUI.CollapseSidebar', 'Collapse sidebar')}
              onClick={toggleCollapsed}
            >
              {collapsed ? <ChevronsRight size={15} /> : <ChevronsLeft size={15} />}
            </button>
          </div>
        </div>
      </aside>

      {/* ── Content ───────────────────────────────────────────────── */}
      <main style={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', background: 'var(--bg-1)' }}>
        {running.length > 0 && (
          <button
            onClick={() => navigate('/backups')}
            style={{
              display: 'flex', alignItems: 'center', gap: 10, flexShrink: 0,
              background: 'var(--accent-soft)', color: 'var(--accent)',
              border: 'none', borderBottom: '1px solid var(--border)',
              padding: '8px 24px', fontSize: 12, fontFamily: 'inherit',
              cursor: 'pointer', textAlign: 'left',
            }}
          >
            <Loader size={13} className="spin" />
            <span style={{ fontWeight: 500 }}>
              {running.length === 1
                ? t('ReactUI.BackupProgressNamed', 'Backing up "{name}"… {percent}%')
                    .replace('{name}', nameOf(running[0].backupConfigurationId))
                    .replace('{percent}', String(running[0].progress ?? 0))
                : t('ReactUI.BackupsRunningToast', '{count} backups running').replace('{count}', String(running.length))}
            </span>
            {running.length === 1 && (
              <div className="progress" style={{ maxWidth: 160 }}>
                <div style={{ width: `${Math.min(100, running[0].progress ?? 0)}%` }} />
              </div>
            )}
          </button>
        )}
        <div style={{ flex: 1, minHeight: 0, overflow: 'auto', padding: '22px 28px' }}>
          <Outlet />
        </div>
      </main>
    </div>
  )
}

function SidebarLink({ item, collapsed }: { item: NavItem; collapsed: boolean }) {
  const { t } = useTranslation()
  const { to, label, icon: Icon, locked, badge } = item
  const lockedSuffix = t('ReactUI.LockedNavSuffix', 'Pro feature, subscription expired')
  const tooltip = locked ? `${label} — ${lockedSuffix}` : collapsed ? label : undefined

  return (
    <NavLink to={to} title={tooltip} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}${collapsed ? ' collapsed' : ''}`}>
      <span className="nav-icon">
        <Icon size={16} />
        {collapsed && locked && <Lock size={9} style={{ position: 'absolute', bottom: -3, right: -5 }} />}
        {collapsed && badge && (
          <span style={{ position: 'absolute', top: -3, right: -4, width: 7, height: 7, borderRadius: '50%', background: 'var(--accent)' }} />
        )}
      </span>
      {!collapsed && <span className="truncate" style={{ flex: 1 }}>{label}</span>}
      {!collapsed && locked && <Lock size={11} color="var(--text-dim)" style={{ flexShrink: 0 }} />}
      {!collapsed && badge && <span className="nav-badge">{badge}</span>}
    </NavLink>
  )
}

function initials(name?: string, surname?: string) {
  return `${name?.[0] ?? ''}${surname?.[0] ?? ''}`.toUpperCase()
}
