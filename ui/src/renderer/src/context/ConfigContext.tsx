import { createContext, useContext, useEffect, useState } from 'react'
import { configApi } from '../services/api'
import type { AppConfig } from '../types'

const DEFAULT: AppConfig = {
  version: '',
  email: '',
  links: { donatePaypal: '', donateBuymeacoffee: '', infoPage: '', issuePage: '', share: '', website: '' },
  gui: { width: 1280, height: 800, minWidth: 900, minHeight: 600 },
  menuItems: {
    BackupList: true, Dashboard: true, History: true,
    Settings: true, Donate: true, BugReport: true, About: true,
  },
}

const ConfigContext = createContext<AppConfig>(DEFAULT)

export function ConfigProvider({ children }: { children: React.ReactNode }) {
  const [cfg, setCfg] = useState<AppConfig>(DEFAULT)

  useEffect(() => {
    configApi.get().then(setCfg).catch(() => {/* keep defaults */})
  }, [])

  return <ConfigContext.Provider value={cfg}>{children}</ConfigContext.Provider>
}

export const useConfig = () => useContext(ConfigContext)
