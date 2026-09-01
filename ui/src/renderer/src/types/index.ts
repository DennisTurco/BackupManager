export interface TimeInterval {
  days: number
  hours: number
  minutes: number
}

export interface BackupConfig {
  id: number
  name: string
  targetPath: string
  destinationPath: string
  lastBackupDate: string | null
  automatic: boolean
  nextBackupDate: string | null
  timeIntervalBackup: TimeInterval | null
  notes: string | null
  creationDate: string
  lastUpdateDate: string
  count: number
  maxToKeep: number
}

export interface BackupRequest {
  backupRequestId: number
  backupConfigurationId: number
  startedDate: string
  completionDate: string | null
  status: 'IN_PROGRESS' | 'FINISHED' | 'TERMINATED'
  progress: number
  triggeredBy: 'USER' | 'SCHEDULER' | 'API'
  durationMs: number | null
  outputPath: string | null
  unzippedTargetSize: number
  zippedTargetSize: number | null
  filesCount: number
  errorMessage: string | null
}

export interface SubscriptionInfo {
  status: 'NONE' | 'ACTIVE' | 'EXPIRATION' | 'EXPIRED'
  validFrom: string | null
  validUntil: string | null
}

export interface AnalyticsSnapshot {
  totalRequests: number
  successCount: number
  failedCount: number
  successRate: number
  avgDurationMs: number
  avgCompressionRate: number
  totalDiskUsageBytes: number
  durationTrend: Record<string, number>
}

export interface User {
  id: number
  name: string
  surname: string
  email: string
  language: string
}

export interface AppConfig {
  version: string
  email: string
  links: {
    donatePaypal: string
    donateBuymeacoffee: string
    infoPage: string
    issuePage: string
    share: string
    website: string
  }
  gui: { width: number; height: number; minWidth: number; minHeight: number }
  menuItems: {
    BugReport?: boolean
    Settings?: boolean
    Donate?: boolean
    PaypalDonate?: boolean
    BuymeacoffeeDonate?: boolean
    History?: boolean
    InfoPage?: boolean
    Import?: boolean
    Export?: boolean
    Support?: boolean
    ContactUs?: boolean
    Website?: boolean
    BackupList?: boolean
    Dashboard?: boolean
    About?: boolean
  }
}

export type CreateBackupPayload = {
  name: string
  targetPath: string
  destinationPath: string
  automatic: boolean
  timeIntervalBackup: TimeInterval | null
  notes: string
  maxToKeep: number
}
