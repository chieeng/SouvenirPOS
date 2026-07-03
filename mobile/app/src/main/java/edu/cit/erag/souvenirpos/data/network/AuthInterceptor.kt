package edu.cit.erag.souvenirpos.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the stored JWT as an `Authorization: Bearer <token>` header on every request,
 * mirroring what the web client does in web/src/api/client.js.
 */
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
