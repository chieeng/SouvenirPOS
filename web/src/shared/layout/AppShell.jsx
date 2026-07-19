import { useEffect, useRef, useState } from 'react'
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
const LockIcon = ({ size = 19 }) => (
  <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3.5" y="7" width="9" height="6" rx="1.5" />
    <path d="M5.5 7V5a2.5 2.5 0 0 1 5 0v2" />
  </svg>
)

const SignOutIcon = () => (
  <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M6 2.5H4A1.5 1.5 0 0 0 2.5 4v8A1.5 1.5 0 0 0 4 13.5h2" />
    <path d="M10.5 11 13.5 8l-3-3" />
    <line x1="13.5" y1="8" x2="6" y2="8" />
  </svg>
)

/**
 * The avatar at the foot of the rail, plus the account menu it opens. This is the single
 * home for account actions — they used to sit in the rail (Password/Sign out) and the top
 * bar (name/role), which the POS screen no longer renders.
 *
 * Closes on outside click, on Escape, and on choosing an item. `pointerdown` rather than
 * `click` so the menu is already gone before a click elsewhere lands; the ref check keeps
 * a press on the avatar itself from closing and instantly reopening it.
 */
function ProfileMenu({ user, isOwner }) {
  const [open, setOpen] = useState(false)
  const wrapRef = useRef(null)
  const buttonRef = useRef(null)
  const navigate = useNavigate()
  const { logout } = useAuth()

  useEffect(() => {
    if (!open) return undefined

    function handlePointerDown(event) {
      if (!wrapRef.current?.contains(event.target)) setOpen(false)
    }
    function handleKeyDown(event) {
      if (event.key !== 'Escape') return
      setOpen(false)
      buttonRef.current?.focus() // return focus to the trigger, not the page body
    }

    document.addEventListener('pointerdown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  function handleChangePassword() {
    setOpen(false)
    navigate('/change-password')
  }

  function handleLogout() {
    setOpen(false)
    logout()
    navigate('/login')
  }

  return (
    <div className="rail-profile" ref={wrapRef}>
      {open && (
        <div className="profile-menu" role="menu" aria-label="Account">
          <div className="profile-menu-head">
            <div className="profile-menu-name">{user?.name}</div>
            <div className="profile-menu-role">{isOwner ? 'Store admin' : 'Cashier'}</div>
          </div>
          <button className="profile-menu-item" role="menuitem" onClick={handleChangePassword}>
            <LockIcon size={15} />
            <span>Change Password</span>
          </button>
          <button
            className="profile-menu-item profile-menu-item-danger"
            role="menuitem"
            onClick={handleLogout}
          >
            <SignOutIcon />
            <span>Sign out</span>
          </button>
        </div>
      )}

      <button
        ref={buttonRef}
        className={`rail-avatar ${isOwner ? 'is-owner' : 'is-cashier'}`}
        onClick={() => setOpen((prev) => !prev)}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label={`Account menu for ${user?.name ?? 'current user'}`}
      >
        {initialsOf(user?.name)}
      </button>
    </div>
  )
}

const NAV = [
  { to: '/', label: 'Home', icon: HomeIcon, end: true },
  { to: '/dashboard', label: 'Dashboard', icon: GridIcon, ownerOnly: true },
  { to: '/history', label: 'Sales', icon: ReceiptIcon },
  { to: '/categories', label: 'Categories', icon: TagIcon, ownerOnly: true },
  { to: '/users', label: 'Team', icon: TeamIcon, ownerOnly: true },
]

/**
 * Shared application chrome: fixed left rail + optional top bar.
 * `variant="flush"` hands the content area to the page unpadded (used by POS).
 * `header={false}` drops the top bar entirely for a decluttered, full-height screen —
 * only POS uses it. Other pages route real controls through `actions` (the sales-history
 * date filters, the dashboard live tag), so the bar has to stay for them.
 */
export default function AppShell({
  title,
  subtitle,
  roleBadge = true,
  actions,
  variant = 'default',
  header = true,
  children,
}) {
  const { user } = useAuth()
  const isOwner = user?.role === 'OWNER'

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

        <ProfileMenu user={user} isOwner={isOwner} />
      </nav>

      <div className="shell-main">
        {header && (
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
        )}

        <main className={variant === 'flush' ? 'shell-content shell-content-flush' : 'shell-content'}>
          {children}
        </main>
      </div>
    </div>
  )
}
