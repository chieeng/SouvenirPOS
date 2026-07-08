package edu.cit.erag.souvenirpos.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Turns network/HTTP exceptions into a short, user-facing message. */
fun Throwable.userMessage(): String = when (this) {
    is HttpException -> parseServerMessage(this) ?: "Request failed (${code()})"
    is IOException -> "Cannot reach the server. Check your connection and that the backend is running."
    else -> message ?: "Something went wrong"
}

private fun parseServerMessage(ex: HttpException): String? = try {
    val raw = ex.response()?.errorBody()?.string()
    if (raw.isNullOrBlank()) {
        null
    } else {
        @Suppress("UNCHECKED_CAST")
        val map = Gson().fromJson(raw, Map::class.java) as? Map<String, Any?>
        map?.get("message") as? String
    }
} catch (e: Exception) {
    null
}
