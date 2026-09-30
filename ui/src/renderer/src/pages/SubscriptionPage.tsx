import { Shield, AlertTriangle, AlertOctagon, Mail, Check, Lock } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useConfig } from '../context/ConfigContext'
import { useTranslation } from '../context/TranslationContext'
import { authApi, subscriptionApi } from '../services/api'
import type { SubscriptionInfo, User } from '../types'
import { PageHeader } from '../components/ui'

export default function SubscriptionPage() {
  const cfg = useConfig()
  const { t } = useTranslation()
  const { data: sub, isLoading } = useQuery({
    queryKey: ['subscription-status'],
    queryFn: subscriptionApi.getStatus,
    refetchInterval: 60_000,
  })

  const { data: user } = useQuery({ queryKey: ['user'], queryFn: authApi.getUser, staleTime: Infinity })
  const supportEmail = cfg.email || 'dennisturco@gmail.com'

  return (
    <div className="page page-narrow">
      <PageHeader
        title={t('ReactUI.SubscriptionTitle', 'Subscription')}
        desc={t('ReactUI.SubscriptionDesc', 'Real subscription status and automatic backups')}
      />

      {/* Current status */}
      {isLoading ? (
        <div className="card" style={{ padding: '18px 22px', color: 'var(--text-muted)', fontSize: 13 }}>
          {t('ReactUI.LoadingText', 'Loading…')}
        </div>
      ) : sub ? (
        <SubscriptionBanner sub={sub} renewalHref={renewalMailto(supportEmail, sub, user, cfg.version)} />
      ) : (
        <div className="card" style={{ padding: '18px 22px', color: 'var(--error)', fontSize: 13 }}>
          {t('ReactUI.SubscriptionLoadFailed', 'Unable to retrieve the subscription status.')}
        </div>
      )}

      {/* Footer: donation + contact us */}
      {(cfg.links.donatePaypal || cfg.links.donateBuymeacoffee) && cfg.menuItems.Donate !== false && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: 10 }}>
          {cfg.links.donatePaypal && cfg.menuItems.PaypalDonate !== false && (
            <a href={cfg.links.donatePaypal} target="_blank" rel="noreferrer" className="btn btn-ghost" style={{ fontSize: 12 }}>
              ❤️ {t('ReactUI.DonatePaypal', 'Donate via PayPal')}
            </a>
          )}
          {cfg.links.donateBuymeacoffee && cfg.menuItems.BuymeacoffeeDonate !== false && (
            <a href={cfg.links.donateBuymeacoffee} target="_blank" rel="noreferrer" className="btn btn-ghost" style={{ fontSize: 12 }}>
              ☕ {t('ReactUI.BuyMeACoffee', 'Buy me a coffee')}
            </a>
          )}
        </div>
      )}
      <p style={{ fontSize: 12, color: 'var(--text-muted)', textAlign: 'center' }}>
        {t('ReactUI.QuestionsContactUs', 'Questions? Write to')}{' '}
        <a href={`mailto:${supportEmail}`} target="_blank" rel="noreferrer">{supportEmail}</a>
      </p>
    </div>
  )
}

/* ─── Status banner ──────────────────────────────────────────────────────── */

function fmtDate(iso: string | null) {
  return iso ? new Date(iso).toLocaleDateString() : null
}

const BANNER_BY_STATUS: Record<SubscriptionInfo['status'], {
  icon: React.ReactNode; color: string; bg: string
  titleKey: string; titleFallback: string; badgeKey: string; badgeFallback: string
}> = {
  NONE: {
    icon: <Shield size={20} />, color: 'var(--accent)', bg: 'var(--accent-soft)',
    titleKey: 'ReactUI.BannerTitleNone', titleFallback: 'Subscription non richiesta',
    badgeKey: 'ReactUI.BannerBadgeNone', badgeFallback: 'Non richiesta',
  },
  ACTIVE: {
    icon: <Shield size={20} />, color: 'var(--success)', bg: 'var(--success-soft)',
    titleKey: 'ReactUI.BannerTitleActive', titleFallback: 'Subscription attiva',
    badgeKey: 'ReactUI.BannerBadgeActive', badgeFallback: 'Attiva',
  },
  EXPIRATION: {
    icon: <AlertTriangle size={20} />, color: 'var(--warning)', bg: 'var(--warning-soft)',
    titleKey: 'ReactUI.BannerTitleExpiration', titleFallback: 'Subscription in scadenza',
    badgeKey: 'ReactUI.BannerBadgeExpiration', badgeFallback: 'In scadenza',
  },
  EXPIRED: {
    icon: <AlertOctagon size={20} />, color: 'var(--error)', bg: 'var(--error-soft)',
    titleKey: 'ReactUI.BannerTitleExpired', titleFallback: 'Subscription scaduta',
    badgeKey: 'ReactUI.BannerBadgeExpired', badgeFallback: 'Scaduta',
  },
}

function BenefitItem({ enabled, label }: { enabled: boolean; label: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: enabled ? 'var(--text)' : 'var(--text-dim)' }}>
      {enabled
        ? <Check size={13} color="var(--success)" />
        : <Lock size={11} color="var(--text-dim)" />}
      {label}
    </div>
  )
}

function SubscriptionBanner({ sub, renewalHref }: { sub: SubscriptionInfo; renewalHref: string }) {
  const { t } = useTranslation()
  const cfg = BANNER_BY_STATUS[sub.status]
  const from = fmtDate(sub.validFrom)
  const to = fmtDate(sub.validUntil)

  return (
    <div className="card" style={{
      padding: '18px 22px',
      display: 'flex', flexDirection: 'column', gap: 14,
      borderColor: cfg.color,
      background: cfg.bg,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{
            width: 40, height: 40, borderRadius: 10,
            background: `color-mix(in srgb, ${cfg.color} 16%, transparent)`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: cfg.color, flexShrink: 0,
          }}>
            {cfg.icon}
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--text)' }}>{t(cfg.titleKey, cfg.titleFallback)}</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
              {sub.status === 'NONE' && t('ReactUI.StatusDescNone', "Questa installazione non richiede una subscription: tutte le funzionalità sono sempre attive.")}
              {sub.status === 'ACTIVE' && t('ReactUI.StatusDescActive', 'Subscription attiva: hai accesso a tutte le funzionalità Pro.')}
              {sub.status === 'EXPIRATION' && t('ReactUI.StatusDescExpiration', 'Le funzionalità Pro sono ancora attive, ma la subscription sta per scadere.')}
              {sub.status === 'EXPIRED' && t('ReactUI.StatusDescExpired', 'Le funzionalità Pro sono in pausa. I backup manuali restano sempre disponibili.')}
            </div>
          </div>
        </div>
        <span className="badge" style={{ fontSize: 12, padding: '4px 12px', background: `color-mix(in srgb, ${cfg.color} 16%, transparent)`, color: cfg.color, flexShrink: 0 }}>
          {t(cfg.badgeKey, cfg.badgeFallback)}
        </span>
      </div>

      {/* What an active subscription unlocks */}
      <div style={{
        display: 'flex', flexWrap: 'wrap', gap: 16,
        borderTop: `1px solid color-mix(in srgb, ${cfg.color} 25%, transparent)`, paddingTop: 12,
      }}>
        <BenefitItem enabled={sub.status !== 'EXPIRED'} label={t('ReactUI.BenefitAutoBackups', 'Backup automatici')} />
        <BenefitItem enabled={sub.status !== 'EXPIRED'} label={t('ReactUI.BenefitAnalyticsDashboard', 'Dashboard Analytics')} />
        <BenefitItem enabled={sub.status !== 'EXPIRED'} label={t('ReactUI.BenefitPriorityAssistance', 'Assistenza prioritaria')} />
      </div>

      {/* Duration — only meaningful while not expired */}
      {sub.status !== 'EXPIRED' && sub.status !== 'NONE' && (from || to) && (
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8,
          fontSize: 12, color: 'var(--text-muted)',
          borderTop: `1px solid color-mix(in srgb, ${cfg.color} 25%, transparent)`, paddingTop: 12,
        }}>
          <span>{t('ReactUI.DurationLabel', 'Durata')}:</span>
          <strong style={{ color: 'var(--text)' }}>{from ?? '—'}</strong>
          <span>→</span>
          <strong style={{ color: 'var(--text)' }}>{to ?? '—'}</strong>
        </div>
      )}

      {/* Renewal request — only when expired */}
      {sub.status === 'EXPIRED' && (
        <div style={{ borderTop: `1px solid color-mix(in srgb, ${cfg.color} 25%, transparent)`, paddingTop: 12 }}>
          <a className="btn btn-primary" href={renewalHref} target="_blank" rel="noreferrer">
            <Mail size={13} /> {t('ReactUI.RequestRenewalButton', 'Request renewal')}
          </a>
          <div className="field-hint" style={{ marginTop: 6 }}>
            {t('ReactUI.RenewalMailHint', 'Opens your email program with the renewal request already filled in.')}
          </div>
        </div>
      )}
    </div>
  )
}

/* ─── Renewal request ────────────────────────────────────────────────────── */

// The app doesn't send emails itself: the request opens in the user's own mail client
function renewalMailto(to: string, sub: SubscriptionInfo, user: User | undefined, version: string) {
  const subject = 'BackupManager - Subscription renewal request'
  const body = [
    'Hello, I would like to renew my BackupManager subscription.',
    '',
    user ? `Name: ${user.name} ${user.surname}` : null,
    user ? `Email: ${user.email}` : null,
    `Subscription status: ${sub.status}`,
    sub.validUntil ? `Valid until: ${sub.validUntil}` : null,
    version ? `App version: ${version}` : null,
  ].filter(line => line !== null).join('\n')
  return `mailto:${to}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`
}
