import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../../shared/api/client'
import '../styles/ManageCategoriesPage.css'

export default function ManageCategoriesPage() {
  const navigate = useNavigate()
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
    <div className="cats-page">
      <header className="cats-header">
        <h1>Manage Categories</h1>
        <button onClick={() => navigate('/')}>Back to POS</button>
      </header>

      <main className="cats-main">
        <form className="cats-form" onSubmit={handleSubmit}>
          <h2>Add Category</h2>

          <label>Category Name</label>
          <input
            value={name}
            onChange={(e) => {
              setName(e.target.value)
              setError('')
              setSuccess('')
            }}
            placeholder="e.g. Keychain"
            required
          />

          {error && <div className="cats-error">{error}</div>}
          {success && <div className="cats-success">{success}</div>}

          <button type="submit" disabled={submitting}>
            {submitting ? 'Adding…' : 'Add Category'}
          </button>
        </form>

        <div className="cats-list">
          <h2>Current Categories {!loading && <span>({categories.length})</span>}</h2>
          {loading ? (
            <p className="cats-empty">Loading…</p>
          ) : categories.length === 0 ? (
            <p className="cats-empty">No categories yet.</p>
          ) : (
            <ul>
              {categories.map((c) => (
                <li key={c.id}>{c.name}</li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </div>
  )
}
