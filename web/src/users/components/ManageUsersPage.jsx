import { useEffect, useState } from 'react'
import AppShell, { initialsOf } from '../../shared/layout/AppShell'
import client from '../../shared/api/client'
import '../styles/ManageUsersPage.css'

const AVATAR_COLORS = ['#c98a3a', '#147a6e', '#5c7d8a', '#9a7bb0', '#2f8f5b']

export default function ManageUsersPage() {
  const [users, setUsers] = useState([])
  const [name, setName] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('CASHIER')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function loadUsers() {
    const { data } = await client.get('/users')
    setUsers(data)
  }

  useEffect(() => {
    loadUsers()
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await client.post('/users', { name, username, password, role })
      setName('')
      setUsername('')
      setPassword('')
      setRole('CASHIER')
      await loadUsers()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create account')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppShell title="Team & access" subtitle="Staff accounts">
      <div className="page-head">
        <div>
          <h2>Team &amp; access</h2>
          <div className="page-head-sub">Create staff logins and set who can manage the store</div>
        </div>
      </div>

      <div className="settings-grid">
        <form className="card settings-card" onSubmit={handleSubmit}>
          <div className="settings-card-head">Add staff</div>
          <div className="settings-card-body">
            <label className="field-label" htmlFor="u-name">
              Full name
            </label>
            <input
              id="u-name"
              className="field-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />

            <label className="field-label" htmlFor="u-username">
              Username
            </label>
            <input
              id="u-username"
              className="field-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />

            <label className="field-label" htmlFor="u-password">
              Password
            </label>
            <input
              id="u-password"
              className="field-input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={6}
              required
            />

            <label className="field-label" htmlFor="u-role">
              Role
            </label>
            <select
              id="u-role"
              className="field-select"
              value={role}
              onChange={(e) => setRole(e.target.value)}
            >
              <option value="CASHIER">Cashier — sell only</option>
              <option value="OWNER">Owner — full access</option>
            </select>

            {error && <div className="form-error">{error}</div>}

            <button type="submit" className="form-submit" disabled={submitting}>
              <span className="form-submit-plus">+</span>
              {submitting ? 'Creating…' : 'Create account'}
            </button>
          </div>
        </form>

        <div className="card settings-card">
          <div className="settings-card-head">
            <span>Staff accounts</span>
            <span className="settings-count">{users.length}</span>
          </div>
          <div className="users-table">
            <div className="users-row users-head">
              <span>Staff</span>
              <span>Username</span>
              <span className="users-right">Role</span>
            </div>
            {users.map((u, i) => {
              const isOwner = u.role === 'OWNER'
              return (
                <div key={u.id} className="users-row">
                  <span className="users-staff">
                    <span
                      className="users-avatar"
                      style={{ background: AVATAR_COLORS[i % AVATAR_COLORS.length] }}
                    >
                      {initialsOf(u.name)}
                    </span>
                    {u.name}
                  </span>
                  <span className="users-username">{u.username}</span>
                  <span className="users-right">
                    <span className={`badge ${isOwner ? 'badge-admin' : 'badge-cashier'}`}>
                      {isOwner ? 'Owner' : 'Cashier'}
                    </span>
                  </span>
                </div>
              )
            })}
            {users.length === 0 && <p className="settings-empty">No staff yet.</p>}
          </div>
        </div>
      </div>
    </AppShell>
  )
}
