import { useState, useEffect, useMemo, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { RefreshCw, Download, ArrowDown, Search, X } from 'lucide-react'
import { downloadBlob, historyApi, logsApi } from '../services/api'
import { useTranslation } from '../context/TranslationContext'
import { useBackupRuns } from '../context/BackupRunsContext'
import { PageHeader, Segmented, CenteredMessage, RunStatusBadge } from '../components/ui'
import { fmtBytes, fmtDateTime, fmtDuration } from '../utils/format'
import type { BackupRequest } from '../types'

type Tab = 'runs' | 'log'

export default function HistoryPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<Tab>(() => {
    try { return localStorage.getItem('historyTab') === 'log' ? 'log' : 'runs' } catch { return 'runs' }
  })
  const selectTab = (next: Tab) => {
    setTab(next)
    try { localStorage.setItem('historyTab', next) } catch { /* ignore */ }
  }

  return (
    <div className="page page-fill">
      <PageHeader
        title={t('ReactUI.NavHistory', 'History')}
        desc={t('ReactUI.HistoryPageDesc', 'Every backup execution and the application log')}
      />
      <div className="tabs">
        <button className={tab === 'runs' ? 'active' : ''} onClick={() => selectTab('runs')}>
          {t('ReactUI.TabExecutions', 'Executions')}
        </button>
        <button className={tab === 'log' ? 'active' : ''} onClick={() => selectTab('log')}>
          {t('ReactUI.HistoryTitle', 'Application Log')}
        </button>
      </div>
      {tab === 'runs' ? <ExecutionsTab /> : <LogTab />}
    </div>
  )
}

/* ─── Executions ─────────────────────────────────────────────────────────── */

type StatusFilter = 'all' | 'FINISHED' | 'TERMINATED' | 'IN_PROGRESS'

function ExecutionsTab() {
  const { t } = useTranslation()
  const { backups } = useBackupRuns()
  const [status, setStatus] = useState<StatusFilter>('all')
  const [configId, setConfigId] = useState<number | 'all'>('all')

  const { data: history = [], isLoading, isError } = useQuery({
    queryKey: ['history'],
    queryFn: historyApi.getAll,
    refetchInterval: 10_000,
  })

  const nameById = useMemo(() => new Map(backups.map(b => [b.id, b.name])), [backups])

  const rows = useMemo(() =>
    history
      .filter(r => status === 'all' || r.status === status)
      .filter(r => configId === 'all' || r.backupConfigurationId === configId)
      .sort((a, b) => new Date(b.startedDate).getTime() - new Date(a.startedDate).getTime()),
  [history, status, configId])

  return (
    <>
      <div className="toolbar">
        <Segmented<StatusFilter>
          value={status}
          onChange={setStatus}
          options={[
            { value: 'all', label: t('ReactUI.FilterAll', 'All') },
            { value: 'FINISHED', label: t('ReactUI.StatusCompleted', 'Completed') },
            { value: 'TERMINATED', label: t('ReactUI.StatusFailed', 'Failed') },
            { value: 'IN_PROGRESS', label: t('ReactUI.StatusRunning', 'Running') },
          ]}
        />
        <select className="input" style={{ width: 240 }} value={configId}
          onChange={e => setConfigId(e.target.value === 'all' ? 'all' : Number(e.target.value))}>
          <option value="all">{t('ReactUI.FilterAllBackups', 'All backups')}</option>
          {[...backups].sort((a, b) => a.name.localeCompare(b.name)).map(b => (
            <option key={b.id} value={b.id}>{b.name}</option>
          ))}
        </select>
        <span className="text-dim" style={{ fontSize: 12 }}>
          {t('ReactUI.RunsCount', '{count} executions').replace('{count}', String(rows.length))}
        </span>
      </div>

      <div className="card" style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>{t('ReactUI.ColStarted', 'Started')}</th>
                <th>{t('ReactUI.ColName', 'Backup Name')}</th>
                <th>{t('ReactUI.ColStatus', 'Status')}</th>
                <th>{t('ReactUI.ColTrigger', 'Trigger')}</th>
                <th style={{ textAlign: 'right' }}>{t('ReactUI.ColDuration', 'Duration')}</th>
                <th style={{ textAlign: 'right' }}>{t('ReactUI.ColSize', 'Size')}</th>
                <th style={{ textAlign: 'right' }}>{t('ReactUI.ColFiles', 'Files')}</th>
                <th>{t('ReactUI.ColError', 'Error')}</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={8}><CenteredMessage>{t('ReactUI.LoadingText', 'Loading…')}</CenteredMessage></td></tr>
              ) : isError ? (
                <tr><td colSpan={8}><CenteredMessage>{t('ReactUI.LoadFailed', 'Unable to load data.')}</CenteredMessage></td></tr>
              ) : rows.length === 0 ? (
                <tr><td colSpan={8}><CenteredMessage>{t('ReactUI.NoRunsYet', 'No runs yet.')}</CenteredMessage></td></tr>
              ) : rows.map(r => <RunRow key={r.backupRequestId} run={r} name={nameById.get(r.backupConfigurationId)} />)}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}

function RunRow({ run, name }: { run: BackupRequest; name: string | undefined }) {
  const { t } = useTranslation()
  // Runs started from this UI go through the REST API, so API counts as a manual run here
  const triggerLabel = run.triggeredBy === 'SCHEDULER'
    ? t('ReactUI.TriggerScheduler', 'Scheduled')
    : t('ReactUI.TriggerUser', 'Manual')
  const ratio = run.zippedTargetSize && run.unzippedTargetSize
    ? Math.round((1 - run.zippedTargetSize / run.unzippedTargetSize) * 100)
    : null

  return (
    <tr>
      <td style={{ whiteSpace: 'nowrap' }}>{fmtDateTime(run.startedDate)}</td>
      <td style={{ maxWidth: 220 }}>
        <div className="truncate" style={{ fontWeight: 500 }}>
          {name ?? <span className="text-dim">{t('ReactUI.DeletedBackup', 'Deleted configuration')}</span>}
        </div>
      </td>
      <td>
        {run.status === 'IN_PROGRESS'
          ? <span className="badge badge-accent">{run.progress ?? 0}%</span>
          : <RunStatusBadge status={run.status} />}
      </td>
      <td className="text-muted">{triggerLabel}</td>
      <td className="text-muted" style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>{fmtDuration(run.durationMs)}</td>
      <td className="text-muted" style={{ textAlign: 'right', whiteSpace: 'nowrap' }}
        title={run.unzippedTargetSize ? `${fmtBytes(run.unzippedTargetSize)} → ${fmtBytes(run.zippedTargetSize)}` : undefined}>
        {run.zippedTargetSize ? fmtBytes(run.zippedTargetSize) : '—'}
        {ratio !== null && ratio > 0 && <div className="cell-sub">−{ratio}%</div>}
      </td>
      <td className="text-muted" style={{ textAlign: 'right' }}>{run.filesCount || '—'}</td>
      <td style={{ maxWidth: 280, color: 'var(--error)' }}>
        <div className="truncate" title={run.errorMessage ?? undefined}>{run.errorMessage ?? ''}</div>
      </td>
    </tr>
  )
}

/* ─── Application log ────────────────────────────────────────────────────── */

type Level = 'error' | 'warn' | 'info' | 'debug'
const MAX_LINES = 3000

function LogTab() {
  const { t } = useTranslation()
  const [autoScroll, setAutoScroll] = useState(true)
  const [query, setQuery] = useState('')
  const [level, setLevel] = useState<'all' | 'warn' | 'error'>('all')
  const bottomRef = useRef<HTMLDivElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  const { data: logContent = '', isLoading, isFetching, refetch } = useQuery({
    queryKey: ['logs'],
    queryFn: logsApi.get,
    refetchInterval: 10_000,
  })

  const allLines = useMemo(() => {
    const lines = logContent ? logContent.split('\n') : []
    if (lines.length && lines[lines.length - 1] === '') lines.pop()
    return lines.map((text, i) => ({ n: i + 1, text, level: detectLevel(text) }))
  }, [logContent])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    const out = allLines.filter(l =>
      (level === 'all' || l.level === 'error' || (level === 'warn' && l.level === 'warn')) &&
      (!q || l.text.toLowerCase().includes(q)))
    return out.slice(-MAX_LINES)
  }, [allLines, query, level])

  const truncated = filtered.length === MAX_LINES

  useEffect(() => {
    if (autoScroll) bottomRef.current?.scrollIntoView()
  }, [filtered, autoScroll])

  const handleScroll = () => {
    const el = containerRef.current
    if (!el) return
    setAutoScroll(el.scrollHeight - el.scrollTop - el.clientHeight < 40)
  }

  return (
    <>
      <div className="toolbar">
        <div className="search-box" style={{ width: 260 }}>
          <Search size={13} color="var(--text-dim)" />
          <input value={query} onChange={e => setQuery(e.target.value)} placeholder={t('ReactUI.FilterLog', 'Filter log…')} />
          {query && <button className="icon-btn" style={{ padding: 2 }} onClick={() => setQuery('')}><X size={13} /></button>}
        </div>
        <Segmented
          value={level}
          onChange={setLevel}
          options={[
            { value: 'all', label: t('ReactUI.FilterAll', 'All') },
            { value: 'warn', label: t('ReactUI.FilterWarnings', 'Warnings+') },
            { value: 'error', label: t('ReactUI.FilterErrors', 'Errors') },
          ]}
        />
        <div style={{ flex: 1 }} />
        <button className="btn btn-ghost" onClick={() => refetch()} disabled={isFetching}>
          <RefreshCw size={13} className={isFetching ? 'spin' : ''} />
          {t('ReactUI.RefreshButton', 'Refresh')}
        </button>
        <button
          className="btn btn-ghost"
          onClick={() => setAutoScroll(v => !v)}
          style={{ color: autoScroll ? 'var(--accent)' : undefined }}
        >
          <ArrowDown size={13} />
          {autoScroll ? t('ReactUI.AutoScrollOn', 'Auto-scroll on') : t('ReactUI.AutoScrollOff', 'Auto-scroll off')}
        </button>
        <button className="btn btn-ghost" disabled={!logContent}
          onClick={() => downloadBlob(new Blob([logContent], { type: 'text/plain' }), 'backup-manager.log')}>
          <Download size={13} /> {t('ReactUI.DownloadButton', 'Download')}
        </button>
      </div>

      <div
        ref={containerRef}
        onScroll={handleScroll}
        className="card mono"
        style={{ flex: 1, minHeight: 0, overflow: 'auto', fontSize: 12, lineHeight: 1.6 }}
      >
        {isLoading ? (
          <CenteredMessage>{t('ReactUI.LoadingLog', 'Loading log…')}</CenteredMessage>
        ) : filtered.length === 0 ? (
          <CenteredMessage>{allLines.length === 0 ? t('ReactUI.LogEmpty', 'Log file is empty.') : t('ReactUI.NoResults', 'No results.')}</CenteredMessage>
        ) : (
          <>
            {truncated && (
              <div className="text-dim" style={{ padding: '6px 14px', fontSize: 11, borderBottom: '1px solid var(--border)' }}>
                {t('ReactUI.LogTruncated', 'Showing the last {count} lines — download the file for the full log.').replace('{count}', String(MAX_LINES))}
              </div>
            )}
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <tbody>
                {filtered.map(l => <LogLine key={l.n} number={l.n} text={l.text} level={l.level} />)}
              </tbody>
            </table>
          </>
        )}
        <div ref={bottomRef} />
      </div>
    </>
  )
}

const LEVEL_COLORS: Record<Level, string> = {
  error: 'var(--error)',
  warn: 'var(--warning)',
  info: 'var(--text)',
  debug: 'var(--text-dim)',
}

function LogLine({ number, text, level }: { number: number; text: string; level: Level | null }) {
  return (
    <tr className="log-row" style={{ background: level === 'error' ? 'var(--error-soft)' : level === 'warn' ? 'var(--warning-soft)' : undefined }}>
      <td style={{
        userSelect: 'none', padding: '1px 12px', color: 'var(--text-dim)', textAlign: 'right',
        whiteSpace: 'nowrap', verticalAlign: 'top', width: 56, borderRight: '1px solid var(--border)', fontSize: 11,
      }}>
        {number}
      </td>
      <td style={{
        padding: '1px 14px', color: level ? LEVEL_COLORS[level] : 'var(--text)',
        whiteSpace: 'pre-wrap', wordBreak: 'break-all', verticalAlign: 'top',
      }}>
        {text || ' '}
      </td>
    </tr>
  )
}

function detectLevel(line: string): Level | null {
  const upper = line.toUpperCase()
  if (/\bERROR\b/.test(upper) || /EXCEPTION/.test(upper) || /^\s+AT\s/.test(upper)) return 'error'
  if (/\bWARN(ING)?\b/.test(upper)) return 'warn'
  if (/\bINFO\b/.test(upper)) return 'info'
  if (/\bDEBUG\b|\bTRACE\b/.test(upper)) return 'debug'
  return null
}
