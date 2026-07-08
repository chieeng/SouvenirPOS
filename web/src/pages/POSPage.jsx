import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../features/auth/AuthContext'
import client from '../shared/api/client'
import './POSPage.css'

const NUMPAD_KEYS = ['7', '8', '9', '4', '5', '6', '1', '2', '3', '.', '0', '⌫']

function formatPeso(amount) {
  return `₱${Number(amount).toFixed(2)}`
}

export default function POSPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [categories, setCategories] = useState([])
  const [category, setCategory] = useState(null)
  const [priceInput, setPriceInput] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [cart, setCart] = useState([])
  const [payment, setPayment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [completedMessage, setCompletedMessage] = useState('')

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

  const paymentAmount = parseFloat(payment) || 0
  const change = paymentAmount - total

  function handleNumpadPress(key) {
    setCompletedMessage('')
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
      setCompletedMessage(
        `Sale #${data.id} completed. Total ${formatPeso(data.totalAmount)}, Change ${formatPeso(data.changeAmount)}`,
      )
      setCart([])
      setPayment('')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save the sale')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="pos-page">
      <header className="pos-header">
        <div>
          <h1>SouvenirPOS</h1>
          <span className="pos-header-name">{user?.name}</span>
        </div>
        <div className="pos-header-actions">
          <button onClick={() => navigate('/history')}>Sales History</button>
          {user?.role === 'OWNER' && (
            <button onClick={() => navigate('/users')}>Manage Users</button>
          )}
          <button onClick={logout}>Logout</button>
        </div>
      </header>

      <main className="pos-main">
        <section className="pos-entry">
          <div className="price-panel">
            <span className="price-panel-label">Price</span>
            <span className="price-panel-value">{priceInput ? formatPeso(parseFloat(priceInput) || 0) : '₱0'}</span>
          </div>

          <div className="category-block">
            <div className="category-block-header">
              <span>Category</span>
              <span className="category-hint">tap to select</span>
            </div>
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
          </div>

          <div className="numpad">
            {NUMPAD_KEYS.map((key) => (
              <button key={key} onClick={() => handleNumpadPress(key)}>
                {key}
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
            + Add to Cart
          </button>
        </section>

        <section className="pos-cart">
          <h2>Sales Breakdown</h2>
          <div className="cart-lines">
            {cart.length === 0 && <p className="cart-empty">Nothing added yet.</p>}
            {cart.map((line) => (
              <div key={line.id} className="cart-line">
                <div className="cart-line-info">
                  <strong>{line.category.name}</strong>
                  <span>{formatPeso(line.price)} · x{line.quantity}</span>
                </div>
                <div className="cart-line-price">
                  {formatPeso(line.price * line.quantity)}
                </div>
                <button className="cart-line-remove" onClick={() => handleRemoveLine(line.id)}>
                  ✕
                </button>
              </div>
            ))}
          </div>

          <div className="pos-field">
            <label>Payment Received</label>
            <input
              type="text"
              inputMode="decimal"
              value={payment}
              onChange={handlePaymentChange}
              placeholder="0.00"
            />
          </div>

          <div className="cart-change">
            <span>Change</span>
            <span>{formatPeso(Math.max(change, 0))}</span>
          </div>

          {error && <div className="cart-error">{error}</div>}
          {completedMessage && <div className="cart-success">{completedMessage}</div>}
        </section>
      </main>

      <footer className="pos-footer">
        <div className="pos-footer-total">
          <span>TOTAL</span>
          <strong>{formatPeso(total)}</strong>
        </div>
        <button
          className="pos-checkout-btn"
          onClick={handleCheckout}
          disabled={cart.length === 0 || paymentAmount < total || submitting}
        >
          {submitting ? 'Saving…' : 'Checkout'}
        </button>
      </footer>
    </div>
  )
}
