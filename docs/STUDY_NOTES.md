# SouvenirPOS — Study Notes

These notes explain how the system currently works, why it was built this way, and the
kinds of questions a professor is likely to ask about it. Written to be read out loud,
not just skimmed.

---

## 1. The big picture

SouvenirPOS is a 3-tier system:

```
React (web/)  --HTTP/JSON-->  Spring Boot REST API (backend/)  --JDBC-->  Supabase (Postgres)
```

- **Backend is the only thing that talks to the database.** The frontend never sees a
  connection string — it only calls REST endpoints under `/api/...`. This satisfies
  NFR-011 in the SRS ("the backend shall be the sole component that writes to the
  database").
- **Auth is stateless (JWT), not session-based.** The server does not remember who is
  logged in between requests. Every request carries a signed token in the
  `Authorization: Bearer <token>` header, and the server re-verifies it each time. This
  is what lets a web client and a (future) Android client both authenticate against the
  same backend without sharing server-side session state — required because the SRS
  calls for "token-based (JWT) authentication shared across the web and mobile
  applications."

If your professor asks "why JWT instead of sessions/cookies?" — that's the answer:
statelessness makes the backend trivially usable from multiple client types (web,
mobile) without needing sticky sessions or a shared session store.

---

## 2. Backend walkthrough (`backend/`, package `edu.cit.erag.souvenirpos`)

### 2.1 Entity layer
- `entity/User.java` — one row per account. Fields: `id`, `name`, `username`,
  `passwordHash`, `role`. Note it's `passwordHash`, not `password` — the plaintext
  password is never stored (see §3).
- `entity/Role.java` — a Java `enum { OWNER, CASHIER }`. Storing role as an enum
  (mapped with `@Enumerated(EnumType.STRING)`) means the database has a `CHECK`
  constraint restricting the column to those two values — invalid roles are rejected
  at the DB level, not just in Java.

### 2.2 Repository layer
- `repository/UserRepository.java` extends Spring Data's `JpaRepository`. You don't
  write SQL — Spring Data generates it from method names like `findByUsername` and
  `existsByUsername`. This is standard Spring Data JPA "query derivation."

### 2.3 Service layer
- `service/UserService.java` — the only place account-creation business logic lives:
  reject duplicate usernames, hash the password before saving. Controllers never touch
  the repository directly for writes; they go through the service. This separation is
  what makes `@PreAuthorize` (below) and business rules easy to unit test in isolation
  from HTTP concerns.

### 2.4 Controllers (`controller/`)
- `AuthController` — one endpoint: `POST /api/auth/login`. Takes username+password,
  runs them through Spring's `AuthenticationManager`, and if valid, issues a JWT.
- `UserController` — `POST /api/users` (create account) and `GET /api/users` (list
  accounts). Both are annotated `@PreAuthorize("hasRole('OWNER')")` — only an owner can
  manage accounts. This implements FR-004/FR-017 (role-based access control) and BR-006
  (only owners create/update/delete catalog-adjacent resources).

### 2.5 Security (`security/` + `config/SecurityConfig.java`)
This is the part most worth being able to explain end-to-end, since it's the most
"designed" part of the backend.

**Login flow:**
1. Client `POST`s `{username, password}` to `/api/auth/login`.
2. `AuthController` hands those credentials to Spring's `AuthenticationManager`.
3. The manager delegates to `DaoAuthenticationProvider`, which uses
   `CustomUserDetailsService` to load the user from the DB and `BCryptPasswordEncoder`
   to compare the submitted password against the stored hash.
4. If it matches, `JwtService.generateToken(username, role)` signs a JWT (HMAC-SHA,
   `jjwt` library) containing the username as subject and role as a custom claim, with
   an expiry (`souvenirpos.jwt.expiration-ms`, default 24h).
5. The token + user info goes back to the client. The client stores it (frontend puts
   it in `localStorage`) and attaches it to every subsequent request.

**Every subsequent request flow:**
1. `JwtAuthFilter` (a `OncePerRequestFilter`, registered *before* Spring Security's
   normal username/password filter) reads the `Authorization` header.
2. If a token is present and valid, it loads the user via `CustomUserDetailsService`
   and manually populates Spring Security's `SecurityContextHolder` with an
   authenticated principal — this is what makes `@PreAuthorize("hasRole('OWNER')")`
   and `.anyRequest().authenticated()` work further down the filter chain.
3. If there's no token, or it's invalid/expired, the context stays empty. Then
   Spring Security's `authorizeHttpRequests` rule (`anyRequest().authenticated()`)
   rejects the request.

**Why 401 vs 403 both appear, and why that distinction was deliberately fixed:**
By default, Spring Security returns **403 Forbidden** for *any* unauthenticated
request when no login form/entry point is configured — which technically fails
NFR-004 ("100% of unauthenticated requests must be rejected with HTTP 401"). This was
caught during manual testing (see `SecurityConfig.authenticationEntryPoint()`, a
`HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)`). So now:
- **No token / bad token** → 401 (you're not who you say you are — an *authentication*
  failure, handled by the entry point).
- **Valid token, wrong role** (e.g. a cashier calling `POST /api/users`) → 403 (we know
  who you are, you're just not allowed — an *authorization* failure, handled by
  `GlobalExceptionHandler.handleAccessDenied`).

This 401-vs-403 distinction is a good thing to be able to explain clearly — it's a
common interview/defense question and easy to get backwards.

### 2.6 Password hashing (NFR-003)
`BCryptPasswordEncoder` is a one-way, salted hash. "One-way" means there is no
`decode()` — you can never recover the original password from the hash, only check
whether a candidate password produces the same hash (`matches()`). "Salted" means two
users with the same password get different stored hashes, which defeats precomputed
rainbow-table attacks. This is why `UserResponse` (the DTO returned to the frontend)
deliberately has no password field at all — it's never sent back over the wire, even
hashed.

### 2.7 Config & seeding
- `config/SecurityConfig.java` — wires all of the above into one `SecurityFilterChain`
  bean, plus CORS (currently wide open — fine for local dev, would need tightening for
  a real deployment).
- `config/DataSeeder.java` — a `CommandLineRunner` that runs once at startup. If the
  `users` table is empty, it creates one seed `OWNER` account (`admin` / whatever's in
  `application-local.properties`) so there's a way to log in for the very first time
  and start creating real accounts from the UI. Without this, the system would have no
  way to bootstrap itself (admin creates accounts, but the first admin has to come from
  somewhere).

### 2.8 Secrets management — why `application-local.properties` exists
`application.properties` (committed to git) has **no secrets** in it — no DB password,
no JWT signing key. It just sets `spring.profiles.active=local`, which tells Spring
Boot to also load `application-local.properties` and merge it in. That second file
holds the real Supabase credentials and JWT secret, and it's listed in `.gitignore` —
it physically cannot be committed by accident. `application-local.properties.example`
is committed instead, as a template showing which keys are needed without real values.

This matters because committing a real database password to a GitHub repo means
*anyone with repo access, forever in git history* has your database credentials, even
if you later "delete" the file — git history keeps it. Splitting config this way is
the standard fix.

---

## 3. Frontend walkthrough (`web/`, React + Vite, no TypeScript)

### 3.1 Why manual scaffold instead of `create-vite`
Not architecturally significant, but if asked: the installed Node version was older
than what the latest `create-vite` CLI requires, so the Vite project (`package.json`,
`vite.config.js`, `index.html`) was written by hand instead of generated. Functionally
identical to a generated project.

### 3.2 Routing & auth state
- `main.jsx` wraps the app in `BrowserRouter` and `AuthProvider`.
- `context/AuthContext.jsx` holds `user` (name/username/role) in React state, backed by
  `localStorage` so a page refresh doesn't log you out. `login()` calls
  `POST /api/auth/login`, stores the returned JWT under `localStorage['souvenirpos_token']`.
- `api/client.js` — a shared `axios` instance. Its request interceptor automatically
  attaches `Authorization: Bearer <token>` to every outgoing request by reading from
  `localStorage`, so individual pages never have to think about auth headers.
- `components/ProtectedRoute.jsx` — redirects to `/login` if there's no logged-in
  user, or back to `/` if a `requireRole` (e.g. `"OWNER"`) doesn't match. This is a
  **client-side convenience only** — the real enforcement is server-side
  (`@PreAuthorize` in the backend). A user could bypass this in devtools and still get
  a 403 from the API. That's an intentional and correct division of responsibility:
  never trust the client for authorization.

### 3.3 Pages
- `LoginPage.jsx` — simple controlled form, calls `login()` from context, navigates to
  `/` on success.
- `ManageUsersPage.jsx` — owner-only. Lists existing users (`GET /api/users`) and has a
  form to create new ones (`POST /api/users`). This is the UI for FR-001 ("admin
  creates accounts") — there is deliberately **no public self-registration page**,
  matching the SRS's stated flow (owner creates cashier accounts, not open sign-up).
- `POSPage.jsx` — the main cashier screen. Covered in depth below since it's the file
  you had open.

### 3.4 `POSPage.jsx` in depth

**State variables and what each one owns:**
| State | Purpose |
|---|---|
| `category` | which category chip is currently selected (defaults to the first one) |
| `priceInput` | the price being typed via the numpad, as a *string* (not a number yet) |
| `quantity` | quantity for the item currently being built, min 1 |
| `cart` | the array of line items already added — this is the actual "sale in progress" |
| `payment` | cash tendered, as a sanitized string |
| `completedMessage` | success banner text shown briefly after checkout |

**Why `priceInput` is a string, not a number:**
The numpad builds up a price digit-by-digit (`"1"` → `"15"` → `"15."` → `"15.5"`), the
same way a physical calculator works. If it were stored as a `number`, you couldn't
represent an in-progress value like `"15."` (trailing decimal point) — `parseFloat`
would just drop it. It's only converted with `parseFloat()` at the point of use
(display, and when adding to cart).

**`handleNumpadPress`:** appends a digit, handles backspace (`⌫`), and blocks a second
`.` from being entered (`priceInput.includes('.')` guard) — you can't type `"1..5"`.

**`handlePaymentChange` — why it's `type="text"` instead of `type="number"`:**
Browsers render up/down spinner arrows on `<input type="number">`, which you were
asked to remove. The fix: use `type="text"` with `inputMode="decimal"` (still brings up
a numeric keyboard on mobile) and manually sanitize the input with a regex —
`replace(/[^0-9.]/g, '')` strips anything that isn't a digit or a dot, then a second
check collapses multiple dots into one (so `"12.3.4"` typed quickly becomes `"12.34"`
rather than something `parseFloat` would choke on).

**`adjustQuantity(delta)`:** the entire quantity stepper. `Math.max(1, prev + delta)`
is doing double duty — it both increments/decrements *and* enforces the floor of 1 in
the same expression, so quantity can never go to 0 or negative via the buttons.

**`handleAddToCart`:** validates `price > 0` and `quantity >= 1` (guards against the
Add button being tapped with an empty/zero price), then pushes a new object onto
`cart` using the spread pattern (`[...prev, newLine]`) — this is the standard
React-safe way to update array state immutably, which is what lets React detect the
change and re-render.

**`total` via `useMemo`:** `cart.reduce((sum, line) => sum + line.price * line.quantity, 0)`,
recomputed only when `cart` changes (not on every keystroke in the price/payment
fields) — a small performance optimization, not strictly necessary at this scale but
good practice.

**`change` calculation:** `paymentAmount - total`, displayed with `Math.max(change, 0)`
so it never shows a negative number while typing an insufficient payment — but note
the **Checkout button itself** is disabled via
`disabled={cart.length === 0 || paymentAmount < total}`, so an insufficient payment
can't actually be submitted; the clamped display is just cosmetic.

**`handleCheckout`:** currently **client-side only** — it builds a success message,
then clears `cart` and `payment`. There is no `POST /api/sales` call yet, because the
Sale/SaleItem backend (FR-006 through FR-011) hasn't been built. This is the biggest
gap to be upfront about if asked "is this a complete system?" — **the cart/checkout UI
is fully wired and usable, but nothing is persisted to the database yet.** That's the
next milestone, not a bug.

### 3.5 Category chips vs. a real item catalog
The category list (`CATEGORIES` array) is currently a **hardcoded list of strings** in
the component, not fetched from a `souvenir_items` table. The SRS calls for a real
item catalog (FR-004/FR-005: owner manages items with name/category/price). What
exists today is a placeholder that lets the POS *flow* be built and tested end-to-end
before the catalog CRUD exists — a deliberate "build the shape first" sequencing
choice, not a misunderstanding of the requirement.

---

## 4. What's implemented vs. what's still open (map to the SRS)

| SRS ID | Requirement | Status |
|---|---|---|
| FR-001 | User registration (admin creates accounts) | ✅ done — `UserController.createUser`, owner-only |
| FR-002 | User login, JWT issuance | ✅ done |
| FR-003 | Logout | ⚠️ client-side only (clears localStorage) — with stateless JWT there's no server session to invalidate; a real "logout everywhere" would need a token blocklist, out of scope for now |
| FR-004/005 | Souvenir item catalog | ❌ not built — categories are hardcoded strings today |
| FR-006–011 | Sale transaction processing, persistence, receipts | ❌ not built — POS UI works, nothing persists yet |
| FR-012–015 | Sales history, filtering, dashboard | ❌ not built (depends on FR-006–011 existing first) |
| FR-016 | Cross-platform sync | N/A yet — no mobile app started, and no persisted sales to sync |
| FR-017 | Role-based access control | ✅ done — `@PreAuthorize("hasRole('OWNER')")` |
| FR-018 | Online accessibility / deployment | ❌ not deployed — everything currently runs on `localhost` only |
| NFR-003 | Password hashing | ✅ BCrypt |
| NFR-004 | 401 for unauthenticated requests | ✅ fixed via custom `AuthenticationEntryPoint` |
| NFR-005 | HTTPS | ❌ N/A locally; would come from the hosting platform (Railway/Render/Vercel) at deploy time |
| NFR-011 | Backend is sole DB writer | ✅ by construction — frontend only calls REST endpoints |

Being able to say *why* something isn't done yet (sequencing, not oversight) tends to
land much better in a defense than pretending everything is finished.

---

## 5. Likely questions and how to answer them

**"Why Spring Security instead of writing your own auth filter from scratch?"**
Spring Security is the standard, well-audited way to do this in the Spring ecosystem;
hand-rolling auth is a classic source of security bugs (timing attacks on password
comparison, missing CSRF/session fixation protection, etc.). The only custom piece
here is the JWT filter (`JwtAuthFilter`) and token logic (`JwtService`) — everything
else (password encoding, authentication provider, method-level `@PreAuthorize`) is
Spring Security's own tested machinery.

**"Why is CSRF disabled (`csrf.disable()`)?"**
CSRF protection exists to protect *cookie/session-based* auth, where a browser
automatically attaches cookies to any request, including ones triggered by a malicious
third-party site. This API uses stateless bearer tokens sent explicitly in an
`Authorization` header — nothing is automatically attached by the browser, so the CSRF
attack this protects against doesn't apply here.

**"What happens if the JWT secret leaks?"**
Anyone with it could forge valid tokens for any user/role — which is exactly why it's
in the gitignored `application-local.properties` rather than the committed
`application.properties`, and why real deployments should use a long random secret
rather than the placeholder committed to the example file.

**"How would you extend this to the sales feature?"**
Add `Sale` and `SaleItem` entities (already modeled in the SRS's ERD), a
`SaleService`/`SaleController` following the exact same layered pattern as `User` (repo
→ service → controller), then swap `POSPage.jsx`'s `handleCheckout` to `POST` the cart
to `/api/sales` instead of just clearing local state.

---

## 6. Quick glossary (if pressed on terminology)
- **JWT (JSON Web Token):** a signed, self-contained token — the server can verify it
  without a database lookup, because tampering with it invalidates the signature.
- **BCrypt:** an adaptive, salted password-hashing algorithm — "adaptive" means you can
  tune how slow it is to keep pace with faster hardware over time.
- **DTO (Data Transfer Object):** a plain class (`LoginRequest`, `UserResponse`, etc.)
  used to shape exactly what crosses the HTTP boundary, so internal entities (with
  fields like `passwordHash`) are never accidentally serialized straight to JSON.
- **`@PreAuthorize`:** a Spring Security annotation that checks a permission
  expression *before* a method runs — enabled here via `@EnableMethodSecurity` in
  `SecurityConfig`.
