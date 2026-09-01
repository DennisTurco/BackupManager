import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  LineChart, Line, CartesianGrid
} from 'recharts'
import { Database, Play, Timer, Archive, Info, Lock, Zap } from 'lucide-react'
import { analyticsApi, historyApi } from '../services/api'
import { useSubscription } from '../context/SubscriptionContext'
import { useTranslation } from '../context/TranslationContext'
import type { BackupRequest } from '../types'

export default function DashboardPage() {
  const { isLocked, isLoading: loadingSub } = useSubscription()
  const { t } = useTranslation()

  const { data: snapshot, isLoading: loadingSnap } = useQuery({
    queryKey: ['analytics'],
    queryFn: analyticsApi.getSnapshot,
    refetchInterval: 15_000,
    enabled: !isLocked,
  })

  const { data: history = [] } = useQuery({
    queryKey: ['history'],
    queryFn: historyApi.getAll,
    refetchInterval: 15_000,
    enabled: !isLocked,
  })

  if (loadingSub) return <Spinner />
  if (isLocked) return <LockedDashboard />
  if (loadingSnap || !snapshot) return <Spinner />

  const executionsByMonth = computeExecutionsByMonth(history)

  const durationTrend = Object.entries(snapshot.durationTrend)
    .sort(([a], [b]) => a.localeCompare(b))
    .slice(-30)
    .map(([date, ms]) => ({ date: shortDate(date), avgS: +(ms / 60_000).toFixed(2) }))

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Header */}
      <div className="page-header">
        <div>
          <div className="page-title">{t('ReactUI.DashboardTitle', 'Backup Analytics Dashboard')}</div>
          <div className="page-desc">{t('ReactUI.DashboardDesc', 'Overview of backup configurations and execution statistics')}</div>
        </div>
      </div>

      {/* KPI cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        <KpiCard
          icon={<Database size={18} />} color="#2196f3"
          label={t('ReactUI.KpiTotalConfigs', 'Total Configurations')}
          value={String(history.filter((r, i, arr) => arr.findIndex(x => x.backupConfigurationId === r.backupConfigurationId) === i).length)}
          sub=""
        />
        <KpiCard
          icon={<Play size={18} />} color="#5aad4e"
          label={t('ReactUI.KpiTotalExecutions', 'Total Executions')}
          value={String(snapshot.totalRequests)}
          sub={`${snapshot.successRate.toFixed(1)}${t('ReactUI.KpiSuccessRateSuffix', '% success rate')}`}
        />
        <KpiCard
          icon={<Timer size={18} />} color="#e8a735"
          label={t('ReactUI.KpiAvgDuration', 'Avg Duration')}
          value={fmtDuration(snapshot.avgDurationMs)}
          sub=""
        />
        <KpiCard
          icon={<Archive size={18} />} color="#9c6ae1"
          label={t('ReactUI.KpiAvgCompression', 'Avg Compression')}
          value={`${(snapshot.avgCompressionRate * 100).toFixed(1)}%`}
          sub=""
        />
      </div>

      {/* Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        {/* Executions by month */}
        <ChartCard title={t('ReactUI.ChartExecutionsByMonth', 'Executions by month')}>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={executionsByMonth} barSize={22}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
              <XAxis dataKey="month" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip content={<CustomTooltip unit={t('ReactUI.ChartUnitExecutions', 'executions')} />} />
              <Bar dataKey="count" fill="var(--accent)" radius={[4, 4, 0, 0]} name="Executions" />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Duration trend */}
        <ChartCard title={t('ReactUI.ChartAvgDurationTrend', 'Avg duration trend (min)')}>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={durationTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
              <XAxis dataKey="date" tick={{ fill: 'var(--text-muted)', fontSize: 10 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} />
              <Tooltip content={<CustomTooltip unit={t('ReactUI.ChartUnitMin', 'min')} />} />
              <Line dataKey="avgS" stroke="#e8a735" strokeWidth={2} dot={false} name="Avg (min)" />
            </LineChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      {/* Quick status strip */}
      <div className="card" style={{ display: 'flex', gap: 0, overflow: 'hidden' }}>
        <StatusStrip label={t('ReactUI.StatusSuccessful', 'Successful')} value={snapshot.successCount} color="var(--success)" />
        <StatusStrip
          label={t('ReactUI.StatusFailed', 'Failed')} value={snapshot.failedCount} color="var(--error)" borderLeft
          info={t('ReactUI.FailedInfoTooltip', 'A backup counts as failed when it gets interrupted before completing — e.g. the app was closed or crashed mid-backup, or the process was stopped manually. Any partial output file is discarded automatically.')}
        />
        <StatusStrip label={t('ReactUI.StatusDiskUsed', 'Disk used')}  value={fmtBytes(snapshot.totalDiskUsageBytes)} color="var(--accent)" borderLeft />
      </div>
    </div>
  )
}

/* ─── Sub-components ─────────────────────────────────────────────────────── */

function KpiCard({ icon, color, label, value, sub }: {
  icon: React.ReactNode; color: string
  label: string; value: string; sub: string
}) {
  return (
    <div className="card" style={{ padding: '16px 18px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
        <div style={{
          width: 34, height: 34, borderRadius: 8,
          background: `${color}22`,
          color, display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          {icon}
        </div>
        <span style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 500, lineHeight: 1.3 }}>{label}</span>
      </div>
      <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--text)', lineHeight: 1 }}>{value}</div>
      {sub && <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 4 }}>{sub}</div>}
    </div>
  )
}

function ChartCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="card" style={{ padding: '16px 18px' }}>
      <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 14 }}>{title}</div>
      {children}
    </div>
  )
}

function StatusStrip({ label, value, color, borderLeft, info }: {
  label: string; value: string | number; color: string; borderLeft?: boolean; info?: string
}) {
  return (
    <div style={{
      flex: 1, padding: '14px 20px',
      borderLeft: borderLeft ? '1px solid var(--border)' : undefined,
      display: 'flex', flexDirection: 'column', gap: 3,
    }}>
      <span style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 500, display: 'flex', alignItems: 'center', gap: 5 }}>
        {label}
        {info && (
          <span title={info} style={{ display: 'inline-flex', cursor: 'help' }}>
            <Info size={12} color="var(--text-dim)" style={{ flexShrink: 0 }} />
          </span>
        )}
      </span>
      <span style={{ fontSize: 18, fontWeight: 700, color }}>{value}</span>
    </div>
  )
}

function CustomTooltip({ active, payload, label, unit }: {
  active?: boolean; payload?: {name: string; value: number}[]; label?: string; unit: string
}) {
  if (!active || !payload?.length) return null
  return (
    <div style={{
      background: 'var(--bg-2)', border: '1px solid var(--border)',
      borderRadius: 6, padding: '8px 12px', fontSize: 12,
    }}>
      <div style={{ color: 'var(--text-muted)', marginBottom: 4 }}>{label}</div>
      {payload.map(p => (
        <div key={p.name} style={{ color: 'var(--text)', fontWeight: 600 }}>
          {p.value} {unit}
        </div>
      ))}
    </div>
  )
}

function LockedDashboard() {
  const { t } = useTranslation()
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div className="page-header">
        <div>
          <div className="page-title">{t('ReactUI.DashboardTitle', 'Backup Analytics Dashboard')}</div>
          <div className="page-desc">{t('ReactUI.DashboardDesc', 'Overview of backup configurations and execution statistics')}</div>
        </div>
      </div>
      <div className="card" style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center',
        gap: 14, padding: '48px 24px',
        borderColor: 'var(--error)', background: 'rgba(224,82,82,.06)',
      }}>
        <div style={{
          width: 44, height: 44, borderRadius: 12,
          background: 'rgba(224,82,82,.15)', color: 'var(--error)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Lock size={20} />
        </div>
        <div>
          <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--text)' }}>{t('ReactUI.LockedTitle', 'Analytics Dashboard is a Pro feature')}</div>
          <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 6, maxWidth: 420 }}>
            {t('ReactUI.LockedDescLine1', "La tua subscription è scaduta. Rinnovala per tornare ad avere accesso alla dashboard analytics,")}{' '}
            {t('ReactUI.LockedDescLine2', "oltre ai backup automatici e all'assistenza prioritaria.")}
          </div>
        </div>
        <Link to="/subscription" className="btn btn-primary" style={{ marginTop: 4 }}>
          <Zap size={13} /> {t('ReactUI.GoToSubscription', 'Go to Subscription')}
        </Link>
      </div>
    </div>
  )
}

function Spinner() {
  const { t } = useTranslation()
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 200, color: 'var(--text-muted)' }}>
      {t('ReactUI.LoadingAnalytics', 'Loading analytics…')}
    </div>
  )
}

/* ─── Helpers ────────────────────────────────────────────────────────────── */

function computeExecutionsByMonth(history: BackupRequest[]) {
  const map = new Map<string, number>()
  for (const r of history) {
    const m = r.startedDate.slice(0, 7) // "2024-12"
    map.set(m, (map.get(m) ?? 0) + 1)
  }
  return Array.from(map.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .slice(-12)
    .map(([month, count]) => ({ month: month.slice(2), count })) // "24-12"
}

function shortDate(iso: string) {
  const d = new Date(iso)
  return `${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getDate().toString().padStart(2, '0')}`
}

function fmtDuration(ms: number) {
  if (ms < 1000)    return `${Math.round(ms)}ms`
  if (ms < 60_000)  return `${(ms / 1000).toFixed(1)}s`
  return `${(ms / 60_000).toFixed(2)} min`
}

function fmtBytes(bytes: number) {
  if (!bytes) return '0 B'
  const k = 1024, sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`
}
