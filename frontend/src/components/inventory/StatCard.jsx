export function StatGrid({ columns = 4, children }) {
  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))`,
        gap: 16,
        marginBottom: 20,
      }}
    >
      {children}
    </div>
  )
}

export default function StatCard({ label, value, unit, note, color = '#1677ff', loading }) {
  return (
    <div
      style={{
        background: '#fff',
        border: '1px solid #f0f0f0',
        borderTop: `3px solid ${color}`,
        borderRadius: 8,
        padding: '14px 18px',
        minWidth: 0,
      }}
    >
      <div style={{ fontSize: 13, color: '#8c8c8c', fontWeight: 600 }}>{label}</div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, marginTop: 6 }}>
        <span style={{ fontSize: 26, fontWeight: 700, color: '#0f2942', lineHeight: 1.2 }}>
          {loading ? '…' : value}
        </span>
        {unit && <span style={{ fontSize: 13, color: '#8c8c8c' }}>{unit}</span>}
      </div>
      {note && <div style={{ fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>{note}</div>}
    </div>
  )
}
