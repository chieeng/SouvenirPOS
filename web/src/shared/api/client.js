import axios from 'axios'

// API base: in production VITE_API_BASE_URL points at the deployed backend origin; in dev it
// is empty so we use the relative "/api" path that the Vite dev server proxies to the local
// backend. Any trailing slash on the origin is trimmed before appending "/api".
const apiOrigin = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const client = axios.create({
  baseURL: apiOrigin ? `${apiOrigin}/api` : '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('souvenirpos_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Normalize any error into a single human-readable string on `error.normalizedMessage`, so
// callers can rely on one field regardless of whether the failure was an HTTP error (backend
// sends `{ "message": ... }`), a network/timeout error (no response), or a cancelled request.
// The original error is still rejected, so existing `err.response?.data?.message` reads keep
// working — this only adds a consistent fallback-friendly message on top.
function normalizeMessage(error) {
  const data = error.response?.data
  if (data?.message) return data.message
  if (typeof data === 'string' && data.trim()) return data
  if (error.response) return `Request failed (${error.response.status}).`
  if (error.request) return 'Unable to reach the server. Check your connection and try again.'
  return error.message || 'Something went wrong. Please try again.'
}

// If the server rejects our token (expired, revoked via password change / force-logout /
// deactivation), drop the local session and send the user back to login. The login call
// itself is excluded so a bad-credentials 401 stays on the login screen.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    error.normalizedMessage = normalizeMessage(error)

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
