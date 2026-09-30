import { app, BrowserWindow, Tray, Menu, nativeImage, shell, ipcMain, dialog, Notification } from 'electron'
import { join } from 'path'
import { existsSync } from 'fs'
import { spawn, ChildProcess } from 'child_process'

const API_PORT = 7089
const API_BASE = `http://localhost:${API_PORT}`
const ROOT_DIR = app.isPackaged ? process.resourcesPath : join(__dirname, '../../..')
const JAR_PATH = app.isPackaged
  ? join(process.resourcesPath, 'backend.jar')
  : join(ROOT_DIR, 'target/backupmanager-jar-with-dependencies.jar')
// Set when the backend is launched separately (e.g. by the VS Code Java debugger)
const BACKEND_EXTERNAL = !!process.env.BACKEND_EXTERNAL

if (process.env.REMOTE_DEBUGGING_PORT) {
  app.commandLine.appendSwitch('remote-debugging-port', process.env.REMOTE_DEBUGGING_PORT)
}

let mainWindow: BrowserWindow | null = null
let tray: Tray | null = null
let javaProcess: ChildProcess | null = null
let javaExitCode: number | null = null

// Prefer the bundled JRE, then JAVA_HOME, then whatever `java` is on PATH
function resolveJavaExecutable(): string {
  const exe = process.platform === 'win32' ? 'java.exe' : 'java'
  const candidates = [join(ROOT_DIR, 'jre', 'bin', exe)]
  if (process.env.JAVA_HOME) candidates.push(join(process.env.JAVA_HOME, 'bin', exe))
  return candidates.find((p) => existsSync(p)) ?? 'java'
}

function spawnJavaBackend(): void {
  if (!existsSync(JAR_PATH)) {
    throw new Error(`Backend jar not found at:\n${JAR_PATH}\n\nBuild it with: mvnw package -DskipTests`)
  }

  javaProcess = spawn(resolveJavaExecutable(), ['-jar', JAR_PATH, '--api-server'], {
    cwd: ROOT_DIR,
    stdio: ['ignore', 'pipe', 'pipe']
  })

  javaProcess.stdout?.on('data', (data) => process.stdout.write(`[java] ${data}`))
  javaProcess.stderr?.on('data', (data) => process.stderr.write(`[java] ${data}`))

  javaProcess.on('error', (err) => {
    console.error('Unable to start Java backend:', err)
    javaExitCode = -1
  })
  javaProcess.on('exit', (code) => {
    console.log(`Java backend exited with code ${code}`)
    javaExitCode = code ?? -1
  })
}

async function isApiUp(): Promise<boolean> {
  try {
    const res = await fetch(`${API_BASE}/api/status`)
    return res.ok
  } catch {
    return false
  }
}

async function waitForApi(maxWaitMs: number): Promise<void> {
  const start = Date.now()
  while (Date.now() - start < maxWaitMs) {
    if (await isApiUp()) return
    if (javaExitCode !== null) {
      throw new Error(`Java backend exited during startup (code ${javaExitCode})`)
    }
    await new Promise((r) => setTimeout(r, 500))
  }
  throw new Error(
    BACKEND_EXTERNAL
      ? `No backend answered on ${API_BASE} (BACKEND_EXTERNAL is set, start it separately)`
      : 'Java backend did not start in time'
  )
}

let lastNotifiedSubscriptionStatus: string | null = null

async function checkSubscriptionStatus(): Promise<void> {
  try {
    const res = await fetch(`${API_BASE}/api/subscription/status`)
    if (!res.ok) return
    const { status } = (await res.json()) as { status: string; validUntil: string | null }

    // Only notify once per status change, not on every poll
    if (status === lastNotifiedSubscriptionStatus) return
    lastNotifiedSubscriptionStatus = status

    if (status === 'EXPIRED') {
      new Notification({
        title: 'BackupManager — Subscription expired',
        body: 'Automatic backups are paused until you renew. Manual backups are still available.',
        icon: appIcon()
      }).show()
    } else if (status === 'EXPIRATION') {
      new Notification({
        title: 'BackupManager — Subscription expiring soon',
        body: 'Renew soon to keep automatic backups running without interruption.',
        icon: appIcon()
      }).show()
    }
  } catch {
    // API not reachable yet — ignore, next poll will retry
  }
}

interface AppSettings {
  NOTIFY_ON_COMPLETE?: string
  NOTIFY_ON_FAILURE?: string
  START_MINIMIZED?: string
}

async function getSettings(): Promise<AppSettings> {
  try {
    const res = await fetch(`${API_BASE}/api/settings`)
    if (!res.ok) return {}
    return (await res.json()) as AppSettings
  } catch {
    return {}
  }
}

// Tracks configId → whether it was seen running on the previous poll, so we can detect the
// exact moment a backup goes from running to not-running and look up how it actually ended.
let previouslyRunningConfigIds = new Set<number>()

async function checkBackupCompletions(): Promise<void> {
  try {
    const settings = await getSettings()
    const notifyOnComplete = settings.NOTIFY_ON_COMPLETE === 'true'
    const notifyOnFailure = settings.NOTIFY_ON_FAILURE !== 'false' // default on
    if (!notifyOnComplete && !notifyOnFailure) {
      previouslyRunningConfigIds = new Set()
      return
    }

    const runningRes = await fetch(`${API_BASE}/api/backups/running`)
    if (!runningRes.ok) return
    const running = (await runningRes.json()) as { backupConfigurationId: number }[]
    const currentlyRunning = new Set(running.map((r) => r.backupConfigurationId))

    for (const configId of previouslyRunningConfigIds) {
      if (currentlyRunning.has(configId)) continue

      // This one just stopped running — find out how it ended
      const [backupRes, historyRes] = await Promise.all([
        fetch(`${API_BASE}/api/backups/${configId}`),
        fetch(`${API_BASE}/api/history/${configId}`)
      ])
      if (!backupRes.ok || !historyRes.ok) continue
      const backup = (await backupRes.json()) as { name: string }
      const history = (await historyRes.json()) as { status: string; startedDate: string }[]
      const latest = history.sort(
        (a, b) => new Date(b.startedDate).getTime() - new Date(a.startedDate).getTime()
      )[0]
      if (!latest) continue

      if (latest.status === 'FINISHED' && notifyOnComplete) {
        new Notification({
          title: 'BackupManager',
          body: `Backup "${backup.name}" completed successfully.`,
          icon: appIcon()
        }).show()
      } else if (latest.status === 'TERMINATED' && notifyOnFailure) {
        new Notification({
          title: 'BackupManager',
          body: `Backup "${backup.name}" failed or was interrupted.`,
          icon: appIcon()
        }).show()
      }
    }

    previouslyRunningConfigIds = currentlyRunning
  } catch {
    // API not reachable — ignore, next poll will retry
  }
}

function appIcon(): Electron.NativeImage {
  const iconFile = app.isPackaged
    ? join(process.resourcesPath, 'icon.ico')
    : join(__dirname, '../../resources/icon.ico')
  return nativeImage.createFromPath(iconFile)
}

function createWindow(startMinimized: boolean): void {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 900,
    minHeight: 600,
    show: false,
    autoHideMenuBar: true,
    icon: appIcon(),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false,
      contextIsolation: true
    }
  })

  mainWindow.on('ready-to-show', () => {
    if (!startMinimized) mainWindow?.show()
  })

  mainWindow.on('close', (e) => {
    e.preventDefault()
    mainWindow?.hide()
  })

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  if (!app.isPackaged) {
    // electron-vite exposes the actual dev server URL (the port shifts when 5173 is taken)
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'] ?? 'http://localhost:5173')
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
}

function createTray(): void {
  tray = new Tray(appIcon())
  tray.setToolTip('BackupManager')
  tray.setContextMenu(
    Menu.buildFromTemplate([
      { label: 'Open', click: () => mainWindow?.show() },
      { type: 'separator' },
      { label: 'Quit', click: () => { mainWindow?.destroy(); app.quit() } }
    ])
  )
  tray.on('double-click', () => mainWindow?.show())
}

ipcMain.handle('dialog:openFolder', async () => {
  const result = await dialog.showOpenDialog({ properties: ['openDirectory'] })
  return result.canceled ? null : result.filePaths[0]
})

ipcMain.handle('shell:openPath', (_event, path: string) => shell.openPath(path))

// A second launch just brings the existing window forward instead of opening another tray app
if (!app.requestSingleInstanceLock()) {
  app.quit()
} else {
  app.on('second-instance', () => {
    if (!mainWindow) return
    if (mainWindow.isMinimized()) mainWindow.restore()
    mainWindow.show()
    mainWindow.focus()
  })
}

app.whenReady().then(async () => {
  if (!app.hasSingleInstanceLock()) return
  try {
    // Reuse a backend that is already running (external/debugged one or leftover instance)
    if (!BACKEND_EXTERNAL && !(await isApiUp())) spawnJavaBackend()
    await waitForApi(BACKEND_EXTERNAL ? 90_000 : 30_000)
  } catch (err) {
    console.error(err)
    dialog.showErrorBox('BackupManager — backend unavailable', String((err as Error).message ?? err))
    app.quit()
    return
  }

  const settings = await getSettings()
  createWindow(settings.START_MINIMIZED === 'true')
  createTray()

  checkSubscriptionStatus()
  setInterval(checkSubscriptionStatus, 6 * 60 * 60 * 1000) // re-check every 6 hours

  checkBackupCompletions()
  setInterval(checkBackupCompletions, 3000)
})

app.on('before-quit', () => {
  mainWindow?.destroy()
  javaProcess?.kill()
})

app.on('window-all-closed', () => {
  // keep running in tray on all platforms
})
