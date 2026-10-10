import axios from 'axios'
import type {
  AnalyticsSnapshot,
  AppConfig,
  BackupConfig,
  BackupRequest,
  CreateBackupPayload,
  SubscriptionInfo
} from '../types'

const client = axios.create({
  baseURL: 'http://127.0.0.1:7089',
  headers: { 'Content-Type': 'application/json' }
})

// ── Backups ──────────────────────────────────────────────────────────────────

export const backupApi = {
  getAll: () => client.get<BackupConfig[]>('/api/backups').then((r) => r.data),
  getById: (id: number) => client.get<BackupConfig>(`/api/backups/${id}`).then((r) => r.data),
  create: (payload: CreateBackupPayload) =>
    client.post<BackupConfig>('/api/backups', payload).then((r) => r.data),
  update: (id: number, payload: CreateBackupPayload) =>
    client.put<BackupConfig>(`/api/backups/${id}`, payload).then((r) => r.data),
  delete: (id: number) => client.delete(`/api/backups/${id}`),
  run: (id: number) => client.post(`/api/backups/${id}/run`),
  interrupt: (id: number) => client.post(`/api/backups/${id}/interrupt`)
}

// ── History & Analytics ───────────────────────────────────────────────────────

export const historyApi = {
  getAll: () => client.get<BackupRequest[]>('/api/history').then((r) => r.data),
  getByConfig: (configId: number) =>
    client.get<BackupRequest[]>(`/api/history/${configId}`).then((r) => r.data),
  getRunning: () => client.get<BackupRequest[]>('/api/backups/running').then((r) => r.data),
}

export const translationsApi = {
  getLanguages: () => client.get<{ code: string; label: string }[]>('/api/translations/languages').then((r) => r.data),
  getTranslations: (code: string) =>
    client.get<Record<string, Record<string, string>>>(`/api/translations/${code}`).then((r) => r.data)
}

export const subscriptionApi = {
  getStatus: () => client.get<SubscriptionInfo>('/api/subscription/status').then((r) => r.data)
}

export const analyticsApi = {
  getSnapshot: () => client.get<AnalyticsSnapshot>('/api/analytics').then((r) => r.data)
}

// ── Status ────────────────────────────────────────────────────────────────────

export const statusApi = {
  check: () => client.get('/api/status').then((r) => r.data)
}

// ── Settings ──────────────────────────────────────────────────────────────────

export const settingsApi = {
  get: () => client.get<Record<string, string>>('/api/settings').then((r) => r.data),
  update: (data: Record<string, string>) =>
    client.put('/api/settings', data).then((r) => r.data)
}

// ── App config ────────────────────────────────────────────────────────────────

export const configApi = {
  get: () => client.get<AppConfig>('/api/config').then(r => {
    const d = r.data
    // menuItemsJson is a raw JSON string — parse it into the object
    return { ...d, menuItems: JSON.parse((d as unknown as { menuItemsJson: string }).menuItemsJson ?? '{}') } as AppConfig
  })
}

// ── Logs ──────────────────────────────────────────────────────────────────────

export const logsApi = {
  get: () => client.get<string>('/api/logs', { responseType: 'text' }).then((r) => r.data)
}

export const exportApi = {
  backupsCsv: () => client.get<Blob>('/api/backups/export.csv', { responseType: 'blob' }).then((r) => r.data)
}

/** Saves a blob through the browser download flow (never navigates the Electron window) */
export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
