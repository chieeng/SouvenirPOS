import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import '../styles/LoginPage.css'

export default function ChangePasswordPage() {
  const { user, changePassword, logout } = useAuth()
  const navigate = useNavigate()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (!user) {
    return <Navigate to="/login" replace />
  }

  const forced = user.mustChangePassword

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters')
      return
    }
    if (newPassword !== confirmPassword) {
      setError('New passwords do not match')
      return
    }
    setSubmitting(true)
    try {
      await changePassword(currentPassword, newPassword)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not change password')
    } finally {
      setSubmitting(false)
    }
  }

  function handleCancel() {
    // Forced users have no session to return to yet, so cancelling signs them out.
    // Voluntary visitors just go back to the app.
    if (forced) {
      logout()
      navigate('/login')
    } else {
      navigate('/')
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <aside className="login-brand">
          <div className="login-brand-pattern" aria-hidden="true" />
          <div className="login-brand-blob" aria-hidden="true" />
          <div className="login-brand-top">
            <div className="login-brand-mark">S</div>
            <span className="login-brand-name">SouvenirPOS</span>
          </div>
          <div className="login-brand-copy">
            <h2>Secure your account.</h2>
            <p>
              {forced
                ? 'Set a new password before you start ringing up sales.'
                : 'Choose a fresh password to keep your register drawer safe.'}
            </p>
          </div>
          <div className="login-brand-status">
            <span className="login-brand-dot" />
            Signed in as {user.name}
          </div>
        </aside>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="login-form-inner">
            <h1>{forced ? 'Set a new password' : 'Change password'}</h1>
            <p className="login-subtitle">
              {forced
                ? 'Your account still uses its initial password. Please replace it now.'
                : 'Enter your current password and a new one.'}
            </p>

            <label className="login-label" htmlFor="current">
              Current password
            </label>
            <div className="login-input">
              <input
                id="current"
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                autoFocus
                required
              />
            </div>

            <label className="login-label" htmlFor="new">
              New password
            </label>
            <div className="login-input">
              <input
                id="new"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                minLength={8}
                required
              />
            </div>

            <label className="login-label" htmlFor="confirm">
              Confirm new password
            </label>
            <div className="login-input">
              <input
                id="confirm"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                minLength={8}
                required
              />
            </div>

            {error && <div className="login-error">{error}</div>}

            <button type="submit" className="login-submit" disabled={submitting}>
              {submitting ? 'Saving…' : 'Update password'}
            </button>

            <button type="button" className="login-show" onClick={handleCancel} style={{ marginTop: 12 }}>
              {forced ? 'Sign out instead' : 'Cancel'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
