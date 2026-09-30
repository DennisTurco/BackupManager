import { createContext, useCallback, useContext, useRef, useState } from 'react'
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react'

type ToastKind = 'success' | 'error' | 'info'
interface Toast { id: number; kind: ToastKind; text: string }

interface ToastCtx {
  toast: (text: string, kind?: ToastKind) => void
}

const ToastContext = createContext<ToastCtx>({ toast: () => {} })

const ICONS = { success: CheckCircle2, error: AlertCircle, info: Info }

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const nextId = useRef(1)

  const dismiss = useCallback((id: number) => setToasts(prev => prev.filter(t => t.id !== id)), [])

  const toast = useCallback((text: string, kind: ToastKind = 'success') => {
    const id = nextId.current++
    setToasts(prev => [...prev.slice(-4), { id, kind, text }])
    setTimeout(() => dismiss(id), kind === 'error' ? 7000 : 4500)
  }, [dismiss])

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <div className="toast-stack" role="status" aria-live="polite">
        {toasts.map(t => {
          const Icon = ICONS[t.kind]
          return (
            <div key={t.id} className={`toast toast-${t.kind}`}>
              <Icon size={16} className="toast-icon" />
              <span style={{ flex: 1 }}>{t.text}</span>
              <button className="icon-btn" onClick={() => dismiss(t.id)} aria-label="Dismiss">
                <X size={13} />
              </button>
            </div>
          )
        })}
      </div>
    </ToastContext.Provider>
  )
}

export const useToast = () => useContext(ToastContext)
