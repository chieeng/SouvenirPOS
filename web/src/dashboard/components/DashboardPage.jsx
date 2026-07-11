import { useEffect, useRef, useState } from 'react'
import AppShell from '../../shared/layout/AppShell'
import client from '../../shared/api/client'
import '../styles/DashboardPage.css'

const REFRESH_MS = 5000 // FR-016 / NFR-002: reflect cross-platform sales within 5s.

function formatPeso(amount) {
  return `₱${Number(amount || 0).toFixed(2)}`
}

function dayLabel(iso) {
  const d = new Date(`${iso}T00:00:00`)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString(undefined, { weekday: 'short' })
}

// Warm teal → sand ramp so the tallest bars read as the busiest days.
const BAR_COLORS = ['#147a6e', '#2f8f5b', '#8fc9c0', '#c98a3a', '#cfe6e2', '#e3ded4', '#e3ded4']

export default function DashboardPage() {
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
  const todayCount = summary?.todayCount ?? 0
  const avgBasket = todayCount > 0 ? Number(summary.todayTotal) / todayCount : 0

  const liveTag = (
    <span className="dash-live">
      <span className="dash-live-dot" />
      {loading ? 'Loading…' : 'Live · every 5s'}
    </span>
  )

  return (
    <AppShell title="Dashboard" subtitle="Store overview" actions={liveTag}>
      <div className="page-head">
        <div>
          <h2>Sales dashboard</h2>
          <div className="page-head-sub">Real-time totals across every register and device</div>
        </div>
      </div>

      {error && <div className="dash-error">{error}</div>}

      <div className="dash-kpis">
        <div className="kpi">
          <div className="kpi-label">Today&apos;s sales</div>
          <div className="kpi-value">{formatPeso(summary?.todayTotal)}</div>
          <div className="kpi-meta">
            {todayCount} sale{todayCount === 1 ? '' : 's'} today
          </div>
        </div>
        <div className="kpi">
          <div className="kpi-label">Transactions</div>
          <div className="kpi-value">{todayCount}</div>
          <div className="kpi-meta">today</div>
        </div>
        <div className="kpi">
          <div className="kpi-label">Avg. basket</div>
          <div className="kpi-value">{formatPeso(avgBasket)}</div>
          <div className="kpi-meta">per sale today</div>
        </div>
        <div className="kpi kpi-accent">
          <div className="kpi-label">This week</div>
          <div className="kpi-value">{formatPeso(summary?.weekTotal)}</div>
          <div className="kpi-meta">
            {summary?.weekCount ?? 0} sale{summary?.weekCount === 1 ? '' : 's'}
          </div>
        </div>
      </div>

      <div className="card dash-chart">
        <div className="dash-chart-head">
          <div className="dash-chart-title">Sales · last 7 days</div>
          <div className="dash-chart-sub">Daily gross</div>
        </div>

        {daily.length === 0 && !loading ? (
          <p className="dash-empty">No sales recorded yet.</p>
        ) : (
          <div className="dash-bars">
            {daily.map((d, i) => {
              const total = Number(d.total)
              const pct = maxDaily > 0 ? Math.max((total / maxDaily) * 100, 3) : 3
              return (
                <div key={d.date} className="dash-bar">
                  <span className="dash-bar-value">{formatPeso(total)}</span>
                  <div className="dash-bar-track">
                    <div
                      className="dash-bar-fill"
                      style={{ height: `${pct}%`, background: BAR_COLORS[i % BAR_COLORS.length] }}
                      aria-hidden="true"
                    />
                  </div>
                  <span className="dash-bar-day">{dayLabel(d.date)}</span>
                  <span className="dash-bar-count">{d.count}</span>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </AppShell>
  )
}
