import { useState } from 'react'
import { authApi } from '../services/api'
import { useTranslation } from '../context/TranslationContext'
import { Alert, TextField } from '../components/ui'

interface Props { onRegistered: () => void }

export default function LoginPage({ onRegistered }: Props) {
  const { t } = useTranslation()
  const [name, setName]       = useState('')
  const [surname, setSurname] = useState('')
  const [email, setEmail]     = useState('')
  const [error, setError]     = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await authApi.register(name.trim(), surname.trim(), email.trim())
      onRegistered()
    } catch {
      setError(t('ReactUI.RegistrationFailed', 'Registration failed. Please try again.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      height: '100vh', display: 'flex', alignItems: 'center',
      justifyContent: 'center', background: 'var(--bg-0)', padding: 24,
    }}>
      <div className="card" style={{ width: 380, padding: 32, borderRadius: 12, boxShadow: 'var(--shadow-lg)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
          <img src="/icon.png" alt="" style={{ width: 40, height: 40, borderRadius: 10, objectFit: 'contain' }} />
          <div style={{ fontWeight: 700, fontSize: 16 }}>Backup Manager</div>
        </div>
        <p className="text-muted" style={{ fontSize: 12, lineHeight: 1.6, marginBottom: 22 }}>
          {t('ReactUI.WelcomeText', 'Welcome! Create your profile to get started.')}
        </p>

        <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <TextField label={t('ReactUI.FirstNameLabel', 'First name')} value={name} onChange={setName} placeholder="Mario" required autoFocus />
            <TextField label={t('ReactUI.LastNameLabel', 'Last name')} value={surname} onChange={setSurname} placeholder="Rossi" required />
          </div>
          <TextField label={t('ReactUI.EmailLabel', 'Email')} value={email} onChange={setEmail} placeholder="mario@example.com" type="email" required />

          {error && <Alert kind="error">{error}</Alert>}

          <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: 4, padding: '8px 0' }}>
            {loading ? t('ReactUI.CreatingProfile', 'Creating profile…') : t('ReactUI.GetStarted', 'Get started')}
          </button>
        </form>
      </div>
    </div>
  )
}
