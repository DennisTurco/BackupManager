import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from './context/ThemeContext'
import { ConfigProvider } from './context/ConfigContext'
import { TranslationProvider } from './context/TranslationContext'
import { SubscriptionProvider } from './context/SubscriptionContext'
import { ToastProvider } from './context/ToastContext'
import App from './App'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, retry: 1 } }
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider>
      <QueryClientProvider client={queryClient}>
        <ConfigProvider>
          <TranslationProvider>
            <SubscriptionProvider>
              <ToastProvider>
                <App />
              </ToastProvider>
            </SubscriptionProvider>
          </TranslationProvider>
        </ConfigProvider>
      </QueryClientProvider>
    </ThemeProvider>
  </React.StrictMode>
)
