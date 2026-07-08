package edu.cit.erag.souvenirpos.core.data

import android.content.Context
import edu.cit.erag.souvenirpos.core.data.model.LoginResponse

/**
 * Persists the JWT and basic user info in SharedPreferences so the session survives
 * app restarts — the mobile equivalent of the web app's localStorage keys.
 */
class TokenStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveSession(login: LoginResponse) {
        prefs.edit()
            .putString(KEY_TOKEN, login.token)
            .putLong(KEY_USER_ID, login.userId)
            .putString(KEY_NAME, login.name)
            .putString(KEY_USERNAME, login.username)
            .putString(KEY_ROLE, login.role)
            .apply()
    }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)
    fun name(): String? = prefs.getString(KEY_NAME, null)
    fun role(): String? = prefs.getString(KEY_ROLE, null)
    fun isLoggedIn(): Boolean = !token().isNullOrBlank()

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val PREFS = "souvenirpos"
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "name"
        const val KEY_USERNAME = "username"
        const val KEY_ROLE = "role"
    }
}
