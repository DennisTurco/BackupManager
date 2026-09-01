import { createContext, useContext } from 'react'
import { useQuery } from '@tanstack/react-query'
import { settingsApi, translationsApi } from '../services/api'

type Dict = Map<string, string>

interface TranslationCtx {
  t: (key: string, fallback: string) => string
  language: string
}

const TranslationContext = createContext<TranslationCtx>({
  t: (_key, fallback) => fallback,
  language: 'en',
})

function flatten(raw: Record<string, Record<string, string>>): Dict {
  const dict: Dict = new Map()
  for (const [category, entries] of Object.entries(raw)) {
    for (const [key, value] of Object.entries(entries)) {
      dict.set(`${category}.${key}`, value)
    }
  }
  return dict
}

export function TranslationProvider({ children }: { children: React.ReactNode }) {
  // Shares the ['settings'] cache with SettingsPage — when the user saves a new
  // language there, this query is invalidated too, so translations refresh automatically.
  const { data: settings } = useQuery({
    queryKey: ['settings'],
    queryFn: settingsApi.get,
  })
  const language = settings?.['LANGUAGE'] || 'en'

  const { data: raw } = useQuery({
    queryKey: ['translations', language],
    queryFn: () => translationsApi.getTranslations(language),
  })
  const dict = raw ? flatten(raw) : new Map<string, string>()

  // key is "Category.Key" (matches the backend's res/languages/*.json structure)
  const t = (key: string, fallback: string) => dict.get(key) ?? fallback

  return (
    <TranslationContext.Provider value={{ t, language }}>
      {children}
    </TranslationContext.Provider>
  )
}

export const useTranslation = () => useContext(TranslationContext)
