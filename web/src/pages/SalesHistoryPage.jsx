import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../shared/api/client'
import './SalesHistoryPage.css'

function formatPeso(amount) {
  return `₱${Number(amount).toFixed(2)}`
}

function formatDateTime(iso) {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export default function SalesHistoryPage() {
  const navigate = useNavigate()
  const [sales, setSales] = useState([])
  const [date, setDate] = useState('') // '' = all dates (FR-013 filter)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null) // sale shown in the detail modal (FR-014)

  // FR-012: list past sales, optionally filtered by date (GET /api/sales[?date=]).
  async function loadSales(filterDate) {
    setLoading(true)
    setError('')
    try {
      const { data } = await client.get('/sales', {
        params: filterDate ? { date: filterDate } : {},
      })
      setSales(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load sales history')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadSales(date)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date])

  const totalSales = sales.reduce((sum, s) => sum + Number(s.totalAmount), 0)

  return (
    <div className="history-page">
      <header className="history-header">
        <div>
          <h1>Sales History</h1>
          <span className="history-sub">
            {loading
              ? 'Loading…'
              : `${sales.length} sale${sales.length === 1 ? '' : 's'} · ${formatPeso(totalSales)}`}
          </span>
        </div>
        <button onClick={() => navigate('/')}>Back to POS</button>
      </header>

      <div className="history-toolbar">
        <label className="history-datefilter">
          Filter by date
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </label>
        {date && (
          <button className="history-ghost" onClick={() => setDate('')}>
            Show all
          </button>
        )}
        <button className="history-ghost" onClick={() => loadSales(date)}>
          Refresh
        </button>
      </div>

      {error && <div className="history-error">{error}</div>}

      <main className="history-list">
        {!loading && !error && sales.length === 0 && (
          <p className="history-empty">
            {date ? 'No sales on this date.' : 'No sales recorded yet.'}
          </p>
        )}

        {sales.map((sale) => (
          <button key={sale.id} className="history-row" onClick={() => setSelected(sale)}>
            <div className="history-row-main">
              <strong>Sale #{sale.id}</strong>
              <span className="history-row-date">{formatDateTime(sale.saleDateTime)}</span>
            </div>
            <div className="history-row-meta">
              <span>{sale.cashierName}</span>
              <span>
                {sale.items.length} item{sale.items.length === 1 ? '' : 's'}
              </span>
            </div>
            <div className="history-row-total">{formatPeso(sale.totalAmount)}</div>
          </button>
        ))}
      </main>

      {selected && (
        <div className="history-modal-scrim" onClick={() => setSelected(null)}>
          <div className="history-modal" onClick={(e) => e.stopPropagation()}>
            <div className="history-modal-head">
              <h2>Receipt · Sale #{selected.id}</h2>
              <button
                className="history-modal-close"
                onClick={() => setSelected(null)}
                aria-label="Close"
              >
                ✕
              </button>
            </div>
            <p className="history-modal-sub">
              {formatDateTime(selected.saleDateTime)} · {selected.cashierName}
            </p>

            <table className="history-items">
              <thead>
                <tr>
                  <th>Category</th>
                  <th className="num">Qty</th>
                  <th className="num">Unit</th>
                  <th className="num">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {selected.items.map((it) => (
                  <tr key={it.id}>
                    <td>{it.categoryName}</td>
                    <td className="num">{it.quantity}</td>
                    <td className="num">{formatPeso(it.unitPrice)}</td>
                    <td className="num">{formatPeso(it.subtotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="history-modal-totals">
              <div className="history-total-row strong">
                <span>Total</span>
                <span>{formatPeso(selected.totalAmount)}</span>
              </div>
              <div className="history-total-row">
                <span>Payment</span>
                <span>{formatPeso(selected.paymentAmount)}</span>
              </div>
              <div className="history-total-row">
                <span>Change</span>
                <span>{formatPeso(selected.changeAmount)}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
