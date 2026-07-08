import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './features/auth/ProtectedRoute'
import LoginPage from './features/auth/LoginPage'
import POSPage from './features/pos/POSPage'
import ManageUsersPage from './features/users/ManageUsersPage'
import SalesHistoryPage from './pages/SalesHistoryPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <POSPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/history"
        element={
          <ProtectedRoute>
            <SalesHistoryPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/users"
        element={
          <ProtectedRoute requireRole="OWNER">
            <ManageUsersPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}
