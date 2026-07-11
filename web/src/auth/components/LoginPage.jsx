import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import '../styles/LoginPage.css'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(username, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid username or password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        {/* brand panel */}
        <aside className="login-brand">
          <div className="login-brand-pattern" aria-hidden="true" />
          <div className="login-brand-blob" aria-hidden="true" />
          <div className="login-brand-top">
            <div className="login-brand-mark">S</div>
            <span className="login-brand-name">SouvenirPOS</span>
          </div>
          <div className="login-brand-copy">
            <h2>Every keepsake, every sale, handled in seconds.</h2>
            <p>Ring up mugs, magnets, and postcards with one hand while the line keeps moving.</p>
          </div>
          <div className="login-brand-status">
            <span className="login-brand-dot" />
            Harbor Row Souvenir Co. · Terminal online
          </div>
        </aside>

        {/* form panel */}
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="login-form-inner">
            <h1>Welcome back</h1>
            <p className="login-subtitle">Sign in to open your register drawer.</p>

            <label className="login-label" htmlFor="username">
              Username
            </label>
            <div className="login-input">
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoFocus
                required
              />
            </div>

            <label className="login-label" htmlFor="password">
              Password
            </label>
            <div className="login-input">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                className="login-show"
                onClick={() => setShowPassword((s) => !s)}
                tabIndex={-1}
              >
                {showPassword ? 'hide' : 'show'}
              </button>
            </div>

            {error && <div className="login-error">{error}</div>}

            <button type="submit" className="login-submit" disabled={submitting}>
              {submitting ? 'Signing in…' : 'Sign in & open drawer'}
            </button>

            <div className="login-foot">SouvenirPOS 1.0 · Harbor Row · shift 2</div>
          </div>
        </form>
      </div>
    </div>
  )
}
