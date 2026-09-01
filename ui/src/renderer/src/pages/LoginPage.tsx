import { useState } from 'react'
import { Database } from 'lucide-react'
import { authApi } from '../services/api'

interface Props { onRegistered: () => void }

export default function LoginPage({ onRegistered }: Props) {
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
      await authApi.register(name, surname, email)
      onRegistered()
    } catch {
      setError('Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      height: '100vh', display: 'flex', alignItems: 'center',
      justifyContent: 'center', background: 'var(--bg-0)',
    }}>
      <div style={{
        width: 360,
        background: 'var(--bg-2)',
        border: '1px solid var(--border)',
        borderRadius: 12,
        padding: 32,
        boxShadow: '0 24px 48px rgba(0,0,0,.45)',
      }}>
        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 28 }}>
          <div style={{
            width: 40, height: 40, borderRadius: 10,
            background: 'var(--accent)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Database size={20} color="#fff" />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--text)' }}>Backup Manager</div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 1 }}>Create your profile to get started</div>
          </div>
        </div>

        <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <Field label="First name"  value={name}    onChange={setName}    placeholder="Mario" />
          <Field label="Last name"   value={surname} onChange={setSurname} placeholder="Rossi" />
          <Field label="Email"       value={email}   onChange={setEmail}   placeholder="mario@example.com" type="email" />

          {error && (
            <div style={{
              background: 'rgba(224,82,82,.12)', border: '1px solid rgba(224,82,82,.3)',
              borderRadius: 6, padding: '8px 12px', fontSize: 12, color: 'var(--error)',
            }}>
              {error}
            </div>
          )}

          <button type="submit" className="btn btn-primary" disabled={loading}
            style={{ marginTop: 4, justifyContent: 'center', padding: '8px 0' }}>
            {loading ? 'Creating account…' : 'Get started'}
          </button>
        </form>
      </div>
    </div>
  )
}

function Field({ label, value, onChange, placeholder, type = 'text' }: {
  label: string; value: string; onChange: (v: string) => void
  placeholder?: string; type?: string
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
      <label className="section-label">{label}</label>
      <input
        className="input"
        type={type} required
        value={value} placeholder={placeholder}
        onChange={e => onChange(e.target.value)}
      />
    </div>
  )
}
