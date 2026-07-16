package edu.cit.erag.souvenirpos.auth.data

import edu.cit.erag.souvenirpos.core.data.TokenStore
import edu.cit.erag.souvenirpos.core.data.model.ChangePasswordRequest
import edu.cit.erag.souvenirpos.core.data.model.LoginRequest
import edu.cit.erag.souvenirpos.core.data.model.LoginResponse
import edu.cit.erag.souvenirpos.core.network.ApiService

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    /** Logs in against POST /api/auth/login and persists the returned JWT + user. */
    suspend fun login(username: String, password: String): LoginResponse {
        val response = api.login(LoginRequest(username.trim(), password))
        tokenStore.saveSession(response)
        return response
    }

    /**
     * Changes the current user's password. The server revokes the old token and returns a
     * fresh one, which we persist so the session continues without a re-login.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): LoginResponse {
        val response = api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        tokenStore.saveSession(response)
        return response
    }

    fun logout() = tokenStore.clear()
}
