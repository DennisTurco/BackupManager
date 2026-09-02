import { createContext, useContext, useEffect, useState } from 'react'

type Mode = 'light' | 'dark' | 'system'
type Theme = 'light' | 'dark'

interface ThemeCtx {
  mode: Mode
  theme: Theme
  setMode: (m: Mode) => void
  toggle: () => void
}

const ThemeContext = createContext<ThemeCtx>({
  mode: 'dark',
  theme: 'dark',
  setMode: () => {},
  toggle: () => {}
})

function getSystemPrefersDark(): boolean {
  try { return window.matchMedia('(prefers-color-scheme: dark)').matches } catch { return true }
}

function loadStoredMode(): Mode {
  try {
    const stored = localStorage.getItem('bm-theme-mode')
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored
    // Migrate the old key, which only ever held 'light' | 'dark'
    const legacy = localStorage.getItem('bm-theme')
    if (legacy === 'light' || legacy === 'dark') return legacy
  } catch { /* ignore */ }
  return 'dark'
}

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [mode, setModeState] = useState<Mode>(loadStoredMode)
  const [systemPrefersDark, setSystemPrefersDark] = useState(getSystemPrefersDark)

  // Track the OS theme live, so 'system' mode updates without needing a restart
  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return
    const mq = window.matchMedia('(prefers-color-scheme: dark)')
    const handler = (e: MediaQueryListEvent) => setSystemPrefersDark(e.matches)
    mq.addEventListener('change', handler)
    return () => mq.removeEventListener('change', handler)
  }, [])

  const theme: Theme = mode === 'system' ? (systemPrefersDark ? 'dark' : 'light') : mode

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
  }, [theme])

  const setMode = (m: Mode) => {
    setModeState(m)
    try { localStorage.setItem('bm-theme-mode', m) } catch { /* ignore */ }
  }

  // Quick toggle (sidebar icon button) flips between light/dark explicitly — picking "system"
  // is a deliberate choice made from Settings, not something a binary toggle should land on.
  const toggle = () => setMode(theme === 'dark' ? 'light' : 'dark')

  return <ThemeContext.Provider value={{ mode, theme, setMode, toggle }}>{children}</ThemeContext.Provider>
}

export const useTheme = () => useContext(ThemeContext)
