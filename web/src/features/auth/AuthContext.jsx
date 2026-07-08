import { createContext, useContext, useState } from 'react'
import client from '../../shared/api/client'

const AuthContext = createContext(null)

const STORAGE_KEY = 'souvenirpos_token'
const USER_KEY = 'souvenirpos_user'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? JSON.parse(stored) : null
  })

  async function login(username, password) {
    const { data } = await client.post('/auth/login', { username, password })
    localStorage.setItem(STORAGE_KEY, data.token)
    const loggedInUser = {
      id: data.userId,
      name: data.name,
      username: data.username,
      role: data.role,
    }
    localStorage.setItem(USER_KEY, JSON.stringify(loggedInUser))
    setUser(loggedInUser)
    return loggedInUser
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
