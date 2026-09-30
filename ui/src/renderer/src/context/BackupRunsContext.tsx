import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { backupApi, historyApi } from '../services/api'
import { useToast } from './ToastContext'
import { useTranslation } from './TranslationContext'
import type { BackupConfig, BackupRequest } from '../types'

interface BackupRunsCtx {
  backups: BackupConfig[]
  backupsLoading: boolean
  running: BackupRequest[]
  progressById: Map<number, number>
  /** running on the server, or triggered from this client and not yet confirmed finished */
  busyIds: Set<number>
  trackRun: (configId: number) => void
}

const BackupRunsContext = createContext<BackupRunsCtx>({
  backups: [],
  backupsLoading: true,
  running: [],
  progressById: new Map(),
  busyIds: new Set(),
  trackRun: () => {},
})

const PENDING_TIMEOUT_MS = 3 * 60_000

/**
 * Single source of truth for backup configs and running state, plus completion toasts.
 * A fast backup can start and finish between two polls of the running list, so completions are
 * detected from three signals (a config leaving the running list, its lastBackupDate changing, or
 * a run triggered here) and deduplicated by backupRequestId.
 */
export function BackupRunsProvider({ children }: { children: React.ReactNode }) {
  const qc = useQueryClient()
  const { toast } = useToast()
  const { t } = useTranslation()

  const { data: backups = [], isLoading: backupsLoading } = useQuery({
    queryKey: ['backups'],
    queryFn: backupApi.getAll,
    refetchInterval: 5000,
  })

  const [pending, setPending] = useState<Record<number, number>>({})
  const hasPending = Object.keys(pending).length > 0

  const { data: running = [] } = useQuery({
    queryKey: ['backups-running'],
    queryFn: historyApi.getRunning,
    refetchInterval: hasPending ? 700 : 1500,
  })

  const progressById = useMemo(() => {
    const map = new Map<number, number>()
    for (const r of running) map.set(r.backupConfigurationId, r.progress ?? 0)
    return map
  }, [running])

  const busyIds = useMemo(() => {
    const set = new Set(progressById.keys())
    for (const id of Object.keys(pending)) set.add(Number(id))
    return set
  }, [progressById, pending])

  const notifiedRequests = useRef(new Set<number>())
  const backupsRef = useRef(backups)
  backupsRef.current = backups

  const clearPending = (id: number) =>
    setPending(prev => {
      if (!(id in prev)) return prev
      const next = { ...prev }
      delete next[id]
      return next
    })

  /** Looks up the latest finished run of a config and toasts it once. Returns true when found. */
  const reportLatest = useCallback(async (configId: number, since: number): Promise<boolean> => {
    let history: BackupRequest[]
    try {
      history = await historyApi.getByConfig(configId)
    } catch {
      return false
    }
    const latest = history
      .filter(r => new Date(r.startedDate).getTime() >= since)
      .sort((a, b) => new Date(b.startedDate).getTime() - new Date(a.startedDate).getTime())[0]
    if (!latest || latest.status === 'IN_PROGRESS') return false

    if (!notifiedRequests.current.has(latest.backupRequestId)) {
      notifiedRequests.current.add(latest.backupRequestId)
      const name = backupsRef.current.find(b => b.id === configId)?.name ?? `#${configId}`
      if (latest.status === 'FINISHED') {
        toast(t('ReactUI.BackupRunSuccess', 'Backup "{name}" completed successfully').replace('{name}', name), 'success')
      } else {
        toast(t('ReactUI.BackupRunFailedResult', 'Backup "{name}" failed').replace('{name}', name), 'error')
      }
      qc.invalidateQueries({ queryKey: ['backups'] })
      qc.invalidateQueries({ queryKey: ['history'] })
      qc.invalidateQueries({ queryKey: ['analytics'] })
    }
    return true
  }, [qc, t, toast])

  // Signal 1: a config left the running list
  const prevRunning = useRef<Set<number> | null>(null)
  useEffect(() => {
    const current = new Set(running.map(r => r.backupConfigurationId))
    if (prevRunning.current) {
      for (const id of prevRunning.current) {
        if (!current.has(id)) reportLatest(id, Date.now() - 24 * 3_600_000)
      }
    }
    prevRunning.current = current
  }, [running, reportLatest])

  // Signal 2: lastBackupDate changed (catches scheduler runs shorter than a poll)
  const prevDates = useRef<Map<number, string | null> | null>(null)
  useEffect(() => {
    if (prevDates.current) {
      for (const b of backups) {
        const before = prevDates.current.get(b.id)
        if (before !== undefined && before !== b.lastBackupDate && b.lastBackupDate) {
          reportLatest(b.id, Date.now() - 24 * 3_600_000)
        }
      }
    }
    prevDates.current = new Map(backups.map(b => [b.id, b.lastBackupDate]))
  }, [backups, reportLatest])

  // Signal 3: runs triggered from this client
  useEffect(() => {
    if (!hasPending) return
    const timer = setInterval(async () => {
      for (const [idStr, triggeredAt] of Object.entries(pending)) {
        const id = Number(idStr)
        if (Date.now() - triggeredAt > PENDING_TIMEOUT_MS) { clearPending(id); continue }
        if (progressById.has(id)) continue // still running, wait for it
        if (await reportLatest(id, triggeredAt - 2000)) clearPending(id)
      }
    }, 700)
    return () => clearInterval(timer)
  }, [pending, hasPending, progressById, reportLatest])

  const trackRun = useCallback((configId: number) => {
    setPending(prev => ({ ...prev, [configId]: Date.now() }))
    qc.invalidateQueries({ queryKey: ['backups-running'] })
  }, [qc])

  return (
    <BackupRunsContext.Provider value={{ backups, backupsLoading, running, progressById, busyIds, trackRun }}>
      {children}
    </BackupRunsContext.Provider>
  )
}

export const useBackupRuns = () => useContext(BackupRunsContext)
