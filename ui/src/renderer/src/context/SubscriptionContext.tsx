import { createContext, useContext } from 'react'
import { useQuery } from '@tanstack/react-query'
import { subscriptionApi } from '../services/api'
import type { SubscriptionInfo } from '../types'

interface SubscriptionCtx {
  status: SubscriptionInfo['status']
  validFrom: string | null
  validUntil: string | null
  isLoading: boolean
  // true only when a subscription is required and none is currently valid —
  // gates Pro-only features (Analytics dashboard, priority assistance). Automatic
  // backups follow the same rule server-side (see MainApp.runApiServer).
  isLocked: boolean
}

const SubscriptionContext = createContext<SubscriptionCtx>({
  status: 'NONE',
  validFrom: null,
  validUntil: null,
  isLoading: true,
  isLocked: false,
})

export function SubscriptionProvider({ children }: { children: React.ReactNode }) {
  const { data, isLoading } = useQuery({
    queryKey: ['subscription-status'],
    queryFn: subscriptionApi.getStatus,
    refetchInterval: 60_000,
  })

  const value: SubscriptionCtx = {
    status: data?.status ?? 'NONE',
    validFrom: data?.validFrom ?? null,
    validUntil: data?.validUntil ?? null,
    isLoading,
    isLocked: data?.status === 'EXPIRED',
  }

  return (
    <SubscriptionContext.Provider value={value}>
      {children}
    </SubscriptionContext.Provider>
  )
}

export const useSubscription = () => useContext(SubscriptionContext)
