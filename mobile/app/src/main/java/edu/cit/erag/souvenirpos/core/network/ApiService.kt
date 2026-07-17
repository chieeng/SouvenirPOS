package edu.cit.erag.souvenirpos.core.network

import edu.cit.erag.souvenirpos.core.data.model.Category
import edu.cit.erag.souvenirpos.core.data.model.CategoryCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.ChangePasswordRequest
import edu.cit.erag.souvenirpos.core.data.model.LoginRequest
import edu.cit.erag.souvenirpos.core.data.model.LoginResponse
import edu.cit.erag.souvenirpos.core.data.model.SaleCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.SaleResponse
import edu.cit.erag.souvenirpos.core.data.model.SaleSummaryResponse
import edu.cit.erag.souvenirpos.core.data.model.UserCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints are relative to BuildConfig.API_BASE_URL, which ends in ".../api/".
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    // Self-service password change; returns a fresh token (the old one is revoked server-side).
    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): LoginResponse

    @GET("categories")
    suspend fun categories(): List<Category>

    // Owner-only on the backend (@PreAuthorize("hasRole('OWNER')")); a cashier token gets a 403.
    @POST("categories")
    suspend fun createCategory(@Body body: CategoryCreateRequest): Category

    @POST("sales")
    suspend fun createSale(@Body body: SaleCreateRequest): SaleResponse

    // Dashboard aggregates: today's and this week's totals + 7-day breakdown (FR-015).
    @GET("sales/summary")
    suspend fun salesSummary(): SaleSummaryResponse

    // Sales history (FR-012). Optional ?from=&to= filters a date range (FR-013); passing
    // only `from` filters that single day. Null params are omitted, returning all sales.
    @GET("sales")
    suspend fun sales(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): List<SaleResponse>

    // Owner-only on the backend (@PreAuthorize("hasRole('OWNER')")); a cashier token gets a 403.
    @GET("users")
    suspend fun users(): List<UserResponse>

    @POST("users")
    suspend fun createUser(@Body body: UserCreateRequest): UserResponse

    // Owner-only: disable/enable an account (deactivation also revokes its active session).
    @POST("users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: Long): UserResponse

    @POST("users/{id}/reactivate")
    suspend fun reactivateUser(@Path("id") id: Long): UserResponse
}
