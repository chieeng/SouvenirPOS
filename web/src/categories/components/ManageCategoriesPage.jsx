import { useEffect, useState } from 'react'
import AppShell from '../../shared/layout/AppShell'
import client from '../../shared/api/client'
import '../styles/ManageCategoriesPage.css'

const SWATCHES = ['#147a6e', '#2f8f5b', '#8fc9c0', '#c98a3a', '#cfe6e2', '#9a7bb0', '#5c7d8a', '#e3ded4']

export default function ManageCategoriesPage() {
  const [categories, setCategories] = useState([])
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(true)

  async function loadCategories() {
    try {
      const { data } = await client.get('/categories')
      setCategories(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load categories')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCategories()
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    const trimmed = name.trim()
    if (!trimmed) {
      setError('Enter a category name')
      return
    }
    setSubmitting(true)
    try {
      // BR-006 / FR-019: only the owner may add categories (also enforced server-side).
      const { data } = await client.post('/categories', { name: trimmed })
      setSuccess(`Added "${data.name}"`)
      setName('')
      await loadCategories()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add category')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppShell title="Categories" subtitle="Product catalog">
      <div className="page-head">
        <div>
          <h2>Categories</h2>
          <div className="page-head-sub">Group the products your cashiers ring up at the register</div>
        </div>
      </div>

      <div className="settings-grid">
        <form className="card settings-card cats-form" onSubmit={handleSubmit}>
          <div className="settings-card-head">Add category</div>
          <div className="settings-card-body">
            <label className="field-label" htmlFor="cat-name">
              Category name
            </label>
            <input
              id="cat-name"
              className="field-input"
              value={name}
              onChange={(e) => {
                setName(e.target.value)
                setError('')
                setSuccess('')
              }}
              placeholder="e.g. Keychains"
              required
            />

            {error && <div className="form-error">{error}</div>}
            {success && <div className="form-success">{success}</div>}

            <button type="submit" className="form-submit" disabled={submitting}>
              <span className="form-submit-plus">+</span>
              {submitting ? 'Adding…' : 'Add category'}
            </button>
          </div>
        </form>

        <div className="card settings-card">
          <div className="settings-card-head">
            <span>Current categories</span>
            {!loading && <span className="settings-count">{categories.length}</span>}
          </div>
          {loading ? (
            <p className="settings-empty">Loading…</p>
          ) : categories.length === 0 ? (
            <p className="settings-empty">No categories yet.</p>
          ) : (
            <div className="cats-list">
              {categories.map((c, i) => (
                <div key={c.id} className="cats-row">
                  <span className="cats-name">
                    <span
                      className="cats-swatch"
                      style={{ background: SWATCHES[i % SWATCHES.length] }}
                    />
                    {c.name}
                  </span>
                  <span className="cats-tag">Active</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}
