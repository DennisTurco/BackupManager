import { useEffect, useRef, useState } from 'react'
import { ChevronRight } from 'lucide-react'

export interface MenuItem {
  label?: string
  type?: 'separator'
  checked?: boolean
  disabled?: boolean
  danger?: boolean
  onClick?: () => void
  submenu?: MenuItem[]
}

interface Props {
  x: number
  y: number
  items: MenuItem[]
  onClose: () => void
}

export function ContextMenu({ x, y, items, onClose }: Props) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onClose()
    }
    const keyHandler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('mousedown', handler)
    document.addEventListener('keydown', keyHandler)
    return () => {
      document.removeEventListener('mousedown', handler)
      document.removeEventListener('keydown', keyHandler)
    }
  }, [onClose])

  // Clamp to viewport
  const [pos, setPos] = useState({ left: x, top: y })
  useEffect(() => {
    if (!ref.current) return
    const { width, height } = ref.current.getBoundingClientRect()
    setPos({
      left: Math.max(4, x + width > window.innerWidth - 4 ? x - width : x),
      top:  Math.max(4, y + height > window.innerHeight - 4 ? y - height : y),
    })
  }, [x, y])

  return (
    <div
      ref={ref}
      style={{
        position: 'fixed',
        left: pos.left,
        top:  pos.top,
        zIndex: 1000,
      }}
    >
      <MenuPanel items={items} onClose={onClose} />
    </div>
  )
}

function MenuPanel({ items, onClose }: { items: MenuItem[]; onClose: () => void }) {
  return (
    <div style={{
      background: 'var(--bg-2)',
      border: '1px solid var(--border)',
      borderRadius: 6,
      padding: '3px 0',
      minWidth: 190,
      boxShadow: 'var(--shadow-md)',
      userSelect: 'none',
    }}>
      {items.map((item, i) => {
        if (item.type === 'separator') {
          return <div key={i} style={{ height: 1, background: 'var(--border)', margin: '3px 0' }} />
        }
        return (
          <MenuRow key={i} item={item} onClose={onClose} />
        )
      })}
    </div>
  )
}

function MenuRow({ item, onClose }: { item: MenuItem; onClose: () => void }) {
  const [open, setOpen] = useState(false)
  const rowRef = useRef<HTMLDivElement>(null)
  const hasSubmenu = !!item.submenu?.length

  return (
    <div
      ref={rowRef}
      onMouseEnter={() => hasSubmenu && setOpen(true)}
      onMouseLeave={() => hasSubmenu && setOpen(false)}
      onClick={() => {
        if (item.disabled || hasSubmenu) return
        item.onClick?.()
        onClose()
      }}
      style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '5px 14px',
        fontSize: 13,
        color: item.disabled ? 'var(--text-dim)' : item.danger ? 'var(--error)' : 'var(--text)',
        cursor: item.disabled ? 'default' : 'pointer',
        borderRadius: 4,
        margin: '0 3px',
        gap: 8,
        background: open ? 'var(--bg-3)' : 'transparent',
        transition: 'background 0.08s',
        position: 'relative',
      }}
      onMouseOver={e => {
        if (!item.disabled) (e.currentTarget as HTMLDivElement).style.background = 'var(--bg-3)'
      }}
      onMouseOut={e => {
        if (!open) (e.currentTarget as HTMLDivElement).style.background = 'transparent'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        {item.checked !== undefined && (
          <span style={{ width: 14, fontSize: 12, color: 'var(--accent)' }}>
            {item.checked ? '✓' : ''}
          </span>
        )}
        <span>{item.label}</span>
      </div>
      {hasSubmenu && (
        <ChevronRight size={12} color="var(--text-muted)" />
      )}

      {/* Submenu */}
      {hasSubmenu && open && (
        <div style={{
          position: 'absolute',
          left: '100%',
          top: -4,
          paddingLeft: 4,
        }}>
          <MenuPanel items={item.submenu!} onClose={onClose} />
        </div>
      )}
    </div>
  )
}
