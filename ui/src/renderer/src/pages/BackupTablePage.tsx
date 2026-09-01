import { useState, useMemo, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Play, Pencil, Trash2, Search, X, ChevronUp, FolderOpen } from 'lucide-react'
import { backupApi } from '../services/api'
import type { BackupConfig, CreateBackupPayload, TimeInterval } from '../types'
import { ContextMenu } from '../components/ContextMenu'

declare global {
  interface Window {
    electron?: {
      openFolder: () => Promise<string | null>
      openPath:   (path: string) => Promise<string>
    }
  }
}

interface CtxState { x: number; y: number; backup: BackupConfig }

export default function BackupTablePage() {
  const qc = useQueryClient()
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

  const [runError, setRunError] = useState<string | null>(null)
  const runMutation = useMutation({
    mutationFn: (id: number) => backupApi.run(id),
    onSuccess: () => { setRunError(null); qc.invalidateQueries({ queryKey: ['backups'] }) },
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setRunError(msg ?? 'Failed to start backup')
      setTimeout(() => setRunError(null), 5000)
    }
  })
  const deleteMutation = useMutation({
    mutationFn: (id: number) => backupApi.delete(id),
    onSuccess: () => { setSelected(null); qc.invalidateQueries({ queryKey: ['backups'] }) }
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
    { label: 'Edit',      onClick: () => setEditing(b) },
    { label: 'Delete',    onClick: () => { if (confirm(`Delete "${b.name}"?`)) deleteMutation.mutate(b.id) } },
    { label: 'Duplicate', onClick: () => duplicateMutation.mutate(b) },
    { label: 'Rename',    onClick: () => setRenaming(b) },
    { type: 'separator' as const },
    { label: 'Open source path',      onClick: () => window.electron?.openPath(b.targetPath),      disabled: !window.electron },
    { label: 'Open destination path', onClick: () => window.electron?.openPath(b.destinationPath), disabled: !window.electron },
    { type: 'separator' as const },
    {
      label: 'Backup',
      submenu: [
        { label: 'Run single backup', onClick: () => runMutation.mutate(b.id) },
        {
          label: 'Auto backup',
          checked: b.automatic,
          onClick: () => {
            if (!b.automatic && !hasValidInterval(b.timeIntervalBackup)) {
              setEditing(b)   // force user to set an interval first
            } else {
              toggleAutoMutation.mutate(b)
            }
          }
        },
        { label: 'Interrupt backup process', disabled: true },
      ]
    },
    { type: 'separator' as const },
    {
      label: 'Copy text',
      submenu: [
        { label: 'Copy backup name',      onClick: () => navigator.clipboard.writeText(b.name) },
        { label: 'Copy source path',      onClick: () => navigator.clipboard.writeText(b.targetPath) },
        { label: 'Copy destination path', onClick: () => navigator.clipboard.writeText(b.destinationPath) },
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
          <div className="page-title">Elenco backup</div>
          <div className="page-desc">Gestisci e monitora le configurazioni di backup, inclusa la creazione, modifica, pianificazione ed esecuzione.</div>
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
            placeholder="Cerca…"
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
          <Plus size={13} /> Crea
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected}
          onClick={() => selected && setEditing(selected)}
        >
          <Pencil size={13} /> Modifica
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected}
          style={selected ? { color: 'var(--error)' } : {}}
          onClick={() => {
            if (selected && confirm(`Eliminare "${selected.name}"?`))
              deleteMutation.mutate(selected.id)
          }}
        >
          <Trash2 size={13} /> Elimina
        </button>
        <button
          className="btn btn-ghost"
          disabled={!selected}
          style={selected ? { color: 'var(--success)' } : {}}
          onClick={() => selected && runMutation.mutate(selected.id)}
          title="Avvia backup ora"
        >
          <Play size={13} /> Avvia
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

      {/* Table */}
      <div className="card" style={{ overflow: 'hidden', flex: 1, minHeight: 0 }}>
        <div style={{ overflowX: 'auto', height: '100%' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome del Backup</th>
                <th>Percorso Iniziale</th>
                <th>Percorso di Destinazione</th>
                <th>Ultimo Backup</th>
                <th style={{ textAlign: 'center' }}>Backup Automatico</th>
                <th>Data del Prossimo</th>
                <th>Intervallo (gg.HH:mm)</th>
                <th style={{ textAlign: 'center' }}>Numero massimo</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={8} style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>Caricamento…</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={8} style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>
                  {search ? 'Nessun risultato.' : 'Nessuna configurazione di backup.'}
                </td></tr>
              ) : filtered.map(b => (
                <tr
                  key={b.id}
                  className={selected?.id === b.id ? 'selected' : ''}
                  onClick={() => setSelected(selected?.id === b.id ? null : b)}
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
                  <td style={{ color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                    {b.lastBackupDate ? fmtDate(b.lastBackupDate) : ''}
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <input
                      type="checkbox"
                      readOnly
                      checked={b.automatic}
                      style={{ accentColor: 'var(--accent)', width: 14, height: 14, cursor: 'default', pointerEvents: 'none' }}
                    />
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
  const fields: Array<[string, string]> = [
    ['NomeBackup', backup.name],
    ['PercorsoIniziale', backup.targetPath],
    ['PercorsoDestinazione', backup.destinationPath],
    ['UltimoBackup', backup.lastBackupDate ? fmtDate(backup.lastBackupDate) : '—'],
    ['ProssimoBackup', backup.nextBackupDate ? fmtDate(backup.nextBackupDate) : '—'],
    ['IntervalloDiTempo', backup.timeIntervalBackup ? fmtInterval(backup.timeIntervalBackup) : '—'],
    ['DataCreazione', backup.creationDate ? fmtDate(backup.creationDate) : '—'],
    ['ConteggioBackup', String(backup.count)],
    ['MassimoNumeroBackupDaMantenere', String(backup.maxToKeep)],
    ...(backup.notes ? [['Note', backup.notes] as [string, string]] : []),
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
        title="Chiudi"
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
      setError('Rename failed')
    } finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal-box" style={{ maxWidth: 380 }}>
        <div className="modal-title">Rename backup</div>
        <form onSubmit={save} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
            <label className="section-label">Name</label>
            <input className="input" autoFocus value={name} onChange={e => setName(e.target.value)} required />
          </div>
          {error && (
            <div style={{ fontSize: 12, color: 'var(--error)' }}>{error}</div>
          )}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving…' : 'Rename'}
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
  const [form, setForm] = useState<CreateBackupPayload>({
    name: initial?.name ?? '',
    targetPath: initial?.targetPath ?? '',
    destinationPath: initial?.destinationPath ?? '',
    automatic: initial?.automatic ?? false,
    timeIntervalBackup: initial?.timeIntervalBackup ?? null,
    notes: initial?.notes ?? '',
    maxToKeep: initial?.maxToKeep ?? 5,
  })
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
      setError('Set a time interval before enabling automatic backup.')
      return
    }
    setLoading(true); setError(null)
    try {
      if (initial) await backupApi.update(initial.id, form)
      else         await backupApi.create(form)
      onSaved()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? (err instanceof Error ? err.message : 'Save failed'))
    } finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal-box">
        <div className="modal-title">{initial ? `Edit — ${initial.name}` : 'New backup configuration'}</div>

        <form onSubmit={save} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <FormRow>
            <Field label="Name *" value={form.name} onChange={v => set('name', v)} placeholder="My Documents" required />
          </FormRow>
          <FormRow>
            <PathField label="Source path *" value={form.targetPath} onChange={v => set('targetPath', v)} placeholder="/home/user/documents" required />
          </FormRow>
          <FormRow>
            <PathField label="Destination path *" value={form.destinationPath} onChange={v => set('destinationPath', v)} placeholder="/backups/documents" required />
          </FormRow>
          <FormRow cols={2}>
            <Field label="Notes" value={form.notes} onChange={v => set('notes', v)} placeholder="Optional description" />
            <NumField label="Max backups to keep" value={form.maxToKeep} onChange={v => set('maxToKeep', v)} min={1} />
          </FormRow>

          <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', userSelect: 'none' }}>
            <input type="checkbox" checked={form.automatic} onChange={e => {
              set('automatic', e.target.checked)
              if (!e.target.checked) set('timeIntervalBackup', null)
            }} style={{ accentColor: 'var(--accent)', width: 15, height: 15 }} />
            <span style={{ fontSize: 13, color: 'var(--text)' }}>Enable automatic backup</span>
          </label>

          {form.automatic && (
            <div style={{ background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6, padding: 14 }}>
              <div className="section-label" style={{ marginBottom: 10 }}>Time interval</div>
              <FormRow cols={3}>
                <NumField label="Days"    value={form.timeIntervalBackup?.days ?? 0}    onChange={v => setTi('days', v)}    min={0} />
                <NumField label="Hours"   value={form.timeIntervalBackup?.hours ?? 0}   onChange={v => setTi('hours', v)}   min={0} max={23} />
                <NumField label="Minutes" value={form.timeIntervalBackup?.minutes ?? 0} onChange={v => setTi('minutes', v)} min={0} max={59} />
              </FormRow>
            </div>
          )}

          {form.automatic && !hasValidInterval(form.timeIntervalBackup) && (
            <div style={{ fontSize: 12, color: 'var(--warning)', background: 'rgba(232,167,53,.1)',
              border: '1px solid rgba(232,167,53,.3)', borderRadius: 5, padding: '7px 10px' }}>
              Set a time interval (days / hours / minutes) before enabling automatic backup.
            </div>
          )}
          {form.automatic && hasValidInterval(form.timeIntervalBackup) && intervalTotalMinutes(form.timeIntervalBackup) < 5 && (
            <div style={{ fontSize: 12, color: 'var(--warning)', background: 'rgba(232,167,53,.1)',
              border: '1px solid rgba(232,167,53,.3)', borderRadius: 5, padding: '7px 10px' }}>
              Warning: interval is very short ({intervalTotalMinutes(form.timeIntervalBackup)} min). A low interval may impact system performance.
            </div>
          )}

          {error && (
            <div style={{ fontSize: 12, color: 'var(--error)', background: 'rgba(224,82,82,.1)',
              border: '1px solid rgba(224,82,82,.25)', borderRadius: 5, padding: '7px 10px' }}>
              {error}
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving…' : 'Save'}
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
            style={{ padding: '5px 9px', flexShrink: 0 }} title="Browse folder">
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
