import { app, BrowserWindow, Tray, Menu, nativeImage, shell, ipcMain, dialog } from 'electron'
import { join } from 'path'
import { spawn, ChildProcess } from 'child_process'

const API_PORT = 7070
const API_BASE = `http://localhost:${API_PORT}`
const JAR_PATH = app.isPackaged
  ? join(process.resourcesPath, 'backend.jar')
  : join(__dirname, '../../../target/backupmanager-jar-with-dependencies.jar')

let mainWindow: BrowserWindow | null = null
let tray: Tray | null = null
let javaProcess: ChildProcess | null = null

function spawnJavaBackend(): void {
  javaProcess = spawn('java', ['-jar', JAR_PATH, '--api-server'], {
    stdio: ['ignore', 'pipe', 'pipe']
  })

  javaProcess.stdout?.on('data', (data) => process.stdout.write(`[java] ${data}`))
  javaProcess.stderr?.on('data', (data) => process.stderr.write(`[java] ${data}`))

  javaProcess.on('exit', (code) => {
    console.log(`Java backend exited with code ${code}`)
  })
}

async function waitForApi(maxWaitMs = 30_000): Promise<void> {
  const start = Date.now()
  while (Date.now() - start < maxWaitMs) {
    try {
      const res = await fetch(`${API_BASE}/api/status`)
      if (res.ok) return
    } catch {
      // not ready yet
    }
    await new Promise((r) => setTimeout(r, 500))
  }
  throw new Error('Java backend did not start in time')
}

function appIcon(): Electron.NativeImage {
  const iconFile = app.isPackaged
    ? join(process.resourcesPath, 'icon.ico')
    : join(__dirname, '../../resources/icon.ico')
  return nativeImage.createFromPath(iconFile)
}

function createWindow(): void {
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

  mainWindow.on('ready-to-show', () => mainWindow?.show())

  mainWindow.on('close', (e) => {
    e.preventDefault()
    mainWindow?.hide()
  })

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  if (!app.isPackaged) {
    mainWindow.loadURL('http://localhost:5173')
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

app.whenReady().then(async () => {
  spawnJavaBackend()

  try {
    await waitForApi()
  } catch (err) {
    console.error(err)
    app.quit()
    return
  }

  createWindow()
  createTray()
})

app.on('before-quit', () => {
  mainWindow?.destroy()
  javaProcess?.kill()
})

app.on('window-all-closed', () => {
  // keep running in tray on all platforms
})
