package edu.cit.erag.souvenirpos.data.network

import edu.cit.erag.souvenirpos.data.model.Category
import edu.cit.erag.souvenirpos.data.model.LoginRequest
import edu.cit.erag.souvenirpos.data.model.LoginResponse
import edu.cit.erag.souvenirpos.data.model.SaleCreateRequest
import edu.cit.erag.souvenirpos.data.model.SaleResponse
import edu.cit.erag.souvenirpos.data.model.UserCreateRequest
import edu.cit.erag.souvenirpos.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Endpoints are relative to BuildConfig.API_BASE_URL, which ends in ".../api/".
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("categories")
    suspend fun categories(): List<Category>

    @POST("sales")
    suspend fun createSale(@Body body: SaleCreateRequest): SaleResponse

    // Sales history (FR-012). Optional ?date=YYYY-MM-DD filters to one day (FR-013);
    // a null date omits the query param and returns all sales, newest first.
    @GET("sales")
    suspend fun sales(@Query("date") date: String? = null): List<SaleResponse>

    // Owner-only on the backend (@PreAuthorize("hasRole('OWNER')")); a cashier token gets a 403.
    @GET("users")
    suspend fun users(): List<UserResponse>

    @POST("users")
    suspend fun createUser(@Body body: UserCreateRequest): UserResponse
}
