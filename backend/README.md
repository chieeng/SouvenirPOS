# SouvenirPOS — Backend (Spring Boot REST API)

The single writer to the database. Web and mobile clients talk only to this API.

- **Stack:** Spring Boot 4.1, Java 17, Spring Security (JWT), Spring Data JPA, PostgreSQL.
- **Config:** all secrets/URLs come from environment variables, loaded in local dev from a
  `.env` file (spring-dotenv). See [`.env.example`](.env.example).

## Running locally

1. `cp .env.example .env` and fill in real values (Supabase URL, DB user/password, a long
   random `JWT_SECRET`).
2. `./mvnw spring-boot:run`
3. Log in with the seeded owner account (see the startup log for the generated one-time
   password, unless you set `SEED_OWNER_PASSWORD`).

Local dev uses `spring.jpa.hibernate.ddl-auto=update` and does **not** run Flyway.

---

## Production deployment

Production runs with the `prod` profile, which changes three things for safety:
schema is **validated** (not auto-created), the admin bootstrap is **off** by default, and
CORS is **locked** to the single frontend origin.

Activate it by setting `SPRING_PROFILES_ACTIVE=prod` on the host.

### 1. Admin account (one-time bootstrap)

The first owner (admin) account is created by a config-gated, idempotent routine:

- Gated by `ADMIN_BOOTSTRAP_ENABLED` (prod default **false**).
- Idempotent: it checks whether the owner username already exists and never creates a
  duplicate, even if left enabled.
- The password is stored only as a **BCrypt hash** (never plaintext, never logged). If
  `SEED_OWNER_PASSWORD` is unset, a strong random one is generated and printed **once** to
  the startup log.
- The new owner is flagged **must-change-password**, forcing a reset on first login.

**First deploy:**

1. Set `ADMIN_BOOTSTRAP_ENABLED=true` (and optionally `SEED_OWNER_USERNAME` /
   `SEED_OWNER_PASSWORD`).
2. Deploy. Log in with the owner account and immediately change the password.
3. Set `ADMIN_BOOTSTRAP_ENABLED=false` (or remove it) and redeploy so the routine no longer
   runs. Rotate the credentials if the generated password was exposed in logs.

### 2. Database safety (validate + Flyway migrations)

- The `prod` profile sets `spring.jpa.hibernate.ddl-auto=validate` — Hibernate only checks
  that the live schema matches the entities; it never creates or alters tables at startup.
- Schema is owned by versioned **Flyway** migrations in
  [`src/main/resources/db/migration`](src/main/resources/db/migration) (baseline:
  `V1__init_schema.sql`). Every change is a new `V2__...sql`, `V3__...sql`, … file — reviewable
  and repeatable.
- `spring.flyway.baseline-on-migrate=true` lets Flyway adopt a database that already has
  tables (e.g. one created earlier by dev auto-DDL): it records a baseline instead of failing,
  then applies only newer migrations. A fresh database gets the full schema from `V1`.
- **To change the schema:** add a new migration file, never edit an applied one, and never
  rely on auto-DDL in production.

### 3. API security (CORS)

- Allowed origins come from `FRONTEND_URL` (a single value) — **required** in prod, so the
  app refuses to start without it. No wildcard is ever used; `"*"` combined with credentials
  (the `Authorization` header) is unsafe and rejected by browsers.
- Allowed methods are limited to **GET, POST, OPTIONS** (all the API exposes; OPTIONS is the
  preflight). Allowed headers are limited to **Authorization** and **Content-Type**.

---

## Environment variables

| Variable | Required | Purpose |
|---|---|---|
| `DATABASE_URL` | ✅ | JDBC URL for PostgreSQL (Supabase) |
| `DB_USERNAME` | ✅ | Database username |
| `DB_PASSWORD` | ✅ | Database password |
| `JWT_SECRET` | ✅ | HMAC-SHA signing key, ≥ 32 bytes / 256 bits |
| `FRONTEND_URL` | ✅ (prod) | Deployed web origin; the single allowed CORS origin |
| `SPRING_PROFILES_ACTIVE` | ✅ (prod) | Set to `prod` to enable validate + Flyway + locked CORS |
| `ACCESS_TOKEN_EXPIRATION` | ❌ | Token lifetime in **ms** (default 3600000 = 1h) |
| `ADMIN_BOOTSTRAP_ENABLED` | ❌ | `true` for first deploy to create the owner, then `false` |
| `SEED_OWNER_USERNAME` | ❌ | Initial owner username (default `admin`) |
| `SEED_OWNER_PASSWORD` | ❌ | Initial owner password; random + logged once if unset |
| `PORT` | ❌ | HTTP port (Render sets this automatically) |
| `LOGIN_MAX_ATTEMPTS` / `LOGIN_WINDOW_SECONDS` | ❌ | Login rate-limit tuning |

### Deploying on Render (build once, set env)

- **Build:** `./mvnw -q -DskipTests package`
- **Start:** `java -jar target/souvenirpos-0.0.1-SNAPSHOT.jar`
- **Env vars:** set all the ✅ rows above plus `SPRING_PROFILES_ACTIVE=prod`. Render provides
  `PORT` automatically. Use `ADMIN_BOOTSTRAP_ENABLED=true` for the first deploy only.
