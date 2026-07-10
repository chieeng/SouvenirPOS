import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../../shared/api/client'
import '../styles/DashboardPage.css'

const REFRESH_MS = 5000 // FR-016 / NFR-002: reflect cross-platform sales within 5s.

function formatPeso(amount) {
  return `₱${Number(amount || 0).toFixed(2)}`
}

function dayLabel(iso) {
  const d = new Date(`${iso}T00:00:00`)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString(undefined, { weekday: 'short', month: 'numeric', day: 'numeric' })
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  // Keep the interval callback pointed at a stable loader without re-subscribing.
  const loadRef = useRef(null)

  async function loadSummary() {
    try {
      const { data } = await client.get('/sales/summary')
      setSummary(data)
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load the dashboard')
    } finally {
      setLoading(false)
    }
  }
  loadRef.current = loadSummary

  useEffect(() => {
    loadRef.current()
    const timer = setInterval(() => loadRef.current(), REFRESH_MS)
    return () => clearInterval(timer)
  }, [])

  const daily = summary?.daily ?? []
  const maxDaily = daily.reduce((max, d) => Math.max(max, Number(d.total)), 0)

  return (
    <div className="dash-page">
      <header className="dash-header">
        <div>
          <h1>Sales Dashboard</h1>
          <span className="dash-sub">{loading ? 'Loading…' : 'Live · updates every 5s'}</span>
        </div>
        <button onClick={() => navigate('/')}>Back to POS</button>
      </header>

      {error && <div className="dash-error">{error}</div>}

      <main className="dash-main">
        <section className="dash-cards">
          <div className="dash-card">
            <span className="dash-card-label">Today</span>
            <span className="dash-card-total">{formatPeso(summary?.todayTotal)}</span>
            <span className="dash-card-meta">
              {summary?.todayCount ?? 0} sale{summary?.todayCount === 1 ? '' : 's'}
            </span>
          </div>
          <div className="dash-card accent">
            <span className="dash-card-label">This Week</span>
            <span className="dash-card-total">{formatPeso(summary?.weekTotal)}</span>
            <span className="dash-card-meta">
              {summary?.weekCount ?? 0} sale{summary?.weekCount === 1 ? '' : 's'}
            </span>
          </div>
        </section>

        <section className="dash-breakdown">
          <h2>Last 7 days</h2>
          <div className="dash-bars">
            {daily.map((d) => {
              const total = Number(d.total)
              const pct = maxDaily > 0 ? (total / maxDaily) * 100 : 0
              return (
                <div key={d.date} className="dash-bar-row">
                  <span className="dash-bar-day">{dayLabel(d.date)}</span>
                  <div className="dash-bar-track">
                    <div
                      className="dash-bar-fill"
                      style={{ width: `${pct}%` }}
                      aria-hidden="true"
                    />
                  </div>
                  <span className="dash-bar-value">
                    {formatPeso(total)}
                    <span className="dash-bar-count">{d.count}</span>
                  </span>
                </div>
              )
            })}
            {daily.length === 0 && !loading && (
              <p className="dash-empty">No sales recorded yet.</p>
            )}
          </div>
        </section>
      </main>
    </div>
  )
}
