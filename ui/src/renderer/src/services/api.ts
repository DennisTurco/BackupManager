import axios from 'axios'
import type {
  AnalyticsSnapshot,
  AppConfig,
  BackupConfig,
  BackupRequest,
  CreateBackupPayload,
  User
} from '../types'

const client = axios.create({
  baseURL: 'http://localhost:7070',
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
  run: (id: number) => client.post(`/api/backups/${id}/run`)
}

// ── History & Analytics ───────────────────────────────────────────────────────

export const historyApi = {
  getAll: () => client.get<BackupRequest[]>('/api/history').then((r) => r.data),
  getByConfig: (configId: number) =>
    client.get<BackupRequest[]>(`/api/history/${configId}`).then((r) => r.data),
  getRunning: () => client.get<BackupRequest[]>('/api/backups/running').then((r) => r.data),
}

export const analyticsApi = {
  getSnapshot: () => client.get<AnalyticsSnapshot>('/api/analytics').then((r) => r.data)
}

// ── Auth / User ───────────────────────────────────────────────────────────────

export const authApi = {
  status: () =>
    client.get<{ firstAccess: boolean }>('/api/auth/status').then((r) => r.data),
  getUser: () => client.get<User>('/api/auth/user').then((r) => r.data),
  register: (name: string, surname: string, email: string) =>
    client.post<User>('/api/auth/register', { name, surname, email }).then((r) => r.data)
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
  get: () =>
    axios.get<string>('http://localhost:7070/api/logs', { responseType: 'text' }).then((r) => r.data)
}
