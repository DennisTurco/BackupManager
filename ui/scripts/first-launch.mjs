// Simulates a first launch of the app without touching your real data.
//   node scripts/first-launch.mjs            fresh sandbox, standard edition
//   node scripts/first-launch.mjs --demo     fresh sandbox + demo marker (like the demo installer)
//   node scripts/first-launch.mjs --keep     reuse the previous sandbox (test the *second* launch)
// Any other argument is forwarded to `npm run dev` (e.g. --sourcemap).
//
// The sandbox is a throwaway home folder: the backend runs with -Duser.home=<sandbox>, so its
// database (Documents\Shard\data), logs (.backupmanager\logs) and the Electron profile all live there.
import { spawn } from 'child_process'
import { mkdirSync, rmSync, writeFileSync } from 'fs'
import { join, resolve } from 'path'
import { fileURLToPath } from 'url'

const uiDir = resolve(fileURLToPath(import.meta.url), '../..')
const args = process.argv.slice(2)
const demo = args.includes('--demo')
const keep = args.includes('--keep')
const devArgs = args.filter(a => a !== '--demo' && a !== '--keep')

const sandbox = join(uiDir, '.sandbox', demo ? 'demo' : 'standard')
const dataDir = join(sandbox, 'Documents', 'Shard', 'data')

if (!keep) rmSync(sandbox, { recursive: true, force: true })
mkdirSync(dataDir, { recursive: true })
// Same marker the demo installer writes; the backend applies it only when it creates a new database
if (demo && !keep) writeFileSync(join(dataDir, '.demo-init'), 'demo')

console.log(`[first-launch] ${keep ? 'reusing' : 'fresh'} ${demo ? 'demo' : 'standard'} sandbox: ${sandbox}`)

const child = spawn('npm', ['run', 'dev', ...(devArgs.length ? ['--', ...devArgs] : [])], {
  cwd: uiDir,
  stdio: 'inherit',
  shell: true,
  env: { ...process.env, BM_SANDBOX_HOME: sandbox },
})
child.on('exit', code => process.exit(code ?? 0))
