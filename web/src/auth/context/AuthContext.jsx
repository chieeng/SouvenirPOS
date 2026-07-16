import { createContext, useContext, useState } from 'react'
import client from '../../shared/api/client'

const AuthContext = createContext(null)

const STORAGE_KEY = 'souvenirpos_token'
const USER_KEY = 'souvenirpos_user'

function userFromResponse(data) {
  return {
    id: data.userId,
    name: data.name,
    username: data.username,
    role: data.role,
    mustChangePassword: data.mustChangePassword,
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? JSON.parse(stored) : null
  })

  function persist(data) {
    localStorage.setItem(STORAGE_KEY, data.token)
    const loggedInUser = userFromResponse(data)
    localStorage.setItem(USER_KEY, JSON.stringify(loggedInUser))
    setUser(loggedInUser)
    return loggedInUser
  }

  async function login(username, password) {
    const { data } = await client.post('/auth/login', { username, password })
    return persist(data)
  }

  // Changing the password returns a fresh token (the old one is now revoked server-side)
  // and clears the must-change flag.
  async function changePassword(currentPassword, newPassword) {
    const { data } = await client.post('/auth/change-password', { currentPassword, newPassword })
    return persist(data)
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, changePassword, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
