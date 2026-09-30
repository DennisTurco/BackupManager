import { useEffect, useId } from 'react'
import { FolderOpen, X } from 'lucide-react'
import { useTranslation } from '../context/TranslationContext'
import type { BackupRequest } from '../types'

/* ─── Page header ────────────────────────────────────────────────────────── */

export function PageHeader({ title, desc, actions }: {
  title: string; desc?: string; actions?: React.ReactNode
}) {
  return (
    <div className="page-header">
      <div>
        <h1 className="page-title">{title}</h1>
        {desc && <div className="page-desc">{desc}</div>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </div>
  )
}

/* ─── Modal ──────────────────────────────────────────────────────────────── */

export function Modal({ title, onClose, children, footer, width = 520 }: {
  title: string; onClose: () => void; children: React.ReactNode; footer?: React.ReactNode; width?: number
}) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="modal-overlay" onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal-box" style={{ maxWidth: width }} role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head">
          <div className="modal-title">{title}</div>
          <button type="button" className="icon-btn" onClick={onClose} aria-label="Close"><X size={15} /></button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  )
}

export function ConfirmDialog({ title, message, confirmLabel, danger, onConfirm, onClose }: {
  title: string; message: string; confirmLabel: string; danger?: boolean
  onConfirm: () => void; onClose: () => void
}) {
  const { t } = useTranslation()
  return (
    <Modal
      title={title}
      onClose={onClose}
      width={400}
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose}>{t('General.CancelButton', 'Cancel')}</button>
          <button
            className={`btn ${danger ? 'btn-danger-solid' : 'btn-primary'}`}
            autoFocus
            onClick={() => { onConfirm(); onClose() }}
          >
            {confirmLabel}
          </button>
        </>
      }
    >
      <p className="text-muted" style={{ lineHeight: 1.6 }}>{message}</p>
    </Modal>
  )
}

/* ─── Feedback ───────────────────────────────────────────────────────────── */

export function Alert({ kind, children }: { kind: 'error' | 'warning' | 'info' | 'success'; children: React.ReactNode }) {
  return <div className={`alert alert-${kind}`}>{children}</div>
}

export function CenteredMessage({ children }: { children: React.ReactNode }) {
  return <div className="centered-message">{children}</div>
}

/* ─── Form fields ────────────────────────────────────────────────────────── */

export function Field({ label, hint, children }: { label: string; hint?: string; children: (id: string) => React.ReactNode }) {
  const id = useId()
  return (
    <div className="field">
      <label className="field-label" htmlFor={id}>{label}</label>
      {children(id)}
      {hint && <div className="field-hint">{hint}</div>}
    </div>
  )
}

export function TextField({ label, value, onChange, onBlur, placeholder, required, autoFocus, hint, type = 'text' }: {
  label: string; value: string; onChange: (v: string) => void; onBlur?: () => void
  placeholder?: string; required?: boolean; autoFocus?: boolean; hint?: string; type?: string
}) {
  return (
    <Field label={label} hint={hint}>
      {id => (
        <input id={id} className="input" type={type} value={value} placeholder={placeholder}
          required={required} autoFocus={autoFocus} onBlur={onBlur}
          onChange={e => onChange(e.target.value)} />
      )}
    </Field>
  )
}

export function NumField({ label, value, onChange, onBlur, min, max, hint }: {
  label: string; value: number; onChange: (v: number) => void; onBlur?: () => void
  min?: number; max?: number; hint?: string
}) {
  return (
    <Field label={label} hint={hint}>
      {id => (
        <input id={id} className="input" type="number" value={Number.isNaN(value) ? '' : value}
          min={min} max={max} onBlur={onBlur}
          onChange={e => {
            const n = e.target.value === '' ? 0 : Number(e.target.value)
            onChange(max !== undefined ? Math.min(max, n) : n)
          }} />
      )}
    </Field>
  )
}

export function PathField({ label, value, onChange, onCommit, placeholder, required, hint }: {
  label: string; value: string; onChange: (v: string) => void
  /** called when the value is final (blur or folder picked) */
  onCommit?: (v: string) => void
  placeholder?: string; required?: boolean; hint?: string
}) {
  const { t } = useTranslation()
  const browse = async () => {
    const path = await window.electron?.openFolder()
    if (path) { onChange(path); onCommit?.(path) }
  }
  return (
    <Field label={label} hint={hint}>
      {id => (
        <div style={{ display: 'flex', gap: 6 }}>
          <input id={id} className="input" value={value} placeholder={placeholder} required={required}
            onChange={e => onChange(e.target.value)} onBlur={() => onCommit?.(value)} style={{ flex: 1 }} />
          {window.electron && (
            <button type="button" className="btn btn-ghost btn-square" onClick={browse}
              title={t('ReactUI.BrowseFolderTooltip', 'Browse folder')}>
              <FolderOpen size={14} />
            </button>
          )}
        </div>
      )}
    </Field>
  )
}

export function Switch({ label, desc, checked, onChange }: {
  label: string; desc?: string; checked: boolean; onChange: (v: boolean) => void
}) {
  return (
    <label className="switch-row">
      <span style={{ flex: 1 }}>
        <span className="switch-label">{label}</span>
        {desc && <span className="field-hint" style={{ display: 'block' }}>{desc}</span>}
      </span>
      <input type="checkbox" role="switch" className="switch" checked={checked}
        onChange={e => onChange(e.target.checked)} />
    </label>
  )
}

export function Segmented<T extends string>({ options, value, onChange }: {
  options: { value: T; label: string; icon?: React.ReactNode }[]; value: T; onChange: (v: T) => void
}) {
  return (
    <div className="segmented" role="radiogroup">
      {options.map(o => (
        <button key={o.value} type="button" role="radio" aria-checked={value === o.value}
          className={value === o.value ? 'active' : ''} onClick={() => onChange(o.value)}>
          {o.icon}{o.label}
        </button>
      ))}
    </div>
  )
}

/* ─── Status badges ──────────────────────────────────────────────────────── */

export function RunStatusBadge({ status }: { status: BackupRequest['status'] }) {
  const { t } = useTranslation()
  if (status === 'IN_PROGRESS') return <span className="badge badge-accent">{t('ReactUI.StatusRunning', 'Running')}</span>
  if (status === 'FINISHED') return <span className="badge badge-success">{t('ReactUI.StatusCompleted', 'Completed')}</span>
  return <span className="badge badge-error">{t('ReactUI.StatusFailed', 'Failed')}</span>
}
