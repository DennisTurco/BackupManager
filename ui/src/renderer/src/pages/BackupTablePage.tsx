import { useState, useMemo, useEffect, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Play, Pencil, Trash2, Search, X, ChevronUp, FolderOpen, ArrowUp, ArrowDown, ChevronsUpDown, StopCircle, CheckCircle2 } from 'lucide-react'
import { backupApi, historyApi, settingsApi } from '../services/api'
import type { BackupConfig, CreateBackupPayload, TimeInterval } from '../types'
import { ContextMenu } from '../components/ContextMenu'
import { useTranslation } from '../context/TranslationContext'

declare global {
  interface Window {
    electron?: {
      openFolder: () => Promise<string | null>
      openPath:   (path: string) => Promise<string>
    }
  }
}

interface CtxState { x: number; y: number; backup: BackupConfig }

type SortKey = 'name' | 'targetPath' | 'destinationPath' | 'lastBackupDate' | 'automatic' | 'nextBackupDate' | 'interval' | 'maxToKeep'

function compareBackups(a: BackupConfig, b: BackupConfig, key: SortKey): number {
  switch (key) {
    case 'name':            return a.name.localeCompare(b.name)
    case 'targetPath':      return a.targetPath.localeCompare(b.targetPath)
    case 'destinationPath': return a.destinationPath.localeCompare(b.destinationPath)
    case 'lastBackupDate':  return (a.lastBackupDate ?? '').localeCompare(b.lastBackupDate ?? '')
    case 'nextBackupDate':  return (a.nextBackupDate ?? '').localeCompare(b.nextBackupDate ?? '')
    case 'automatic':       return Number(a.automatic) - Number(b.automatic)
    case 'maxToKeep':       return a.maxToKeep - b.maxToKeep
    case 'interval':        return intervalTotalMinutes(a.timeIntervalBackup) - intervalTotalMinutes(b.timeIntervalBackup)
  }
}

export default function BackupTablePage() {
  const qc = useQueryClient()
  const { t } = useTranslation()
  const [search, setSearch]       = useState('')
  const [selected, setSelected]   = useState<BackupConfig | null>(null)
  const [editing, setEditing]     = useState<BackupConfig | null>(null)
  const [creating, setCreating]   = useState(false)
  const [renaming, setRenaming]   = useState<BackupConfig | null>(null)
  const [ctx, setCtx]             = useState<CtxState | null>(null)

  const { data: backups = [], isLoading } = useQuery({
    queryKey: ['backups'],
    queryFn: backupApi.getAll,
    refetchInterval: 5000,
  })

  // Poll running backups often so the in-progress bar stays responsive
  const { data: running = [] } = useQuery({
    queryKey: ['backups-running'],
    queryFn: historyApi.getRunning,
    refetchInterval: 1000,
  })
  const progressByConfigId = useMemo(() => {
    const map = new Map<number, number>()
    for (const r of running) map.set(r.backupConfigurationId, r.progress)
    return map
  }, [running])

  // A backup just finished for this config — force-refresh its row (lastBackupDate, count, …)
  const prevRunningIds = usePrevious(new Set(running.map(r => r.backupConfigurationId)))
  useEffect(() => {
    const currentIds = new Set(running.map(r => r.backupConfigurationId))
    const justFinished = [...(prevRunningIds ?? [])].some(id => !currentIds.has(id))
    if (justFinished) qc.invalidateQueries({ queryKey: ['backups'] })
  }, [running]) // eslint-disable-line react-hooks/exhaustive-deps

  // Backups triggered by this client, keyed by id → trigger timestamp. A fast backup can start
  // and finish between two polls of `backups-running`, so it never appears as "running" at all —
  // relying only on that poll would leave the user with zero feedback. Instead, once triggered we
  // actively confirm completion by checking history directly, independent of the running-list poll.
  const [pendingRuns, setPendingRuns] = useState<Record<number, number>>({})
  const [runResult, setRunResult] = useState<{ name: string; ok: boolean } | null>(null)

  // Ids considered "busy" for disabling actions — either server-confirmed running or
  // locally triggered and not yet confirmed complete.
  const busyIds = useMemo(() => {
    const set = new Set(progressByConfigId.keys())
    for (const id of Object.keys(pendingRuns)) set.add(Number(id))
    return set
  }, [progressByConfigId, pendingRuns])

  useEffect(() => {
    const ids = Object.keys(pendingRuns).map(Number)
    if (ids.length === 0) return

    const poll = async () => {
      for (const id of ids) {
        const triggeredAt = pendingRuns[id]
        // Give up after 3 minutes so a stuck/unreachable request doesn't poll forever
        if (Date.now() - triggeredAt > 3 * 60_000) {
          setPendingRuns(prev => { const next = { ...prev }; delete next[id]; return next })
          continue
        }
        try {
          const history = await historyApi.getByConfig(id)
          const latest = history
            .filter(r => new Date(r.startedDate).getTime() >= triggeredAt - 2000)
            .sort((a, b) => new Date(b.startedDate).getTime() - new Date(a.startedDate).getTime())[0]

          if (latest && latest.status !== 'IN_PROGRESS') {
            setPendingRuns(prev => { const next = { ...prev }; delete next[id]; return next })
            const backup = backups.find(b => b.id === id)
            setRunResult({ name: backup?.name ?? String(id), ok: latest.status === 'FINISHED' })
            setTimeout(() => setRunResult(null), 5000)
            qc.invalidateQueries({ queryKey: ['backups'] })
          }
        } catch {
          // network hiccup — try again on the next tick
        }
      }
    }

    const interval = setInterval(poll, 400)
    return () => clearInterval(interval)
  }, [pendingRuns]) // eslint-disable-line react-hooks/exhaustive-deps

  // Keep selected row in sync when data refreshes (TanStack Query v5 removed onSuccess)
  useEffect(() => {
    setSelected(prev => {
      if (!prev) return prev
      const fresh = backups.find(b => b.id === prev.id)
      return fresh ?? prev
    })
  }, [backups])

  const filtered = useMemo(() => {
    const q = search.toLowerCase()
    return q ? backups.filter(b =>
      b.name.toLowerCase().includes(q) ||
      b.targetPath.toLowerCase().includes(q) ||
      b.destinationPath.toLowerCase().includes(q)
    ) : backups
  }, [backups, search])

  const [sort, setSort] = useState<{ key: SortKey; dir: 'asc' | 'desc' }>({ key: 'name', dir: 'asc' })
  const toggleSort = (key: SortKey) => {
    setSort(prev => prev.key === key ? { key, dir: prev.dir === 'asc' ? 'desc' : 'asc' } : { key, dir: 'asc' })
  }
  const sorted = useMemo(() => {
    const factor = sort.dir === 'asc' ? 1 : -1
    return [...filtered].sort((a, b) => factor * compareBackups(a, b, sort.key))
  }, [filtered, sort])

  const [runError, setRunError] = useState<string | null>(null)
  const runMutation = useMutation({
    mutationFn: (id: number) => backupApi.run(id),
    onSuccess: (_data, id) => {
      setRunError(null)
      setPendingRuns(prev => ({ ...prev, [id]: Date.now() }))
      qc.invalidateQueries({ queryKey: ['backups'] })
    },
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setRunError(msg ?? t('ReactUI.FailedToStartBackup', 'Failed to start backup'))
      setTimeout(() => setRunError(null), 5000)
    }
  })
  const deleteMutation = useMutation({
    mutationFn: (id: number) => backupApi.delete(id),
    onSuccess: () => { setSelected(null); qc.invalidateQueries({ queryKey: ['backups'] }) }
  })
  const interruptMutation = useMutation({
    mutationFn: (id: number) => backupApi.interrupt(id),
    onSuccess: () => {
      setRunError(null)
      qc.invalidateQueries({ queryKey: ['backups-running'] })
      qc.invalidateQueries({ queryKey: ['backups'] })
    },
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setRunError(msg ?? t('ReactUI.FailedToInterruptBackup', 'Failed to interrupt backup'))
      setTimeout(() => setRunError(null), 5000)
    }
  })
  const duplicateMutation = useMutation({
    mutationFn: (b: BackupConfig) => backupApi.create({
      name: `${b.name} (copy)`,
      targetPath: b.targetPath,
      destinationPath: b.destinationPath,
      automatic: b.automatic,
      timeIntervalBackup: b.timeIntervalBackup,
      notes: b.notes ?? '',
      maxToKeep: b.maxToKeep,
    }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['backups'] })
  })
  const toggleAutoMutation = useMutation({
    mutationFn: (b: BackupConfig) => backupApi.update(b.id, {
      name: b.name,
      targetPath: b.targetPath,
      destinationPath: b.destinationPath,
      automatic: !b.automatic,
      timeIntervalBackup: b.timeIntervalBackup,
      notes: b.notes ?? '',
      maxToKeep: b.maxToKeep,
    }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['backups'] })
  })

  const buildMenu = (b: BackupConfig) => [
    { label: t('General.EditButton', 'Edit'),     onClick: () => setEditing(b), disabled: busyIds.has(b.id) },
    { label: t('General.DeleteButton', 'Delete'), onClick: () => { if (confirm(t('ReactUI.DeleteConfirm', 'Delete "{name}"?').replace('{name}', b.name))) deleteMutation.mutate(b.id) }, disabled: busyIds.has(b.id) },
    { label: t('ReactUI.MenuDuplicate', 'Duplicate'), onClick: () => duplicateMutation.mutate(b) },
    { label: t('ReactUI.MenuRename', 'Rename'),       onClick: () => setRenaming(b) },
    { type: 'separator' as const },
    { label: t('ReactUI.MenuOpenSourcePath', 'Open source path'),      onClick: () => window.electron?.openPath(b.targetPath),      disabled: !window.electron },
    { label: t('ReactUI.MenuOpenDestPath', 'Open destination path'), onClick: () => window.electron?.openPath(b.destinationPath), disabled: !window.electron },
    { type: 'separator' as const },
    {
      label: t('General.Backup', 'Backup'),
      submenu: [
        { label: t('ReactUI.MenuRunSingleBackup', 'Run single backup'), onClick: () => runMutation.mutate(b.id), disabled: busyIds.has(b.id) },
        {
          label: t('ReactUI.MenuAutoBackup', 'Auto backup'),
          checked: b.automatic,
          onClick: () => {
            if (!b.automatic && !hasValidInterval(b.timeIntervalBackup)) {
              setEditing(b)   // force user to set an interval first
            } else {
              toggleAutoMutation.mutate(b)
            }
          }
        },
        { label: t('ReactUI.MenuInterruptBackup', 'Interrupt backup process'), onClick: () => interruptMutation.mutate(b.id), disabled: !busyIds.has(b.id) },
      ]
    },
    { type: 'separator' as const },
    {
      label: t('ReactUI.MenuCopyText', 'Copy text'),
      submenu: [
        { label: t('ReactUI.MenuCopyName', 'Copy backup name'),      onClick: () => navigator.clipboard.writeText(b.name) },
        { label: t('ReactUI.MenuCopySourcePath', 'Copy source path'),      onClick: () => navigator.clipboard.writeText(b.targetPath) },
        { label: t('ReactUI.MenuCopyDestPath', 'Copy destination path'), onClick: () => navigator.clipboard.writeText(b.destinationPath) },
      ]
    },
  ]

  const handleRightClick = (e: React.MouseEvent, b: BackupConfig) => {
    e.preventDefault()
    e.stopPropagation()
    setCtx({ x: e.clientX, y: e.clientY, backup: b })
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 0 }}>
      {/* Header */}
      <div className="page-header" style={{ marginBottom: 10 }}>
        <div>
          <div className="page-title">{t('ReactUI.BackupListTitle', 'Elenco backup')}</div>
          <div className="page-desc">{t('ReactUI.BackupListDesc', 'Gestisci e monitora le configurazioni di backup, inclusa la creazione, modifica, pianificazione ed esecuzione.')}</div>
        </div>
      </div>

      {/* Toolbar: search + action buttons */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          background: 'var(--bg-3)', border: '1px solid var(--border)',
          borderRadius: 6, padding: '0 10px', width: 260, flexShrink: 0,
        }}>
          <Search size={13} color="var(--text-dim)" />
          <input
            value={search} onChange={e => setSearch(e.target.value)}
            placeholder={t('General.QuickSearch', 'Cerca…')}
            style={{ flex: 1, background: 'none', border: 'none', outline: 'none',
              color: 'var(--text)', fontSize: 13, padding: '6px 0' }}
          />
          {search && (
            <button onClick={() => setSearch('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex' }}>
              <X size={13} />
            </button>
          )}
        </div>

        <div style={{ flex: 1 }} />

        <button className="btn btn-primary" onClick={() => setCreating(true)}>
          <Plus size={13} /> {t('General.CreateButton', 'Crea')}
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected || busyIds.has(selected.id)}
          onClick={() => selected && setEditing(selected)}
          title={selected && busyIds.has(selected.id) ? t('ReactUI.BackupInProgress', 'Backup in progress') : undefined}
        >
          <Pencil size={13} /> {t('General.EditButton', 'Modifica')}
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected || busyIds.has(selected.id)}
          style={selected ? { color: 'var(--error)' } : {}}
          onClick={() => {
            if (selected && confirm(t('ReactUI.DeleteConfirm', 'Delete "{name}"?').replace('{name}', selected.name)))
              deleteMutation.mutate(selected.id)
          }}
          title={selected && busyIds.has(selected.id) ? t('ReactUI.BackupInProgress', 'Backup in progress') : undefined}
        >
          <Trash2 size={13} /> {t('General.DeleteButton', 'Elimina')}
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected || busyIds.has(selected.id)}
          style={selected ? { color: 'var(--success)' } : {}}
          onClick={() => selected && runMutation.mutate(selected.id)}
          title={selected && busyIds.has(selected.id) ? t('ReactUI.BackupAlreadyInProgress', 'Backup already in progress') : t('ReactUI.RunBackupNow', 'Run backup now')}
        >
          <Play size={13} /> {t('ReactUI.RunButton', 'Avvia')}
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected || !busyIds.has(selected.id)}
          style={selected && busyIds.has(selected.id) ? { color: 'var(--error)' } : {}}
          onClick={() => selected && interruptMutation.mutate(selected.id)}
          title={t('ReactUI.MenuInterruptBackup', 'Interrupt backup process')}
        >
          <StopCircle size={13} /> {t('ReactUI.StopButton', 'Interrompi')}
        </button>
      </div>

      {/* Run error banner */}
      {runError && (
        <div style={{
          fontSize: 12, color: 'var(--error)',
          background: 'rgba(224,82,82,.1)', border: '1px solid rgba(224,82,82,.25)',
          borderRadius: 6, padding: '7px 12px', marginBottom: 8,
        }}>
          {runError}
        </div>
      )}

      {/* Run result banner — covers backups that finish before the running-poll ever catches them */}
      {runResult && (
        <div style={{
          fontSize: 12, color: runResult.ok ? 'var(--success)' : 'var(--error)',
          background: runResult.ok ? 'rgba(90,173,78,.1)' : 'rgba(224,82,82,.1)',
          border: `1px solid ${runResult.ok ? 'rgba(90,173,78,.25)' : 'rgba(224,82,82,.25)'}`,
          borderRadius: 6, padding: '7px 12px', marginBottom: 8,
        }}>
          {runResult.ok
            ? t('ReactUI.BackupRunSuccess', 'Backup "{name}" completed successfully').replace('{name}', runResult.name)
            : t('ReactUI.BackupRunFailedResult', 'Backup "{name}" failed').replace('{name}', runResult.name)}
        </div>
      )}

      {/* Table */}
      <div className="card" style={{ overflow: 'hidden', flex: 1, minHeight: 0 }}>
        <div style={{ overflowX: 'auto', height: '100%' }}>
          <table className="data-table">
            <thead>
              <tr>
                <SortableHeader label={t('ReactUI.ColName', 'Nome del Backup')} sortKey="name" sort={sort} onSort={toggleSort} />
                <SortableHeader label={t('ReactUI.ColSourcePath', 'Percorso Iniziale')} sortKey="targetPath" sort={sort} onSort={toggleSort} />
                <SortableHeader label={t('ReactUI.ColDestPath', 'Percorso di Destinazione')} sortKey="destinationPath" sort={sort} onSort={toggleSort} />
                <th style={{ width: 140 }}>{t('ReactUI.ColStatus', 'Stato')}</th>
                <SortableHeader label={t('ReactUI.ColLastBackup', 'Ultimo Backup')} sortKey="lastBackupDate" sort={sort} onSort={toggleSort} nowrap />
                <SortableHeader label={t('ReactUI.ColAutoBackup', 'Auto')} sortKey="automatic" sort={sort} onSort={toggleSort} align="center" />
                <SortableHeader label={t('ReactUI.ColNextDate', 'Data del Prossimo')} sortKey="nextBackupDate" sort={sort} onSort={toggleSort} nowrap />
                <SortableHeader
                  label={t('ReactUI.ColInterval', 'Intervallo')}
                  tooltip={t('ReactUI.ColIntervalFormatHint', 'Formato: gg.HH:mm')}
                  sortKey="interval" sort={sort} onSort={toggleSort}
                />
                <SortableHeader label={t('ReactUI.ColMaxToKeep', 'Numero massimo')} sortKey="maxToKeep" sort={sort} onSort={toggleSort} align="center" />
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={9} style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>{t('ReactUI.LoadingText', 'Caricamento…')}</td></tr>
              ) : sorted.length === 0 ? (
                <tr><td colSpan={9} style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>
                  {search ? t('ReactUI.NoResults', 'Nessun risultato.') : t('ReactUI.NoBackupConfigs', 'Nessuna configurazione di backup.')}
                </td></tr>
              ) : sorted.map(b => (
                <tr
                  key={b.id}
                  className={selected?.id === b.id ? 'selected' : ''}
                  onClick={() => setSelected(selected?.id === b.id ? null : b)}
                  onDoubleClick={() => { if (!busyIds.has(b.id)) setEditing(b) }}
                  onContextMenu={e => handleRightClick(e, b)}
                  style={{ cursor: 'pointer' }}
                >
                  <td style={{ fontWeight: 500 }}>{b.name}</td>
                  <td style={{ color: 'var(--text-muted)', maxWidth: 160 }}>
                    <Truncated text={b.targetPath} />
                  </td>
                  <td style={{ color: 'var(--text-muted)', maxWidth: 160 }}>
                    <Truncated text={b.destinationPath} />
                  </td>
                  <td>
                    <ProgressCell progress={progressByConfigId.get(b.id) ?? (pendingRuns[b.id] !== undefined ? 0 : undefined)} />
                  </td>
                  <td style={{ color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                    {b.lastBackupDate ? fmtDate(b.lastBackupDate) : ''}
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      {b.automatic && <CheckCircle2 size={15} style={{ color: 'var(--success)' }} />}
                    </div>
                  </td>
                  <td style={{ color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                    {b.nextBackupDate ? fmtDate(b.nextBackupDate) : ''}
                  </td>
                  <td style={{ color: 'var(--text-muted)' }}>
                    {b.timeIntervalBackup ? fmtInterval(b.timeIntervalBackup) : ''}
                  </td>
                  <td style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                    {b.maxToKeep}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Detail panel */}
      {selected && (
        <DetailPanel
          backup={selected}
          onClose={() => setSelected(null)}
        />
      )}

      {/* Context menu */}
      {ctx && (
        <ContextMenu
          x={ctx.x} y={ctx.y}
          items={buildMenu(ctx.backup)}
          onClose={() => setCtx(null)}
        />
      )}

      {/* Modals */}
      {(creating || editing) && (
        <BackupFormModal
          initial={editing ?? undefined}
          onClose={() => { setCreating(false); setEditing(null) }}
          onSaved={() => { setCreating(false); setEditing(null); qc.invalidateQueries({ queryKey: ['backups'] }) }}
        />
      )}

      {renaming && (
        <RenameModal
          backup={renaming}
          onClose={() => setRenaming(null)}
          onSaved={() => { setRenaming(null); qc.invalidateQueries({ queryKey: ['backups'] }) }}
        />
      )}
    </div>
  )
}

/* ─── Detail panel ───────────────────────────────────────────────────────── */
function DetailPanel({ backup, onClose }: {
  backup: BackupConfig; onClose: () => void
}) {
  const { t } = useTranslation()
  const fields: Array<[string, string]> = [
    [t('ReactUI.DetailName', 'Backup Name'), backup.name],
    [t('ReactUI.DetailSourcePath', 'Source Path'), backup.targetPath],
    [t('ReactUI.DetailDestPath', 'Destination Path'), backup.destinationPath],
    [t('ReactUI.DetailLastBackup', 'Last Backup'), backup.lastBackupDate ? fmtDate(backup.lastBackupDate) : '—'],
    [t('ReactUI.DetailNextBackup', 'Next Backup'), backup.nextBackupDate ? fmtDate(backup.nextBackupDate) : '—'],
    [t('ReactUI.DetailInterval', 'Time Interval'), backup.timeIntervalBackup ? fmtInterval(backup.timeIntervalBackup) : '—'],
    [t('ReactUI.DetailCreationDate', 'Creation Date'), backup.creationDate ? fmtDate(backup.creationDate) : '—'],
    [t('ReactUI.DetailCount', 'Backup Count'), String(backup.count)],
    [t('ReactUI.DetailMaxToKeep', 'Max to Keep'), String(backup.maxToKeep)],
    ...(backup.notes ? [[t('ReactUI.DetailNotes', 'Notes'), backup.notes] as [string, string]] : []),
  ]

  return (
    <div style={{
      marginTop: 8, flexShrink: 0,
      border: '1px solid var(--border)',
      borderRadius: 6,
      background: 'var(--bg-2)',
      padding: '10px 14px',
      position: 'relative',
      minHeight: 70,
    }}>
      <button
        onClick={onClose}
        title={t('General.CloseButton', 'Chiudi')}
        style={{
          position: 'absolute', top: 8, right: 8,
          background: 'none', border: 'none', cursor: 'pointer',
          color: 'var(--text-muted)', display: 'flex', padding: 2,
        }}
      >
        <ChevronUp size={14} />
      </button>
      <p style={{ fontSize: 12, color: 'var(--text)', lineHeight: 1.7, paddingRight: 20 }}>
        {fields.map(([label, value], i) => (
          <span key={label}>
            <strong style={{ color: 'var(--text)' }}>{label}:</strong>{' '}
            <span style={{ color: 'var(--text-muted)' }}>{value}</span>
            {i < fields.length - 1 ? '. ' : '.'}
          </span>
        ))}
      </p>
    </div>
  )
}

/* ─── Rename modal ───────────────────────────────────────────────────────── */
function RenameModal({ backup, onClose, onSaved }: {
  backup: BackupConfig; onClose: () => void; onSaved: () => void
}) {
  const { t } = useTranslation()
  const [name, setName]     = useState(backup.name)
  const [loading, setLoading] = useState(false)
  const [error, setError]   = useState<string | null>(null)

  const save = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    setLoading(true); setError(null)
    try {
      await backupApi.update(backup.id, {
        name: name.trim(),
        targetPath: backup.targetPath,
        destinationPath: backup.destinationPath,
        automatic: backup.automatic,
        timeIntervalBackup: backup.timeIntervalBackup,
        notes: backup.notes ?? '',
        maxToKeep: backup.maxToKeep,
      })
      onSaved()
    } catch {
      setError(t('ReactUI.RenameFailed', 'Rename failed'))
    } finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal-box" style={{ maxWidth: 380 }}>
        <div className="modal-title">{t('ReactUI.RenameModalTitle', 'Rename backup')}</div>
        <form onSubmit={save} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
            <label className="section-label">{t('ReactUI.NameLabel', 'Name')}</label>
            <input className="input" autoFocus value={name} onChange={e => setName(e.target.value)} required />
          </div>
          {error && (
            <div style={{ fontSize: 12, color: 'var(--error)' }}>{error}</div>
          )}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <button type="button" className="btn btn-ghost" onClick={onClose}>{t('General.CancelButton', 'Cancel')}</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving…' : t('ReactUI.RenameButton', 'Rename')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

/* ─── Form modal ─────────────────────────────────────────────────────────── */
function BackupFormModal({ initial, onClose, onSaved }: {
  initial?: BackupConfig; onClose: () => void; onSaved: () => void
}) {
  const { t } = useTranslation()
  const { data: settings } = useQuery({ queryKey: ['settings'], queryFn: settingsApi.get })
  const [form, setForm] = useState<CreateBackupPayload>({
    name: initial?.name ?? '',
    targetPath: initial?.targetPath ?? '',
    destinationPath: initial?.destinationPath ?? '',
    automatic: initial?.automatic ?? false,
    timeIntervalBackup: initial?.timeIntervalBackup ?? null,
    notes: initial?.notes ?? '',
    maxToKeep: initial?.maxToKeep ?? 5,
  })

  // Apply the user's configured defaults once, only for a brand-new backup and only if
  // the field hasn't already been touched (dialogs are freshly mounted each time they open).
  useEffect(() => {
    if (initial || !settings) return
    setForm(f => ({
      ...f,
      destinationPath: f.destinationPath || settings['DEFAULT_DESTINATION_PATH'] || '',
      maxToKeep: settings['DEFAULT_MAX_TO_KEEP'] ? Number(settings['DEFAULT_MAX_TO_KEEP']) : f.maxToKeep,
    }))
  }, [settings]) // eslint-disable-line react-hooks/exhaustive-deps

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const set = <K extends keyof CreateBackupPayload>(k: K, v: CreateBackupPayload[K]) =>
    setForm(f => ({ ...f, [k]: v }))

  const setTi = (field: keyof TimeInterval, val: number) =>
    setForm(f => ({
      ...f,
      timeIntervalBackup: {
        days: f.timeIntervalBackup?.days ?? 0,
        hours: f.timeIntervalBackup?.hours ?? 0,
        minutes: f.timeIntervalBackup?.minutes ?? 0,
        [field]: val,
      }
    }))

  const save = async (e: React.FormEvent) => {
    e.preventDefault()
    if (form.automatic && !hasValidInterval(form.timeIntervalBackup)) {
      setError(t('ReactUI.SetIntervalError', 'Set a time interval before enabling automatic backup.'))
      return
    }
    setLoading(true); setError(null)
    try {
      if (initial) await backupApi.update(initial.id, form)
      else         await backupApi.create(form)
      onSaved()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? (err instanceof Error ? err.message : t('ReactUI.SaveFailed', 'Save failed')))
    } finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal-box">
        <div className="modal-title">{initial ? `${t('ReactUI.EditModalTitlePrefix', 'Edit —')} ${initial.name}` : t('ReactUI.NewBackupModalTitle', 'New backup configuration')}</div>

        <form onSubmit={save} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <FormRow>
            <Field label={t('ReactUI.NameRequiredLabel', 'Name *')} value={form.name} onChange={v => set('name', v)} placeholder="My Documents" required />
          </FormRow>
          <FormRow>
            <PathField label={t('ReactUI.SourcePathRequiredLabel', 'Source path *')} value={form.targetPath} onChange={v => set('targetPath', v)} placeholder="/home/user/documents" required />
          </FormRow>
          <FormRow>
            <PathField label={t('ReactUI.DestPathRequiredLabel', 'Destination path *')} value={form.destinationPath} onChange={v => set('destinationPath', v)} placeholder="/backups/documents" required />
          </FormRow>
          <FormRow cols={2}>
            <Field label={t('ReactUI.NotesLabel', 'Notes')} value={form.notes} onChange={v => set('notes', v)} placeholder="Optional description" />
            <NumField label={t('ReactUI.MaxBackupsToKeepLabel', 'Max backups to keep')} value={form.maxToKeep} onChange={v => set('maxToKeep', v)} min={1} />
          </FormRow>

          <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', userSelect: 'none' }}>
            <input type="checkbox" checked={form.automatic} onChange={e => {
              set('automatic', e.target.checked)
              if (!e.target.checked) set('timeIntervalBackup', null)
            }} style={{ accentColor: 'var(--accent)', width: 15, height: 15 }} />
            <span style={{ fontSize: 13, color: 'var(--text)' }}>{t('ReactUI.EnableAutoBackupLabel', 'Enable automatic backup')}</span>
          </label>

          {form.automatic && (
            <div style={{ background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6, padding: 14 }}>
              <div className="section-label" style={{ marginBottom: 10 }}>{t('ReactUI.TimeIntervalLabel', 'Time interval')}</div>
              <FormRow cols={3}>
                <NumField label={t('ReactUI.DaysLabel', 'Days')}    value={form.timeIntervalBackup?.days ?? 0}    onChange={v => setTi('days', v)}    min={0} />
                <NumField label={t('ReactUI.HoursLabel', 'Hours')}   value={form.timeIntervalBackup?.hours ?? 0}   onChange={v => setTi('hours', v)}   min={0} max={23} />
                <NumField label={t('ReactUI.MinutesLabel', 'Minutes')} value={form.timeIntervalBackup?.minutes ?? 0} onChange={v => setTi('minutes', v)} min={0} max={59} />
              </FormRow>
            </div>
          )}

          {form.automatic && !hasValidInterval(form.timeIntervalBackup) && (
            <div style={{ fontSize: 12, color: 'var(--warning)', background: 'rgba(232,167,53,.1)',
              border: '1px solid rgba(232,167,53,.3)', borderRadius: 5, padding: '7px 10px' }}>
              {t('ReactUI.SetIntervalWarning', 'Set a time interval (days / hours / minutes) before enabling automatic backup.')}
            </div>
          )}
          {form.automatic && hasValidInterval(form.timeIntervalBackup) && intervalTotalMinutes(form.timeIntervalBackup) < 5 && (
            <div style={{ fontSize: 12, color: 'var(--warning)', background: 'rgba(232,167,53,.1)',
              border: '1px solid rgba(232,167,53,.3)', borderRadius: 5, padding: '7px 10px' }}>
              {t('ReactUI.ShortIntervalWarningPart1', 'Warning: interval is very short')} ({intervalTotalMinutes(form.timeIntervalBackup)} min). {t('ReactUI.ShortIntervalWarningPart2', 'A low interval may impact system performance.')}
            </div>
          )}

          {error && (
            <div style={{ fontSize: 12, color: 'var(--error)', background: 'rgba(224,82,82,.1)',
              border: '1px solid rgba(224,82,82,.25)', borderRadius: 5, padding: '7px 10px' }}>
              {error}
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost" onClick={onClose}>{t('General.CancelButton', 'Cancel')}</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving…' : t('General.SaveButton', 'Save')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

/* ─── Shared form atoms ──────────────────────────────────────────────────── */
function FormRow({ children, cols = 1 }: { children: React.ReactNode; cols?: number }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: `repeat(${cols}, 1fr)`, gap: 12 }}>
      {children}
    </div>
  )
}
function Field({ label, value, onChange, placeholder, required }: {
  label: string; value: string; onChange: (v: string) => void; placeholder?: string; required?: boolean
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
      <label className="section-label">{label}</label>
      <input className="input" value={value} placeholder={placeholder} required={required}
        onChange={e => onChange(e.target.value)} />
    </div>
  )
}
function PathField({ label, value, onChange, placeholder, required }: {
  label: string; value: string; onChange: (v: string) => void; placeholder?: string; required?: boolean
}) {
  const { t } = useTranslation()
  const browse = async () => {
    const path = await window.electron?.openFolder()
    if (path) onChange(path)
  }
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
      <label className="section-label">{label}</label>
      <div style={{ display: 'flex', gap: 6 }}>
        <input className="input" value={value} placeholder={placeholder} required={required}
          onChange={e => onChange(e.target.value)} style={{ flex: 1 }} />
        {window.electron && (
          <button type="button" className="btn btn-ghost" onClick={browse}
            style={{ padding: '5px 9px', flexShrink: 0 }} title={t('ReactUI.BrowseFolderTooltip', 'Browse folder')}>
            <FolderOpen size={14} />
          </button>
        )}
      </div>
    </div>
  )
}
function NumField({ label, value, onChange, min, max }: {
  label: string; value: number; onChange: (v: number) => void; min?: number; max?: number
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
      <label className="section-label">{label}</label>
      <input className="input" type="number" value={value} min={min} max={max}
        onChange={e => onChange(Number(e.target.value))} />
    </div>
  )
}
function ProgressCell({ progress }: { progress: number | undefined }) {
  if (progress === undefined) {
    return <span style={{ color: 'var(--text-dim)' }}>—</span>
  }
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div style={{
        flex: 1, height: 6, borderRadius: 3, overflow: 'hidden',
        background: 'var(--bg-3)', border: '1px solid var(--border)',
      }}>
        <div style={{
          width: `${Math.min(100, Math.max(0, progress))}%`, height: '100%',
          background: 'var(--accent)', transition: 'width .3s ease',
        }} />
      </div>
      <span style={{ fontSize: 11, color: 'var(--text-muted)', minWidth: 30, textAlign: 'right' }}>
        {progress}%
      </span>
    </div>
  )
}
function usePrevious<T>(value: T): T | undefined {
  const ref = useRef<T>()
  useEffect(() => { ref.current = value })
  return ref.current
}
function SortableHeader({ label, sortKey, sort, onSort, align, nowrap, tooltip }: {
  label: string; sortKey: SortKey; sort: { key: SortKey; dir: 'asc' | 'desc' }; onSort: (key: SortKey) => void
  align?: 'center' | 'left'; nowrap?: boolean; tooltip?: string
}) {
  const active = sort.key === sortKey
  return (
    <th
      onClick={() => onSort(sortKey)}
      style={{ cursor: 'pointer', userSelect: 'none', textAlign: align, whiteSpace: nowrap ? 'nowrap' : undefined }}
      title={tooltip ?? label}
    >
      <span style={{
        display: 'inline-flex', alignItems: 'center', gap: 4,
        justifyContent: align === 'center' ? 'center' : 'flex-start',
        color: active ? 'var(--text)' : undefined,
      }}>
        {label}
        {active
          ? (sort.dir === 'asc' ? <ArrowUp size={11} /> : <ArrowDown size={11} />)
          : <ChevronsUpDown size={11} color="var(--text-dim)" />}
      </span>
    </th>
  )
}
function Truncated({ text }: { text: string }) {
  return (
    <span title={text} style={{ display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: 160 }}>
      {text}
    </span>
  )
}

/* ─── Formatters ─────────────────────────────────────────────────────────── */
function fmtDate(iso: string) { return new Date(iso).toLocaleString() }
function fmtInterval(t: { days: number; hours: number; minutes: number }) {
  return `${t.days}.${t.hours}:${String(t.minutes).padStart(2, '0')}`
}

/* ─── Helpers ────────────────────────────────────────────────────────────── */
function hasValidInterval(ti: { days: number; hours: number; minutes: number } | null | undefined): boolean {
  if (!ti) return false
  return ti.days > 0 || ti.hours > 0 || ti.minutes > 0
}

function intervalTotalMinutes(ti: { days: number; hours: number; minutes: number } | null | undefined): number {
  if (!ti) return 0
  return ti.days * 1440 + ti.hours * 60 + ti.minutes
}
