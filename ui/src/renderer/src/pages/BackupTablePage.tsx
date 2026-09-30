import { useState, useMemo, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Plus, Play, Pencil, Search, X, ArrowUp, ArrowDown, ChevronsUpDown, Square,
  MoreHorizontal, Download, ArrowRight, CalendarClock, Hand,
} from 'lucide-react'
import { backupApi, exportApi, historyApi, settingsApi, downloadBlob } from '../services/api'
import type { BackupConfig, BackupRequest, CreateBackupPayload, TimeInterval } from '../types'
import { ContextMenu, type MenuItem } from '../components/ContextMenu'
import { Alert, ConfirmDialog, Modal, NumField, PageHeader, PathField, RunStatusBadge, Switch, TextField } from '../components/ui'
import { useTranslation } from '../context/TranslationContext'
import { useBackupRuns } from '../context/BackupRunsContext'
import { useToast } from '../context/ToastContext'
import { useConfig } from '../context/ConfigContext'
import {
  apiErrorMessage, fmtBytes, fmtDateTime, fmtDuration, fmtInterval, fmtRelative,
  hasValidInterval, intervalTotalMinutes,
} from '../utils/format'

type SortKey = 'name' | 'paths' | 'status' | 'schedule' | 'maxToKeep'

function compareBackups(a: BackupConfig, b: BackupConfig, key: SortKey): number {
  switch (key) {
    case 'name':      return a.name.localeCompare(b.name)
    case 'paths':     return a.targetPath.localeCompare(b.targetPath)
    case 'status':    return (a.lastBackupDate ?? '').localeCompare(b.lastBackupDate ?? '')
    // automatic backups first, ordered by next run; manual ones last
    case 'schedule':  return a.automatic !== b.automatic
      ? Number(b.automatic) - Number(a.automatic)
      : (a.nextBackupDate ?? '').localeCompare(b.nextBackupDate ?? '')
    case 'maxToKeep': return a.maxToKeep - b.maxToKeep
  }
}

const toPayload = (b: BackupConfig, patch: Partial<CreateBackupPayload> = {}): CreateBackupPayload => ({
  name: b.name,
  targetPath: b.targetPath,
  destinationPath: b.destinationPath,
  automatic: b.automatic,
  timeIntervalBackup: b.timeIntervalBackup,
  notes: b.notes ?? '',
  maxToKeep: b.maxToKeep,
  ...patch,
})

interface MenuState { x: number; y: number; backup: BackupConfig }

export default function BackupTablePage() {
  const qc = useQueryClient()
  const { t, language } = useTranslation()
  const { toast } = useToast()
  const cfg = useConfig()
  const { backups, backupsLoading, progressById, busyIds, trackRun } = useBackupRuns()

  const [search, setSearch]         = useState('')
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [editing, setEditing]       = useState<BackupConfig | null>(null)
  const [creating, setCreating]     = useState(false)
  const [renaming, setRenaming]     = useState<BackupConfig | null>(null)
  const [deleting, setDeleting]     = useState<BackupConfig | null>(null)
  const [menu, setMenu]             = useState<MenuState | null>(null)
  const [sort, setSort] = useState<{ key: SortKey; dir: 'asc' | 'desc' }>({ key: 'name', dir: 'asc' })

  const selected = backups.find(b => b.id === selectedId) ?? null

  // Full history powers the "last result" badge of every row and the detail panel
  const { data: history = [] } = useQuery({
    queryKey: ['history'],
    queryFn: historyApi.getAll,
    refetchInterval: 15_000,
  })
  const runsByConfig = useMemo(() => {
    const map = new Map<number, BackupRequest[]>()
    for (const r of history) {
      const list = map.get(r.backupConfigurationId) ?? []
      list.push(r)
      map.set(r.backupConfigurationId, list)
    }
    for (const list of map.values()) {
      list.sort((a, b) => new Date(b.startedDate).getTime() - new Date(a.startedDate).getTime())
    }
    return map
  }, [history])

  const sorted = useMemo(() => {
    const q = search.trim().toLowerCase()
    const filtered = q
      ? backups.filter(b =>
          b.name.toLowerCase().includes(q) ||
          b.targetPath.toLowerCase().includes(q) ||
          b.destinationPath.toLowerCase().includes(q) ||
          (b.notes ?? '').toLowerCase().includes(q))
      : backups
    const factor = sort.dir === 'asc' ? 1 : -1
    return [...filtered].sort((a, b) => factor * compareBackups(a, b, sort.key))
  }, [backups, search, sort])

  const toggleSort = (key: SortKey) =>
    setSort(prev => prev.key === key ? { key, dir: prev.dir === 'asc' ? 'desc' : 'asc' } : { key, dir: 'asc' })

  const invalidateBackups = () => qc.invalidateQueries({ queryKey: ['backups'] })

  const runMutation = useMutation({
    mutationFn: (id: number) => backupApi.run(id),
    onSuccess: (_d, id) => trackRun(id),
    onError: err => toast(apiErrorMessage(err) ?? t('ReactUI.FailedToStartBackup', 'Failed to start backup'), 'error'),
  })
  const interruptMutation = useMutation({
    mutationFn: (id: number) => backupApi.interrupt(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['backups-running'] })
      invalidateBackups()
    },
    onError: err => toast(apiErrorMessage(err) ?? t('ReactUI.FailedToInterruptBackup', 'Failed to interrupt backup'), 'error'),
  })
  const deleteMutation = useMutation({
    mutationFn: (id: number) => backupApi.delete(id),
    onSuccess: (_d, id) => {
      if (selectedId === id) setSelectedId(null)
      invalidateBackups()
    },
    onError: err => toast(apiErrorMessage(err) ?? t('ReactUI.DeleteFailed', 'Delete failed'), 'error'),
  })
  const duplicateMutation = useMutation({
    mutationFn: (b: BackupConfig) => backupApi.create(toPayload(b, { name: `${b.name} (copy)`, automatic: false })),
    onSuccess: created => { invalidateBackups(); setSelectedId(created.id) },
    onError: err => toast(apiErrorMessage(err) ?? t('ReactUI.SaveFailed', 'Save failed'), 'error'),
  })
  const toggleAutoMutation = useMutation({
    mutationFn: (b: BackupConfig) => backupApi.update(b.id, toPayload(b, { automatic: !b.automatic })),
    onSuccess: invalidateBackups,
    onError: err => toast(apiErrorMessage(err) ?? t('ReactUI.SaveFailed', 'Save failed'), 'error'),
  })

  const exportCsv = async () => {
    try {
      downloadBlob(await exportApi.backupsCsv(), 'backups.csv')
    } catch {
      toast(t('ReactUI.ExportFailed', 'Export failed'), 'error')
    }
  }

  const openPath = async (path: string) => {
    const err = await window.electron?.openPath(path)
    if (err) toast(err, 'error')
  }

  const toggleAuto = (b: BackupConfig) => {
    // Enabling needs an interval first — open the editor so the user can set one
    if (!b.automatic && !hasValidInterval(b.timeIntervalBackup)) setEditing(b)
    else toggleAutoMutation.mutate(b)
  }

  const buildMenu = (b: BackupConfig): MenuItem[] => {
    const busy = busyIds.has(b.id)
    return [
      busy
        ? { label: t('ReactUI.MenuInterruptBackup', 'Interrupt backup process'), onClick: () => interruptMutation.mutate(b.id), danger: true }
        : { label: t('ReactUI.MenuRunSingleBackup', 'Run single backup'), onClick: () => runMutation.mutate(b.id) },
      { label: t('ReactUI.MenuAutoBackup', 'Auto backup'), checked: b.automatic, onClick: () => toggleAuto(b) },
      { type: 'separator' },
      { label: t('General.EditButton', 'Edit'), onClick: () => setEditing(b), disabled: busy },
      { label: t('ReactUI.MenuRename', 'Rename'), onClick: () => setRenaming(b) },
      { label: t('ReactUI.MenuDuplicate', 'Duplicate'), onClick: () => duplicateMutation.mutate(b) },
      { type: 'separator' },
      { label: t('ReactUI.MenuOpenSourcePath', 'Open source path'), onClick: () => openPath(b.targetPath), disabled: !window.electron },
      { label: t('ReactUI.MenuOpenDestPath', 'Open destination path'), onClick: () => openPath(b.destinationPath), disabled: !window.electron },
      {
        label: t('ReactUI.MenuCopyText', 'Copy text'),
        submenu: [
          { label: t('ReactUI.MenuCopyName', 'Copy backup name'), onClick: () => navigator.clipboard.writeText(b.name) },
          { label: t('ReactUI.MenuCopySourcePath', 'Copy source path'), onClick: () => navigator.clipboard.writeText(b.targetPath) },
          { label: t('ReactUI.MenuCopyDestPath', 'Copy destination path'), onClick: () => navigator.clipboard.writeText(b.destinationPath) },
        ],
      },
      { type: 'separator' },
      { label: t('General.DeleteButton', 'Delete'), onClick: () => setDeleting(b), disabled: busy, danger: true },
    ]
  }

  const modalOpen = creating || !!editing || !!renaming || !!deleting

  // Keyboard shortcuts on the selected row: Enter = edit, Delete = delete, Esc = deselect
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (modalOpen || menu) return
      const tag = (e.target as HTMLElement).tagName
      if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return
      if (!selected) return
      if (e.key === 'Escape') setSelectedId(null)
      else if (e.key === 'Enter' && !busyIds.has(selected.id)) setEditing(selected)
      else if (e.key === 'Delete' && !busyIds.has(selected.id)) setDeleting(selected)
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [selected, busyIds, modalOpen, menu])

  const colSpan = 6

  return (
    <div className="page page-fill">
      <PageHeader
        title={t('ReactUI.BackupListTitle', 'Backup List')}
        desc={t('ReactUI.BackupListDesc', 'Manage and monitor backup configurations, including creation, editing, scheduling and execution.')}
        actions={
          <>
            {cfg.menuItems.Export !== false && (
              <button className="btn btn-ghost" onClick={exportCsv} disabled={backups.length === 0}>
                <Download size={13} /> {t('ReactUI.ExportCsv', 'Export CSV')}
              </button>
            )}
            <button className="btn btn-primary" onClick={() => setCreating(true)}>
              <Plus size={14} /> {t('ReactUI.NewBackupButton', 'New backup')}
            </button>
          </>
        }
      />

      <div className="toolbar">
        <div className="search-box" style={{ width: 300 }}>
          <Search size={13} color="var(--text-dim)" />
          <input
            value={search} onChange={e => setSearch(e.target.value)}
            placeholder={t('General.QuickSearch', 'Quick search')}
          />
          {search && (
            <button className="icon-btn" style={{ padding: 2 }} onClick={() => setSearch('')}><X size={13} /></button>
          )}
        </div>
        <span className="text-dim" style={{ fontSize: 12 }}>
          {search
            ? t('ReactUI.FilteredCount', '{shown} of {total}').replace('{shown}', String(sorted.length)).replace('{total}', String(backups.length))
            : t('ReactUI.ConfigCount', '{count} configurations').replace('{count}', String(backups.length))}
        </span>
      </div>

      <div className="card" style={{ overflow: 'hidden', flex: 1, minHeight: 160 }}>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <SortableHeader label={t('ReactUI.ColName', 'Backup Name')} sortKey="name" sort={sort} onSort={toggleSort} />
                <SortableHeader label={t('ReactUI.ColPaths', 'Source → Destination')} sortKey="paths" sort={sort} onSort={toggleSort} />
                <SortableHeader label={t('ReactUI.ColStatus', 'Status')} sortKey="status" sort={sort} onSort={toggleSort} width={170} />
                <SortableHeader label={t('ReactUI.ColSchedule', 'Schedule')} sortKey="schedule" sort={sort} onSort={toggleSort} width={210} />
                <SortableHeader label={t('ReactUI.ColMaxToKeep', 'Max to Keep')} sortKey="maxToKeep" sort={sort} onSort={toggleSort} align="center" width={90} />
                <th style={{ width: 100 }} />
              </tr>
            </thead>
            <tbody>
              {backupsLoading ? (
                <tr><td colSpan={colSpan} className="centered-message">{t('ReactUI.LoadingText', 'Loading…')}</td></tr>
              ) : sorted.length === 0 ? (
                <tr><td colSpan={colSpan}>
                  {search
                    ? <div className="centered-message">{t('ReactUI.NoResults', 'No results.')}</div>
                    : <EmptyState onCreate={() => setCreating(true)} />}
                </td></tr>
              ) : sorted.map(b => {
                const busy = busyIds.has(b.id)
                return (
                  <tr
                    key={b.id}
                    className={selectedId === b.id ? 'selected' : ''}
                    onClick={() => setSelectedId(selectedId === b.id ? null : b.id)}
                    onDoubleClick={() => { if (!busy) setEditing(b) }}
                    onContextMenu={e => { e.preventDefault(); setMenu({ x: e.clientX, y: e.clientY, backup: b }) }}
                    style={{ cursor: 'pointer' }}
                  >
                    <td style={{ maxWidth: 220 }}>
                      <div className="truncate" style={{ fontWeight: 600 }} title={b.name}>{b.name}</div>
                      {b.notes && <div className="cell-sub truncate" title={b.notes}>{b.notes}</div>}
                    </td>
                    <td style={{ maxWidth: 360 }}>
                      <div className="truncate" title={b.targetPath}>{b.targetPath}</div>
                      <div className="cell-sub truncate" title={b.destinationPath} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <ArrowRight size={10} style={{ flexShrink: 0 }} />
                        <span className="truncate">{b.destinationPath}</span>
                      </div>
                    </td>
                    <td>
                      <StatusCell
                        busy={busy}
                        progress={progressById.get(b.id)}
                        lastRun={runsByConfig.get(b.id)?.[0]}
                        lastBackupDate={b.lastBackupDate}
                        language={language}
                      />
                    </td>
                    <td>
                      <ScheduleCell backup={b} language={language} />
                    </td>
                    <td style={{ textAlign: 'center' }} className="text-muted">{b.maxToKeep}</td>
                    <td onClick={e => e.stopPropagation()} onDoubleClick={e => e.stopPropagation()}>
                      <div className="row-actions">
                        {busy ? (
                          <button className="icon-btn danger" title={t('ReactUI.StopButton', 'Stop')}
                            onClick={() => interruptMutation.mutate(b.id)}>
                            <Square size={14} />
                          </button>
                        ) : (
                          <button className="icon-btn success" title={t('ReactUI.RunBackupNow', 'Run backup now')}
                            onClick={() => runMutation.mutate(b.id)}>
                            <Play size={15} />
                          </button>
                        )}
                        <button className="icon-btn" title={t('General.EditButton', 'Edit')} disabled={busy}
                          onClick={() => setEditing(b)}>
                          <Pencil size={14} />
                        </button>
                        <button className="icon-btn" title={t('ReactUI.MoreActions', 'More actions')}
                          onClick={e => {
                            const r = e.currentTarget.getBoundingClientRect()
                            setMenu({ x: r.right - 190, y: r.bottom + 4, backup: b })
                          }}>
                          <MoreHorizontal size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>

      {selected && (
        <DetailPanel
          backup={selected}
          runs={runsByConfig.get(selected.id) ?? []}
          onClose={() => setSelectedId(null)}
        />
      )}

      {menu && (
        <ContextMenu x={menu.x} y={menu.y} items={buildMenu(menu.backup)} onClose={() => setMenu(null)} />
      )}

      {(creating || editing) && (
        <BackupFormModal
          initial={editing ?? undefined}
          onClose={() => { setCreating(false); setEditing(null) }}
          onSaved={saved => {
            setCreating(false); setEditing(null)
            setSelectedId(saved.id)
            invalidateBackups()
          }}
        />
      )}

      {renaming && (
        <RenameModal
          backup={renaming}
          onClose={() => setRenaming(null)}
          onSaved={() => { setRenaming(null); invalidateBackups() }}
        />
      )}

      {deleting && (
        <ConfirmDialog
          title={t('ReactUI.DeleteModalTitle', 'Delete backup configuration')}
          message={t('ReactUI.DeleteConfirm', 'Delete "{name}"?').replace('{name}', deleting.name)
            + ' ' + t('ReactUI.DeleteConfirmNote', 'Backup files already created are not removed.')}
          confirmLabel={t('General.DeleteButton', 'Delete')}
          danger
          onConfirm={() => deleteMutation.mutate(deleting.id)}
          onClose={() => setDeleting(null)}
        />
      )}
    </div>
  )
}

/* ─── Cells ──────────────────────────────────────────────────────────────── */

function StatusCell({ busy, progress, lastRun, lastBackupDate, language }: {
  busy: boolean; progress: number | undefined; lastRun: BackupRequest | undefined
  lastBackupDate: string | null; language: string
}) {
  const { t } = useTranslation()
  if (busy) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div className={`progress${progress === undefined ? ' indeterminate' : ''}`}>
          <div style={{ width: `${Math.min(100, Math.max(0, progress ?? 0))}%` }} />
        </div>
        <span className="text-muted" style={{ fontSize: 11, minWidth: 30, textAlign: 'right' }}>
          {progress !== undefined ? `${progress}%` : '…'}
        </span>
      </div>
    )
  }
  const when = lastRun?.completionDate ?? lastRun?.startedDate ?? lastBackupDate
  if (!when) return <span className="badge badge-muted">{t('ReactUI.StatusNeverRun', 'Never run')}</span>
  const failed = lastRun?.status === 'TERMINATED'
  return (
    <div>
      <span className={`badge ${failed ? 'badge-error' : 'badge-success'}`}>
        {failed ? t('ReactUI.StatusFailed', 'Failed') : t('ReactUI.StatusCompleted', 'Completed')}
      </span>
      <div className="cell-sub" title={fmtDateTime(when)}>{fmtRelative(when, language)}</div>
    </div>
  )
}

function ScheduleCell({ backup, language }: { backup: BackupConfig; language: string }) {
  const { t } = useTranslation()
  if (!backup.automatic) {
    return (
      <span className="text-dim" style={{ display: 'inline-flex', alignItems: 'center', gap: 5, fontSize: 12 }}>
        <Hand size={12} /> {t('ReactUI.ScheduleManual', 'Manual')}
      </span>
    )
  }
  return (
    <div>
      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, fontSize: 12, color: 'var(--accent)', fontWeight: 500, whiteSpace: 'nowrap' }}>
        <CalendarClock size={12} />
        {t('ReactUI.ScheduleEvery', 'Every {interval}').replace('{interval}', fmtInterval(backup.timeIntervalBackup))}
      </span>
      {backup.nextBackupDate && (
        <div className="cell-sub" title={fmtDateTime(backup.nextBackupDate)}>
          {t('ReactUI.NextRunPrefix', 'Next')}: {fmtRelative(backup.nextBackupDate, language)}
        </div>
      )}
    </div>
  )
}

function EmptyState({ onCreate }: { onCreate: () => void }) {
  const { t } = useTranslation()
  return (
    <div className="centered-message" style={{ flexDirection: 'column', gap: 12, padding: '56px 16px' }}>
      <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)' }}>{t('ReactUI.NoBackupConfigs', 'No backup configurations.')}</div>
      <div style={{ maxWidth: 380 }}>{t('ReactUI.EmptyStateHint', 'Create your first configuration: choose a folder to protect and where to save its backups.')}</div>
      <button className="btn btn-primary" onClick={onCreate}><Plus size={14} /> {t('ReactUI.NewBackupButton', 'New backup')}</button>
    </div>
  )
}

function SortableHeader({ label, sortKey, sort, onSort, align, width }: {
  label: string; sortKey: SortKey; sort: { key: SortKey; dir: 'asc' | 'desc' }
  onSort: (key: SortKey) => void; align?: 'center'; width?: number
}) {
  const active = sort.key === sortKey
  return (
    <th className="sortable" onClick={() => onSort(sortKey)} style={{ textAlign: align, width }}
      aria-sort={active ? (sort.dir === 'asc' ? 'ascending' : 'descending') : undefined}>
      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: active ? 'var(--text)' : undefined }}>
        {label}
        {active
          ? (sort.dir === 'asc' ? <ArrowUp size={11} /> : <ArrowDown size={11} />)
          : <ChevronsUpDown size={11} color="var(--text-dim)" />}
      </span>
    </th>
  )
}

/* ─── Detail panel ───────────────────────────────────────────────────────── */

function DetailPanel({ backup, runs, onClose }: { backup: BackupConfig; runs: BackupRequest[]; onClose: () => void }) {
  const { t } = useTranslation()
  const fields: Array<[string, string]> = [
    [t('ReactUI.DetailSourcePath', 'Source Path'), backup.targetPath],
    [t('ReactUI.DetailDestPath', 'Destination Path'), backup.destinationPath],
    [t('ReactUI.DetailLastBackup', 'Last Backup'), fmtDateTime(backup.lastBackupDate) || '—'],
    [t('ReactUI.DetailNextBackup', 'Next Backup'), backup.automatic ? (fmtDateTime(backup.nextBackupDate) || '—') : '—'],
    [t('ReactUI.DetailInterval', 'Time Interval'), backup.timeIntervalBackup && hasValidInterval(backup.timeIntervalBackup) ? fmtInterval(backup.timeIntervalBackup) : '—'],
    [t('ReactUI.DetailCount', 'Backup Count'), String(backup.count)],
    [t('ReactUI.DetailMaxToKeep', 'Max to Keep'), String(backup.maxToKeep)],
    [t('ReactUI.DetailCreationDate', 'Creation Date'), fmtDateTime(backup.creationDate) || '—'],
  ]

  return (
    <div className="card" style={{ flexShrink: 0, padding: '14px 18px', display: 'flex', flexDirection: 'column', gap: 14, maxHeight: '42%', overflow: 'auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <div style={{ fontWeight: 700, fontSize: 14 }} className="truncate">{backup.name}</div>
        <button className="icon-btn" onClick={onClose} title={t('General.CloseButton', 'Close')}><X size={14} /></button>
      </div>

      <dl className="kv-grid">
        {fields.map(([label, value]) => (
          <div key={label}><dt>{label}</dt><dd>{value}</dd></div>
        ))}
      </dl>

      {backup.notes && (
        <div>
          <div className="text-dim" style={{ fontSize: 11, marginBottom: 2 }}>{t('ReactUI.DetailNotes', 'Notes')}</div>
          <div style={{ fontSize: 13, whiteSpace: 'pre-wrap' }}>{backup.notes}</div>
        </div>
      )}

      <div>
        <div className="section-label" style={{ marginBottom: 8 }}>{t('ReactUI.RecentRuns', 'Recent runs')}</div>
        {runs.length === 0 ? (
          <div className="text-dim" style={{ fontSize: 12 }}>{t('ReactUI.NoRunsYet', 'No runs yet.')}</div>
        ) : (
          <table className="data-table" style={{ fontSize: 12 }}>
            <tbody>
              {runs.slice(0, 5).map(r => (
                <tr key={r.backupRequestId}>
                  <td style={{ padding: '6px 8px', width: 110 }}><RunStatusBadge status={r.status} /></td>
                  <td style={{ padding: '6px 8px' }} className="text-muted">{fmtDateTime(r.startedDate)}</td>
                  <td style={{ padding: '6px 8px' }} className="text-muted">{fmtDuration(r.durationMs)}</td>
                  <td style={{ padding: '6px 8px' }} className="text-muted">{r.zippedTargetSize ? fmtBytes(r.zippedTargetSize) : ''}</td>
                  <td style={{ padding: '6px 8px', color: 'var(--error)', maxWidth: 260 }} className="truncate" title={r.errorMessage ?? ''}>{r.errorMessage ?? ''}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

/* ─── Rename modal ───────────────────────────────────────────────────────── */

function RenameModal({ backup, onClose, onSaved }: { backup: BackupConfig; onClose: () => void; onSaved: () => void }) {
  const { t } = useTranslation()
  const [name, setName] = useState(backup.name)
  const mutation = useMutation({
    mutationFn: () => backupApi.update(backup.id, toPayload(backup, { name: name.trim() })),
    onSuccess: onSaved,
  })

  return (
    <Modal
      title={t('ReactUI.RenameModalTitle', 'Rename backup')}
      onClose={onClose}
      width={400}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose}>{t('General.CancelButton', 'Cancel')}</button>
          <button type="submit" form="rename-form" className="btn btn-primary" disabled={mutation.isPending || !name.trim()}>
            {t('ReactUI.RenameButton', 'Rename')}
          </button>
        </>
      }
    >
      <form id="rename-form" onSubmit={e => { e.preventDefault(); if (name.trim()) mutation.mutate() }}>
        <TextField label={t('ReactUI.NameLabel', 'Name')} value={name} onChange={setName} autoFocus required />
      </form>
      {mutation.isError && <Alert kind="error">{apiErrorMessage(mutation.error) ?? t('ReactUI.RenameFailed', 'Rename failed')}</Alert>}
    </Modal>
  )
}

/* ─── Create / edit modal ────────────────────────────────────────────────── */

function BackupFormModal({ initial, onClose, onSaved }: {
  initial?: BackupConfig; onClose: () => void; onSaved: (saved: BackupConfig) => void
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

  // Apply the user's configured defaults once, only for a brand-new backup
  useEffect(() => {
    if (initial || !settings) return
    setForm(f => ({
      ...f,
      destinationPath: f.destinationPath || settings['DEFAULT_DESTINATION_PATH'] || '',
      maxToKeep: settings['DEFAULT_MAX_TO_KEEP'] ? Number(settings['DEFAULT_MAX_TO_KEEP']) : f.maxToKeep,
    }))
  }, [settings]) // eslint-disable-line react-hooks/exhaustive-deps

  const [error, setError] = useState<string | null>(null)
  const set = <K extends keyof CreateBackupPayload>(k: K, v: CreateBackupPayload[K]) => setForm(f => ({ ...f, [k]: v }))
  const setTi = (field: keyof TimeInterval, val: number) =>
    setForm(f => ({
      ...f,
      timeIntervalBackup: {
        days: f.timeIntervalBackup?.days ?? 0,
        hours: f.timeIntervalBackup?.hours ?? 0,
        minutes: f.timeIntervalBackup?.minutes ?? 0,
        [field]: Math.max(0, val),
      },
    }))

  const mutation = useMutation({
    mutationFn: () => {
      // Keep the interval around even when disabling automatic runs, so re-enabling restores it
      const payload = { ...form, name: form.name.trim() }
      return initial ? backupApi.update(initial.id, payload) : backupApi.create(payload)
    },
    onSuccess: onSaved,
    onError: err => setError(apiErrorMessage(err) ?? (err instanceof Error ? err.message : t('ReactUI.SaveFailed', 'Save failed'))),
  })

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    if (form.automatic && !hasValidInterval(form.timeIntervalBackup)) {
      setError(t('ReactUI.SetIntervalError', 'Set a time interval before enabling automatic backup.'))
      return
    }
    setError(null)
    mutation.mutate()
  }

  const totalMin = intervalTotalMinutes(form.timeIntervalBackup)

  return (
    <Modal
      title={initial ? `${t('ReactUI.EditModalTitlePrefix', 'Edit —')} ${initial.name}` : t('ReactUI.NewBackupModalTitle', 'New backup configuration')}
      onClose={onClose}
      width={560}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose}>{t('General.CancelButton', 'Cancel')}</button>
          <button type="submit" form="backup-form" className="btn btn-primary" disabled={mutation.isPending}>
            {t('General.SaveButton', 'Save')}
          </button>
        </>
      }
    >
      <form id="backup-form" onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <TextField label={t('ReactUI.NameRequiredLabel', 'Name *')} value={form.name} onChange={v => set('name', v)}
          placeholder="My Documents" required autoFocus={!initial} />
        <PathField label={t('ReactUI.SourcePathRequiredLabel', 'Source path *')} value={form.targetPath}
          onChange={v => set('targetPath', v)} placeholder="C:\Users\me\Documents" required />
        <PathField label={t('ReactUI.DestPathRequiredLabel', 'Destination path *')} value={form.destinationPath}
          onChange={v => set('destinationPath', v)} placeholder="D:\Backups" required />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 160px', gap: 12 }}>
          <div className="field">
            <label className="field-label" htmlFor="backup-notes">{t('ReactUI.NotesLabel', 'Notes')}</label>
            <textarea id="backup-notes" className="input" rows={2} value={form.notes}
              onChange={e => set('notes', e.target.value)} />
          </div>
          <NumField label={t('ReactUI.MaxBackupsToKeepLabel', 'Max backups to keep')} value={form.maxToKeep}
            onChange={v => set('maxToKeep', Math.max(1, v))} min={1} />
        </div>

        <div style={{ background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 8, padding: '10px 14px', display: 'flex', flexDirection: 'column', gap: 10 }}>
          <Switch
            label={t('ReactUI.EnableAutoBackupLabel', 'Enable automatic backup')}
            checked={form.automatic}
            onChange={v => set('automatic', v)}
          />
          {form.automatic && (
            <>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 }}>
                <NumField label={t('ReactUI.DaysLabel', 'Days')} value={form.timeIntervalBackup?.days ?? 0} onChange={v => setTi('days', v)} min={0} />
                <NumField label={t('ReactUI.HoursLabel', 'Hours')} value={form.timeIntervalBackup?.hours ?? 0} onChange={v => setTi('hours', v)} min={0} max={23} />
                <NumField label={t('ReactUI.MinutesLabel', 'Minutes')} value={form.timeIntervalBackup?.minutes ?? 0} onChange={v => setTi('minutes', v)} min={0} max={59} />
              </div>
              {!hasValidInterval(form.timeIntervalBackup) ? (
                <Alert kind="warning">{t('ReactUI.SetIntervalWarning', 'Set a time interval (days / hours / minutes) before enabling automatic backup.')}</Alert>
              ) : totalMin < 5 ? (
                <Alert kind="warning">
                  {t('ReactUI.ShortIntervalWarningPart1', 'Warning: interval is very short')} ({totalMin} min). {t('ReactUI.ShortIntervalWarningPart2', 'A low interval may impact system performance.')}
                </Alert>
              ) : (
                <div className="field-hint">
                  {t('ReactUI.ScheduleEvery', 'Every {interval}').replace('{interval}', fmtInterval(form.timeIntervalBackup))}
                </div>
              )}
            </>
          )}
        </div>

        {error && <Alert kind="error">{error}</Alert>}
      </form>
    </Modal>
  )
}
