package edu.cit.erag.souvenirpos.core.data.model

// Request/response payloads that mirror the Spring Boot DTOs. Field names must match
// the backend JSON exactly so Gson can (de)serialize them.

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val username: String,
    val role: String,
)

data class Category(
    val id: Long,
    val name: String,
)

data class CategoryCreateRequest(
    val name: String,
)

data class UserResponse(
    val id: Long,
    val name: String,
    val username: String,
    val role: String,
)

data class UserCreateRequest(
    val name: String,
    val username: String,
    val password: String,
    val role: String,
)

data class SaleLineRequest(
    val categoryId: Long,
    val quantity: Int,
    val unitPrice: Double,
)

data class SaleCreateRequest(
    val items: List<SaleLineRequest>,
    val paymentAmount: Double,
)

data class SaleItemResponse(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
)

data class SaleResponse(
    val id: Long,
    val saleDateTime: String,
    val cashierId: Long,
    val cashierName: String,
    val totalAmount: Double,
    val paymentAmount: Double,
    val changeAmount: Double,
    val items: List<SaleItemResponse>,
)

// Dashboard aggregates (FR-015). Mirrors the backend SaleSummaryResponse.
data class DailyBucket(
    val date: String, // ISO date, e.g. "2026-07-10"
    val total: Double,
    val count: Int,
)

data class SaleSummaryResponse(
    val todayTotal: Double,
    val todayCount: Int,
    val weekTotal: Double,
    val weekCount: Int,
    val daily: List<DailyBucket>,
)
