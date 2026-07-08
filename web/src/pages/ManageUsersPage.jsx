import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../shared/api/client'
import './ManageUsersPage.css'

export default function ManageUsersPage() {
  const navigate = useNavigate()
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
    <div className="users-page">
      <header className="users-header">
        <h1>Manage Users</h1>
        <button onClick={() => navigate('/')}>Back to POS</button>
      </header>

      <main className="users-main">
        <form className="users-form" onSubmit={handleSubmit}>
          <h2>Create Account</h2>

          <label>Full Name</label>
          <input value={name} onChange={(e) => setName(e.target.value)} required />

          <label>Username</label>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />

          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={6}
            required
          />

          <label>Role</label>
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="CASHIER">Cashier</option>
            <option value="OWNER">Owner</option>
          </select>

          {error && <div className="users-error">{error}</div>}

          <button type="submit" disabled={submitting}>
            {submitting ? 'Creating...' : 'Create Account'}
          </button>
        </form>

        <div className="users-list">
          <h2>Existing Users</h2>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Username</th>
                <th>Role</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.name}</td>
                  <td>{u.username}</td>
                  <td>{u.role}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  )
}
