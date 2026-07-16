import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './auth/routing/ProtectedRoute'
import LoginPage from './auth/components/LoginPage'
import ChangePasswordPage from './auth/components/ChangePasswordPage'
import POSPage from './pos/components/POSPage'
import ManageUsersPage from './users/components/ManageUsersPage'
import SalesHistoryPage from './sales-history/components/SalesHistoryPage'
import DashboardPage from './dashboard/components/DashboardPage'
import ManageCategoriesPage from './categories/components/ManageCategoriesPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      {/* Not wrapped in ProtectedRoute: a must-change user is redirected here, so guarding
          it with ProtectedRoute would loop. The page checks for a session itself. */}
      <Route path="/change-password" element={<ChangePasswordPage />} />
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
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
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
      <Route
        path="/categories"
        element={
          <ProtectedRoute requireRole="OWNER">
            <ManageCategoriesPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}
