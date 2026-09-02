import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Save, Sun, Moon, Monitor } from 'lucide-react'
import { settingsApi } from '../services/api'
import { useTheme } from '../context/ThemeContext'
import { useTranslation } from '../context/TranslationContext'

const LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'it', label: 'Italiano' },
  { code: 'fr', label: 'Français' },
  { code: 'de', label: 'Deutsch' },
  { code: 'es', label: 'Español' },
]

export default function SettingsPage() {
  const { t } = useTranslation()
  const { mode, setMode } = useTheme()
  const qc = useQueryClient()
  const [saved, setSaved] = useState(false)

  const { data: settings, isLoading } = useQuery({
    queryKey: ['settings'],
    queryFn: settingsApi.get,
  })

  const [form, setForm] = useState<Record<string, string>>({})
  useEffect(() => { if (settings) setForm(settings) }, [settings])

  const mutation = useMutation({
    mutationFn: (data: Record<string, string>) => settingsApi.update(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['settings'] })
      setSaved(true)
      setTimeout(() => setSaved(false), 2500)
    }
  })

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }))

  if (isLoading) {
    return (
      <div style={{ color: 'var(--text-muted)', padding: 32, textAlign: 'center' }}>
        {t('ReactUI.LoadingSettings', 'Loading settings…')}
      </div>
    )
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Header */}
      <div className="page-header">
        <div>
          <div className="page-title">{t('ReactUI.SettingsTitle', 'Settings')}</div>
          <div className="page-desc">{t('ReactUI.SettingsDesc', 'Application preferences and configuration')}</div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {saved && (
            <span style={{ fontSize: 12, color: 'var(--success)' }}>{t('ReactUI.SavedText', 'Saved!')}</span>
          )}
          <button
            className="btn btn-primary"
            onClick={() => mutation.mutate(form)}
            disabled={mutation.isPending}
          >
            <Save size={13} />
            {mutation.isPending ? 'Saving…' : t('General.SaveButton', 'Save changes')}
          </button>
        </div>
      </div>

      {/* Appearance */}
      <Section title={t('ReactUI.SectionAppearance', 'Appearance')}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 }}>
          <ThemeCard
            icon={<Sun size={18} />}
            label={t('ReactUI.ThemeLight', 'Light')}
            active={mode === 'light'}
            onClick={() => setMode('light')}
          />
          <ThemeCard
            icon={<Moon size={18} />}
            label={t('ReactUI.ThemeDark', 'Dark')}
            active={mode === 'dark'}
            onClick={() => setMode('dark')}
          />
          <ThemeCard
            icon={<Monitor size={18} />}
            label={t('ReactUI.ThemeSystem', 'System')}
            active={mode === 'system'}
            onClick={() => setMode('system')}
          />
        </div>
      </Section>

      {/* Language */}
      <Section title={t('ReactUI.SectionLanguage', 'Language')}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 8 }}>
          {LANGUAGES.map(lang => (
            <button
              key={lang.code}
              onClick={() => set('LANGUAGE', lang.code)}
              style={{
                padding: '10px 0',
                borderRadius: 6,
                border: `1px solid ${form['LANGUAGE'] === lang.code ? 'var(--accent)' : 'var(--border)'}`,
                background: form['LANGUAGE'] === lang.code ? 'rgba(33,150,243,.12)' : 'var(--bg-3)',
                color: form['LANGUAGE'] === lang.code ? 'var(--accent)' : 'var(--text-muted)',
                fontWeight: form['LANGUAGE'] === lang.code ? 600 : 400,
                fontSize: 13,
                cursor: 'pointer',
                transition: 'all 0.12s',
              }}
            >
              {lang.label}
            </button>
          ))}
        </div>
      </Section>

      {/* Backup settings */}
      <Section title={t('ReactUI.SectionBackupDefaults', 'Backup defaults')}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
          <FormField label={t('ReactUI.DefaultDestPathLabel', 'Default destination path')} value={form['DEFAULT_DESTINATION_PATH'] ?? ''}
            onChange={v => set('DEFAULT_DESTINATION_PATH', v)} placeholder="/backups" />
          <NumField label={t('ReactUI.DefaultMaxToKeepLabel', 'Default max backups to keep')} value={Number(form['DEFAULT_MAX_TO_KEEP'] ?? 5)}
            onChange={v => set('DEFAULT_MAX_TO_KEEP', String(v))} min={1} />
        </div>
      </Section>

      {/* Notifications */}
      <Section title={t('ReactUI.SectionNotifications', 'Notifications')}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Toggle
            label={t('ReactUI.NotifyOnComplete', 'Show notification on backup completion')}
            checked={form['NOTIFY_ON_COMPLETE'] === 'true'}
            onChange={v => set('NOTIFY_ON_COMPLETE', String(v))}
          />
          <Toggle
            label={t('ReactUI.NotifyOnFailure', 'Show notification on backup failure')}
            checked={form['NOTIFY_ON_FAILURE'] !== 'false'}
            onChange={v => set('NOTIFY_ON_FAILURE', String(v))}
          />
          <Toggle
            label={t('ReactUI.StartMinimized', 'Start minimized to system tray')}
            checked={form['START_MINIMIZED'] === 'true'}
            onChange={v => set('START_MINIMIZED', String(v))}
          />
        </div>
      </Section>
    </div>
  )
}

/* ─── Sub-components ─────────────────────────────────────────────────────── */

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="card" style={{ padding: '16px 20px' }}>
      <div className="section-label" style={{ marginBottom: 14, fontSize: 11 }}>{title}</div>
      {children}
    </div>
  )
}

function ThemeCard({ icon, label, active, onClick, disabled }: {
  icon: React.ReactNode; label: string; active: boolean; onClick?: () => void; disabled?: boolean
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        padding: '16px 0',
        borderRadius: 8,
        border: `1px solid ${active ? 'var(--accent)' : 'var(--border)'}`,
        background: active ? 'rgba(33,150,243,.12)' : 'var(--bg-3)',
        color: active ? 'var(--accent)' : 'var(--text-muted)',
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.5 : 1,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
        fontSize: 13, fontWeight: active ? 600 : 400,
        transition: 'all 0.12s',
      }}
    >
      {icon}
      {label}
      {active && (
        <span style={{
          width: 6, height: 6, borderRadius: '50%',
          background: 'var(--accent)',
        }} />
      )}
    </button>
  )
}

function FormField({ label, value, onChange, placeholder }: {
  label: string; value: string; onChange: (v: string) => void; placeholder?: string
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
      <label className="section-label">{label}</label>
      <input className="input" value={value} placeholder={placeholder}
        onChange={e => onChange(e.target.value)} />
    </div>
  )
}

function NumField({ label, value, onChange, min }: {
  label: string; value: number; onChange: (v: number) => void; min?: number
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
      <label className="section-label">{label}</label>
      <input className="input" type="number" value={value} min={min}
        onChange={e => onChange(Number(e.target.value))} />
    </div>
  )
}

function Toggle({ label, checked, onChange }: {
  label: string; checked: boolean; onChange: (v: boolean) => void
}) {
  return (
    <label style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', userSelect: 'none' }}>
      <div
        onClick={() => onChange(!checked)}
        style={{
          width: 34, height: 18, borderRadius: 9,
          background: checked ? 'var(--accent)' : 'var(--bg-4)',
          position: 'relative', flexShrink: 0,
          cursor: 'pointer',
          transition: 'background 0.18s',
        }}
      >
        <div style={{
          position: 'absolute', top: 2, left: checked ? 18 : 2,
          width: 14, height: 14, borderRadius: '50%',
          background: '#fff',
          transition: 'left 0.18s',
          boxShadow: '0 1px 3px rgba(0,0,0,.4)',
        }} />
      </div>
      <span style={{ fontSize: 13, color: 'var(--text)' }}>{label}</span>
    </label>
  )
}
