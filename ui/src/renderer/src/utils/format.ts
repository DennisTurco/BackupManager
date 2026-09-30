import type { TimeInterval } from '../types'

export function fmtDateTime(iso: string | null | undefined): string {
  return iso ? new Date(iso).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' }) : ''
}

export function fmtDate(iso: string | null | undefined): string {
  return iso ? new Date(iso).toLocaleDateString() : ''
}

// "3 min ago", "in 2 h", … — falls back to the absolute date beyond a week
export function fmtRelative(iso: string | null | undefined, locale?: string): string {
  if (!iso) return ''
  const diffMs = new Date(iso).getTime() - Date.now()
  const abs = Math.abs(diffMs)
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto', style: 'short' })
  if (abs < 60_000) return rtf.format(Math.round(diffMs / 1000), 'second')
  if (abs < 3_600_000) return rtf.format(Math.round(diffMs / 60_000), 'minute')
  if (abs < 86_400_000) return rtf.format(Math.round(diffMs / 3_600_000), 'hour')
  if (abs < 7 * 86_400_000) return rtf.format(Math.round(diffMs / 86_400_000), 'day')
  return fmtDateTime(iso)
}

export function fmtDuration(ms: number | null | undefined): string {
  if (ms == null) return '—'
  if (ms < 1000) return `${Math.round(ms)} ms`
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)} s`
  const min = Math.floor(ms / 60_000)
  const sec = Math.round((ms % 60_000) / 1000)
  return min < 60 ? `${min}m ${sec}s` : `${Math.floor(min / 60)}h ${min % 60}m`
}

export function fmtBytes(bytes: number | null | undefined): string {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.min(sizes.length - 1, Math.floor(Math.log(bytes) / Math.log(k)))
  return `${(bytes / Math.pow(k, i)).toFixed(i === 0 ? 0 : 1)} ${sizes[i]}`
}

// "1d 2h 30m" — omits zero parts
export function fmtInterval(ti: TimeInterval | null | undefined): string {
  if (!ti) return ''
  const parts: string[] = []
  if (ti.days) parts.push(`${ti.days}d`)
  if (ti.hours) parts.push(`${ti.hours}h`)
  if (ti.minutes) parts.push(`${ti.minutes}m`)
  return parts.join(' ') || '0m'
}

export function hasValidInterval(ti: TimeInterval | null | undefined): boolean {
  return !!ti && (ti.days > 0 || ti.hours > 0 || ti.minutes > 0)
}

export function intervalTotalMinutes(ti: TimeInterval | null | undefined): number {
  return ti ? ti.days * 1440 + ti.hours * 60 + ti.minutes : 0
}

export function apiErrorMessage(err: unknown): string | undefined {
  return (err as { response?: { data?: { message?: string } } })?.response?.data?.message
}
