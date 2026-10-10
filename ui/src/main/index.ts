import { app, BrowserWindow, Tray, Menu, nativeImage, shell, ipcMain, dialog, Notification, powerMonitor } from 'electron'
import { join } from 'path'
import { existsSync, mkdirSync, writeFileSync } from 'fs'
import { homedir } from 'os'
import { spawn, ChildProcess } from 'child_process'

const API_PORT = 7089
// The backend listens on the IPv4 loopback only: "localhost" may resolve to ::1 first
const API_BASE = `http://127.0.0.1:${API_PORT}`
const ROOT_DIR = app.isPackaged ? process.resourcesPath : join(__dirname, '../../..')
const JAR_PATH = app.isPackaged
  ? join(process.resourcesPath, 'backend.jar')
  : join(ROOT_DIR, 'target/backupmanager-jar-with-dependencies.jar')
// Set when the backend is launched separately (e.g. by the VS Code Java debugger)
const BACKEND_EXTERNAL = !!process.env.BACKEND_EXTERNAL
// Dev only (see ui/scripts/first-launch.mjs): run against a throwaway home, so the backend creates a
// fresh database/logs there and Electron uses a fresh profile — i.e. a simulated first launch
const SANDBOX_HOME = app.isPackaged ? undefined : process.env.BM_SANDBOX_HOME
if (SANDBOX_HOME) app.setPath('userData', join(SANDBOX_HOME, 'electron-profile'))

if (process.env.REMOTE_DEBUGGING_PORT) {
  app.commandLine.appendSwitch('remote-debugging-port', process.env.REMOTE_DEBUGGING_PORT)
}

let mainWindow: BrowserWindow | null = null
let tray: Tray | null = null
let javaProcess: ChildProcess | null = null
let javaExitCode: number | null = null
// Set once the app is quitting (Quit menu, Cmd+Q, OS shutdown/logout): the window must then really
// close instead of hiding in the tray, and the backend exiting is expected rather than a crash
let isQuitting = false
// The backend we spawned answered at least once: from then on, its exit is a crash to recover from
let backendStarted = false
const backendRestartTimes: number[] = []

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

  const sandboxArgs = SANDBOX_HOME ? [`-Duser.home=${SANDBOX_HOME}`] : []
  // --exit-with-parent: the backend exits when our end of its stdin closes, so it can't outlive the
  // app as an orphan even if Electron crashes or is killed
  const child = spawn(
    resolveJavaExecutable(),
    [...sandboxArgs, '-jar', JAR_PATH, '--api-server', '--exit-with-parent'],
    {
      cwd: ROOT_DIR,
      stdio: ['pipe', 'pipe', 'pipe'],
      // Windows: java.exe is a console program, without this it opens a console window
      windowsHide: true
    }
  )
  javaProcess = child

  javaProcess.stdout?.on('data', (data) => process.stdout.write(`[java] ${data}`))
  javaProcess.stderr?.on('data', (data) => process.stderr.write(`[java] ${data}`))

  javaProcess.on('error', (err) => {
    console.error('Unable to start Java backend:', err)
    javaExitCode = -1
  })
  javaProcess.on('exit', (code) => {
    console.log(`Java backend exited with code ${code}`)
    javaExitCode = code ?? -1
    if (javaProcess === child) javaProcess = null
    if (backendStarted && !isQuitting) restartCrashedBackend()
  })
}

// The app keeps running in the tray, possibly for weeks: if the backend dies, automatic backups
// would silently stop. Restart it, but give up on a crash loop instead of respawning forever.
function restartCrashedBackend(): void {
  const now = Date.now()
  const recent = backendRestartTimes.filter((t) => now - t < 10 * 60 * 1000)
  if (recent.length >= 3) {
    new Notification({
      title: 'BackupManager — Backend stopped',
      body: 'The backup service stopped unexpectedly. Restart BackupManager to resume automatic backups.',
      icon: appIcon()
    }).show()
    return
  }
  backendRestartTimes.splice(0, backendRestartTimes.length, ...recent, now)

  setTimeout(async () => {
    if (isQuitting) return
    try {
      javaExitCode = null
      if (!(await isApiUp())) spawnJavaBackend()
      await waitForApi(30_000)
      console.log('Java backend restarted')
    } catch (err) {
      console.error('Unable to restart the Java backend:', err)
    }
  }, 2000)
}

function stopJavaBackend(): void {
  if (!javaProcess) return
  // Closing stdin makes the backend exit on its own, running its shutdown hooks (see --exit-with-parent)
  javaProcess.stdin?.end()
  // SIGTERM is graceful too on Linux/macOS; on Windows kill() is a hard TerminateProcess, so rely on stdin
  if (process.platform !== 'win32') javaProcess.kill()
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
    const notifyOnComplete = settings.NOTIFY_ON_COMPLETE !== 'false' // default on
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

// nativeImage reads .ico only on Windows: Linux and macOS need the PNG
function appIcon(): Electron.NativeImage {
  const iconName = process.platform === 'win32' ? 'icon.ico' : 'icon.png'
  const iconFile = app.isPackaged
    ? join(process.resourcesPath, iconName)
    : join(__dirname, '../../resources', iconName)
  return nativeImage.createFromPath(iconFile)
}

// The tray image is shown at its pixel size in the macOS menu bar (no automatic scaling like on
// Windows/Linux): shrink it to the standard 18pt height, with a 2x representation for Retina
function trayIcon(): Electron.NativeImage {
  const icon = appIcon()
  if (process.platform !== 'darwin') return icon
  const image = nativeImage.createEmpty()
  for (const scaleFactor of [1, 2]) {
    const resized = icon.resize({ height: 18 * scaleFactor, quality: 'best' })
    image.addRepresentation({ scaleFactor, buffer: resized.toPNG() })
  }
  return image
}

// Counterpart of the autostart entry the Windows installer creates: added once, on the first
// launch, so removing it from the system's startup apps is respected afterwards
function ensureAutostart(): void {
  if (process.platform === 'win32' || !app.isPackaged) return
  const marker = join(app.getPath('userData'), 'autostart-configured')
  if (existsSync(marker)) {
    if (process.platform === 'darwin') migrateMacLoginItem()
    return
  }
  try {
    if (process.platform === 'darwin') {
      writeMacLaunchAgent()
    } else {
      writeLinuxAutostartEntry()
    }
    writeFileSync(marker, '')
  } catch (err) {
    console.error('Unable to create the autostart entry:', err)
  }
}

// Same value as build.appId in package.json, i.e. the bundle identifier of the macOS app
const MAC_BUNDLE_ID = 'io.github.dennisturco.backupmanager'

// A login item (app.setLoginItemSettings) gets no arguments, and from macOS 13 on
// wasOpenedAtLogin is always false, so the app couldn't tell a login launch from a manual one.
// A LaunchAgent can pass --background. It starts the app through LaunchServices by bundle id
// rather than by path, so it keeps working if the app is moved, or was first opened from the
// DMG or from a translocated (quarantined) copy. "-g" keeps it from taking the focus.
function writeMacLaunchAgent(): void {
  const agentsDir = join(homedir(), 'Library', 'LaunchAgents')
  const args = ['/usr/bin/open', '-g', '-b', MAC_BUNDLE_ID, '--args', '--background']
  mkdirSync(agentsDir, { recursive: true })
  writeFileSync(
    join(agentsDir, `${MAC_BUNDLE_ID}.plist`),
    [
      '<?xml version="1.0" encoding="UTF-8"?>',
      '<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">',
      '<plist version="1.0">',
      '<dict>',
      '  <key>Label</key>',
      `  <string>${MAC_BUNDLE_ID}</string>`,
      '  <key>ProgramArguments</key>',
      '  <array>',
      ...args.map((arg) => `    <string>${arg}</string>`),
      '  </array>',
      // Started once at login; no KeepAlive: "open" exits right away, and quitting from the tray must stick
      '  <key>RunAtLoad</key>',
      '  <true/>',
      '</dict>',
      '</plist>',
      ''
    ].join('\n')
  )
}

// Earlier macOS builds registered a login item instead: replace it with the LaunchAgent, unless the
// user had already turned it off (then neither is enabled, as they chose)
function migrateMacLoginItem(): void {
  if (!app.getLoginItemSettings().openAtLogin) return
  try {
    writeMacLaunchAgent()
    app.setLoginItemSettings({ openAtLogin: false })
  } catch (err) {
    console.error('Unable to replace the login item with a LaunchAgent:', err)
  }
}

// Electron's login item API is Windows/macOS only: Linux desktops follow the XDG autostart spec
function writeLinuxAutostartEntry(): void {
  const configHome = process.env.XDG_CONFIG_HOME || join(homedir(), '.config')
  const autostartDir = join(configHome, 'autostart')
  // An AppImage runs from a temporary mount: the stable path is the .AppImage file itself.
  // Packaged builds run as "<name>.bin" behind the sandbox-detecting launcher script
  // (build/linux-after-pack.cjs): autostart goes through the launcher too.
  const exe = process.env.APPIMAGE ?? process.execPath.replace(/\.bin$/, '')
  mkdirSync(autostartDir, { recursive: true })
  writeFileSync(
    join(autostartDir, 'backupmanager.desktop'),
    [
      '[Desktop Entry]',
      'Type=Application',
      'Name=BackupManager',
      `Exec="${exe}" --background`,
      // Installed by the .deb under the executable name (an AppImage has none: the entry just has no icon)
      'Icon=backupmanager',
      'X-GNOME-Autostart-enabled=true',
      'Terminal=false',
      ''
    ].join('\n')
  )
}

// macOS keeps a running app in the Dock even with every window hidden: BackupManager should look
// like a tray-only app while it runs in the background, so the Dock icon follows the window
function updateDockVisibility(): void {
  if (process.platform !== 'darwin') return
  if (mainWindow && !mainWindow.isDestroyed() && mainWindow.isVisible()) {
    void app.dock.show()
  } else {
    app.dock.hide()
  }
}

function showMainWindow(): void {
  if (!mainWindow) return
  // macOS: bring the Dock icon back before showing, otherwise the window can open behind other apps
  if (process.platform === 'darwin') void app.dock.show()
  if (mainWindow.isMinimized()) mainWindow.restore()
  mainWindow.show()
  mainWindow.focus()
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
    if (isQuitting) return
    e.preventDefault()
    mainWindow?.hide()
  })

  // Windows shutdown/restart/sign-out doesn't go through before-quit: a window that keeps cancelling
  // its close would show up as "this app is preventing shutdown"
  mainWindow.on('session-end', () => {
    isQuitting = true
    stopJavaBackend()
    app.quit()
  })

  mainWindow.on('show', updateDockVisibility)
  mainWindow.on('hide', updateDockVisibility)

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
  tray = new Tray(trayIcon())
  tray.setToolTip('BackupManager')
  tray.setContextMenu(
    Menu.buildFromTemplate([
      { label: 'Open', click: showMainWindow },
      { type: 'separator' },
      { label: 'Quit', click: () => app.quit() }
    ])
  )
  tray.on('double-click', showMainWindow)
  // Windows users expect a single click on the tray icon to open the app (right click opens the menu).
  // Linux AppIndicators only support the menu; on macOS a click opens the menu.
  if (process.platform === 'win32') tray.on('click', showMainWindow)
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
  app.on('second-instance', (_event, argv) => {
    // An autostart launch while the app is already running in the tray shouldn't pop the window up
    if (!argv.includes('--background')) showMainWindow()
  })
  // macOS: clicking the app in the Dock/Finder while it runs in the tray
  app.on('activate', showMainWindow)
}

// Windows only shows notifications for an app whose ID matches its Start Menu shortcut
// (AppUserModelID in installer/BackupManager_common.iss); same value as build.appId in package.json
if (process.platform === 'win32') app.setAppUserModelId('io.github.dennisturco.backupmanager')

app.whenReady().then(async () => {
  if (!app.hasSingleInstanceLock()) return
  try {
    // Reuse a backend that is already running (external/debugged one or leftover instance)
    if (SANDBOX_HOME && (await isApiUp())) {
      // Reusing it would mean working on the real database instead of the sandbox
      throw new Error(`A BackupManager backend is already running on ${API_BASE}.\n\nClose it before simulating a first launch.`)
    }
    if (!BACKEND_EXTERNAL && !(await isApiUp())) spawnJavaBackend()
    await waitForApi(BACKEND_EXTERNAL ? 90_000 : 30_000)
    backendStarted = javaProcess !== null
  } catch (err) {
    console.error(err)
    dialog.showErrorBox('BackupManager — backend unavailable', String((err as Error).message ?? err))
    app.quit()
    return
  }

  const settings = await getSettings()
  // --background is passed by the autostart entry (Windows installer / Linux .desktop file /
  // macOS LaunchAgent): start in the tray
  const startInBackground = process.argv.includes('--background')
  const startHidden = startInBackground || settings.START_MINIMIZED === 'true'
  createWindow(startHidden)
  // Started hidden: no "show" event will fire, hide the Dock icon now
  if (startHidden) updateDockVisibility()
  createTray()
  ensureAutostart()

  checkSubscriptionStatus()
  setInterval(checkSubscriptionStatus, 6 * 60 * 60 * 1000) // re-check every 6 hours

  checkBackupCompletions()
  setInterval(checkBackupCompletions, 3000)

  // Linux/macOS shutdown or logout: quit cleanly instead of being killed with the backend mid-write
  powerMonitor.on('shutdown', () => app.quit())
})

app.on('before-quit', () => {
  isQuitting = true
  mainWindow?.destroy()
  stopJavaBackend()
})

app.on('window-all-closed', () => {
  // keep running in tray on all platforms
})
