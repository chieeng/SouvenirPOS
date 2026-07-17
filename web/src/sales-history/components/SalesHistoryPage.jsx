import { useEffect, useState } from 'react'
import AppShell, { initialsOf } from '../../shared/layout/AppShell'
import client from '../../shared/api/client'
import '../styles/SalesHistoryPage.css'

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

function formatTime(iso) {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  return d
    .toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' })
    .replace(/\s?([AP])M/i, (_, p) => p.toLowerCase())
}

function itemSummary(items) {
  const names = items.map((it) =>
    it.quantity > 1 ? `${it.categoryName} ×${it.quantity}` : it.categoryName,
  )
  const shown = names.slice(0, 3).join(', ')
  return names.length > 3 ? `${shown} +${names.length - 3}` : shown
}

export default function SalesHistoryPage() {
  const [sales, setSales] = useState([])
  // FR-013: '' = unbounded. Set only `from` for a single day, or both for a range.
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null) // sale shown in the detail modal (FR-014)

  const hasFilter = Boolean(from || to)

  // FR-012/FR-013: list past sales, optionally filtered by a from/to date range
  // (GET /api/sales[?from=&to=]). `silent` skips the loading flag so the 5s
  // auto-refresh doesn't flicker the UI.
  async function loadSales(fromDate, toDate, { silent = false } = {}) {
    if (!silent) setLoading(true)
    setError('')
    try {
      const params = {}
      if (fromDate) params.from = fromDate
      if (toDate) params.to = toDate
      const { data } = await client.get('/sales', { params })
      setSales(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load sales history')
    } finally {
      if (!silent) setLoading(false)
    }
  }

  useEffect(() => {
    loadSales(from, to)
    // FR-016 / NFR-002: poll so a sale rung up on another device shows within 5s.
    const timer = setInterval(() => loadSales(from, to, { silent: true }), 5000)
    return () => clearInterval(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [from, to])

  const totalSales = sales.reduce((sum, s) => sum + Number(s.totalAmount), 0)
  const itemsSold = sales.reduce(
    (sum, s) => sum + s.items.reduce((n, it) => n + it.quantity, 0),
    0,
  )
  const avgBasket = sales.length > 0 ? totalSales / sales.length : 0

  const filterTools = (
    <div className="hist-tools">
      <label className="hist-range-label">From</label>
      <input
        type="date"
        className="hist-date"
        value={from}
        max={to || undefined}
        onChange={(e) => setFrom(e.target.value)}
      />
      <label className="hist-range-label">To</label>
      <input
        type="date"
        className="hist-date"
        value={to}
        min={from || undefined}
        onChange={(e) => setTo(e.target.value)}
      />
      {hasFilter && (
        <button
          className="pill-btn"
          onClick={() => {
            setFrom('')
            setTo('')
          }}
        >
          Show all
        </button>
      )}
      <button className="pill-btn" onClick={() => loadSales(from, to)}>
        Refresh
      </button>
    </div>
  )

  return (
    <AppShell title="Sales history" subtitle="Transactions" actions={filterTools}>
      <div className="page-head">
        <div>
          <h2>Sales history</h2>
          <div className="page-head-sub">
            {loading
              ? 'Loading…'
              : `${sales.length} transaction${sales.length === 1 ? '' : 's'} · `}
            {!loading && <strong className="hist-collected">{formatPeso(totalSales)}</strong>}
            {!loading && ' collected'}
          </div>
        </div>
      </div>

      {error && <div className="hist-error">{error}</div>}

      <div className="hist-kpis">
        <div className="kpi">
          <div className="kpi-label">Gross sales</div>
          <div className="kpi-value">{formatPeso(totalSales)}</div>
        </div>
        <div className="kpi">
          <div className="kpi-label">Transactions</div>
          <div className="kpi-value">{sales.length}</div>
        </div>
        <div className="kpi">
          <div className="kpi-label">Items sold</div>
          <div className="kpi-value">{itemsSold}</div>
        </div>
        <div className="kpi">
          <div className="kpi-label">Avg. basket</div>
          <div className="kpi-value hist-teal">{formatPeso(avgBasket)}</div>
        </div>
      </div>

      <div className="card hist-table">
        <div className="hist-row hist-head">
          <span>Order</span>
          <span>Time</span>
          <span>Items</span>
          <span className="hist-center">Qty</span>
          <span>Cashier</span>
          <span className="hist-right">Total</span>
          <span className="hist-right">Status</span>
        </div>

        {!loading && !error && sales.length === 0 && (
          <p className="hist-empty">{hasFilter ? 'No sales in this range.' : 'No sales recorded yet.'}</p>
        )}

        {sales.map((sale) => {
          const qty = sale.items.reduce((n, it) => n + it.quantity, 0)
          return (
            <button key={sale.id} className="hist-row hist-data" onClick={() => setSelected(sale)}>
              <span className="hist-order">#{sale.id}</span>
              <span className="hist-muted">{formatTime(sale.saleDateTime)}</span>
              <span className="hist-items">{itemSummary(sale.items)}</span>
              <span className="hist-center hist-muted">{qty}</span>
              <span className="hist-cashier">
                <span className="hist-cashier-avatar">{initialsOf(sale.cashierName)}</span>
                <span className="hist-cashier-name">{sale.cashierName}</span>
              </span>
              <span className="hist-right hist-total">{formatPeso(sale.totalAmount)}</span>
              <span className="hist-right">
                <em className="hist-status">Paid</em>
              </span>
            </button>
          )
        })}
      </div>

      {selected && (
        <div className="hist-modal-scrim" onClick={() => setSelected(null)}>
          <div className="hist-modal" onClick={(e) => e.stopPropagation()}>
            <div className="hist-modal-head">
              <h2>Receipt · Sale #{selected.id}</h2>
              <button
                className="hist-modal-close"
                onClick={() => setSelected(null)}
                aria-label="Close"
              >
                ✕
              </button>
            </div>
            <p className="hist-modal-sub">
              {formatDateTime(selected.saleDateTime)} · {selected.cashierName}
            </p>

            <table className="hist-items-table">
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

            <div className="hist-modal-totals">
              <div className="hist-total-row strong">
                <span>Total</span>
                <span>{formatPeso(selected.totalAmount)}</span>
              </div>
              <div className="hist-total-row">
                <span>Payment</span>
                <span>{formatPeso(selected.paymentAmount)}</span>
              </div>
              <div className="hist-total-row">
                <span>Change</span>
                <span>{formatPeso(selected.changeAmount)}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </AppShell>
  )
}
