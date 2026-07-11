import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'
import './AppShell.css'

export function initialsOf(name = '') {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '··'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

const HomeIcon = () => (
  <svg width="19" height="19" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
    <rect x="3" y="5" width="10" height="9" rx="1.5" />
    <path d="M5.5 5.5a2.5 2.5 0 0 1 5 0" />
  </svg>
)
const GridIcon = () => (
  <svg width="19" height="19" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
    <rect x="2" y="2" width="5" height="5" rx="1" />
    <rect x="9" y="2" width="5" height="5" rx="1" />
    <rect x="2" y="9" width="5" height="5" rx="1" />
    <rect x="9" y="9" width="5" height="5" rx="1" />
  </svg>
)
const ReceiptIcon = () => (
  <svg width="19" height="19" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
    <path d="M4 2.5h8v11l-2-1.1-2 1.1-2-1.1-2 1.1z" />
    <line x1="6" y1="6" x2="10" y2="6" />
    <line x1="6" y1="8.5" x2="10" y2="8.5" />
  </svg>
)
const TagIcon = () => (
  <svg width="19" height="19" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
    <path d="M2.5 2.5h4.2l6.8 6.8-4.2 4.2-6.8-6.8z" />
    <circle cx="5" cy="5" r="0.9" fill="currentColor" stroke="none" />
  </svg>
)
const TeamIcon = () => (
  <svg width="19" height="19" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
    <circle cx="6" cy="5.5" r="2.2" />
    <path d="M2.5 13a3.5 3.5 0 0 1 7 0" />
    <path d="M10.5 4.2a2 2 0 0 1 0 3.6" />
    <path d="M11 9.5a3.4 3.4 0 0 1 2.5 3.3" />
  </svg>
)

const NAV = [
  { to: '/', label: 'Home', icon: HomeIcon, end: true },
  { to: '/dashboard', label: 'Dashboard', icon: GridIcon, ownerOnly: true },
  { to: '/history', label: 'Sales', icon: ReceiptIcon },
  { to: '/categories', label: 'Categories', icon: TagIcon, ownerOnly: true },
  { to: '/users', label: 'Team', icon: TeamIcon, ownerOnly: true },
]

/**
 * Shared application chrome: fixed left rail + top bar.
 * `variant="flush"` hands the content area to the page unpadded (used by POS).
 */
export default function AppShell({
  title,
  subtitle,
  roleBadge = true,
  actions,
  variant = 'default',
  children,
}) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const isOwner = user?.role === 'OWNER'

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="shell">
      <nav className="rail">
        <div className="rail-brand" title="SouvenirPOS">S</div>

        <div className="rail-nav">
          {NAV.filter((item) => !item.ownerOnly || isOwner).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `rail-item${isActive ? ' rail-item-active' : ''}`}
            >
              <item.icon />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </div>

        <button className="rail-logout" onClick={handleLogout} title="Sign out">
          <svg width="19" height="19" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M6 2.5H4A1.5 1.5 0 0 0 2.5 4v8A1.5 1.5 0 0 0 4 13.5h2" />
            <path d="M10.5 11 13.5 8l-3-3" />
            <line x1="13.5" y1="8" x2="6" y2="8" />
          </svg>
          <span>Sign out</span>
        </button>
        <div className={`rail-avatar ${isOwner ? 'is-owner' : 'is-cashier'}`}>
          {initialsOf(user?.name)}
        </div>
      </nav>

      <div className="shell-main">
        <header className="topbar">
          <div className="topbar-titles">
            <h1 className="topbar-title">{title}</h1>
            {roleBadge && user && (
              <span className={`badge ${isOwner ? 'badge-admin' : 'badge-cashier'}`}>
                {isOwner ? 'ADMIN' : 'CASHIER'}
              </span>
            )}
            {subtitle && <span className="topbar-subtitle">{subtitle}</span>}
          </div>

          <div className="topbar-right">
            {actions}
            <div className="topbar-user">
              <div className={`topbar-user-avatar ${isOwner ? 'is-owner' : 'is-cashier'}`}>
                {initialsOf(user?.name)}
              </div>
              <div className="topbar-user-meta">
                <div className="topbar-user-name">{user?.name}</div>
                <div className="topbar-user-role">{isOwner ? 'Store admin' : 'Cashier'}</div>
              </div>
            </div>
          </div>
        </header>

        <main className={variant === 'flush' ? 'shell-content shell-content-flush' : 'shell-content'}>
          {children}
        </main>
      </div>
    </div>
  )
}
