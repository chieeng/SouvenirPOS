import { useEffect, useMemo, useState } from 'react'
import AppShell from '../../shared/layout/AppShell'
import { useAuth } from '../../auth/context/AuthContext'
import client from '../../shared/api/client'
import '../styles/POSPage.css'

const NUMPAD_KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', '⌫']

function formatPeso(amount) {
  return `₱${Number(amount).toFixed(2)}`
}

function formatDateTime(iso) {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export default function POSPage() {
  const { user } = useAuth()

  const [categories, setCategories] = useState([])
  const [category, setCategory] = useState(null)
  const [priceInput, setPriceInput] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [cart, setCart] = useState([])
  const [payment, setPayment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [receipt, setReceipt] = useState(null) // completed sale shown as a digital receipt (FR-010)

  // Load the owner-managed category list from the backend (FR-005).
  useEffect(() => {
    async function loadCategories() {
      try {
        const { data } = await client.get('/categories')
        setCategories(data)
        setCategory((prev) => prev ?? data[0] ?? null)
      } catch (err) {
        setError(err.response?.data?.message || 'Could not load categories')
      }
    }
    loadCategories()
  }, [])

  const total = useMemo(
    () => cart.reduce((sum, line) => sum + line.price * line.quantity, 0),
    [cart],
  )
  const cartCount = cart.reduce((sum, line) => sum + line.quantity, 0)

  const paymentAmount = parseFloat(payment) || 0
  const change = paymentAmount - total
  const priceValue = parseFloat(priceInput) || 0

  function handleNumpadPress(key) {
    if (key === '⌫') {
      setPriceInput((prev) => prev.slice(0, -1))
      return
    }
    if (key === '.' && priceInput.includes('.')) {
      return
    }
    setPriceInput((prev) => prev + key)
  }

  function handlePaymentChange(e) {
    const cleaned = e.target.value.replace(/[^0-9.]/g, '')
    const parts = cleaned.split('.')
    const safe = parts.length > 2 ? `${parts[0]}.${parts.slice(1).join('')}` : cleaned
    setPayment(safe)
  }

  function adjustQuantity(delta) {
    setQuantity((prev) => Math.max(1, prev + delta))
  }

  function handleAddToCart() {
    const price = parseFloat(priceInput)
    // FR-007: a sale line needs a category, a manually entered price, and quantity >= 1.
    if (!category || !price || price <= 0 || quantity < 1) {
      return
    }
    setCart((prev) => [...prev, { id: Date.now(), category, price, quantity }])
    setPriceInput('')
    setQuantity(1)
  }

  function handleRemoveLine(id) {
    setCart((prev) => prev.filter((line) => line.id !== id))
  }

  async function handleCheckout() {
    if (cart.length === 0 || paymentAmount < total || submitting) {
      return
    }
    setSubmitting(true)
    setError('')
    try {
      // The backend recomputes totals/change and persists the sale (FR-008/009/011).
      const { data } = await client.post('/sales', {
        items: cart.map((line) => ({
          categoryId: line.category.id,
          quantity: line.quantity,
          unitPrice: line.price,
        })),
        paymentAmount,
      })
      // Show the server-computed sale as a full digital receipt (FR-010).
      setReceipt(data)
      setCart([])
      setPayment('')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save the sale')
    } finally {
      setSubmitting(false)
    }
  }

  const drawerStatus = (
    <div className="pos-drawer">
      <div className="pos-drawer-reg">Register · Front 01</div>
      <div className="pos-drawer-open">● Drawer open</div>
    </div>
  )

  return (
    <AppShell
      title="Point of sale"
      roleBadge={false}
      variant="flush"
      actions={drawerStatus}
    >
      <div className="pos">
        {/* ---------- item entry ---------- */}
        <section className="pos-entry">
          <div className="pos-entry-label">Choose category</div>
          <div className="category-chips">
            {categories.length === 0 && <span className="category-hint">Loading categories…</span>}
            {categories.map((c) => (
              <button
                key={c.id}
                className={`chip ${category?.id === c.id ? 'chip-active' : ''}`}
                onClick={() => setCategory(c)}
              >
                {c.name}
              </button>
            ))}
          </div>

          <div className="price-panel">
            <span className="price-panel-label">Item amount</span>
            <div className="price-panel-value">
              ₱{Math.trunc(priceValue) || 0}
              <span className="price-panel-decimals">
                .{(priceInput.split('.')[1] ?? '00').padEnd(2, '0').slice(0, 2)}
              </span>
            </div>
          </div>

          <div className="numpad">
            {NUMPAD_KEYS.map((key) => (
              <button
                key={key}
                className={key === '⌫' ? 'numpad-del' : ''}
                onClick={() => handleNumpadPress(key)}
              >
                {key === '⌫' ? (
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 4H8l-7 8 7 8h13a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z" />
                    <line x1="18" y1="9" x2="12" y2="15" />
                    <line x1="12" y1="9" x2="18" y2="15" />
                  </svg>
                ) : (
                  key
                )}
              </button>
            ))}
          </div>

          <div className="quantity-row">
            <span>Quantity</span>
            <div className="quantity-stepper">
              <button onClick={() => adjustQuantity(-1)} aria-label="Decrease quantity">
                −
              </button>
              <span className="quantity-value">{quantity}</span>
              <button onClick={() => adjustQuantity(1)} aria-label="Increase quantity">
                +
              </button>
            </div>
          </div>

          <button className="pos-add-btn" onClick={handleAddToCart}>
            <span className="pos-add-plus">+</span> Add to sale
          </button>
        </section>

        {/* ---------- current sale ---------- */}
        <aside className="pos-cart">
          <div className="pos-cart-head">
            <div>
              <div className="pos-cart-title">Current sale</div>
              <div className="pos-cart-meta">
                {cartCount} item{cartCount === 1 ? '' : 's'}
              </div>
            </div>
            <div className="pos-cart-cashier">{user?.name}</div>
          </div>

          <div className="cart-lines">
            {cart.length === 0 && <p className="cart-empty">Nothing added yet.</p>}
            {cart.map((line) => (
              <div key={line.id} className="cart-line">
                <div className="cart-line-swatch" aria-hidden="true" />
                <div className="cart-line-info">
                  <strong>{line.category.name}</strong>
                  <span>
                    {formatPeso(line.price)} × {line.quantity}
                  </span>
                </div>
                <div className="cart-line-price">{formatPeso(line.price * line.quantity)}</div>
                <button
                  className="cart-line-remove"
                  onClick={() => handleRemoveLine(line.id)}
                  aria-label="Remove item"
                >
                  ✕
                </button>
              </div>
            ))}
          </div>

          <div className="cart-breakdown">
            <div className="cart-breakdown-row">
              <span>Subtotal</span>
              <span>{formatPeso(total)}</span>
            </div>
            <div className="cart-breakdown-total">
              <span>Total due</span>
              <span>{formatPeso(total)}</span>
            </div>
          </div>

          <div className="cart-pay">
            <label className="cart-pay-field">
              <span>Payment received</span>
              <div className="cart-pay-input">
                <span className="cart-pay-peso">₱</span>
                <input
                  type="text"
                  inputMode="decimal"
                  value={payment}
                  onChange={handlePaymentChange}
                  placeholder="0.00"
                />
              </div>
            </label>

            <div className="cart-change">
              <span>Change due</span>
              <span>{formatPeso(Math.max(change, 0))}</span>
            </div>

            {error && <div className="cart-error">{error}</div>}

            <button
              className="pos-checkout-btn"
              onClick={handleCheckout}
              disabled={cart.length === 0 || paymentAmount < total || submitting}
            >
              {submitting ? 'Saving…' : `Checkout · ${formatPeso(total)}`}
            </button>
          </div>
        </aside>
      </div>

      {receipt && (
        <div className="pos-receipt-scrim" onClick={() => setReceipt(null)}>
          <div className="pos-receipt" onClick={(e) => e.stopPropagation()}>
            <div className="pos-receipt-check" aria-hidden="true">
              <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="5 12.5 10 17.5 19 7" />
              </svg>
            </div>
            <h2 className="pos-receipt-title">Payment complete</h2>
            <p className="pos-receipt-sub">
              {formatPeso(receipt.changeAmount)} change · Sale #{receipt.id}
            </p>

            <div className="pos-receipt-paper">
              <div className="pos-receipt-brand">
                <div className="pos-receipt-store">HARBOR ROW SOUVENIR CO.</div>
                <div className="pos-receipt-line">{formatDateTime(receipt.saleDateTime)}</div>
                <div className="pos-receipt-line">Cashier · {receipt.cashierName}</div>
              </div>
              <div className="pos-receipt-items">
                {receipt.items.map((it) => (
                  <div key={it.id} className="pos-receipt-item">
                    <span>
                      {it.categoryName} ×{it.quantity}
                    </span>
                    <span>{Number(it.subtotal).toFixed(2)}</span>
                  </div>
                ))}
              </div>
              <div className="pos-receipt-totals">
                <div className="pos-receipt-total">
                  <span>TOTAL</span>
                  <span>{formatPeso(receipt.totalAmount)}</span>
                </div>
                <div className="pos-receipt-item muted">
                  <span>Cash</span>
                  <span>{Number(receipt.paymentAmount).toFixed(2)}</span>
                </div>
                <div className="pos-receipt-item muted">
                  <span>Change</span>
                  <span>{Number(receipt.changeAmount).toFixed(2)}</span>
                </div>
              </div>
            </div>

            <button className="pos-receipt-done" onClick={() => setReceipt(null)}>
              New sale
            </button>
          </div>
        </div>
      )}
    </AppShell>
  )
}
