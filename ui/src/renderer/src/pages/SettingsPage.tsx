import { useState, useEffect, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Sun, Moon, Monitor, Check, Loader } from 'lucide-react'
import { settingsApi, translationsApi } from '../services/api'
import { useTheme } from '../context/ThemeContext'
import { useTranslation } from '../context/TranslationContext'
import { useToast } from '../context/ToastContext'
import { CenteredMessage, NumField, PageHeader, PathField, Segmented, Switch } from '../components/ui'

// Used only if the backend language list can't be fetched
const FALLBACK_LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'it', label: 'Italiano' },
  { code: 'fr', label: 'Français' },
  { code: 'de', label: 'Deutsch' },
  { code: 'es', label: 'Español' },
]

export default function SettingsPage() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { mode, setMode } = useTheme()
  const qc = useQueryClient()

  const { data: settings, isLoading } = useQuery({ queryKey: ['settings'], queryFn: settingsApi.get })
  const { data: languages = FALLBACK_LANGUAGES } = useQuery({
    queryKey: ['languages'],
    queryFn: translationsApi.getLanguages,
    staleTime: Infinity,
  })

  const [form, setForm] = useState<Record<string, string>>({})
  useEffect(() => { if (settings) setForm(settings) }, [settings])

  const [savedAt, setSavedAt] = useState<number | null>(null)
  const savedTimer = useRef<ReturnType<typeof setTimeout>>()

  // Settings save as soon as they change (text fields on blur) — no separate Save button
  const mutation = useMutation({
    mutationFn: (patch: Record<string, string>) => settingsApi.update(patch),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['settings'] })
      setSavedAt(Date.now())
      clearTimeout(savedTimer.current)
      savedTimer.current = setTimeout(() => setSavedAt(null), 2000)
    },
    onError: () => toast(t('ReactUI.SaveFailed', 'Save failed'), 'error'),
  })

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }))
  const commit = (k: string, v: string) => {
    if ((settings?.[k] ?? '') === v) return
    set(k, v)
    mutation.mutate({ [k]: v })
  }

  if (isLoading) return <CenteredMessage>{t('ReactUI.LoadingSettings', 'Loading settings…')}</CenteredMessage>

  return (
    <div className="page page-narrow">
      <PageHeader
        title={t('ReactUI.SettingsTitle', 'Settings')}
        desc={t('ReactUI.SettingsDesc', 'Application preferences and configuration')}
        actions={
          mutation.isPending
            ? <span className="text-muted" style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 6 }}><Loader size={12} className="spin" />{t('ReactUI.SavingText', 'Saving…')}</span>
            : savedAt
              ? <span style={{ fontSize: 12, color: 'var(--success)', display: 'flex', alignItems: 'center', gap: 6 }}><Check size={13} />{t('ReactUI.SavedText', 'Saved!')}</span>
              : <span className="text-dim" style={{ fontSize: 12 }}>{t('ReactUI.AutoSaveHint', 'Changes are saved automatically')}</span>
        }
      />

      <Section title={t('ReactUI.SectionAppearance', 'Appearance')}>
        <Row label={t('ReactUI.ThemeLabel', 'Theme')}>
          <Segmented
            value={mode}
            onChange={setMode}
            options={[
              { value: 'light', label: t('ReactUI.ThemeLight', 'Light'), icon: <Sun size={13} /> },
              { value: 'dark', label: t('ReactUI.ThemeDark', 'Dark'), icon: <Moon size={13} /> },
              { value: 'system', label: t('ReactUI.ThemeSystem', 'System'), icon: <Monitor size={13} /> },
            ]}
          />
        </Row>
        <Row label={t('ReactUI.SectionLanguage', 'Language')}>
          <select className="input" style={{ width: 220 }} value={form['LANGUAGE'] ?? 'en'}
            onChange={e => commit('LANGUAGE', e.target.value)}>
            {languages.map(l => <option key={l.code} value={l.code}>{l.label}</option>)}
          </select>
        </Row>
      </Section>

      <Section title={t('ReactUI.SectionBackupDefaults', 'Backup defaults')}
        desc={t('ReactUI.BackupDefaultsDesc', 'Pre-filled values when you create a new backup configuration.')}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 200px', gap: 14 }}>
          <PathField
            label={t('ReactUI.DefaultDestPathLabel', 'Default destination path')}
            value={form['DEFAULT_DESTINATION_PATH'] ?? ''}
            onChange={v => set('DEFAULT_DESTINATION_PATH', v)}
            onCommit={v => commit('DEFAULT_DESTINATION_PATH', v)}
            placeholder="D:\Backups"
          />
          <NumField
            label={t('ReactUI.DefaultMaxToKeepLabel', 'Default max backups to keep')}
            value={Number(form['DEFAULT_MAX_TO_KEEP'] ?? 5)}
            onChange={v => set('DEFAULT_MAX_TO_KEEP', String(Math.max(1, v)))}
            onBlur={() => commit('DEFAULT_MAX_TO_KEEP', form['DEFAULT_MAX_TO_KEEP'] ?? '5')}
            min={1}
          />
        </div>
      </Section>

      <Section title={t('ReactUI.SectionNotifications', 'Notifications')}>
        <Switch
          label={t('ReactUI.NotifyOnComplete', 'Show notification on backup completion')}
          checked={form['NOTIFY_ON_COMPLETE'] !== 'false'}
          onChange={v => commit('NOTIFY_ON_COMPLETE', String(v))}
        />
        <Switch
          label={t('ReactUI.NotifyOnFailure', 'Show notification on backup failure')}
          checked={form['NOTIFY_ON_FAILURE'] !== 'false'}
          onChange={v => commit('NOTIFY_ON_FAILURE', String(v))}
        />
      </Section>

      <Section title={t('ReactUI.SectionStartup', 'Startup')}>
        <Switch
          label={t('ReactUI.StartMinimized', 'Start minimized to system tray')}
          checked={form['START_MINIMIZED'] === 'true'}
          onChange={v => commit('START_MINIMIZED', String(v))}
        />
      </Section>
    </div>
  )
}

function Section({ title, desc, children }: { title: string; desc?: string; children: React.ReactNode }) {
  return (
    <section className="card" style={{ padding: '16px 20px' }}>
      <div style={{ marginBottom: 12 }}>
        <div style={{ fontSize: 14, fontWeight: 600 }}>{title}</div>
        {desc && <div className="field-hint" style={{ marginTop: 2 }}>{desc}</div>}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>{children}</div>
    </section>
  )
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, minHeight: 34 }}>
      <span style={{ fontSize: 13 }}>{label}</span>
      {children}
    </div>
  )
}
