import { Check, Zap, Shield, Clock, HardDrive, Mail } from 'lucide-react'
import { useConfig } from '../context/ConfigContext'

const FREE_FEATURES = [
  'Up to 5 backup configurations',
  'Manual and scheduled backups',
  'Light / Dark theme',
  'Multilingual support (EN, IT, DE, ES, FR)',
  'Backup history & log viewer',
  'Tray icon with background service',
]

const PRO_FEATURES = [
  'Unlimited backup configurations',
  'Everything in Free',
  'Email notifications on completion / failure',
  'Priority support',
  'Advanced analytics & reports',
  'Cloud destination support (coming soon)',
]

export default function SubscriptionPage() {
  const cfg = useConfig()
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div className="page-header">
        <div>
          <div className="page-title">Subscription</div>
          <div className="page-desc">Your current plan and available upgrades</div>
        </div>
      </div>

      {/* Current plan banner */}
      <div className="card" style={{
        padding: '18px 22px',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        borderColor: 'var(--accent)',
        background: 'rgba(33,150,243,.07)',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{
            width: 40, height: 40, borderRadius: 10,
            background: 'rgba(33,150,243,.18)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: 'var(--accent)',
          }}>
            <Shield size={20} />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--text)' }}>Free plan</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
              You are on the free tier — no expiration
            </div>
          </div>
        </div>
        <span className="badge badge-accent" style={{ fontSize: 12, padding: '4px 12px' }}>Active</span>
      </div>

      {/* Stats strip */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 }}>
        <StatCard icon={<HardDrive size={16} />} color="#2196f3" label="Backup configs" value="Free · up to 5" />
        <StatCard icon={<Clock size={16} />}     color="#5aad4e" label="Scheduler"      value="Included" />
        <StatCard icon={<Mail size={16} />}       color="#e8a735" label="Notifications"  value="Pro only" />
      </div>

      {/* Plans */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <PlanCard
          name="Free"
          price="€0"
          period="forever"
          features={FREE_FEATURES}
          current
          accentColor="var(--border)"
          badgeLabel="Current plan"
        />
        <PlanCard
          name="Pro"
          price="€4.99"
          period="per month"
          features={PRO_FEATURES}
          accentColor="var(--accent)"
          badgeLabel="Upgrade"
          onUpgrade={() => {
            /* open external purchase URL */
          }}
        />
      </div>

      {/* Footer note */}
      {(cfg.links.donatePaypal || cfg.links.donateBuymeacoffee) && cfg.menuItems.Donate !== false && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: 10 }}>
          {cfg.links.donatePaypal && cfg.menuItems.PaypalDonate !== false && (
            <a href={cfg.links.donatePaypal} target="_blank" rel="noreferrer" className="btn btn-ghost" style={{ fontSize: 12 }}>
              ❤️ Donate via PayPal
            </a>
          )}
          {cfg.links.donateBuymeacoffee && cfg.menuItems.BuymeacoffeeDonate !== false && (
            <a href={cfg.links.donateBuymeacoffee} target="_blank" rel="noreferrer" className="btn btn-ghost" style={{ fontSize: 12 }}>
              ☕ Buy me a coffee
            </a>
          )}
        </div>
      )}
      <p style={{ fontSize: 12, color: 'var(--text-muted)', textAlign: 'center' }}>
        Questions? Contact us at{' '}
        <a href={`mailto:${cfg.email || 'dennisturco@gmail.com'}`} style={{ color: 'var(--accent)', textDecoration: 'none' }}>
          {cfg.email || 'dennisturco@gmail.com'}
        </a>
      </p>
    </div>
  )
}

/* ─── Sub-components ─────────────────────────────────────────────────────── */

function StatCard({ icon, color, label, value }: {
  icon: React.ReactNode; color: string; label: string; value: string
}) {
  return (
    <div className="card" style={{ padding: '14px 18px', display: 'flex', alignItems: 'center', gap: 12 }}>
      <div style={{
        width: 34, height: 34, borderRadius: 8,
        background: `${color}22`, color,
        display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
      }}>
        {icon}
      </div>
      <div>
        <div className="section-label" style={{ marginBottom: 3 }}>{label}</div>
        <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text)' }}>{value}</div>
      </div>
    </div>
  )
}

function PlanCard({ name, price, period, features, current, accentColor, badgeLabel, onUpgrade }: {
  name: string; price: string; period: string; features: string[]
  current?: boolean; accentColor: string; badgeLabel: string
  onUpgrade?: () => void
}) {
  return (
    <div className="card" style={{
      padding: '22px 24px',
      border: `1px solid ${current ? 'var(--border)' : 'var(--accent)'}`,
      background: current ? 'var(--bg-2)' : 'rgba(33,150,243,.05)',
      display: 'flex', flexDirection: 'column', gap: 18,
    }}>
      {/* Plan header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <div style={{ fontWeight: 700, fontSize: 16, color: 'var(--text)' }}>{name}</div>
          <div style={{ marginTop: 6 }}>
            <span style={{ fontSize: 26, fontWeight: 800, color: accentColor === 'var(--border)' ? 'var(--text)' : 'var(--accent)' }}>
              {price}
            </span>
            <span style={{ fontSize: 12, color: 'var(--text-muted)', marginLeft: 5 }}>{period}</span>
          </div>
        </div>
        {!current && (
          <div style={{
            padding: '3px 10px', borderRadius: 99,
            background: 'rgba(33,150,243,.15)', color: 'var(--accent)',
            fontSize: 11, fontWeight: 600,
          }}>
            <Zap size={11} style={{ display: 'inline', marginRight: 4, verticalAlign: 'middle' }} />
            {badgeLabel}
          </div>
        )}
        {current && (
          <div style={{
            padding: '3px 10px', borderRadius: 99,
            background: 'var(--bg-3)', color: 'var(--text-muted)',
            fontSize: 11, fontWeight: 600,
          }}>
            {badgeLabel}
          </div>
        )}
      </div>

      {/* Features */}
      <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 9 }}>
        {features.map(f => (
          <li key={f} style={{ display: 'flex', alignItems: 'flex-start', gap: 8 }}>
            <Check size={13} color="var(--success)" style={{ flexShrink: 0, marginTop: 1 }} />
            <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{f}</span>
          </li>
        ))}
      </ul>

      {/* CTA */}
      {onUpgrade ? (
        <button className="btn btn-primary" onClick={onUpgrade}
          style={{ justifyContent: 'center', padding: '8px 0', marginTop: 'auto' }}>
          <Zap size={13} /> Upgrade to Pro
        </button>
      ) : (
        <button className="btn btn-ghost" disabled
          style={{ justifyContent: 'center', padding: '8px 0', marginTop: 'auto' }}>
          Current plan
        </button>
      )}
    </div>
  )
}
