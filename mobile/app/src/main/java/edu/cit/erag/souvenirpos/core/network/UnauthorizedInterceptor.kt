package edu.cit.erag.souvenirpos.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Detects rejected sessions. When the backend answers 401 (token expired, or revoked via
 * password change / force-logout / deactivation) on anything other than the login call, it
 * fires [onUnauthorized] so the app can drop the stored session and return to the login
 * screen — mirroring the web client's axios 401 handler.
 */
class UnauthorizedInterceptor(private val onUnauthorized: () -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        // A 401 on login is just wrong credentials; there is no session to drop there.
        if (response.code == 401 && !request.url.encodedPath.endsWith("/auth/login")) {
            onUnauthorized()
        }
        return response
    }
}
