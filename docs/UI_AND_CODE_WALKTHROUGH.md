# SouvenirPOS — UI & Code Walkthrough (Defense Prep)

This doc is built for two things a professor typically does during a demo:

- **A. Points at something on the screen and asks "what is this for?"** → jump to Part A,
  find the element, read the "What it's for" line, and (if pushed) name the file behind it.
- **B. Points at a block of code and asks "what's the logic here?"** → jump to Part B,
  find the block, and explain it in the plain-language walkthrough given.

Every entry names the **exact file and line number** where the code lives, so you can open
it and point to it directly (line numbers are as of this writing — if you edit a file they
may shift by a few lines, but the file and function name will still be correct).

Read it once out loud beforehand so the phrasing is already in your mouth.

---

# PART A — "What is this used for?" (point at the UI)

## A1. Login screen (`web/src/pages/LoginPage.jsx`)

| On screen | What it's for | Code behind it (file:line) |
|---|---|---|
| **Username field** | Identifies which account is signing in | `username` state — `LoginPage.jsx:9` |
| **Password field** | Proves the person owns that account | `password` state, `type="password"` — `LoginPage.jsx:10` |
| **Sign In button** | Sends the credentials to the server to be checked | `handleSubmit` → `login()` — `LoginPage.jsx:14` |
| **Red error text** | Shown only when the server rejects the login | `error` state, set in `catch` — `LoginPage.jsx:22` |

One-line summary to say: *"This page collects a username and password and asks the
backend to verify them. If the backend says they're valid, it hands back a token and we
go to the POS; if not, we show an error."*

## A2. POS main screen (`web/src/pages/POSPage.jsx`) — the important one

All in `web/src/pages/POSPage.jsx`:

| On screen | What it's for | Code behind it (line) |
|---|---|---|
| **"SouvenirPOS" + name** (top-left) | Shows who is logged in | `user?.name` — line 89 |
| **Manage Users button** (top-right) | Owner-only shortcut to create accounts | rendered only if `role === 'OWNER'` — line 92–93 |
| **Logout button** | Ends the session on this device | `onClick={logout}` — line 95 |
| **Price panel** (big ₱ number) | Live display of the price being typed | `.price-panel` shows `priceInput` — line 101–103 |
| **Category chips** | Pick which kind of item this is (tap, no typing) | `.category-chips`, `CATEGORIES.map(...)` — line 111 |
| **Numpad (7-8-9 … ⌫)** | Type the item's price like a calculator | `.numpad`, each key → `handleNumpadPress` — line 124 |
| **Quantity − / value / +** | Set how many of this item, with big tap targets | `.quantity-stepper` → `adjustQuantity` — line 134 |
| **+ Add to Cart button** | Commits the current item into the sale | `handleAddToCart` — line 145 |
| **Sales Breakdown list** | Shows every item added so far, with a ✕ to remove | `.cart-lines` maps `cart`; ✕ → `handleRemoveLine` — line 152, 163 |
| **Payment Received field** | How much cash the customer handed over | `handlePaymentChange` — line 171–178 |
| **Change line** | How much to give back = payment − total | `.cart-change`, `Math.max(change, 0)` — line 181 |
| **TOTAL (bottom bar)** | The amount the customer owes | `.pos-footer-total`, `total` — line 191 |
| **Checkout button** | Finalizes the sale | `handleCheckout` (disabled until payable) — line 196 |

Say it as a flow: *"The cashier taps a category, types the price on the numpad, sets the
quantity, and hits Add to Cart. That builds up the Sales Breakdown and the running Total.
Then they type the cash received, the system shows the change, and Checkout finishes it."*

## A3. Manage Users screen (`web/src/pages/ManageUsersPage.jsx`) — owner only

All in `web/src/pages/ManageUsersPage.jsx`:

| On screen | What it's for | Code behind it (line) |
|---|---|---|
| **Create Account form** | The owner makes a new cashier/owner login | `handleSubmit` → `POST /api/users` — line 25, 30 |
| **Role dropdown** | Decides what the new account is allowed to do | `role` state (`CASHIER` / `OWNER`) — line 12, 70 |
| **Existing Users table** | Shows all accounts already in the system | `loadUsers()` `GET /api/users` — line 16 |

Say it as: *"There is no public sign-up. Only an owner reaches this page, and only an owner
can create accounts — this is where cashier logins come from."*

---

# PART B — "What is the logic of this code block?" (point at the code)

Each entry starts with a 📍 **location line** (file + line numbers), then: **what it does →
why it's written that way.** The "why" is what usually earns the marks, because it shows you
understand it, not just typed it.

**Quick file map** — which blocks live in which file:
- `web/src/pages/POSPage.jsx` → B1, B2, B3, B4, B5, B6
- `web/src/context/AuthContext.jsx` → B7
- `web/src/api/client.js` → B8
- `web/src/components/ProtectedRoute.jsx` → B9
- `backend/.../controller/AuthController.java` → B10
- `backend/.../security/JwtAuthFilter.java` → B11
- `backend/.../controller/UserController.java` → B12
- `backend/.../service/UserService.java` → B13

## B1. The numpad handler
📍 **`web/src/pages/POSPage.jsx`, lines 36–46** (`handleNumpadPress`)
```js
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
```
**Logic:** Every numpad button calls this with the key that was pressed.
- First it clears any old "Sale completed" message, so the screen feels fresh once you
  start a new item.
- If the key is backspace (`⌫`), it chops the last character off the price string
  (`slice(0, -1)`) and stops.
- If the key is a dot **and the price already has a dot**, it does nothing — this stops
  invalid prices like `1.2.3`.
- Otherwise it appends the pressed key to the end of the price string.

**Why a string, not a number:** the price is built up character by character like a real
calculator, so it's stored as text (`priceInput`). A number couldn't hold a half-typed
value like `"15."`. It's only converted to a real number with `parseFloat` when needed.

## B2. The quantity stepper
📍 **`web/src/pages/POSPage.jsx`, lines 55–57** (`adjustQuantity`)
```js
function adjustQuantity(delta) {
  setQuantity((prev) => Math.max(1, prev + delta))
}
```
**Logic:** The `+` button calls this with `+1`, the `−` button with `-1`. It adds that to
the current quantity. `Math.max(1, ...)` clamps the result so it can never drop below 1 —
so one function handles both increasing, decreasing, **and** the "never go to zero" rule
in a single line.

## B3. Adding an item to the cart
📍 **`web/src/pages/POSPage.jsx`, lines 59–67** (`handleAddToCart`)
```js
function handleAddToCart() {
  const price = parseFloat(priceInput)
  if (!price || price <= 0 || quantity < 1) {
    return
  }
  setCart((prev) => [...prev, { id: Date.now(), category, price, quantity }])
  setPriceInput('')
  setQuantity(1)
}
```
**Logic:**
- Convert the typed price string into a real number.
- Guard clause: if the price is missing, zero, negative, or quantity is under 1, just
  stop — nothing gets added. This prevents empty or junk lines.
- Otherwise add a new line to the cart. `[...prev, newItem]` makes a **new** array (a copy
  plus the new item) instead of editing the old one — React only re-renders when it sees a
  new array, so this is the correct "immutable update" pattern. `Date.now()` gives each
  line a unique id so we can find/remove it later.
- Finally reset the price and quantity so the next item starts clean.

## B4. The running total
📍 **`web/src/pages/POSPage.jsx`, lines 28–31** (`total` / `useMemo`)
```js
const total = useMemo(
  () => cart.reduce((sum, line) => sum + line.price * line.quantity, 0),
  [cart],
)
```
**Logic:** `reduce` walks through every line in the cart, multiplies each line's price by
its quantity, and adds them all up starting from 0 — that's the total. `useMemo` means it
only recalculates when the `cart` changes, not on every keystroke elsewhere on the screen.

## B5. Sanitizing the payment field
📍 **`web/src/pages/POSPage.jsx`, lines 48–53** (`handlePaymentChange`)
```js
function handlePaymentChange(e) {
  const cleaned = e.target.value.replace(/[^0-9.]/g, '')
  const parts = cleaned.split('.')
  const safe = parts.length > 2 ? `${parts[0]}.${parts.slice(1).join('')}` : cleaned
  setPayment(safe)
}
```
**Logic:** This is a plain text box (not a number box, on purpose — number boxes show
little up/down arrows we didn't want). Because it's text, we clean it ourselves:
- `replace(/[^0-9.]/g, '')` deletes anything that isn't a digit or a dot, so letters can't
  be entered.
- If someone types more than one dot, `split('.')` produces 3+ pieces; we rejoin them into
  a single-decimal number so `parseFloat` won't choke.

## B6. Checkout
📍 **`web/src/pages/POSPage.jsx`, lines 73–82** (`handleCheckout`)
```js
function handleCheckout() {
  if (cart.length === 0 || paymentAmount < total) {
    return
  }
  setCompletedMessage(`Sale completed. Total ${formatPeso(total)}, Change ${formatPeso(change)}`)
  setCart([])
  setPayment('')
}
```
**Logic:** Refuse to check out if the cart is empty or the cash given is less than the
total. Otherwise show a success message, then empty the cart and payment for the next
customer. **Honest note if asked:** right now this only resets the screen — it does **not
yet save the sale to the database**. Saving sales (the `/api/sales` endpoint and the
Sale/SaleItem tables) is the next feature to build.

## B7. Logging in (front-end side)
📍 **`web/src/context/AuthContext.jsx`, lines 15–23** (`login`)
```js
async function login(username, password) {
  const { data } = await client.post('/auth/login', { username, password })
  localStorage.setItem(STORAGE_KEY, data.token)
  const loggedInUser = { id: data.userId, name: data.name, username: data.username, role: data.role }
  localStorage.setItem(USER_KEY, JSON.stringify(loggedInUser))
  setUser(loggedInUser)
  return loggedInUser
}
```
**Logic:** Send the username/password to the backend. If it succeeds, the backend returns
a **token** plus the user's info. We save the token in `localStorage` (so a page refresh
doesn't log you out), save the user info, and put the user into React state so the UI
updates. If the login fails, `client.post` throws, and the login page's `catch` shows the
error.

## B8. Attaching the token to every request
📍 **`web/src/api/client.js`, lines 7–13** (axios request interceptor)
```js
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('souvenirpos_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```
**Logic:** This runs automatically before **every** API call. It grabs the saved token and
attaches it as an `Authorization: Bearer <token>` header. This is why no individual page
has to remember to send the token — it's added in one place for all requests. The backend
reads this header to know who you are.

## B9. Guarding pages (front-end route guard)
📍 **`web/src/components/ProtectedRoute.jsx`, lines 7–14**
```js
if (!user) return <Navigate to="/login" replace />
if (requireRole && user.role !== requireRole) return <Navigate to="/" replace />
return children
```
**Logic:** Before showing a protected page: if nobody is logged in, redirect to the login
page. If the page requires a specific role (e.g. `OWNER`) and the user doesn't have it,
send them back to the POS. **Important point for the prof:** this is only a *convenience*
on the front-end. The real security is on the backend — even if someone bypassed this in
the browser, the server would still reject them with a 403. Never trust the client for
security.

## B10. Issuing the token (backend login)
📍 **`backend/src/main/java/edu/cit/erag/souvenirpos/controller/AuthController.java`, lines 33–46** (`login`)
```java
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
...
String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
return new LoginResponse(token, user.getId(), user.getName(), user.getUsername(), user.getRole().name());
```
**Logic:** Hand the submitted username/password to Spring Security, which loads the user
and checks the password against the stored **hash** (never the plain password). If it's
valid, generate a signed JWT that contains the username and role, and send it back. If
it's invalid, an exception is thrown and the user gets a 401.

## B11. Checking the token on every request (backend filter)
📍 **`backend/src/main/java/edu/cit/erag/souvenirpos/security/JwtAuthFilter.java`, lines 34–52** (`doFilterInternal`)
```java
String authHeader = request.getHeader("Authorization");
if (authHeader == null || !authHeader.startsWith("Bearer ")) { filterChain.doFilter(request, response); return; }
String token = authHeader.substring(7);
String username = jwtService.extractUsername(token);
if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    if (jwtService.isTokenValid(token, userDetails.getUsername())) {
        // mark this request as authenticated
    }
}
```
**Logic:** This runs on every incoming request. It looks for the `Bearer` token in the
header. If there's no token, the request continues unauthenticated (and protected
endpoints will then reject it). If there is a token, it verifies the signature/expiry and,
if valid, marks the request as coming from that logged-in user — which is what lets the
role checks (`@PreAuthorize("hasRole('OWNER')")`) work.

## B12. Restricting account creation to owners (backend)
📍 **`backend/src/main/java/edu/cit/erag/souvenirpos/controller/UserController.java`, lines 23–28** (`createUser`)
```java
@PreAuthorize("hasRole('OWNER')")
@PostMapping
public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
    return new UserResponse(userService.createUser(request));
}
```
**Logic:** `@PreAuthorize("hasRole('OWNER')")` checks the logged-in user's role **before**
the method runs. A cashier calling this gets a 403 and the code never executes. This is the
server-side enforcement of "only owners create accounts" — the real guard, backing up the
UI-level hiding of the button.

## B13. Hashing passwords (backend)
📍 **`backend/src/main/java/edu/cit/erag/souvenirpos/service/UserService.java`, lines 22–33** (`createUser`)
```java
if (userRepository.existsByUsername(request.getUsername())) {
    throw new IllegalArgumentException("Username already exists");
}
User user = new User(request.getName(), request.getUsername(),
        passwordEncoder.encode(request.getPassword()), request.getRole());
return userRepository.save(user);
```
**Logic:** First reject duplicate usernames. Then build the user with
`passwordEncoder.encode(...)` — this BCrypt-hashes the password so the database never
stores the real password, only a scrambled one-way version. Even we can't reverse it; at
login we can only check whether a submitted password produces the same hash.

---

# One-sentence answers to keep in your back pocket
- **"What does this app do?"** — It's a point-of-sale for a souvenir shop: an owner creates
  logins, cashiers log in and ring up sales on a numpad-based screen.
- **"How does login stay secure?"** — Passwords are stored hashed (BCrypt), and after login
  every request carries a signed token the server re-checks; unauthenticated requests get
  401, wrong-role requests get 403.
- **"Where's the database logic?"** — Only in the backend. The React app never touches the
  database; it only calls REST endpoints.
