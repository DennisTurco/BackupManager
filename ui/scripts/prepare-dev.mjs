// Makes `npm run dev` / VS Code "Run and Debug" plug-and-play:
//   --deps     install UI dependencies if missing or broken
//   --backend  (re)build the backend jar only when Java sources are newer than it
// Set BACKEND_EXTERNAL=1 to skip the backend build (backend launched by the IDE).
import { execSync } from 'child_process'
import { existsSync, readdirSync, statSync } from 'fs'
import { join, resolve } from 'path'
import { fileURLToPath } from 'url'

const uiDir = resolve(fileURLToPath(import.meta.url), '../..')
const rootDir = resolve(uiDir, '..')
const isWin = process.platform === 'win32'
const args = new Set(process.argv.slice(2))

function run(cmd, cwd) {
  console.log(`[prepare-dev] ${cmd}`)
  execSync(cmd, { cwd, stdio: 'inherit' })
}

function ensureDeps() {
  const bin = join(uiDir, 'node_modules', '.bin', isWin ? 'electron-vite.cmd' : 'electron-vite')
  const electronExe = join(uiDir, 'node_modules', 'electron', 'dist', isWin ? 'electron.exe' : 'electron')
  if (existsSync(bin) && existsSync(electronExe)) return
  console.log('[prepare-dev] UI dependencies missing or incomplete, installing...')
  try {
    run('npm install --no-audit --no-fund', uiDir)
  } catch {
    console.error(
      '\n[prepare-dev] npm install failed. If the error above is EBUSY on an electron .asar file,\n' +
        'another Electron app (often VS Code itself) holds it open: close VS Code completely,\n' +
        'run "npm install" in ui/ from an external terminal, then reopen VS Code.\n'
    )
    process.exit(1)
  }
}

function newestMtime(path) {
  const stat = statSync(path)
  if (!stat.isDirectory()) return stat.mtimeMs
  let newest = 0
  for (const entry of readdirSync(path)) {
    newest = Math.max(newest, newestMtime(join(path, entry)))
  }
  return newest
}

function ensureBackend() {
  if (process.env.BACKEND_EXTERNAL) {
    console.log('[prepare-dev] BACKEND_EXTERNAL set, skipping backend build')
    return
  }
  const jar = join(rootDir, 'target', 'backupmanager-jar-with-dependencies.jar')
  const sourcesMtime = Math.max(
    newestMtime(join(rootDir, 'src', 'main', 'java')),
    newestMtime(join(rootDir, 'pom.xml'))
  )
  if (existsSync(jar) && statSync(jar).mtimeMs >= sourcesMtime) {
    console.log('[prepare-dev] backend jar is up to date')
    return
  }
  console.log('[prepare-dev] backend jar missing or stale, building...')
  run(`"${join(rootDir, isWin ? 'mvnw.cmd' : 'mvnw')}" package -DskipTests -q`, rootDir)
}

if (args.has('--deps')) ensureDeps()
if (args.has('--backend')) ensureBackend()
