import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, requireRole }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  // Block every protected page until the user has replaced a temporary/initial password.
  if (user.mustChangePassword) {
    return <Navigate to="/change-password" replace />
  }

  if (requireRole && user.role !== requireRole) {
    return <Navigate to="/" replace />
  }

  return children
}
