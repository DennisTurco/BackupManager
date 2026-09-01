import { useState, useEffect, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { RefreshCw, Download, ArrowDown } from 'lucide-react'
import { logsApi } from '../services/api'

export default function HistoryPage() {
  const [autoScroll, setAutoScroll] = useState(true)
  const bottomRef = useRef<HTMLDivElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  const { data: logContent = '', isLoading, isFetching, refetch } = useQuery({
    queryKey: ['logs'],
    queryFn: logsApi.get,
    refetchInterval: 10_000,
  })

  useEffect(() => {
    if (autoScroll && bottomRef.current) {
      bottomRef.current.scrollIntoView({ behavior: 'smooth' })
    }
  }, [logContent, autoScroll])

  const handleScroll = () => {
    const el = containerRef.current
    if (!el) return
    const atBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 40
    setAutoScroll(atBottom)
  }

  const lines = logContent ? logContent.split('\n') : []

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 0 }}>
      {/* Header */}
      <div className="page-header">
        <div>
          <div className="page-title">Application Log</div>
          <div className="page-desc">Live view of the backup manager log file</div>
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {isFetching && (
            <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>Refreshing…</span>
          )}
          <button className="btn btn-ghost" onClick={() => refetch()} disabled={isFetching}>
            <RefreshCw size={13} className={isFetching ? 'spin' : ''} />
            Refresh
          </button>
          <button
            className="btn btn-ghost"
            onClick={() => setAutoScroll(v => !v)}
            style={{ color: autoScroll ? 'var(--accent)' : undefined }}
          >
            <ArrowDown size={13} />
            {autoScroll ? 'Auto-scroll on' : 'Auto-scroll off'}
          </button>
          {logContent && (
            <button
              className="btn btn-ghost"
              onClick={() => {
                const blob = new Blob([logContent], { type: 'text/plain' })
                const a = document.createElement('a')
                a.href = URL.createObjectURL(blob)
                a.download = 'backup-manager.log'
                a.click()
                URL.revokeObjectURL(a.href)
              }}
            >
              <Download size={13} /> Download
            </button>
          )}
        </div>
      </div>

      {/* Log viewer */}
      <div
        ref={containerRef}
        onScroll={handleScroll}
        className="card"
        style={{
          flex: 1,
          overflow: 'auto',
          padding: 0,
          fontFamily: 'Consolas, "Courier New", monospace',
          fontSize: 12,
          lineHeight: 1.6,
        }}
      >
        {isLoading ? (
          <div style={{ padding: 32, textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading log…
          </div>
        ) : lines.length === 0 ? (
          <div style={{ padding: 32, textAlign: 'center', color: 'var(--text-muted)' }}>
            Log file is empty.
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <tbody>
              {lines.map((line, i) => (
                <LogLine key={i} number={i + 1} text={line} />
              ))}
            </tbody>
          </table>
        )}
        <div ref={bottomRef} />
      </div>
    </div>
  )
}

function LogLine({ number, text }: { number: number; text: string }) {
  const level = detectLevel(text)
  return (
    <tr
      style={{ background: level === 'error' ? 'rgba(224,82,82,.06)' : level === 'warn' ? 'rgba(232,167,53,.06)' : undefined }}
      className="log-row"
    >
      <td style={{
        userSelect: 'none',
        paddingLeft: 12, paddingRight: 12,
        color: 'var(--text-dim)',
        textAlign: 'right',
        whiteSpace: 'nowrap',
        verticalAlign: 'top',
        minWidth: 48,
        borderRight: '1px solid var(--border)',
        fontSize: 11,
        paddingTop: 2, paddingBottom: 2,
      }}>
        {number}
      </td>
      <td style={{
        padding: '2px 14px',
        color: level === 'error' ? 'var(--error)'
          : level === 'warn' ? '#e8a735'
          : level === 'info' ? 'var(--accent)'
          : 'var(--text)',
        whiteSpace: 'pre-wrap',
        wordBreak: 'break-all',
        verticalAlign: 'top',
      }}>
        {text || ' '}
      </td>
    </tr>
  )
}

function detectLevel(line: string): 'error' | 'warn' | 'info' | 'debug' | null {
  const lower = line.toLowerCase()
  if (lower.includes('[error]') || lower.includes('error:') || lower.includes('exception')) return 'error'
  if (lower.includes('[warn]')  || lower.includes('warn:'))  return 'warn'
  if (lower.includes('[info]')  || lower.includes('info:'))  return 'info'
  if (lower.includes('[debug]') || lower.includes('debug:')) return 'debug'
  return null
}
