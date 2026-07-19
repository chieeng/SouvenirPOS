# SouvenirPOS — Android (Kotlin) mobile app

The cashier-facing client. Written in **Kotlin + Jetpack Compose**, it talks only to the
Spring Boot REST API (`../backend`), which is the sole writer to the Supabase database. The
app never connects to Supabase directly (SRS NFR-011).

## Toolchain (pinned to Android Studio Iguana | 2023.2.1)

| Tool | Version |
|------|---------|
| Android Gradle Plugin | 8.3.2 |
| Gradle | 8.4 |
| JDK | 17 |
| Kotlin | 1.9.22 |
| Compose Compiler | 1.5.10 |
| Compose BOM | 2024.02.02 |
| compileSdk / targetSdk | 34 |
| minSdk | 26 (Android 8.0 Oreo — SRS NFR-009) |

> Do not raise these without moving to a newer Android Studio. Kotlin 2.0 / Compose
> Compiler 2.0 / compileSdk 35 require Studio Koala or later and will not build on Iguana.

## Running it

1. Start the backend first (`../backend`, `./mvnw spring-boot:run`) so the API is live on
   `http://localhost:8080`.
2. Open **this `mobile/` folder** in Android Studio Iguana and let Gradle sync. Studio
   writes `local.properties` with your `sdk.dir` automatically.
3. Run the app on an **emulator (API 26+)**. The app reaches the host machine's backend at
   `http://10.0.2.2:8080/api/` (configured as `API_BASE_URL` in `app/build.gradle.kts`).
   - On a **physical device**, change `API_BASE_URL` to your machine's LAN IP or the
     deployed HTTPS URL. Cleartext HTTP is only allowed for local hosts
     (`res/xml/network_security_config.xml`).
4. Log in with the seeded owner account (see the backend's `application-local.properties`),
   then process a sale.

## What it does (first deliverable)

- **Login** → `POST /api/auth/login`, stores the JWT (attached to every later request).
- **POS sale** → loads categories from `GET /api/categories`, cashier picks a category,
  enters price on the numpad, adds lines, enters payment, and checks out via
  `POST /api/sales`. The backend computes totals/change and returns the receipt, shown in a
  dialog. Session persists across restarts; Logout clears it.

## Structure

```
app/src/main/java/edu/cit/erag/souvenirpos/
  SouvenirPosApp.kt        Application; initializes ServiceLocator
  MainActivity.kt          Compose host
  di/ServiceLocator.kt     Retrofit + repositories (manual DI)
  data/
    TokenStore.kt          JWT + user persisted in SharedPreferences
    model/Models.kt        Request/response payloads (match backend DTOs)
    network/               ApiService, AuthInterceptor, error mapping
    repository/            AuthRepository, PosRepository
  ui/
    AppNavigation.kt       login -> pos routing
    login/                 LoginScreen + LoginViewModel
    pos/                   PosScreen + PosViewModel (cart, numpad, receipt)
    theme/Theme.kt
```
