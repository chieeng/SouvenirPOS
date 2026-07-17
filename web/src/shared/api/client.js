import axios from 'axios'

// API base: in production VITE_API_BASE_URL points at the deployed backend origin; in dev it
// is empty so we use the relative "/api" path that the Vite dev server proxies to the local
// backend. Any trailing slash on the origin is trimmed before appending "/api".
const apiOrigin = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const client = axios.create({
  baseURL: apiOrigin ? `${apiOrigin}/api` : '/api',
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('souvenirpos_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// If the server rejects our token (expired, revoked via password change / force-logout /
// deactivation), drop the local session and send the user back to login. The login call
// itself is excluded so a bad-credentials 401 stays on the login screen.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    const isLoginCall = url.includes('/auth/login')
    if (status === 401 && !isLoginCall) {
      localStorage.removeItem('souvenirpos_token')
      localStorage.removeItem('souvenirpos_user')
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

export default client
